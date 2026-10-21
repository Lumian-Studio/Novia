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
package xyz.lumian.novia.api.config.schema.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.NumberType;

import java.util.*;



//**********************************************************************************************************************
public record RangeSchema(double min, double max)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final RangeSchema           DEFAULT = new RangeSchema(-Double.MAX_VALUE, Double.MAX_VALUE);
    public static final MapCodec<RangeSchema> MAP_CODEC;
    
    //==================================================================================================================
    static
    {
        MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .validate(type -> (type.orElse(Value.Type.NUMBER) == Value.Type.NUMBER
                            ? DataResult.success(type)
                            : DataResult.error(() ->
                                "'minimum' and 'maximum' are not compatible with typed requirement '%s'"
                                    .formatted(type.orElseThrow().getSerializedName()))))
                        .forGetter(_ -> Optional.empty()),
                    Codec.DOUBLE
                        .fieldOf("minimum")
                        .forGetter(RangeSchema::min),
                    Codec.DOUBLE
                        .fieldOf("maximum")
                        .forGetter(RangeSchema::max))
                .apply(instance, ((_, min, max) -> new RangeSchema(min, max))));
    }
    
    //******************************************************************************************************************
    public RangeSchema
    {
        if (min > max)
        {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
    }
    
    //******************************************************************************************************************
    @Override public Value.Type getType()    { return Value.Type.NUMBER; }
    @Override public String     getTrigger() { return "minimum"; }
    
    @Override public MapCodec<RangeSchema> createMapCodec(final Codec<IPropertySchema> parent) { return MAP_CODEC; }
    
    //==================================================================================================================
    @Override
    public DataResult<Value> validateValue(final Value value)
    {
        final Number number = value.getNumber();
        
        if (NumberType.isFloatingPointScalar(number))
        {
            final double val = number.doubleValue();
            return (!(val < this.min || val > this.max) ? DataResult.success(value) : makeError(number));
        }
        
        final long val = number.longValue();
        return (!(val < this.min || val > this.max) ? DataResult.success(value) : makeError(number));
    }
    
    //==================================================================================================================
    private DataResult<Value> makeError(final Number number)
    {
        return DataResult.error(() -> String.format(
            "Value '%s' is out of range [%s, %s]",
            number, this.min, this.max));
    }
}
