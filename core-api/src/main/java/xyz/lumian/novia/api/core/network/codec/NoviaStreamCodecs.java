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
package xyz.lumian.novia.api.core.network.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import xyz.lumian.novia.api.core.util.NumberType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;



//**********************************************************************************************************************
/// Provides a collection of useful network stream codecs.
public abstract class NoviaStreamCodecs
{
    //******************************************************************************************************************
    /// Provides a packet codec for [BigInteger] types.
    public static final StreamCodec<ByteBuf, BigInteger> BIG_INTEGER;

    /// Provides a packet codec for [BigDecimal] types.
    public static final StreamCodec<ByteBuf, BigDecimal> BIG_DECIMAL;

    /// Provides a [StreamCodec] that serialises objects of type [Number] depending on the dynamic type of the object
    /// (e.g. [Integer] or [Float]).
    ///
    /// Types with bigger precision (like [BigDecimal]) will be converted to their scalar counterparts,
    /// which may result in precision loss.
    public static final StreamCodec<ByteBuf, Number> NUMBER;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final StreamCodec<ByteBuf, Number> NON_SCALAR;

    //==================================================================================================================
    static
    {
        BIG_INTEGER = ByteBufCodecs.BYTE_ARRAY.map(BigInteger::new, BigInteger::toByteArray);
        BIG_DECIMAL = StreamCodec.composite(
            BIG_INTEGER,       BigDecimal::unscaledValue,
            ByteBufCodecs.INT, BigDecimal::scale,
            ByteBufCodecs.INT, BigDecimal::precision,
            (i, s, p) -> new BigDecimal(i, s, new MathContext(p)));

        NUMBER = NumberType.STREAM_CODEC.dispatch(NumberType::of, (type ->
            switch (type)
            {
                case BYTE       -> ByteBufCodecs.BYTE;
                case SHORT      -> ByteBufCodecs.SHORT;
                case INT        -> ByteBufCodecs.INT;
                case LONG       -> ByteBufCodecs.LONG;
                case FLOAT      -> ByteBufCodecs.FLOAT;
                case DOUBLE     -> ByteBufCodecs.DOUBLE;
                case NON_SCALAR -> NoviaStreamCodecs.NON_SCALAR;
            }));
        
        NON_SCALAR = new StreamCodec<>()
        {
            @Override
            public Number decode(final ByteBuf buf)
            {
                return (buf.readBoolean() ? ByteBufCodecs.LONG.decode(buf) : ByteBufCodecs.DOUBLE.decode(buf));
            }

            @Override
            public void encode(final ByteBuf buf, final Number value)
            {
                if (value.longValue() == value.doubleValue())
                {
                    buf.writeBoolean(true);
                    ByteBufCodecs.LONG.encode(buf, value.longValue());
                }
                else
                {
                    buf.writeBoolean(false);
                    ByteBufCodecs.DOUBLE.encode(buf, value.doubleValue());
                }
            }
        };
    }
    
    //==================================================================================================================
    /// Creates an enum stream codec for a given enum class.
    /// @param enumClass The class of the enum to create a [StreamCodec] for
    /// @return The new [StreamCodec]
    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumeration(final Class<E> enumClass)
    {
        final E[] constants = enumClass.getEnumConstants();
        return ByteBufCodecs.VAR_INT.map((ord -> constants[ord]), Enum::ordinal);
    }
    
    //******************************************************************************************************************
    private NoviaStreamCodecs() {}
}
