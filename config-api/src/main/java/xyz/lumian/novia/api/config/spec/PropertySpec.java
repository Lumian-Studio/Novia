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
package xyz.lumian.novia.api.config.spec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.PropertyValidationException;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.property.PropertyAttorney;
import xyz.lumian.novia.api.config.property.impl.IPropertyConfigHandler;
import xyz.lumian.novia.api.config.schema.IPropertySchema;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.Optional;
import java.util.function.Function;



//**********************************************************************************************************************
/// Describes a property in a configuration.
/// @param pointer The absolute path to this property (JSON Pointer).
/// @param schema  An optional [IPropertySchema] that validates the value before application
/// @param getter  The method reference that fetches a property from a config container.
/// @param <Container> The container type
public record PropertySpec<T, Container>(
    JsonPointer                      pointer,
    Optional<IPropertySchema<T>>     schema,
    Function<Container, Property<T>> getter
) implements IConfigSpec<Container>
{
    //******************************************************************************************************************
    public Property<T> get(final Container container) { return this.getter.apply(container); }
    
    //==================================================================================================================
    @Override public JsonPointer getPointer() { return this.pointer; }
    
    //==================================================================================================================
    public void validate(final T value)
        throws PropertyValidationException
    {
        final DataResult<T> result = this.schema
            .map(schema -> schema.validateValue(value))
            .orElseGet(() -> DataResult.success(value));
        
        if (result.isError())
        {
            throw new PropertyValidationException("validation for property '%s' failed: %s"
                .formatted(this.pointer, result.error().orElseThrow().message()));
        }
    }
    
    public void validateForContainer(final Container container)
        throws PropertyValidationException
    {
        this.validate(this.getter.apply(container).getValue());
    }
    
    //==================================================================================================================
    /// Binds the property managed by this spec to the given container.
    /// @param container The container to bind this property spec to
    /// @param handler   The handler to bind to the container property
    public void bind(final Container container, final @Nullable IPropertyConfigHandler handler)
    {
        PropertyAttorney.bind(this.getter.apply(container), this, handler);
    }
    
    //==================================================================================================================
    @Override
    public <U> DataResult<U> decode(final Container context, final DynamicOps<U> ops, final U input)
    {
        final Property<T>   property = this.get(context);
        final DataResult<T> result   = property.getCodec().parse(ops, input);
        
        if (result.isError())
        {
            return DataResult.error(() -> result.error().orElseThrow().message());
        }
        
        try
        {
            this.validate(result.getOrThrow());
        }
        catch (final PropertyValidationException ex)
        {
            return DataResult.error(ex::getMessage);
        }
        
        return DataResult.success(input);
    }
    
    @Override
    public <U> DataResult<U> encode(final Container context, final DynamicOps<U> ops, final U prefix)
    {
        final Property<T> property = this.get(context);
        return property.getCodec().encode(property.getValue(), ops, prefix);
    }
}
