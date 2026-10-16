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
package xyz.lumian.novia.api.gui.canvas.brush;

import xyz.lumian.novia.api.core.util.Colour;



//**********************************************************************************************************************
/// Describes a colour palette that maps a colour to each vertex of a rectangular area.
/// @param topLeft     The colour value of the top-left vertex
/// @param topRight    The colour value of the top-right vertex
/// @param bottomLeft  The colour value of the bottom-left vertex
/// @param bottomRight The colour value of the bottom-right vertex
public record VertexPalette(int topLeft, int topRight, int bottomLeft, int bottomRight)
{
    //******************************************************************************************************************
    /// A consumer that can be used to apply the vertex colours to an operation.
    @FunctionalInterface
    public interface VertexConsumer
    {
        //**************************************************************************************************************
        void accept(int topLeft, int topRight, int bottomLeft, int bottomRight);
    }
    
    //******************************************************************************************************************
    /// Constructs a solid vertex palette with all vertices having the same colour.
    /// @param all The colour of all vertices
    public VertexPalette(final int all) { this(all, all, all, all); }
    
    /// Constructs a solid vertex palette with all vertices having the same colour.
    /// @param topLeft     The colour value of the top-left vertex
    /// @param topRight    The colour value of the top-right vertex
    /// @param bottomLeft  The colour value of the bottom-left vertex
    /// @param bottomRight The colour value of the bottom-right vertex
    public VertexPalette(final Colour topLeft, final Colour topRight, final Colour bottomLeft, final Colour bottomRight)
    {
        this(topLeft.colour(), topRight.colour(), bottomLeft.colour(), bottomRight.colour());
    }
    
    /// Constructs a solid vertex palette with all vertices having the same colour.
    /// @param all The colour of all vertices
    public VertexPalette(final Colour all) { this(all, all, all, all); }
    
    //==================================================================================================================
    /// Accepts the colours to the given [VertexConsumer].
    /// @param consumer The [VertexConsumer]
    public void accept(final VertexPalette.VertexConsumer consumer)
    {
        consumer.accept(this.topLeft, this.topRight, this.bottomLeft, this.bottomRight);
    }
}
