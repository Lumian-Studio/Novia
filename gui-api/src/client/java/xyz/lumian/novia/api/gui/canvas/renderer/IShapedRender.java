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
package xyz.lumian.novia.api.gui.canvas.renderer;

import xyz.lumian.novia.api.gui.canvas.ICanvasState;
import xyz.lumian.novia.api.gui.canvas.brush.Brush;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Gradient;



//**********************************************************************************************************************
/// Provides a renderer interface for shapes that paint differently based on the specified [Brush]
public interface IShapedRender
    extends ICanvasState.Renderer
{
    //******************************************************************************************************************
    /// Applies a solid [Brush] to the renderer.
    /// @param colour The colour to apply to all vertices
    /// @param opacity The opacity level to apply to the colour
    void applySolid(int colour, float opacity);
    
    /// Applies a gradient [Brush] to the renderer.
    /// @param gradient The [Gradient] to apply to all vertices
    /// @param opacity The opacity level to apply to the gradient
    void applyGradient(Gradient gradient, float opacity);
    
    /// Applies a texture [Brush] to the renderer.
    /// @param colour The base colour to apply to the texture vertices
    /// @param opacity The opacity level to apply to the texture colour
    default void appplyTextured(int colour, float opacity) { this.applySolid(colour, opacity); }
}
