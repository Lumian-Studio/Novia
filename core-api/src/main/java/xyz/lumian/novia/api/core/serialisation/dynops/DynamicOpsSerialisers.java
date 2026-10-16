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
package xyz.lumian.novia.api.core.serialisation.dynops;

import com.google.gson.*;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.*;
import org.apache.commons.io.IOUtils;
import xyz.lumian.novia.api.core.util.RefUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



//**********************************************************************************************************************
/// The registry for all registered dynamic ops serialisers.
public abstract class DynamicOpsSerialisers
{
    //******************************************************************************************************************
    public static abstract class TextSerialiser<T>
        implements IDynamicOpsSerialiser<T>
    {
        //**************************************************************************************************************
        @Override public boolean isReadableDataFormat() { return true; }
        
        //==============================================================================================================
        @Override
        public T deserialise(final InputStream stream)
            throws
                IOException,
                ConfigSerialisationException
        {
            Objects.requireNonNull(stream, "input stream must not be null");
            
            final String document = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return deserialiseString(document);
        }
        
        @Override
        public void serialise(final T input, final OutputStream stream)
            throws IOException
        {
            Objects.requireNonNull(input,  "input must not be null");
            Objects.requireNonNull(stream, "output stream must not be null");
            
            final String document = serialiseString(input);
            IOUtils.write(document, stream, StandardCharsets.UTF_8);
        }
    }
    
    public static abstract class BinarySerialiser<T>
        implements IDynamicOpsSerialiser<T>
    {
        //**************************************************************************************************************
        @Override public boolean isReadableDataFormat() { return false; }
        
        //==============================================================================================================
        @Override public String serialiseString  (final T input)      { throw new UnsupportedOperationException(); }
        @Override public T      deserialiseString(final String input) { throw new UnsupportedOperationException(); }
    }
    
