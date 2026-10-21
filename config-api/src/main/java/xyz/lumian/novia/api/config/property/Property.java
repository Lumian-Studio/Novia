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
package xyz.lumian.novia.api.config.property;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.IPropertyChangeHandler;
import xyz.lumian.novia.api.config.property.impl.IPropertyConfigHandler;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.config.PropertyValidationException;

import xyz.lumian.novia.api.config.LockedPropertyChangeException;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.serialisation.codec.NoviaCodecs;

import java.util.*;



//**********************************************************************************************************************
/// Represents a configuration property used in configuration containers.<p>
/// Every property contains a value and reference to its specification, the specification houses all the attributes
/// of a property that do not directly belong to the property itself. (such as its validator)
public class Property<T>
{
    //******************************************************************************************************************
    /// Creates a new map property.
    /// @param map        The [Map] to initialise this property with
    /// @param keyCodec   The codec of the keys in the map
    /// @param valueCodec The codec of the values in the map
    /// @return The new [Property]
    public static <K, V> Property<Map<K, V>> map(final Map<K, V> map, final Codec<K> keyCodec,
                                                 final Codec<V> valueCodec)
    {
        return new Property<>(map, Codec.unboundedMap(keyCodec, valueCodec));
    }

    /// Creates a new list property.
    /// @param value      The [List] to initialise this property with
    /// @param valueCodec The value codec of the list
    /// @return The new [Property]
    public static <T> Property<List<T>> list(final List<T> value, final Codec<T> valueCodec)
    {
        return new Property<>(value, valueCodec.listOf());
    }

    /// Creates a new [String] property.
    /// @param value The string to initialise this property with
    /// @return The new [Property]
    public static Property<String> string(final String value) { return new Property<>(value, Codec.STRING); }

    /// Creates a new [List] property.
    /// @param value The number to initialise this property with
    /// @return The new [Property]
    public static Property<Number> number(final Number value) { return new Property<>(value, NoviaCodecs.NUMBER); }

    /// Creates a new [Boolean] property.
    /// @param value The boolean value to initialise this property with
    /// @return The new [Property]
    public static Property<Boolean> bool(final Boolean value) { return new Property<>(value, Codec.BOOL); }

    /// Creates a new enum property with the enum names being case-insensitive by default.
    /// @param value      The enum constant to initialise this property with
    /// @param clazz      The enum class
    /// @param ignoreCase Whether the serialised representation doesn't care about case-sensitivity
    /// @return The new [Property]
    public static <E extends Enum<E>> Property<E> enumeration(final E value, final Class<E> clazz,
                                                              final boolean ignoreCase)
    {
        return new Property<>(value, NoviaCodecs.namedEnum(clazz, ignoreCase));
    }
    
    public static Property<Value> var(final Value value) { return new Property<>(value, Value.CODEC); }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Property<Optional<T>> optional(final Optional<T> defaultValue, final Codec<T> valueCodec)
    {
        return new Property<>(defaultValue, new Codec<>()
        {
            @Override
            public <U> DataResult<Pair<Optional<T>, U>> decode(final DynamicOps<U> ops, final U input)
            {
                if (input.equals(ops.empty()))
                {
                    return DataResult.success(Pair.of(Optional.empty(), input));
                }
                
                return valueCodec.decode(ops, input)
                    .map(p -> p.mapFirst(Optional::of));
            }
            
            @Override
            public <U> DataResult<U> encode(final Optional<T> input, final DynamicOps<U> ops, final U prefix)
            {
                return input
                    .map(val -> valueCodec.encode(val, ops, prefix))
                    .orElseGet(() -> DataResult.success(ops.empty()));
            }
        });
    }

    //******************************************************************************************************************
    private final T                              defaultValue;
    private final Set<IPropertyChangeHandler<T>> handlers = new HashSet<>();
    private final Codec<T>                       codec;
    
    @Nullable
    private PropertySpec<T, ?> spec = null;
    
    @Nullable
    private IPropertyConfigHandler handler = null;
    
    @Nullable
    private T override = null;
    
