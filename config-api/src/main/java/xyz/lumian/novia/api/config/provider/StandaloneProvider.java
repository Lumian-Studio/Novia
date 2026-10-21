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
package xyz.lumian.novia.api.config.provider;

import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import xyz.lumian.novia.api.config.spec.ConfigSpec;
import xyz.lumian.novia.api.core.Novia;

import java.nio.file.Path;
import java.util.Objects;



//**********************************************************************************************************************
public final class StandaloneProvider<Container>
    implements IConfigProvider<Container>
{
    //******************************************************************************************************************
    public static class Builder<Container>
        extends IConfigProvider.Builder<Builder<Container>, Container, StandaloneProvider<Container>>
    {
        //**************************************************************************************************************
        private boolean serverSide = false;
        
        //**************************************************************************************************************
        protected Builder(final ConfigSpec<Container> spec) { super(spec); }
        
        //==============================================================================================================
        public Builder<Container> setServerSide(final boolean serverSide)
        {
            this.serverSide = serverSide;
            return this;
        }
        
        //==============================================================================================================
        @Override
        public StandaloneProvider<Container> build(final Identifier id)
        {
            return new StandaloneProvider<>(this.spec, id, this.ops, this.serverSide);
        }
    }
    
    //******************************************************************************************************************
    /// Creates a new [StandaloneProvider] builder.
    /// @param spec The config specification
    /// @return The new builder
    public static <Container> Builder<Container> builder(final ConfigSpec<Container> spec)
    {
        return new Builder<>(spec);
    }
    
    //******************************************************************************************************************
    private final ConfigSpec<Container> spec;
    private final Identifier            id;
    private final Container             container;
    private final DynamicOps<?>         ops;
    private final boolean               serverSide;

    //******************************************************************************************************************
    private StandaloneProvider(final ConfigSpec<Container> spec, final Identifier id, final DynamicOps<?> ops,
                               final boolean serverSide)
    {
        this.spec       = Objects.requireNonNull(spec, "Spec must not be null");
        this.id         = Objects.requireNonNull(id,   "ID must not be null");
        this.ops        = Objects.requireNonNull(ops,  "Ops must not be null");
        this.container  = spec.create();
        this.serverSide = serverSide;
        
        spec.bindContainer(this.container, null);
    }

    //==================================================================================================================
    @Override public ConfigSpec<Container> getSpec()             { return this.spec; }
    @Override public Container             getManagedContainer() { return this.container; }
    @Override public Identifier            getId()               { return this.id; }
    @Override public DynamicOps<?>         getOps()              { return this.ops; }
    
    //==================================================================================================================
    @Override public boolean isSynced() { return false; }
    
    /// Determines whether this standalone provider is server or client side.
    /// @return `true` if this provider is server side only, `false` if client side only
    public boolean isServerSide() { return this.serverSide; }
    
    //==================================================================================================================
    @Override
    public boolean save()
    {
        final Novia novia = Novia.getInstance();
        
        if (this.serverSide ? novia.isRenderThread() : novia.isServerThread())
        {
            Novia.LOGGER.error(
                "trying to save standalone provider on the wrong thread (provider side: {}; thread: {})",
                (this.serverSide ? "server" : "client"),
                (novia.isServerThread() ? "server" : "client"));
        }
        
        return IConfigProvider.super.save();
    }
    
    @Override
    public boolean load(final Path file)
    {
        final Novia novia = Novia.getInstance();
        
        if (this.serverSide ? !novia.isServerThread() : !novia.isRenderThread())
        {
            Novia.LOGGER.error(
                "trying to load standalone provider on the wrong thread (provider side: {}; thread: {})",
                (this.serverSide ? "server" : "client"),
                (novia.isServerThread() ? "not server" : "not client"));
        }
        
        return IConfigProvider.super.load(file);
    }
    
    //==================================================================================================================
    @ApiStatus.Internal
    public boolean saveUnchecked() { return IConfigProvider.super.save(); }
    
    @ApiStatus.Internal
    public boolean loadUnchecked(final Path file) { return IConfigProvider.super.load(file); }
}
