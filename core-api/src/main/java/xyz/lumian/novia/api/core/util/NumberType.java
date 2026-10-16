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
package xyz.lumian.novia.api.core.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.network.codec.NoviaStreamCodecs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.*;



//**********************************************************************************************************************
/// Helper enum that allows an easier distinction between different instantiation of the [Number] class.
public enum NumberType
    implements StringRepresentable
{
    //******************************************************************************************************************
    BYTE        (Byte.class),
    SHORT       (Short.class),
    INT         (Integer.class),
    LONG        (Long.class),
    FLOAT       (Float.class),
    DOUBLE      (Double.class),
    NON_SCALAR  (Number.class),
    ;

    //******************************************************************************************************************
    /// The packet codec for the number type.
    public static final StreamCodec<ByteBuf, NumberType> STREAM_CODEC = NoviaStreamCodecs.enumeration(NumberType.class);

    /// The serialisation codec for the number type.
    public static final Codec<NumberType> CODEC = StringRepresentable.fromEnum(NumberType::values);

    //******************************************************************************************************************
    /// Gets the associated enum constant for the instantiated subclass of [Number].
    /// If the number is neither [Byte], [Short], [Integer], [Long], [Float] nor [Double],
    /// [NumberType#NON_SCALAR] will be returned.
    ///
    /// @param number The number
    /// @return The [NumberType]
    public static NumberType of(final Number number)
    {
        final Class<? extends Number> clazz = number.getClass();
        return Arrays
            .stream(NumberType.values())
            .filter(c -> c.numberClass.equals(clazz))
            .findFirst()
            .orElse(NON_SCALAR);
    }
    
    /// {@return `true` if this type represents either {@link Float} or {@link Double}}
    public static boolean isFloatingPointScalar(final @Nullable Number number)
    {
        return (number instanceof Float || number instanceof Double);
    }
    
    /// {@return `true` if this type represents either {@link BigDecimal}, {@link DoubleAccumulator}
    /// or {@link DoubleAdder}}
    public static boolean isNonScalarFloatingPoint(final @Nullable Number number)
    {
        return (number instanceof BigDecimal
             || number instanceof DoubleAccumulator
             || number instanceof DoubleAdder);
    }
    
    /// {@return `true` if either {@link #isFloatingPointScalar()} or {@link #isNonScalarFloatingPoint(Number)} is
    /// `true`}
    public static boolean isFloatingPoint(final @Nullable Number number)
    {
        return (NumberType.isFloatingPointScalar(number) || NumberType.isNonScalarFloatingPoint(number));
    }

    /// {@return `true` if this type represents either {@link Byte}, {@link Short}, {@link Integer} or {@link Long}}
    public static boolean isIntegralScalar(final @Nullable Number number)
    {
        return (number instanceof Byte
             || number instanceof Short
             || number instanceof Integer
             || number instanceof Long);
    }
    
    /// {@return `true` if this type represents either {@link BigInteger}, {@link AtomicInteger}, {@link AtomicLong},
    /// {@link LongAccumulator} or {@link LongAdder}}
    public static boolean isNonScalarIntegral(final @Nullable Number number)
    {
        return (number instanceof BigInteger
             || number instanceof AtomicInteger
             || number instanceof AtomicLong
             || number instanceof LongAccumulator
             || number instanceof LongAdder);
    }
    
    /// {@return `true` if either {@link #isIntegralScalar()} or {@link #isNonScalarIntegral(Number)} is `true`}
    public static boolean isIntegral(final @Nullable Number number)
    {
        return (NumberType.isIntegralScalar(number) || NumberType.isNonScalarIntegral(number));
    }

    /// {@return `true` if this is any of the types of {@link NumberType} that is not {@link #NON_SCALAR}}
    public static boolean isScalar(final @Nullable Number number)
    {
        return (NumberType.isIntegralScalar(number) || NumberType.isFloatingPointScalar(number));
    }

    //******************************************************************************************************************
    private final Class<? extends Number> numberClass;

    //******************************************************************************************************************
    <T extends Number> NumberType(final Class<T> clazz) { this.numberClass = clazz; }

    //==================================================================================================================
    /// Gets the associated type class.
    /// @return The number type class
    public Class<? extends Number> getTypeClass() { return this.numberClass; }

    //==================================================================================================================
    /// {@return `true` if this type represents either {@link #FLOAT} or {@link #DOUBLE}}
    public boolean isFloatingPointScalar() { return (this == FLOAT || this == DOUBLE); }
    
    /// {@return `true` if this type represents either {@link #BYTE}, {@link #SHORT}, {@link #INT} or {@link #LONG}}
    public boolean isIntegralScalar() { return (this == BYTE || this == SHORT || this == INT || this == LONG); }
    
    /// {@return `true` if this type represents {@link #NON_SCALAR}}
    public boolean isNonScalar() { return this == NON_SCALAR; }

    //==================================================================================================================
    @Override public String getSerializedName() { return this.name().toLowerCase(); }
}
