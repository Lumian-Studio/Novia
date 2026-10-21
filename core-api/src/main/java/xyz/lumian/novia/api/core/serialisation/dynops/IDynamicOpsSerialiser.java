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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.io.*;



//**********************************************************************************************************************
/// Interface describing an abstracting adapter to a serialisation backend such as Jackson or GSON.
/// @param <T> The deserialised object type (such as [com.google.gson.JsonElement])
public interface IDynamicOpsSerialiser<T>
{
    //******************************************************************************************************************
    /// Determines all valid file extensions that this serialiser accepts.
    /// The file extensions should be ordered by conventional occurrence
    /// (e.g. "yaml" first as it is more conventional than "yml").
    /// @return An array of all valid file extensions
    String[] getFileExtensions();
    
    /// Gets the associated [DynamicOps] instance for this data format.
    /// @return The associated [DynamicOps] instance
    DynamicOps<T> getOps();
    
    //==================================================================================================================
    /// {@return whether this data format is in a human-readable representation}
    boolean isReadableDataFormat();
    
    //==================================================================================================================
    /// Takes the given input stream and converts it into the serialiser's preferred representation.
    /// @param stream The [InputStream] to read from
    /// @return The data
    T deserialise(InputStream stream) throws IOException, ConfigSerialisationException;
    
    /// Takes the given de-serialised object and writes it to the given output stream.
    /// @param input  The data
    /// @param stream The [OutputStream] to write to
    void serialise(T input, OutputStream stream) throws IOException;
    
    /// Takes the given input string and converts it into the serialiser's preferred representation.
    /// @param input The string to parse
    /// @return The serialiser's abstract data representation object
    T deserialiseString(String input) throws ConfigSerialisationException;
    
    /// Takes the given de-serialised object and returns it as a serialised string.
    /// @param input  The data
    /// @return The string representing the serialised input
    String serialiseString(T input);
    
    //==================================================================================================================
    /// Decodes the given stream with the given codec and returns a result.
    /// @param stream The [InputStream] to read from
    /// @param codec  The [Codec] the data supposedly is serialised in
    /// @return The [DataResult] of the decoding operation
    default <R> DataResult<R> decode(final InputStream stream, final Codec<R> codec)
        throws IOException
    {
        try
        {
            final T input = this.deserialise(stream);
            return codec.parse(this.getOps(), input);
        }
        catch (final ConfigSerialisationException ex)
        {
            return DataResult.error(ex::getMessage);
        }
    }
    
    /// Decodes the given string with the given codec and returns a result.
    /// @param string The string to parse
    /// @param codec  The [Codec] the data supposedly is serialised in
    /// @return The [DataResult] of the decoding operation
    default <R> DataResult<R> decodeString(final String string, final Codec<R> codec)
    {
        try
        {
            final T input = this.deserialiseString(string);
            return codec.parse(this.getOps(), input);
        }
        catch (final ConfigSerialisationException ex)
        {
            return DataResult.error(ex::getMessage);
        }
    }
    
    /// Encodes the given input object with the given codec and writes it to the stream.
    /// @param stream The [OutputStream] to write to
    /// @param input  The object to encode
    /// @param codec  The [Codec] to encode the object with
    /// @return The intermediate [DataResult] of the encoding operation in the serialiser's object representation
    default <R> DataResult<T> encode(final OutputStream stream, final R input, final Codec<R> codec)
        throws IOException
    {
        final DataResult<T> result = codec.encodeStart(this.getOps(), input);
        
        if (result.isSuccess())
        {
            this.serialise(result.getOrThrow(), stream);
        }
        
        return result;
    }
    
    /// Encodes the given input object with the given codec and returns the result as a string.
    /// @param input The object to encode
    /// @param codec The [Codec] to encode the object with
    /// @return The intermediate [DataResult] of the encoding operation in the serialiser's object representation paired
    ///         with the result string of the operation
    default <R> DataResult<Pair<T, String>> encodeString(final R input, final Codec<R> codec)
    {
        return codec.encodeStart(this.getOps(), input).map(s -> Pair.of(s, this.serialiseString(s)));
    }
}
