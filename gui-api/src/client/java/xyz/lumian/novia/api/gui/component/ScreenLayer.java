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
package xyz.lumian.novia.api.gui.component;

import org.jetbrains.annotations.ApiStatus;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.canvas.IPaletteProvider;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
@ApiStatus.Internal
public abstract class ScreenLayer
{
    //******************************************************************************************************************
    final ScreenInterop screen;
    
    //******************************************************************************************************************
    ScreenLayer(final ScreenInterop screen)
    {
        this.screen = Objects.requireNonNull(screen, "screen must not be null");
    }
    
    //==================================================================================================================
    public GuiFont      getLayerFont()     { return this.screen.getGuiFont(); }
    public IGuiTemplate getLayerTemplate() { return this.screen.getTemplate(); }
    
    public Rectangle getBounds() { return new Rectangle(0, 0, this.screen.width, this.screen.height); }
    
    public abstract IPaletteProvider getPalette();
    
    //==================================================================================================================
    public abstract void init();
    public abstract void release();
    
    //==================================================================================================================
    public abstract void resized(final Rectangle bounds);
    public abstract void render(final Canvas canvas);
    
    //==================================================================================================================
    public abstract void close();
}
