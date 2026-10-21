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

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public record TupleSchema(ImmutableList<IPropertySchema> schemas)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final TupleSchema DEFAULT = new TupleSchema(ImmutableList.of());
    
    //******************************************************************************************************************
    public TupleSchema
    {
        Objects.requireNonNull(schemas, "schemas must not be null");
        
        for (int i = 0; i < schemas.size(); i++)
        {
            Objects.requireNonNull(schemas.get(i), "tuple sub-schema " + (i + 1) + " must not be null");
        }
    }
    
    public TupleSchema(final List<IPropertySchema> schemas) { this(ImmutableList.copyOf(schemas)); }
    
    //==================================================================================================================
    @Override public Value.Type getType()    { return Value.Type.LIST; }
    @Override public String     getTrigger() { return "prefixItems"; }
    
    @Override
    public MapCodec<TupleSchema> createMapCodec(final Codec<IPropertySchema> parent)
    {
        return RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .validate(SchemaCodecHelper.createTypedValidator(Value.Type.LIST, "prefixItems"))
                        .forGetter(schema -> Optional.empty()),
                    parent
                        .listOf()
                        .fieldOf("prefixItems")
                        .forGetter(TupleSchema::schemas))
                .apply(instance, ((type, sub_schemas) -> new TupleSchema(ImmutableList.copyOf(sub_schemas)))));
    }
    
    //==================================================================================================================
    @Override
    public DataResult<Value> validateValue(final Value value)
    {
        final List<Value> values = value.getList();
        
        if (this.schemas.size() != values.size())
        {
            return DataResult.error(() ->
                "Insufficient tuple elements, expected " + this.schemas.size() + " but got " + values.size());
        }
        
        for (int i = 0; i < this.schemas.size(); i++)
        {
            final DataResult<Value> result = this.schemas.get(i).validateValue(values.get(i));
            
            if (result.isError())
            {
                final int pos = (i + 1);
                return DataResult.error(() -> String.format(
                    "Tuple element %d does not match schema: %s",
                    pos, result.error().orElseThrow().message()));
            }
        }
        
        return DataResult.success(value);
    }
}
