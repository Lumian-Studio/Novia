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

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullUnmarked;



//**********************************************************************************************************************
/// The base class for [GuiProperty] and [GuiProperty.Reference].
///
/// This class should not be used as the declarator type of properties, but instead the most recent subclass that
/// declares the property. (e.g. [GuiProperty], [GuiProperty.NonNull] ect.)
/// @param <T> The type of content the property holds
@NullUnmarked
public sealed interface IGuiProperty<T>
    permits
        GuiProperty,
        GuiProperty.Reference
{
    //******************************************************************************************************************
    /// Gets the value the property currently holds.
    /// @return The value
    T get();

    /// Creates a [Codec] for this property that is only applicable to the current instance of the property.
    /// @param contentCodec The codec of the content of the property
    /// @return The [Codec]
    default @NotNull Codec<IGuiProperty<T>> getCodec(final @NotNull Codec<T> contentCodec)
    {
        return contentCodec.xmap(
            (val ->
            {
                this.set(val);
                return this;
            }),
            IGuiProperty::get);
    }

    //==================================================================================================================
    /// Checks whether the property currently has a set value (non-null).
    /// @return `true` if the content is not `null`
    boolean isSet();
    
    //==================================================================================================================
    /// Sets the value currently held in the property to a new value.
    /// @param value The new value
    /// @return `true` if the value could be set
    boolean set(T value);
}
