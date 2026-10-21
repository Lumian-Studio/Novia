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
package xyz.lumian.novia.api.config.client.document;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.client.gui.schema.IComponentFactory;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.Value;

import java.util.*;
import java.util.function.Supplier;



//**********************************************************************************************************************
public record ComponentStyle(
    Optional<String>      variant,
    Map<Value, Component> labels,
    Map<String, Object>   properties
)
{
    //******************************************************************************************************************
    @FunctionalInterface
    public interface Variant
    {
        //**************************************************************************************************************
        static Variant create(final String defaultId, final Supplier<IComponentFactory<?>> defaultGenerator
        )
        {
            Objects.requireNonNull(defaultId,        "default id must not be null");
            Objects.requireNonNull(defaultGenerator, "default generator must not be null");
            
            return (id ->
            {
                if (id != null && !id.equalsIgnoreCase(defaultId))
                {
                    Novia.LOGGER.debug("Unknown variant '{}' for schema in property", id);
                }
                
                return defaultGenerator.get();
            });
        }
        
        //**************************************************************************************************************
        @Nullable IComponentFactory<?> visit(final @Nullable String variant);
        
        //==============================================================================================================
        default Variant ifVar(final String name, final Supplier<IComponentFactory<?>> generator
        )
        {
            Objects.requireNonNull(name,      "name must not be null");
            Objects.requireNonNull(generator, "generator must not be null");
            
            return (id ->
            {
                if (id != null && id.equalsIgnoreCase(name))
                {
                    return generator.get();
                }
                
                return this.visit(name);
            });
        }
        
        //==============================================================================================================
        default @Nullable IComponentFactory<?> build(final ComponentStyle style)
        {
            return this.visit(style.variant.orElse(null));
        }
    }
    
    //******************************************************************************************************************
    public static final Codec<ComponentStyle> CODEC = RecordCodecBuilder.create(instance -> instance
        .group(
            Codec.STRING
                .optionalFieldOf("variant")
                .forGetter(ComponentStyle::variant),
            LabelDefinition.CODEC
                .optionalFieldOf("labels", ImmutableMap.of())
                .forGetter(ComponentStyle::labels),
            Codec.unboundedMap(Codec.STRING, ExtraCodecs.JAVA)
                .optionalFieldOf("properties", Map.of())
                .forGetter(ComponentStyle::properties))
        .apply(instance, ComponentStyle::new));
    
    public static final ComponentStyle EMPTY = new ComponentStyle(Optional.empty(), Map.of(), Map.of());
    
    //******************************************************************************************************************
    public <T> Optional<T> getProperty(final String name, final Class<T> type)
    {
        if (!this.properties.containsKey(name))
        {
            return Optional.empty();
        }
        
        final Object value = this.properties.get(name);

        if (!type.isInstance(value))
        {
            Novia.LOGGER.debug("Invalid type {} for config screen style property '{}'", type.getSimpleName(), name);
            return Optional.empty();
        }
        
        return Optional.of(type.cast(value));
    }
}
