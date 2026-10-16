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
package xyz.lumialights.novia.api.config.spec.builder;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.core.util.JsonPointer;

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
    public ConfigSpecBuilder(@NotNull final Identifier specId, @NotNull final Class<Container> containerClass)
    {
        super(new PropertySpecMap<>(), ConfigSpec.SCHEMA_STORE.loadAndGetSchema(specId));
        
        this.specId         = specId;
        this.containerClass = containerClass;
    }
    
    public ConfigSpecBuilder(@NotNull final String modId, @NotNull final Class<Container> containerClass)
    {
        this(Identifier.of(modId, containerClass.getSimpleName().toLowerCase()), containerClass);
    }

    //==================================================================================================================
    @Override
    public @NotNull ConfigSpecBuilder<Container> withGroup(
        @NotNull final String                                 name,
        @NotNull final UnaryOperator<GroupBuilder<Container>> builder)
    {
        super.withGroup(name, builder);
        return this;
    }
    
    @Override
    public @NotNull ConfigSpecBuilder<Container> withProperty(
        @NotNull final String                        name,
        @NotNull final Function<Container, Property> getter
    )
    {
        super.withProperty(name, getter);
        return this;
    }
    
    @Override
    public @NotNull GroupBuilder<Container> withProperty(
        @NotNull final String                                                           name,
        @NotNull final Function<Container, Property>                                    getter,
        @NotNull final Function<PropertyBuilder<Container>, PropertyBuilder<Container>> builder)
    {
        super.withProperty(name, getter, builder);
        return this;
    }
    
    //==================================================================================================================
    /**
     * Builds a new configuration specification with the given language backend.
     * @param generator The generator function needed to construct new container instances
     * @return The built configuration specification
     */
    public @NotNull ConfigSpec<Container> build(@NotNull final Supplier<Container> generator)
    {
        final GroupSpec<Container> root_group = super.build(JsonPointer.ROOT);
        return new ConfigSpec<>(this.flatSpecs, root_group, this.containerClass, this.specId, generator);
    }
}
