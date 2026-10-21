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

import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.*;
import java.util.function.Function;



//**********************************************************************************************************************
public class PropertyBuilder<T, Container>
    extends SpecBuilderBase<Container>
{
    //******************************************************************************************************************
    private final Function<Container, Property<T>> getter;
    
    @Nullable
    private IPropertySchema<T> schema = null;
    
    //******************************************************************************************************************
    PropertyBuilder(final Function<Container, Property<T>> getter)
    {
        this.getter = getter;
    }

    //==================================================================================================================
    public PropertyBuilder<T, Container> withSchema(final @Nullable IPropertySchema<T> schema)
    {
        Objects.requireNonNull(schema, "schema must not be null");
        this.schema = schema;
        
        return this;
    }

    //==================================================================================================================
    @Override
    protected PropertySpec<T, Container> build(final JsonPointer pointer)
    {
        return new PropertySpec<>(pointer, Optional.ofNullable(this.schema), this.getter);
    }
}
