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
package xyz.lumialights.novia.api.config.schema;

import com.google.gson.JsonElement;
import com.mojang.serialization.*;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class SchemaRegister
{
    //******************************************************************************************************************
    public static final Set<IPropertySchema>      ALL_SCHEMAS;
    public static final MapCodec<IPropertySchema> MAP_CODEC;
    
    //==================================================================================================================
    static
    {
        ALL_SCHEMAS = Set.of(
            ConstantSchema.DEFAULT, ElementSchema.DEFAULT, EnumSchema.DEFAULT, InterfaceSchema.DEFAULT,
            PatternMapSchema.DEFAULT, PatternSchema.DEFAULT, RangeSchema.DEFAULT, TupleSchema.DEFAULT,
            VariantSchema.DEFAULT,
            MapSchema.INSTANCE, ListSchema.INSTANCE, StringSchema.INSTANCE, NumberSchema.INSTANCE,
            BooleanSchema.INSTANCE, NullSchema.INSTANCE);
        
        MAP_CODEC = MapCodec.recursive("PropertySchema", parent -> new MapCodec<>()
        {
            //**********************************************************************************************************
            private final Map<String, MapCodec<? extends IPropertySchema>>         extensionCodecs;
            private final EnumMap<Value.Type, MapCodec<? extends IPropertySchema>> atomicCodecs;
            private final Set<JsonElement>                                         keywords;

            //==========================================================================================================
            {
                final Map<Boolean, List<IPropertySchema>> is_atomic_schemas = ALL_SCHEMAS
                    .stream()
                    .collect(Collectors.groupingBy(schema -> schema instanceof BaseAtomicSchema<?>));
                
                this.extensionCodecs = is_atomic_schemas
                    .get(false)
                    .stream()
                    .collect(Collectors.toMap(IPropertySchema::getTrigger, (schema -> schema.createMapCodec(parent))));
                
                this.atomicCodecs = new EnumMap<>(Value.Type.class);
                this.atomicCodecs.putAll(is_atomic_schemas
                    .get(true)
                    .stream()
                    .collect(Collectors.toMap(IPropertySchema::getType, (schema -> schema.createMapCodec(parent)))));
                
                this.keywords = Stream
                    .concat(this.extensionCodecs.values().stream(), this.atomicCodecs.values().stream())
                    .flatMap(codec -> codec.keys(JsonOps.INSTANCE))
                    .collect(Collectors.toSet());
            }
            
            //**********************************************************************************************************
            @SuppressWarnings("unchecked")
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops)
            {
                if (ops == JsonOps.INSTANCE)
                {
                    return this.keywords.stream().map(elm -> (T) elm);
                }
                
                return this.keywords.stream().map(elm -> JsonOps.INSTANCE.convertTo(ops, elm));
            }
            
            //==========================================================================================================
            @Override
            public <T> DataResult<IPropertySchema> decode(final DynamicOps<T> ops, final MapLike<T> input)
            {
                for (final var entry : this.extensionCodecs.entrySet())
                {
                    final T property = input.get(entry.getKey());
                    
                    if (property != null)
                    {
                        return entry.getValue()
                            .decode(ops, input)
                            .map(Function.identity());
                    }
                }
                
                final T type_op = input.get("type");
                
                if (type_op == null)
                {
                    return DataResult.error(() ->
                        "Invalid schema definition, no type and no schema specific keyword specified");
                }
                
                final DataResult<Value.Type> type_result = Value.Type.CODEC.parse(ops, type_op);
                
                if (type_result.isError())
                {
                    return DataResult.error(() ->
                        "Given type is not a valid schema type: " + type_result.error().orElseThrow().message());
                }
                
                final Value.Type type = type_result.getOrThrow();
                return this.atomicCodecs.get(type)
                    .decode(ops, input)
                    .map(Function.identity());
            }
            
            @Override
            public <T> RecordBuilder<T> encode(final IPropertySchema  input,
                                               final DynamicOps<T>    ops,
                                               final RecordBuilder<T> prefix)
            {
                if (!(input instanceof BaseAtomicSchema<?>))
                {
                    return this.encodeHelper(input, this.extensionCodecs.get(input.getTrigger()), ops, prefix);
                }
                
                return this.encodeHelper(input, this.atomicCodecs.get(input.getType()), ops, prefix);
            }
            
            //----------------------------------------------------------------------------------------------------------
            @SuppressWarnings("unchecked")
            private <T, C extends IPropertySchema> RecordBuilder<T> encodeHelper(final IPropertySchema  input,
                                                                                 final MapCodec<C>      codec,
                                                                                 final DynamicOps<T>    ops,
                                                                                 final RecordBuilder<T> prefix)
            {
                return codec.encode((C) input, ops, prefix);
            }
        });
    }
}