    //==================================================================================================================
    public static class Json
        extends TextSerialiser<JsonElement>
    {
        //**************************************************************************************************************
        public static final Json INSTANCE = new Json();
        
        //--------------------------------------------------------------------------------------------------------------
        private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setStrictness(Strictness.LENIENT)
            .create();
        
        //**************************************************************************************************************
        @Override public String[]                getFileExtensions() { return new String[] { "json" }; }
        @Override public DynamicOps<JsonElement> getOps()            { return JsonOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public JsonElement deserialiseString(final String input)
            throws ConfigSerialisationException
        {
            try
            {
                return Json.GSON.fromJson(input, JsonElement.class);
            }
            catch (final JsonSyntaxException ex)
            {
                throw new ConfigSerialisationException(ex);
            }
        }
        
        @Override
        public String serialiseString(final JsonElement input)
        {
            return Json.GSON.fromJson(input, String.class);
        }
    }
    
    //==================================================================================================================
    public static class Nbt
        extends BinarySerialiser<Tag>
    {
        //**************************************************************************************************************
        public static final Nbt INSTANCE = new Nbt();
        
        //**************************************************************************************************************
        @Override public String[]        getFileExtensions() { return new String[] { "dat" }; }
        @Override public DynamicOps<Tag> getOps()            { return NbtOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public Tag deserialise(final InputStream input)
            throws
                IOException,
                ConfigSerialisationException
        {
            try
            {
                return NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
            }
            catch (final NbtAccounterException ex)
            {
                throw new ConfigSerialisationException(ex);
            }
        }

        @Override
        public void serialise(final Tag input, final OutputStream output)
            throws IOException
        {
            NbtIo.writeCompressed((CompoundTag) input, output);
        }
    }
    
    public static class Java
        extends BinarySerialiser<Object>
    {
        //**************************************************************************************************************
        public static final Java INSTANCE = new Java();
        
        //**************************************************************************************************************
        @Override public String[]           getFileExtensions() { return new String[] { "ser" }; }
        @Override public DynamicOps<Object> getOps()            { return JavaOps.INSTANCE; }
        
        //==============================================================================================================
        @Override
        public Object deserialise(final InputStream input)
            throws
                IOException,
                ConfigSerialisationException
        {
            try
            {
                return (new ObjectInputStream(input)).readObject();
            }
            catch (final ClassNotFoundException | InvalidClassException | StreamCorruptedException
                         | OptionalDataException ex)
            {
                throw new ConfigSerialisationException(ex);
            }
        }

        @Override
        public void serialise(final Object input, final OutputStream output)
            throws IOException
        {
            (new ObjectOutputStream(output)).writeObject(input);
        }
    }
    
    //******************************************************************************************************************
    private static final Map<Class<? extends DynamicOps<?>>, IDynamicOpsSerialiser<?>> PROVIDERS;
    
    //==================================================================================================================
    static
    {
        PROVIDERS = new ConcurrentHashMap<>();
        
        DynamicOpsSerialisers.registerHelper(JsonOps.class, new Json());
        DynamicOpsSerialisers.registerHelper(NbtOps.class,  new Nbt());
        DynamicOpsSerialisers.registerHelper(JavaOps.class, new Java());
    }
    
    //******************************************************************************************************************
    /// Gets a [DynamicOps] serialiser that can read from and write to files based on the given dynops instance.
    /// @param opsClass The [DynamicOps] class to get the serialiser for
    /// @return The serialiser or null if none was found
    /// @param <T> Type of object the dynops instance manages
    @SuppressWarnings("unchecked")
    public static <T> Optional<IDynamicOpsSerialiser<T>> get(final Class<? extends DynamicOps<T>> opsClass)
    {
        return Optional.ofNullable((IDynamicOpsSerialiser<T>) DynamicOpsSerialisers.PROVIDERS.get(opsClass));
    }
    
    /// Gets a [DynamicOps] serialiser that can read from and write to files based on the given dynops instance.
    /// @param ops The [DynamicOps] instance to get the serialiser for
    /// @return The serialiser or null if none was found
    /// @param <T> Type of object the dynops instance manages
    @SuppressWarnings("unchecked")
    public static <T> Optional<IDynamicOpsSerialiser<T>> get(final DynamicOps<T> ops)
    {
        return Optional.ofNullable((IDynamicOpsSerialiser<T>) DynamicOpsSerialisers.PROVIDERS.get(ops.getClass()));
    }
    
    /// Finds all registered serialisers that match the file extension.
    /// @param extension The file extension (with or without leading '.')
    /// @return A set of [IDynamicOpsSerialiser], or empty if none were found
    public static Set<IDynamicOpsSerialiser<?>> findByExtension(final String extension)
    {
        final String extension_adjusted = (extension.startsWith(".") ? extension.substring(1) : extension);
        return DynamicOpsSerialisers.PROVIDERS.values().stream()
            .filter(ser -> Arrays.stream(ser.getFileExtensions()).anyMatch(RefUtils.equal(extension_adjusted)))
            .collect(Collectors.toSet());
    }
    
    /// Finds the first matching registered serialiser that matches the file extension.
    /// @param extension The file extension (with or without leading '.')
    /// @return An optional of an [IDynamicOpsSerialiser], or empty if none was found
    public static Optional<IDynamicOpsSerialiser<?>> findFirstByExtension(final String extension)
    {
        final String extension_adjusted = (extension.startsWith(".") ? extension.substring(1) : extension);
        return DynamicOpsSerialisers.PROVIDERS.values().stream()
            .filter(ser -> Arrays.stream(ser.getFileExtensions()).anyMatch(RefUtils.equal(extension_adjusted)))
            .findFirst();
    }
    
    //==================================================================================================================
    /// Registers a custom serialiser for the given [DynamicOps] class.
    /// @param opsClass   The [DynamicOps] class to register this serialiser for
    /// @param serialiser The serialiser to register
    /// @throws DynOpsRegistryException If a serialiser with that [DynamicOps] class had already been registered
    /// @param <T> Type of object the dynops instance manages
    public static <T> void register(final Class<? extends DynamicOps<T>> opsClass,
                                    final IDynamicOpsSerialiser<T>       serialiser)
    {
        if (DynamicOpsSerialisers.PROVIDERS.containsKey(Objects.requireNonNull(opsClass)))
        {
            throw new DynOpsRegistryException(opsClass.getCanonicalName() + " is already registered");
        }
        
        DynamicOpsSerialisers.registerHelper(opsClass, Objects.requireNonNull(serialiser));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static <T> void registerHelper(final Class<? extends DynamicOps<T>> opsClass,
                                           final IDynamicOpsSerialiser<T>       serialiser)
    {
        DynamicOpsSerialisers.PROVIDERS.put(opsClass, serialiser);
    }
}
