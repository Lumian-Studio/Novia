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
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.ConfigManager;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.spec.ConfigSpec;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.serialisation.dynops.ConfigSerialisationException;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
/// The base for all config providers.
public sealed interface IConfigProvider<Container>
    permits
        StandaloneProvider,
        BaseNetworkProvider
{
    //******************************************************************************************************************
    /// Provides a builder interface that provider sub-implementations can inherit from to build a provider.
    abstract class Builder<Self, Container, Provider extends IConfigProvider<Container>>
    {
        //**************************************************************************************************************
        protected final ConfigSpec<Container> spec;
        
        protected DynamicOps<?> ops = JsonOps.INSTANCE;
        
        //**************************************************************************************************************
        protected Builder(final @NotNull ConfigSpec<Container> spec)
        {
            Objects.requireNonNull(spec, "spec must not be null");
            this.spec = spec;
        }
        
        //==============================================================================================================
        /// Sets the serialisation back-end for this provider, by that means, the configuration language to be used.
        /// By default, this will use [JsonOps#INSTANCE].
        /// @param dynOps The dynamic ops instance
        @SuppressWarnings("unchecked")
        public @NotNull Self setBackend(final @NotNull DynamicOps<?> dynOps)
        {
            this.ops = Objects.requireNonNull(dynOps, "dynamic ops must not be null");
            return (Self) this;
        }
        
        //==============================================================================================================
        /// Builds the provider
        /// @param id The [Identifier] of the provider
        public abstract @NotNull Provider build(final @NotNull Identifier id);
    }
    
    //******************************************************************************************************************
    /// Gets the internal config specification.
    /// @return The [ConfigSpec<Container>]
    @NotNull ConfigSpec<Container> getSpec();

    /// Gets the managed container of this provider.
    ///
    /// The managed container is the instance of the configuration that is actively being maintained by the provider,
    /// it is the one that is to be updated and saved and which contains the most recent updates of properties.
    /// @return The container object
    @NotNull Container getManagedContainer();
    
    /// Returns a list of all properties that is managed by the managed container. (see [#getManagedProperties()])
    /// @return A list of container properties
    default @NotNull List<Property> getManagedProperties()
    {
        final Container container = this.getManagedContainer();
        return this.getSpec()
            .flatPropertySpecs()
            .values()
            .stream()
            .map(spec -> spec.get(container))
            .collect(Collectors.toList());
    }
    
    /// {@return the [Identifier] of this provider}
    @NotNull Identifier getId();
    
    /// {@return the [DynamicOps] instance used to serialise this provider}
    @NotNull DynamicOps<?> getOps();
    
    //==================================================================================================================
    /// Tries saving the managed container of this provider to disk and returns `true` on success, or `false` if this
    /// operation was unsuccessful and logs the reason to console.
    /// @param file The file to save to
    /// @return `true` if the operation was successful, otherwise `false`
    default boolean save(final @NotNull Path file)
    {
        try
        {
            this.getSpec().write(this.getOps(), file, this.getManagedContainer());
            Novia.LOGGER.info("Successfully saved configuration file '{}' for provider '{}', ", file, this.getId());
            
            return true;
        }
        catch (final ConfigSerialisationException ex)
        {
            Novia.LOGGER.error("Could not save config for provider '{}', ", this.getId(), ex);
        }
        
        return false;
    }
    
    /// Tries saving the managed container of this provider to disk and returns `true` on success, or `false` if this
    /// operation was unsuccessful and logs the reason to console.
    /// @return `true` if the operation was successful, otherwise `false`
    default boolean save() { return this.save(ConfigManager.getInstance().getFileForProvider(this)); }
    
    /// Tries loading the managed container of this provider from disk and returns `true` on success, or `false` if this
    /// operation was unsuccessful and logs the reason to console.
    /// @param file The file to load from
    /// @return `true` if the operation was successful, otherwise `false`
    default boolean load(final @NotNull Path file)
    {
        try
        {
            this.getSpec().read(this.getOps(), file, this.getManagedContainer());
            Novia.LOGGER.info("Successfully loaded configuration file '{}' for provider '{}', ", file, this.getId());
            
            return true;
        }
        catch (final ConfigSerialisationException ex)
        {
            Novia.LOGGER.error("Could not read configuration file '{}' for provider '{}', ", file, this.getId(), ex);
        }
        
        return false;
    }
    
    /// Tries loading the managed container of this provider from disk and returns `true` on success, or `false` if this
    /// operation was unsuccessful and logs the reason to console.
    /// @return `true` if the operation was successful, otherwise `false`
    default boolean load() { return this.load(ConfigManager.getInstance().getFileForProvider(this)); }
    
    //==================================================================================================================
    /// Determines whether the provider has the capability to be synced between server and client.
    /// @return `true` if the provider is (possibly) synced
    boolean isSynced();
}
