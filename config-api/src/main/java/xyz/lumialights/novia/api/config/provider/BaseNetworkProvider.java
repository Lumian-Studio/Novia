/**
           .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
          | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
          | | ____  _____  | || |     ____     | || | ____   ____  | || |     _____    | || |      __      | |
          | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_  _| | || |    |_   _|   | || |     /  \     | |
          | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |      | |     | || |    / /\ \    | |
          | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |   / ____ \   | |
          | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | |
          | ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| |
          | |              | || |              | || |              | || |              | || |              | |
          | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
           '----------------'  '----------------'  '----------------'  '----------------'  '----------------'

    MIT License

    Copyright (c) 2025 LumiaLights

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
package xyz.lumialights.novia.api.config.provider;

import com.mojang.serialization.DynamicOps;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.PropertyValidationException;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.serialisation.Value;
import xyz.lumialights.novia.api.core.util.JsonPointer;
import xyz.lumialights.novia.api.core.util.Pair;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public sealed abstract class BaseNetworkProvider<Container>
    implements IConfigProvider<Container>
    permits ConfigProvider
{
    //******************************************************************************************************************
    private final Identifier    id;
    private final boolean       optional;
    private final DynamicOps<?> ops;
    
    private boolean isRemote = false;
    
    //******************************************************************************************************************
    public BaseNetworkProvider(final @NotNull Identifier id, final @NotNull DynamicOps<?> ops, final boolean optional)
    {
        this.id       = Objects.requireNonNull(id,  "ID must not be null");
        this.ops      = Objects.requireNonNull(ops, "dynamic ops must not be null");
        this.optional = optional;
    }
    
    //==================================================================================================================
    /// Gets the client container managed by this config provider. The client container will never be ˋnullˋ on a
    /// physical client, but always on a physical server.
    ///
    /// This is the container the client (render thread) should be using to read configuration data, it will be
    /// automatically synced whenever the server has updates (and if the configuration is actually synced).
    ///
    /// Do not use this container to update the client configuration, this should be done over the managed container
    /// which will sync to the client container automatically.
    /// @return The instance of the client container
    public abstract @Nullable Container getClientContainer();
    
    @Override public @NotNull Identifier    getId()  { return this.id; }
    @Override public @NotNull DynamicOps<?> getOps() { return this.ops; }
    
    /// Returns a list of all properties that is managed by the client container. (see [#getClientContainer()])
    /// @return A list of client container properties
    public @NotNull List<Property> getClientProperties()
    {
        final Container client = this.getClientContainer();
        
        if (client == null)
        {
            return new ArrayList<>();
        }
        
        return this.getPropertiesForContainer(client);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private @NotNull List<Property> getPropertiesForContainer(final @NotNull Container container)
    {
        return this.getSpec()
            .flatPropertySpecs()
            .values()
            .stream()
            .map(spec -> spec.get(container))
            .collect(Collectors.toList());
    }
    
    //==================================================================================================================
    /// Sets whether this provider is on the physical client and is connected to a remote instance.
    /// @param isRemote True if this provider has a remote provider
    protected void setRemote(final boolean isRemote) { this.isRemote = isRemote; }
    
    //==================================================================================================================
    @Override public boolean isSynced() { return true; }
    
    /// Determines whether this config provider is an optional provider.
    ///
    /// A non-optional provider determines that a network provider needs to be available on the client and the server.
    /// If this is not the case, the client shall be unable to join.
    /// @return `true` if this provider is optional
    public boolean isOptional() { return this.optional; }
    
    /// Determines whether the current provider is in a state, that it is connected to a remote server. This will only
    /// return `true` if this is called on a physical client that is connected to a remote server. If the current world
    /// uses an integrated server (is in single player mode), this will return `false`.
    /// @return `true` if this provider is synchronised remotely
    public boolean isRemote() { return this.isRemote; }
    
    //==================================================================================================================
    @Override
    public boolean save()
    {
        if (!Novia.getInstance().isServerThread())
        {
            Novia.LOGGER.error("trying to save config provider outside the server thread");
        }
        
        return IConfigProvider.super.save();
    }
    
    @Override
    public boolean load(final @NotNull Path file)
    {
        if (!Novia.getInstance().isServerThread())
        {
            Novia.LOGGER.error("trying to load config provider outside the server thread");
        }
        
        return IConfigProvider.super.load(file);
    }
    
    //==================================================================================================================
    @ApiStatus.Internal
    public boolean saveUnchecked()
    {
        return IConfigProvider.super.save();
    }
    
    @ApiStatus.Internal
    public boolean loadUnchecked(final @NotNull Path file)
    {
        return IConfigProvider.super.load(file);
    }
    
    //==================================================================================================================
    abstract void updateClient(@NotNull List<Pair<JsonPointer, Value>> values, boolean isRemote)
        throws PropertyValidationException;
    
    abstract void forceUpdateClient(@NotNull List<Pair<JsonPointer, Value>> values, boolean isRemote);
}
