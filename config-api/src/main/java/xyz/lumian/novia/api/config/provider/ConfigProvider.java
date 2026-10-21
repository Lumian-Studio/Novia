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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.ConfigManagerNetwork;
import xyz.lumian.novia.api.config.LockedPropertyChangeException;
import xyz.lumian.novia.api.config.PropertyValidationException;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.spec.ConfigSpec;
import xyz.lumian.novia.api.core.InternalModException;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.util.JsonPointer;
import xyz.lumian.novia.api.core.util.PropagatingException;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;



//**********************************************************************************************************************
public final class ConfigProvider<Container>
    extends BaseNetworkProvider<Container>
{
    //******************************************************************************************************************
    public class UpdateBuilder
    {
        //**************************************************************************************************************
        private record UpdateEntry<T, Container>(Function<Container, Property<T>> getter, Optional<T> value)
        {
            //**********************************************************************************************************
            public Property<T> updateAndGet(final Container container)
                throws PropertyValidationException
            {
                final Property<T> property = this.getter.apply(container);
                
                if (this.value.isPresent())
                {
                    property.setValue(this.value.orElseThrow());
                }
                else
                {
                    property.reset();
                }
                
                return property;
            }
        }

        //**************************************************************************************************************
        private final List<UpdateEntry<?, Container>> entries = new LinkedList<>();
        private final Container                       container;

        //**************************************************************************************************************
        private UpdateBuilder(final Container container) { this.container = container; }

        //==============================================================================================================
        /// Sets a specified container property to the given value.
        /// @param getter   The getter method for the container property
        /// @param newValue The new value to set the property to
        /// @return `this`
        public <T> UpdateBuilder set(final Function<Container, Property<T>> getter, final T newValue)
        {
            this.entries.add(new UpdateEntry<>(getter, Optional.of(newValue)));
            return this;
        }

        /// Resets a specified container property to its default value.
        /// @param getter The getter method for the container property
        /// @return `this`
        public <T> UpdateBuilder reset(final Function<Container, Property<T>> getter)
        {
            this.entries.add(new UpdateEntry<>(getter, Optional.empty()));
            return this;
        }

        //==============================================================================================================
        void send()
            throws PropertyValidationException
        {
            final List<Pair<JsonPointer, Optional<?>>> updates = new ArrayList<>();
            
            for (final var entry : this.entries)
            {
                final Property<?> property = entry.updateAndGet(this.container);
                updates.add(Pair.of(Objects.requireNonNull(property.getSpec()).pointer(), entry.value));
            }
            
            ConfigProvider.this.sendUpdates(updates);
        }
    }
    
    public static class Builder<Container>
        extends IConfigProvider.Builder<Builder<Container>, Container, ConfigProvider<Container>>
    {
        //**************************************************************************************************************
        private boolean optional = false;
        
        //**************************************************************************************************************
        protected Builder(final ConfigSpec<Container> spec) { super(spec); }
        
        //==============================================================================================================
        /// Marks this provider optional, by that means, the client is not required to have it.
        /// @return `this`
        public Builder<Container> setOptional()
        {
            this.optional = true;
            return this;
        }
        
        //==============================================================================================================
        /// Builds the provider.
        /// @param id The unique [Identifier] of the provider
        /// @return The new [ConfigProvider]
        @Override
        public ConfigProvider<Container> build(final Identifier id)
        {
            return new ConfigProvider<>(this.spec, id, this.ops, this.optional);
        }
    }
    
    //******************************************************************************************************************
    /// Creates a new [Builder].
    /// @param spec The [ConfigSpec] to build the provider from
    /// @return The [Builder]
    public static <Container> Builder<Container> builder(final ConfigSpec<Container> spec)
    {
        return new Builder<>(spec);
    }
    
    //==================================================================================================================
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> void trySet(final Property<T> property, final Optional<T> value)
    {
        try
        {
            if (value.isPresent())
            {
                property.setValue(value.orElseThrow());
            }
            else
            {
                property.reset();
            }
        }
        catch (final PropertyValidationException ex)
        {
            throw new PropagatingException(ex);
        }
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> void trySetUnchecked(final Property<T> property, final Optional<T> value)
    {
        value.ifPresentOrElse(property::setValueUnchecked, property::reset);
    }
    
    //******************************************************************************************************************
    private final ConfigSpec<Container> spec;
    
    private final Container serverContainer;
    
    @Nullable
    private final Container clientContainer;

    private boolean batchMode           = false;
    private boolean lockClientContainer = true;

    //******************************************************************************************************************
    private ConfigProvider(final ConfigSpec<Container> spec, final Identifier id, final DynamicOps<?> ops,
                           final boolean optional)
    {
        super(id, ops, optional);
        
        this.spec            = Objects.requireNonNull(spec, "config spec must not be null");
        this.serverContainer = spec.create();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        {
            this.clientContainer = spec.create();
            this.spec.bindContainer(this.clientContainer, this::clientValueChanged);
        }
        else
        {
            this.clientContainer = null;
            this.spec.bindContainer(this.serverContainer, this::serverValueChanged);
        }
    }

    //==================================================================================================================
    @Override public ConfigSpec<Container> getSpec() { return this.spec; }
    
    /// Gets the server container of this config provider.
    ///
    /// Do note that on a client that is connected to a dedicated server, this is unused and any changes to it on
    /// the render thread, regardless of whether the server is dedicated or not, will likely result in a race condition.
    ///
    /// If it is not immediately apparent what context this call currently is in, and there is a non-null
    /// [Level] instance at your disposal, it is almost always better to use [ConfigProvider#getContainer(Level)]
    /// @return The managed (server) container instance
    @Override public Container getManagedContainer() { return this.serverContainer; }
    
    /// Returns the server container of this config provider.
    ///
    /// On a dedicated server this will return null as there is no need for a client configuration.
    /// Calling this on the integrated server thread, however, might result in a race condition if the properties
    /// are read or changed.
    ///
    /// Generally, client containers should never be written to but only read from, that's what server containers are
    /// there for. In fact, writing to a client container will inevitably throw a [LockedPropertyChangeException].
    /// @return The client container instance
    public @Nullable Container getClientContainer() { return this.clientContainer; }

    /// Gets the biased container instance for the given [Level] object.
    ///
    /// This will determine based on [Level#isClientSide()] what container to get, if this flag is false it will return
    /// [ConfigProvider#getManagedContainer()], otherwise [ConfigProvider#getClientContainer()].
    ///
    /// This should only ever be used when reading from a container, as writing to a client container will throw
    /// [LockedPropertyChangeException].
    /// @param level The current world instance
    /// @return The container for that world object
    public @Nullable Container getContainer(final Level level)
    {
        return (level.isClientSide() ? this.clientContainer : this.serverContainer);
    }
    
    //==================================================================================================================
    /// Executes a batch update to multiple properties at the same time.
    ///
    /// If any of the properties throws a [PropertyValidationException], all changes are discarded.
    ///
    /// This is useful if there need to be several properties updated at the same time, but to bundle the updates sent
    /// to the client to reduce network traffic. It is also useful to change a set of properties at the same time
    /// and making sure all properties are only set if all receive valid values.
    ///
    /// This method should only ever be called on the server thread, otherwise a race condition might occur.
    ///
    /// Here is a simple example on how to use this:<br>
    /// We assume we have a `SheepConfig` with properties that define how a custom sheep entity
    /// spawns when summoned. This is defined by the properties `SheepConfig.defaultColour`,
    /// `SheepConfig.defaultSize` and `SheepConfig.defaultHostile`. We also provide a command, that defines some presets
    /// that, when applied, modifies these properties with a specified set of new values. For our example we have a
    /// preset "thunder-sheep", which has yellow colour, is twice the size of a normal sheep and is hostile towards the
    /// player, to implement this preset we do the following:
    /// ```java
    /// // Our configuration
    /// ConfigProvider<SheepConfig> SHEEP_CONFIG;
    ///
    /// // Let's build a batch update
    /// SHEEP_CONFIG.batchUpdate(updater -> updater
    ///     .set(SheepConfig::defaultColour,  new Value("yellow"))
    ///     .set(SheepConfig::defaultSize,    new Value(2))
    ///     .set(SheepConfig::defaultHostile, new Value(true)));
    /// ```
    /// @param updateBuilder The updater lambda expression
    /// @throws PropertyValidationException If any of the properties failed validation before setting the value
    public void batchUpdate(final Function<UpdateBuilder, UpdateBuilder> updateBuilder)
        throws PropertyValidationException
    {
        this.batchMode = true;

        try
        {
            updateBuilder.apply(new UpdateBuilder(this.serverContainer)).send();
        }
        finally
        {
            this.batchMode = false;
        }
    }

    //==================================================================================================================
    @Override
    void updateClient(final List<Pair<JsonPointer, Optional<?>>> updates, final boolean isRemote)
        throws PropertyValidationException
    {
        try
        {
            this.processClientUpdate(updates, ConfigProvider::trySet, isRemote);
        }
        catch (final PropagatingException ex)
        {
            if (ex.getCause() instanceof PropertyValidationException val_ex)
            {
                throw val_ex;
            }
            
            throw new InternalModException("unexpected exception encountered", ex.getCause());
        }
    }
    
    @Override
    void forceUpdateClient(final List<Pair<JsonPointer, Optional<?>>> updates, boolean isRemote)
    {
        this.processClientUpdate(updates, ConfigProvider::trySetUnchecked, isRemote);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> void processClientUpdate(final List<Pair<JsonPointer, Optional<?>>> updates,
                                         final BiConsumer<Property<T>, Optional<T>> consumer,
                                         final boolean                              isRemote)
    {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
        {
            Novia.LOGGER.error("Tried updating client provider on physical server");
            return;
        }
        
        if (!Novia.getInstance().isRenderThread())
        {
            Novia.LOGGER.error("Tried updating client provider not on logical client");
            return;
        }
        
        this.lockClientContainer = false;
        
        try
        {
            // noinspection unchecked
            updates.forEach(update -> consumer.accept(
                (Property<T>) this.spec.flatPropertySpecs()
                    .get(update.getFirst())
                    .get(Objects.requireNonNull(this.clientContainer)),
                (Optional<T>) update.getSecond()));
        }
        finally
        {
            this.lockClientContainer = true;
            this.setRemote(isRemote);
        }
    }

    //==================================================================================================================
    private <T> void serverValueChanged(final Property<T> property, final T value)
    {
        if (!this.batchMode)
        {
            this.sendUpdates(Collections.singletonList(Pair.of(
                Objects.requireNonNull(property.getSpec()).pointer(),
                Optional.of(value))));
        }
    }

    private <T> void clientValueChanged(final Property<T> property, final T ignoreds)
    {
        if (this.lockClientContainer)
        {
            throw new LockedPropertyChangeException(Objects.requireNonNull(property.getSpec()).pointer());
        }
    }
    
    //==================================================================================================================
    private void sendUpdates(final List<Pair<JsonPointer, Optional<?>>> values)
    {
        ConfigManagerNetwork.sendUpdates(this.getId(), values);
    }
}
