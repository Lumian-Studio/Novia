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
package xyz.lumian.novia.api.config.spec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.ConfigManager;
import xyz.lumian.novia.api.config.PropertyValidationException;
import xyz.lumian.novia.api.config.registry.ConfigRegistryException;
import xyz.lumian.novia.api.config.property.impl.IPropertyConfigHandler;
import xyz.lumian.novia.api.core.serialisation.dynops.ConfigSerialisationException;
import xyz.lumian.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;
import xyz.lumian.novia.api.core.serialisation.dynops.IDynamicOpsSerialiser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;



//**********************************************************************************************************************
/// Describes a specification for a config container class.
/// @param flatPropertySpecs A flat map of property specifications mapped to their JSON pointer in the configuration.
/// @param rootGroupSpec     The root group specification that contains all the properties and groups on the first level.
/// @param containerClass    The class instance of the container that this specification describes.
/// @param id                The ID of the specification
/// @param generator         The container generator function that creates new instances of the given class.
/// @param <Container>       The config container type
public record ConfigSpec<Container>(
    PropertySpecMap<Container> flatPropertySpecs,
    GroupSpec<Container>       rootGroupSpec,
    Class<Container>           containerClass,
    Identifier                 id,
    Supplier<Container>        generator
)
{
    //******************************************************************************************************************
    public static final SchemaStore SCHEMA_STORE = new SchemaStore(ConfigManager.REGISTRY);
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Map<Identifier, ConfigSpec<?>> REGISTRY = new HashMap<>();
    
    //******************************************************************************************************************
    public static Optional<ConfigSpec<?>> findSpec(final Identifier id)
    {
        return Optional.ofNullable(ConfigSpec.REGISTRY.get(id));
    }
    
    //******************************************************************************************************************
    public ConfigSpec
    {
        if (ConfigSpec.REGISTRY.containsKey(id))
        {
            throw new ConfigRegistryException("config spec with id '%s' is already registered".formatted(id));
        }
        
        final Container test_container = generator.get();
        
        try
        {
            for (final var prop_spec : flatPropertySpecs.values())
            {
                prop_spec.validateForContainer(test_container);
            }
        }
        catch (final PropertyValidationException ex)
        {
            throw new RuntimeException(
                "could not build config specification '%s', some property's default values do not satisfy their schema"
                    .formatted(id),
                ex);
        }
        
        ConfigSpec.REGISTRY.put(id, this);
    }
    
    //==================================================================================================================
    /// Creates a new serialisation codec for the specified container instance.
    /// @param container The container instance
    /// @return The new codec
    public Codec<Container> createCodec(final Container container)
    {
        return new Codec<>()
        {
            //**********************************************************************************************************
            @Override
            public <T> DataResult<Pair<Container, T>> decode(final DynamicOps<T> ops, final T input)
            {
                return ConfigSpec.this.rootGroupSpec.decode(container, ops, input).map((t -> Pair.of(container, t)));
            }

            @Override
            public <T> DataResult<T> encode(final Container ignored, final DynamicOps<T> ops, final T prefix)
            {
                return ConfigSpec.this.rootGroupSpec.encode(container, ops, prefix);
            }
        };
    }

    /// Creates a new instance of the container class.
    /// @return The new container instance
    public Container create() { return this.generator.get(); }

    /// Binds the given container instance's properties to this specification. The given handler will be executed before
    /// any of the container's properties set their new value, which allows some pre-evaluation logic if needed.
    /// @param container The container to bind
    /// @param handler   The change handler to attach to the properties
    public void bindContainer(final Container container, final @Nullable IPropertyConfigHandler handler)
    {
        this.flatPropertySpecs.values().forEach(spec -> spec.bind(container, handler));
    }

    //==================================================================================================================
    /// Reads the configuration from the specified file and modifies the given container instance based on the readings.
    /// @param ops       The serialiser instance to use
    /// @param file      The file to read from
    /// @param container The container instance to modify
    /// @throws ConfigSerialisationException If there was a problem reading or decoding the configuration file
    public <T> void read(final DynamicOps<T> ops, final Path file, final Container container)
        throws ConfigSerialisationException
    {
        try
        {
            final Optional<IDynamicOpsSerialiser<T>> serialiser = DynamicOpsSerialisers.get(ops);

            if (serialiser.isEmpty())
            {
                throw new ConfigSerialisationException(
                    "problem decoding container %s, didn't know how to decode with %s"
                        .formatted(this.containerClass.getName(), ops.getClass().getName()));
            }

            final T                     input  = serialiser.orElseThrow().deserialise(Files.newInputStream(file));
            final DataResult<Container> result = this.createCodec(container).parse(ops, input);
            
            if (result.isError())
            {
                final DataResult.Error<Container> error = result.error().orElseThrow();
                throw new ConfigSerialisationException("problem decoding container: " + error.message());
            }
        }
        catch (final IOException ex)
        {
            throw new ConfigSerialisationException(String.format(
                "problem reading container %s from file '%s'",
                this.containerClass.getName(), file), ex);
        }
    }

    /// Reads the given container instance and writes the configuration to the specified file.<p>
    /// @param ops       The serialiser instance to use
    /// @param file      The file to read from
    /// @param container The container instance to modify
    /// @throws ConfigSerialisationException If there was a problem writing or encoding the configuration file
    public <T> void write(final DynamicOps<T> ops, final Path file, final Container container)
        throws ConfigSerialisationException
    {
        try
        {
            final DataResult<T> node = this.createCodec(container).encodeStart(ops, container);
            
            if (node.isError())
            {
                final DataResult.Error<T> error = node.error().orElseThrow();
                throw new ConfigSerialisationException("problem encoding container %s, %s"
                    .formatted(this.containerClass.getName(), error.message()));
            }

            final Optional<IDynamicOpsSerialiser<T>> serialiser = DynamicOpsSerialisers.get(ops);

            if (serialiser .isEmpty())
            {
                throw new ConfigSerialisationException(
                    "problem encoding container %s, didn't know how to encode with %s"
                        .formatted(this.containerClass.getName(), ops.getClass().getName()));
            }

            serialiser.orElseThrow().serialise(node.getOrThrow(), Files.newOutputStream(file));
        }
        catch (final IOException ex)
        {
            throw new ConfigSerialisationException(
                "problem writing container %s to file '%s'".formatted(this.containerClass.getName(), file),
                ex);
        }
    }
}
