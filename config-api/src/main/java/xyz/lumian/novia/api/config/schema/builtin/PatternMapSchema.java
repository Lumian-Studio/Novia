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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.serialisation.codec.NoviaCodecs;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public record PatternMapSchema(ImmutableMap<Pattern, IPropertySchema> patternProperties)
    implements IPropertySchema
{
    //******************************************************************************************************************
    public static final PatternMapSchema DEFAULT = new PatternMapSchema(ImmutableMap.of());
    
    //******************************************************************************************************************
    public PatternMapSchema(final Map<Pattern, IPropertySchema> patternProperties)
    {
        this(ImmutableMap.copyOf(patternProperties));
    }
    
    //==================================================================================================================
    @Override public Value.Type getType()    { return Value.Type.MAP; }
    @Override public String     getTrigger() { return "patternProperties"; }
    
    @Override
    public MapCodec<PatternMapSchema> createMapCodec(final Codec<IPropertySchema> parent)
    {
        return RecordCodecBuilder
            .mapCodec(instance -> instance
                .group(
                    Value.Type.CODEC
                        .optionalFieldOf("type")
                        .validate(SchemaCodecHelper.createTypedValidator(Value.Type.MAP, "patternProperties"))
                        .forGetter(s -> Optional.empty()),
                    Codec
                        .unboundedMap(NoviaCodecs.PATTERN, parent)
                        .fieldOf("patternProperties")
                        .forGetter(PatternMapSchema::patternProperties))
                .apply(instance, ((type, patterns) -> new PatternMapSchema(ImmutableMap.copyOf(patterns)))));
    }
    
    //==================================================================================================================
    @Override
    public DataResult<Value> validateValue(final Value value)
    {
        final Map<Value, Value> entries = value.getMap();
        
        for (final var entry : entries.entrySet())
        {
            final String                        key      = entry.getKey().asString();
            final Map<Pattern, IPropertySchema> matching = this.patternProperties
                .entrySet()
                .stream()
                .filter(e -> e.getKey().matcher(key).matches())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            if (matching.isEmpty())
            {
                return DataResult.error(() ->
                    "None of map schema property patterns ([%s]) matched property name '%s'".formatted(
                        this.patternProperties
                            .keySet()
                            .stream()
                            .map(Pattern::pattern)
                            .collect(Collectors.joining(", ")),
                        key));
            }
            
            final Value sub_value = entry.getValue();
            boolean     matched   = false;
            
            for (final var sub_schema : matching.values())
            {
                final DataResult<Value> result = sub_schema.validateValue(sub_value);
                
                if (result.isSuccess())
                {
                    matched = true;
                    break;
                }
            }
            
            if (!matched)
            {
                return DataResult.error(() ->
                    ("None of matching map schema property patterns ([%s]) given sub-schemas "
                        + "matched property's '%s' value '%s'").formatted(
                            matching
                                .keySet()
                                .stream()
                                .map(Pattern::pattern)
                                .collect(Collectors.joining(", ")),
                            key,
                            sub_value.asString()));
            }
        }
        
        return DataResult.success(value);
    }
}
