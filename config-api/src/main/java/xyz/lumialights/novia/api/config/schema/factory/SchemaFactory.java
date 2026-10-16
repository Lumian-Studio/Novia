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
package xyz.lumialights.novia.api.config.schema.factory;

import com.google.common.collect.ImmutableList;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.schema.IPropertySchema;
import xyz.lumialights.novia.api.config.schema.builtin.InterfaceSchema;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;



//**********************************************************************************************************************
public class SchemaFactory
{
    //******************************************************************************************************************
    // STRING SCHEMAS
    public static @NotNull IPropertySchema stringSchema() { return StringSchema.INSTANCE; }
    
    public static @NotNull IPropertySchema patternSchema(@Language("RegExp") @NotNull final String pattern)
    {
        Objects.requireNonNull(pattern, "pattern must not be null");
        return new PatternSchema(Pattern.compile(pattern));
    }
    
    public static @NotNull IPropertySchema patternSchema(@NotNull final Pattern pattern)
    {
        return new PatternSchema(pattern);
    }
    
    //==================================================================================================================
    // NUMBER SCHEMAS
    public static @NotNull IPropertySchema numberSchema() { return NumberSchema.INSTANCE; }
    
    public static @NotNull IPropertySchema rangeSchema(final double start, final double end)
    {
        return new RangeSchema(start, end);
    }
    
    //==================================================================================================================
    // ATOMIC SCHEMAS
    public static @NotNull IPropertySchema booleanSchema() { return BooleanSchema.INSTANCE; }
    
    public static @NotNull IPropertySchema nullSchema() { return NullSchema.INSTANCE; }
    
    //==================================================================================================================
    // LIST SCHEMAS
    public static @NotNull IPropertySchema listSchema()
    {
        return ListSchema.INSTANCE;
    }
    
    public static @NotNull IPropertySchema elementSchema(@NotNull final IPropertySchema subSchema)
    {
        return new ElementSchema(subSchema);
    }
    
    public static @NotNull IPropertySchema tupleSchema(@NotNull final IPropertySchema @NotNull ...schemas)
    {
        return new TupleSchema(Arrays.stream(schemas).collect(ImmutableList.toImmutableList()));
    }
    
    public static @NotNull IPropertySchema tupleSchema(@NotNull final Collection<IPropertySchema> schemas)
    {
        return new TupleSchema(ImmutableList.copyOf(schemas));
    }
    
    //==================================================================================================================
    // MAP SCHEMAS
    public static @NotNull IPropertySchema mapSchema()
    {
        return MapSchema.INSTANCE;
    }
    
    public static @NotNull IPropertySchema interfaceSchema(@NotNull final Map<String, IPropertySchema> properties)
    {
        return new InterfaceSchema(properties);
    }
    
    public static @NotNull IPropertySchema patternMapSchema(
        @NotNull final Map<Pattern, IPropertySchema> patternProperties)
    {
        return new PatternMapSchema(patternProperties);
    }
    
    //==================================================================================================================
    // VARIANT SCHEMAS
    public static @NotNull IPropertySchema constantSchema(@NotNull final Value constant)
    {
        return new ConstantSchema(constant);
    }
    
    public static @NotNull IPropertySchema enumSchema(@NotNull final Value @NotNull ...values)
    {
        Objects.requireNonNull(values, "values must not be null");
        return new EnumSchema(Arrays.stream(values).collect(ImmutableList.toImmutableList()));
    }
    
    public static @NotNull IPropertySchema enumSchema(@NotNull final Collection<Value> values)
    {
        return enumSchema(values, false);
    }
    
    public static @NotNull IPropertySchema enumSchema(@NotNull final Collection<Value> values, final boolean ignoreCase)
    {
        return new EnumSchema(ImmutableList.copyOf(values), ignoreCase);
    }
    
    public static <E extends Enum<E>> @NotNull IPropertySchema enumSchema(@NotNull final Class<E> enumClass)
    {
        return enumSchema(enumClass, false);
    }
    
    public static <E extends Enum<E>> @NotNull IPropertySchema enumSchema(@NotNull final Class<E> enumClass,
                                                                          final boolean           ignoreCase)
    {
        return new EnumSchema(
            Arrays
                .stream(enumClass.getEnumConstants())
                .map(c -> new Value(c.name().toLowerCase()))
                .collect(ImmutableList.toImmutableList()),
            ignoreCase);
    }
    
    public static @NotNull IPropertySchema variantSchema(@NotNull final Function<VariantBuilder, VariantBuilder> builder)
    {
        return builder.apply(new VariantBuilder()).build();
    }
}
