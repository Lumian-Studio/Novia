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
package xyz.lumian.novia.api.config.spec.builder;

import com.google.common.collect.Maps;
import xyz.lumian.novia.api.config.ConfigBuildException;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.spec.GroupSpec;
import xyz.lumian.novia.api.config.spec.IConfigSpec;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.config.spec.PropertySpecMap;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;



//**********************************************************************************************************************
public class GroupBuilder<Container>
    extends SpecBuilderBase<Container>
{
    //******************************************************************************************************************
    protected final PropertySpecMap<Container> flatSpecs;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Map<String, SpecBuilderBase<Container>> subBuilders = new HashMap<>();
    
    //******************************************************************************************************************
    GroupBuilder(final PropertySpecMap<Container> flatSpecs) { this.flatSpecs = flatSpecs; }

    //==================================================================================================================
    /// Specifies a new config group with a builder that allows allocating subgroups and properties.
    /// @param name    The name of the subgroup
    /// @param builder A builder function that takes a new group builder that can be used
    ///                to add more subgroups and properties
    /// @return `this`
    public GroupBuilder<Container> withGroup(final String name, final UnaryOperator<GroupBuilder<Container>> builder)
    {
        if (this.subBuilders.containsKey(name))
        {
            throw new ConfigBuildException("duplicate spec  '" + name + "'");
        }

        final GroupBuilder<Container> sub_builder = new GroupBuilder<>(this.flatSpecs);
        this.subBuilders.put(name, builder.apply(sub_builder));

        return this;
    }
    
    public <T> GroupBuilder<Container> withProperty(final String name, final Function<Container, Property<T>> getter)
    {
        return this.withProperty(name, getter, Function.identity());
    }
    
    /// Specifies a new config property.
    /// @param name    The name of the property
    /// @param getter  The property getter
    /// @param builder The builder instance
    /// @return this
    public <T> GroupBuilder<Container> withProperty(
        final String                                                                 name,
        final Function<Container, Property<T>>                                       getter,
        final Function<PropertyBuilder<T, Container>, PropertyBuilder<T, Container>> builder
    )
    {
        if (this.subBuilders.containsKey(name))
        {
            throw new ConfigBuildException("duplicate spec  '" + name + "'");
        }

        this.subBuilders.put(name, builder.apply(new PropertyBuilder<>(getter)));
        return this;
    }
    
    //==================================================================================================================
    @Override
    protected GroupSpec<Container> build(final JsonPointer pointer)
    {
        final Map<String, IConfigSpec<Container>> rendered_specs = Maps.newHashMap();
        
        for (final var entry : this.subBuilders.entrySet())
        {
            final JsonPointer child_ptr = pointer.getChild(entry.getKey());
            
            final IConfigSpec<Container> spec = entry.getValue().build(child_ptr);
            this.tryEmplaceProperty(child_ptr, spec);

            rendered_specs.put(entry.getKey(), spec);
        }
        
        return new GroupSpec<>(pointer, rendered_specs);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void tryEmplaceProperty(final JsonPointer pointer, final IConfigSpec<?> spec)
    {
        if (spec instanceof PropertySpec<?, ?> pspec)
        {
            this.flatSpecs.put(pointer, (PropertySpec<?, Container>) pspec);
        }
    }
}
