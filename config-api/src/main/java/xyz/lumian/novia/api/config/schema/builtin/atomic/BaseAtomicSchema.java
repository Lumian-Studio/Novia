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
package xyz.lumian.novia.api.config.schema.builtin.atomic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.core.serialisation.Value;



//**********************************************************************************************************************
public sealed abstract class BaseAtomicSchema<T extends BaseAtomicSchema<T>>
    implements IPropertySchema
    permits
        BooleanSchema,
        ListSchema,
        MapSchema,
        StringSchema,
        NumberSchema,
        NullSchema
{
    //******************************************************************************************************************
    private final Value.Type                    type;
    private final MapCodec<BaseAtomicSchema<T>> codec;
    
    //******************************************************************************************************************
    protected BaseAtomicSchema(final Value.Type type)
    {
        this.type  = type;
        this.codec = RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .fieldOf("type")
                        .validate(t -> (t == this.type
                            ? DataResult.success(t)
                            : DataResult.error(() -> "'type' must be '" + this.type.name().toLowerCase() + "'")))
                        .forGetter(BaseAtomicSchema::getType))
                .apply(instance, (_ -> this)));
    }
    
    //==================================================================================================================
    @Override public Value.Type getType()    { return this.type; }
    @Override public String     getTrigger() { return "type"; }
    
    @Override
    public MapCodec<BaseAtomicSchema<T>> createMapCodec(final Codec<IPropertySchema> parent)
    {
        return this.codec;
    }
    
    //==================================================================================================================
    @Override public DataResult<Value> validateValue(final Value value) { return DataResult.success(value); }
}
