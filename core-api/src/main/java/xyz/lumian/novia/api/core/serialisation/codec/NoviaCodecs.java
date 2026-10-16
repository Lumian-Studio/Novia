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
package xyz.lumian.novia.api.core.serialisation.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import xyz.lumian.novia.api.core.util.NumberType;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;



//**********************************************************************************************************************
/// Provides a number of useful DataFixerUpper codecs that Minecraft does not.
public abstract class NoviaCodecs
{
    //******************************************************************************************************************
    /// Provides a generic number codec that encodes/decodes any scalar number type.
    public static final PrimitiveCodec<Number> NUMBER;

    /// Provides a codec for parsing/serialising regex pattern objects.
    public static final Codec<Pattern> PATTERN = Codec.of(
        Codec.STRING.comap(Pattern::pattern),
        Codec.STRING.flatMap(str ->
        {
            try
            {
                return DataResult.success(Pattern.compile(str));
            }
            catch (final PatternSyntaxException e)
            {
                return DataResult.error(() -> "Invalid regular expression '" + str + "'");
            }
        }));
    
    //==================================================================================================================
    static
    {
        NUMBER = new PrimitiveCodec<>()
        {
            @Override
            public <T> DataResult<Number> read(DynamicOps<T> ops, T input)
            {
                return ops.getNumberValue(input);
            }

            @Override
            public <T> T write(final DynamicOps<T> ops, final Number value)
            {
                return switch (NumberType.of(value))
                {
                    case BYTE   -> ops.createByte(value.byteValue());
                    case SHORT  -> ops.createShort(value.shortValue());
                    case INT    -> ops.createInt(value.intValue());
                    case LONG   -> ops.createLong(value.longValue());
                    case FLOAT  -> ops.createFloat(value.floatValue());
                    case DOUBLE -> ops.createDouble(value.doubleValue());

                    case NON_SCALAR -> ((value.longValue() == value.doubleValue())
                        ? ops.createLong(value.longValue())
                        : ops.createDouble(value.doubleValue()));
                };
            }

            @Override public String toString() { return "Number"; }
        };
    }
    
    //******************************************************************************************************************
    /// Unit codecs can be used to denote an empty value (in some languages synonymous to `null`). Similar to
    /// [Codec#unit(Object)], but does not create a [MapCodec].
    /// @param defaultValue The unit value
    /// @return The new unit codec
    /// @param <T> The codec value type
    public static <T> Codec<T> unit(final T defaultValue) { return NoviaCodecs.unit(() -> defaultValue); }
    
    /// Unit codecs can be used to denote an empty value (in some languages synonymous to `null`). Similar to
    /// [Codec#unit(Supplier)], but does not create a [MapCodec].
    /// @param defaultValue The unit value supplier
    /// @return The new unit codec
    /// @param <T> The codec value type
    public static <T> Codec<T> unit(final Supplier<T> defaultValue)
    {
        return new Codec<>()
        {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(final DynamicOps<T1> ops, final T1 input)
            {
                return (input.equals(ops.empty())
                    ? DataResult.success(Pair.of(defaultValue.get(), input))
                    : DataResult.error((() -> "not null"), Pair.of(defaultValue.get(), input)));
            }

            @Override
            public <T1> DataResult<T1> encode(final T input, final DynamicOps<T1> ops, final T1 prefix)
            {
                return DataResult.success(ops.empty());
            }
        };
    }
}
