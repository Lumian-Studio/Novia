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
package xyz.lumian.novia.api.gui.canvas;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.util.Colour;

import java.util.*;



//**********************************************************************************************************************
/// Objects inheriting this interface provide the gui API with the information that the overriding class is an interface
/// that provides a set of colours for use internally.
public interface IPaletteProvider
{
    //******************************************************************************************************************
    /// {@return the full colour palette currently held by this object}
    @Contract(pure = true)
    Palette getPalette();
    
    /// Gets the colour for the given ID.
    /// @param id The [ColourId]
    /// @return The [Colour] or an empty [Optional].
    @Contract(pure = true)
    default Optional<Colour> getColour(final ColourId id)
    {
        return this.getPalette().getColour(id);
    }
    
    /// Sets or unsets the given colour for the palette.
    /// @param id     The [ColourId]
    /// @param colour The new [Colour] or `null` to unset the colour
    /// @return The previously associated colour for that ID, if no such colour was found `null` or the specified
    ///         default colour
    default @Nullable Colour setColour(final ColourId id, final @Nullable Colour colour)
    {
        return this.getPalette().setColour(id, colour);
    }
    
    /// Sets or unsets the given colour for the palette.
    /// @param id     The [ColourId]
    /// @param colour The colour value
    /// @return The previously associated colour for that ID, if no such colour was found `null` or the specified
    ///         default colour
    default @Nullable Colour setColour(final ColourId id, final int colour)
    {
        return this.getPalette().setColour(id, new Colour(colour));
    }
}
