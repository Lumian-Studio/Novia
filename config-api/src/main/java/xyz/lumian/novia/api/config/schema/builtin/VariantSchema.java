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
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public record VariantSchema(ImmutableList<IPropertySchema> schemas)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final VariantSchema DEFAULT = new VariantSchema(ImmutableList.of());
    
    //******************************************************************************************************************
    private static DataResult<Pair<Optional<Value.Type>, List<IPropertySchema>>> validateVariant(
        Pair<Optional<Value.Type>, List<IPropertySchema>> input
    )
    {
        if (input.getFirst().isPresent())
        {
            final Value.Type type = input.getFirst().orElseThrow();
            
            if (input.getSecond().stream().anyMatch(schema -> (schema.getType() != type)))
            {
                return DataResult.error(() -> "some sub-schemas did not satisfy typed requirement '%s'"
                    .formatted(type.getSerializedName()));
            }
        }
        
        return DataResult.success(input);
    }
    
    //******************************************************************************************************************
    public VariantSchema
    {
        Objects.requireNonNull(schemas, "schemas must not be null");
    }
    
    public VariantSchema(final List<IPropertySchema> schemas) { this(ImmutableList.copyOf(schemas)); }
    
    //==================================================================================================================
    @Override public @Nullable Value.Type getType()    { return null; }
    @Override public           String     getTrigger() { return "oneOf"; }
    
    @Override
    public MapCodec<VariantSchema> createMapCodec(final Codec<IPropertySchema> parent)
    {
        return RecordCodecBuilder
            .<Pair<Optional<Value.Type>, List<IPropertySchema>>>mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .forGetter(Pair::getFirst),
                    Codec.list(parent)
                        .fieldOf("oneOf")
                        .forGetter(Pair::getSecond))
                .apply(instance, Pair::of))
            .validate(VariantSchema::validateVariant)
            .xmap(
                (p -> new VariantSchema(ImmutableList.copyOf(p.getSecond()))),
                (schema -> Pair.of(Optional.empty(), schema.schemas)));
    }
    
    //==================================================================================================================
    @Override
    public DataResult<Value> validateValue(final Value value)
    {
        final Optional<DataResult<Value>> opt_result = this.schemas.stream()
            .map(schema -> schema.validate(value))
            .filter(DataResult::isSuccess)
            .findFirst();
        
        return opt_result.orElseGet(()
            -> DataResult.error(() -> "No sub-schema matched the value '" + value.asString() + "'"));
    }
}
