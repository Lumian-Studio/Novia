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
package xyz.lumian.novia.api.gui.property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullUnmarked;
import xyz.lumian.novia.api.core.util.RefUtils;
import xyz.lumian.novia.api.gui.component.GuiComponent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;



//**********************************************************************************************************************
/// A utility class that allows [GuiComponent] classes define its own easily accessible properties.
///
/// Properties can be used for any sort of state in a component; however, it is best to only use them for things that
/// change a component's behaviour or look, so no data like text in a text box but more like item orientation in a
/// list-box.
///
/// When declaring property fields, it is always best to use the most recent subclass as field type; e.g. for non-null
/// properties, use [GuiProperty.NonNull].
/// @param <T> The type of content the property holds
@NullUnmarked
public sealed class GuiProperty<T>
    implements IGuiProperty<T>
    permits
        GuiProperty.NonNull
{
    //******************************************************************************************************************
    /// A [GuiProperty] variant that does not allow `null` values, meaning it must be set.
    /// @param <T> The type of content the property holds
    /// @see GuiProperty
    public static final class NonNull<T>
        extends GuiProperty<@NotNull T>
    {
        //**************************************************************************************************************
        /// Constructs a new gui property.
        /// @param initValue The initial value of the property
        /// @param setter    A setter that is executed whenever the internal value is changed
        /// @param validator A validator that will throw an exception if the value is invalid
        public NonNull(final @NotNull T initValue, final Consumer<@NotNull T> setter,
                       final Predicate<@NotNull T> validator)
        {
            super(Objects.requireNonNull(initValue, "gui property value must not be null"), setter, validator);
        }

        //==============================================================================================================
        @Override public @NotNull T get() { return this.value; }

        //==============================================================================================================
        /// Returns always `true` for [GuiProperty.NonNull] (see [IGuiProperty#isSet()]).
        /// @return `true`
        @Override public boolean isSet() { return true; }

        //==============================================================================================================
        /// Sets the value currently held in the property to a new, non-null, value.
        ///
        /// If unsure and a validator is attached, it is best to test before setting a value with [#isValid(Object)];
        /// otherwise if the value is not accepted by this property, this will throw a [IllegalArgumentException]
        /// exception.
        /// @param value The new non-null value
        /// @return `true` if the value was successfully changed; this is true if it was different from the previous
        /// @throws IllegalArgumentException If the value was not correct according to the attached validator
        /// @throws NullPointerException     If the new value was `null`
        @Override
        public boolean set(final @NotNull T value)
        {
            return super.set(Objects.requireNonNull(value, "gui property value must not be null"));
        }

        //==============================================================================================================
        @Override
        public GuiProperty<@NotNull T> ifSet(final Consumer<@NotNull T> consumer)
        {
            consumer.accept(this.value);
            return this;
        }

        public GuiProperty<@NotNull T> ifEmpty(final Runnable consumer) { return this; }
    }

    /// A [GuiProperty] variant that references a property from another component.
    ///
    /// This can be used for cases where a component consists of one or more child components and the components shall
    /// not be exposed but its properties do need to be exposed; this allows those properties to be
    /// aliased in the parent.
    /// @param <T> The type of content the property holds
    /// @see GuiProperty
    @SuppressWarnings("ClassCanBeRecord")
    public static final class Reference<T>
        implements IGuiProperty<T>
    {
        //**************************************************************************************************************
        final GuiComponent   owner;
        final GuiProperty<T> reference;

        //**************************************************************************************************************
        public Reference(final GuiComponent owner, final GuiProperty<T> reference)
        {
            this.owner     = Objects.requireNonNull(owner,     "owner must not be null");
            this.reference = Objects.requireNonNull(reference, "gui property value must not be null");
        }

        //==============================================================================================================
        @Override public T get() { return this.reference.get(); }

        public GuiComponent getOwner() { return this.owner; }

        //==============================================================================================================
        @Override public boolean isSet() { return this.reference.isSet(); }

        //==============================================================================================================
        @Override public boolean set(final T value) { return this.reference.set(value); }
    }

    //******************************************************************************************************************
    public static <T> GuiProperty<T> nullable(final @Nullable T initialValue)
    {
        return new GuiProperty<>(initialValue, RefUtils.emptyConsumer(), RefUtils.alwaysTrue());
    }
    
    public static <T> GuiProperty.NonNull<T> nonNull(final T initialValue)
    {
        return new GuiProperty.NonNull<>(initialValue, RefUtils.emptyConsumer(), RefUtils.alwaysTrue());
    }
    
    /// Creates a reference to another property.
    /// @param owner    The owning [GuiComponent]
    /// @param property The [GuiProperty] that should be referenced
    /// @return The new [GuiProperty.Reference]
    /// @param <T> The type of content the property holds
    public static <T> Reference<T> referTo(final GuiComponent owner, final GuiProperty<T> property)
    {
        return new Reference<>(owner, property);
    }

    //******************************************************************************************************************
    final Consumer<T>  setter;
    final Predicate<T> validator;

    T value;

    //******************************************************************************************************************
    /// Constructs a new gui property.
    /// @param initValue The initial value of the property
    /// @param setter    A setter that is executed whenever the internal value is changed
    /// @param validator A validator that will throw an exception if the value is invalid
    public GuiProperty(final T initValue, final Consumer<T> setter, final Predicate<T> validator)
    {
        if (!validator.test(initValue))
        {
            throw new IllegalArgumentException(
                "initial value '%s' is invalid according to the given validator".formatted(initValue));
        }

        this.value     = initValue;
        this.setter    = setter;
        this.validator = validator;
    }

    //==================================================================================================================
    @Override public T get() { return this.value; }

    //==================================================================================================================
    @Override public boolean isSet() { return (this.value != null); }

    /// Can be used to check if a value is valid before giving it to the property.
    /// @param value The value to test
    /// @return `true` if the value is valid, `false` otherwise
    public boolean isValid(final T value) { return this.validator.test(value); }

    //==================================================================================================================
    /// Sets the value currently held in the property to a new value.
    ///
    /// If unsure and a validator is attached, it is best to test before setting a value with [#isValid(Object)];
    /// otherwise if the value is not accepted by this property, this will throw a [IllegalArgumentException]
    /// exception.
    /// @param value The new value
    /// @return `true` if the value was successfully changed; this is true if it was different from the previous
    /// @throws IllegalArgumentException If the value was not correct according to the attached validator
    @Override
    public boolean set(final T value)
    {
        if (!Objects.equals(this.value, value))
        {
            if (!this.validator.test(value))
            {
                throw new IllegalArgumentException("value '%s' is invalid for this property".formatted(value));
            }

            this.value = value;
            this.setter.accept(this.value);

            return true;
        }

        return false;
    }

    //==================================================================================================================
     /// Executes a [Consumer] with the currently held value if [#isSet()] returns `true`
     /// (if the content is non-null).
     /// @param consumer The consumer to execute
     /// @return `this`
    public GuiProperty<T> ifSet(final Consumer<T> consumer)
    {
        if (this.value != null)
        {
            consumer.accept(this.value);
        }

        return this;
    }

    /// Executes a [Runnable] if [#isSet()] returns `false` (if the content is `null`).
    /// @param runnable The runnable to execute
    /// @return `this`
    public GuiProperty<T> ifEmpty(final Runnable runnable)
    {
        if (this.value == null)
        {
            runnable.run();
        }

        return this;
    }
}
