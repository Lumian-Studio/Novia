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
package xyz.lumian.novia.api.gui.font;



//**********************************************************************************************************************
/// Represents the metrics of the given font. They will not necessarily represent the real sizes defined by the font
/// files but rather scaled to what should be drawn on screen (as given in the associated provider for that font).
/// @param height The em height of the font.
/// @param ascent The cell ascent of the font (top down to baseline).
/// @param scale  The ratio between the font's height and the default render height
///               (see [GuiFont#getDefaultFontHeight()])
public record FontMetrics(float height, float ascent, float scale)
{
    //******************************************************************************************************************
    /// The cell descent of the font (bottom up to baseline).
    public float descent() { return (this.height - this.ascent); }
    
    //==================================================================================================================
    /// Gets the "logical" ascent of this metrics object, which is the scaled ascent of the font according to
    /// [GuiFont#getDefaultRenderHeight()].
    /// @return The logical ascent
    public float logicalAscent() { return (this.ascent * this.scale); }
    
    /// Gets the "logical" descent of this metrics object, which is the scaled descent of the font according to
    /// [GuiFont#getDefaultRenderHeight()].
    /// @return The logical descent
    public float logicalDescent() { return (this.descent() * this.scale); }
    
    /// Gets the "logical" height of this metrics object, which is the scaled height of the font according to
    /// [GuiFont#getDefaultRenderHeight()].
    /// @return The logical height
    public float logicalHeight() { return (this.height * this.scale); }
}