    private T value;
    
    //******************************************************************************************************************
    public Property(final T value, final Codec<T> codec)
    {
        this.defaultValue = value;
        this.value        = value;
        this.codec        = codec;
    }

    //==================================================================================================================
    void bind(final PropertySpec<T, ?> spec, final @Nullable IPropertyConfigHandler handler)
    {
        this.spec    = spec;
        this.handler = handler;
    }

    //==================================================================================================================
    /// Gets the value that this property is currently managing.
    /// @return The managed property value
    public T getValue() { return (this.override != null ? this.override : this.value); }
    
    /// Gets the property's attached [PropertySpec].
    /// @return The property specification, or null if the property has not been bound to a specification
    public @Nullable PropertySpec<T, ?> getSpec() { return this.spec; }

    /// Gets the specified default value for this property.
    /// @return The value
    public T getDefaultValue() { return this.defaultValue; }
    
    public Codec<T> getCodec() { return this.codec; }
    
    //==================================================================================================================
    /// Sets the value of this property without validating the new value.
    ///
    /// This method should only ever be used when there is some manual checking before the value is being set,
    /// otherwise [Property#setValue(T)]] should almost always be preferred over this one.
    ///
    /// If the new value is different to current, this will signal the attached handler to send updates.
    ///
    /// If this property is part of a non-standalone config container, this method should never be called on a client
    /// container, instead, the mutual server container should handle the update.
    /// It should also never be called on the render or any other thread that is not the server thread.
    ///
    /// For standalone config containers, it should be called on the thread it was designated for.
    ///
    /// @param newValue The new newValue of this property
    /// @throws LockedPropertyChangeException If the property is part of a client container of a mutual configuration
    public void setValueUnchecked(final T newValue)
    {
        if (newValue.equals(this.value))
        {
            return;
        }
        
        this.setValueImpl(newValue);
    }

    /// Sets the value of this property.
    ///
    /// If the new value is different to current, this will signal the attached handler to send updates.
    ///
    /// If this property is part of a non-standalone config container, this method should never be called on a client
    /// container, instead, the mutual server container should handle the update.
    /// It should also never be called on the render or any other thread that is not the server thread.
    ///
    /// For standalone config containers, it should be called on the thread it was designated for.
    ///
    /// @param newValue The new newValue of this property
    /// @throws PropertyValidationException If the associated validator failed validation of the new value
    /// @throws LockedPropertyChangeException If the property is part of a client container of a mutual configuration
    public void setValue(final T newValue)
        throws PropertyValidationException
    {
        if (newValue.equals(this.value))
        {
            return;
        }
        
        if (this.getSpec() != null)
        {
            this.getSpec().validate(this.value);
        }
        
        this.setValueImpl(newValue);
    }

    /// Resets the value of this property to its default. If the default is different to the current,
    /// this will signal the attached handler to send updates.
    ///
    /// If this property is part of a non-standalone config container, this method should never be called on a client
    /// container, instead, the mutual server container should handle the update. It should also never be called on the
    /// render or any other thread that is not the server thread.
    ///
    /// For standalone config containers, it should be called on the thread it was designated for.
    ///
    /// @throws LockedPropertyChangeException If the property is part of a client container of a mutual configuration
    public void reset() { this.setValueUnchecked(this.defaultValue); }
    
    //------------------------------------------------------------------------------------------------------------------
    void setOverride(final @Nullable T override)
    {
        final T old_value = this.getValue();
        this.override = override;
        
        if (!old_value.equals(this.getValue()))
        {
            this.handlers.forEach(handler -> handler.propertyChanged(this, old_value));
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setValueImpl(final T value)
    {
        final T old_value = this.value;
        this.value = value;
        
        Objects.requireNonNull(this.handler).notify(this, this.value);
        
        if (this.override == null)
        {
            this.handlers.forEach(handler -> handler.propertyChanged(this, old_value));
        }
    }
    
    //==================================================================================================================
    public void registerChangeHandler(final IPropertyChangeHandler<T> handler) { this.handlers.add(handler); }
}
