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

import com.mojang.datafixers.util.Function3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullUnmarked;
import xyz.lumian.novia.api.core.util.RefUtils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;



//**********************************************************************************************************************
@NullUnmarked
public final class GuiPropertyBuilder<T, U extends IGuiProperty<T>>
{
    //******************************************************************************************************************
    public static <T> @NotNull GuiPropertyBuilder<T, GuiProperty<T>> nullable(final T initialValue)
    {
        return new GuiPropertyBuilder<>(initialValue, GuiProperty::new);
    }
    
    public static <T> @NotNull GuiPropertyBuilder<T, GuiProperty.NonNull<T>> nonNull(final @NotNull T initialValue)
    {
        return new GuiPropertyBuilder<>(initialValue, GuiProperty.NonNull::new);
    }
    
    //******************************************************************************************************************
    private final Function3<T, Consumer<T>, Predicate<T>, U> generator;
    private final T                                          initialValue;
    
    private Consumer<T>  setter    = RefUtils.emptyConsumer();
    private Predicate<T> validator = RefUtils.alwaysTrue();
    
    //******************************************************************************************************************
    private GuiPropertyBuilder(final          T                                          initialValue,
                               final @NotNull Function3<T, Consumer<T>, Predicate<T>, U> generator)
    {
        this.generator    = generator;
        this.initialValue = initialValue;
    }
    
    //==================================================================================================================
    public @NotNull GuiPropertyBuilder<T, U> withValidator(final @NotNull Predicate<T> validator)
    {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        return this;
    }
    
    public @NotNull GuiPropertyBuilder<T, U> withSetter(final @NotNull Consumer<T> setter)
    {
        this.setter = Objects.requireNonNull(setter, "setter must not be null");
        return this;
    }
    
    public @NotNull GuiPropertyBuilder<T, U> withNoArgSetter(final @NotNull Runnable setter)
    {
        Objects.requireNonNull(setter, "setter must not be null");
        this.setter = (val -> setter.run());
        
        return this;
    }
    
    //==================================================================================================================
    public @NotNull U build() { return this.generator.apply(this.initialValue, this.setter, this.validator); }
}
