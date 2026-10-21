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

import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.spec.ConfigSpec;
import xyz.lumian.novia.api.config.spec.GroupSpec;
import xyz.lumian.novia.api.config.spec.PropertySpecMap;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;



//**********************************************************************************************************************
public class ConfigSpecBuilder<Container>
    extends GroupBuilder<Container>
{
    //******************************************************************************************************************
    private final Class<Container> containerClass;
    private final Identifier       specId;
    
    //******************************************************************************************************************
    public ConfigSpecBuilder(final Identifier specId, final Class<Container> containerClass)
    {
        super(new PropertySpecMap<>(), ConfigSpec.SCHEMA_STORE.loadAndGetSchema(specId));
        
        this.specId         = specId;
        this.containerClass = containerClass;
    }
    
    public ConfigSpecBuilder(final String modId, final Class<Container> containerClass)
    {
        this(Identifier.fromNamespaceAndPath(modId, containerClass.getSimpleName().toLowerCase()), containerClass);
    }

    //==================================================================================================================
    @Override
    public ConfigSpecBuilder<Container> withGroup(final String                                 name,
                                                  final UnaryOperator<GroupBuilder<Container>> builder)
    {
        super.withGroup(name, builder);
        return this;
    }
    
    @Override
    public <T> ConfigSpecBuilder<Container> withProperty(final String                           name,
                                                         final Function<Container, Property<T>> getter)
    {
        super.withProperty(name, getter);
        return this;
    }
    
    @Override
    public <T> GroupBuilder<Container> withProperty(
        final String                                                                 name,
        final Function<Container, Property<T>>                                       getter,
        final Function<PropertyBuilder<T, Container>, PropertyBuilder<T, Container>> builder
    )
    {
        super.withProperty(name, getter, builder);
        return this;
    }
    
    //==================================================================================================================
    /// Builds a new configuration specification with the given language backend.
    /// @param generator The generator function needed to construct new container instances
    /// @return The built configuration specification
    public ConfigSpec<Container> build(final Supplier<Container> generator)
    {
        final GroupSpec<Container> root_group = super.build(JsonPointer.ROOT);
        return new ConfigSpec<>(this.flatSpecs, root_group, this.containerClass, this.specId, generator);
    }
}
