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

import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.IPaletteProvider;
import xyz.lumian.novia.api.gui.component.integration.IHierarchyListener;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
public abstract class ContentLayer
    extends ScreenLayer
    implements IHierarchyListener
{
    //******************************************************************************************************************
    public final GuiComponent content;
    
    //******************************************************************************************************************
    ContentLayer(final ScreenInterop screen, final GuiComponent content)
    {
        super(screen);
        this.content = Objects.requireNonNull(content, "content must not be null");
    }
    
    //==================================================================================================================
    public @Nullable GuiComponent getComponentAt(final int screenX, final int screenY)
    {
        return this.content.getComponentAt(this.content.toRelativeX(screenX), this.content.toRelativeY(screenY));
    }
    
    @Override public IPaletteProvider getPalette() { return this.content; }
    
    //==================================================================================================================
    public void init()
    {
        if (this.content.parent != null)
        {
            this.content.parent.removeChild(this.content);
        }
        
        assert (this.content.parent == null);
        
        this.content.setVisible(true);
        this.content.addHierarchyListeners(this);
        this.content.screen = this.screen;
        
        this.resized(this.getBounds());
    }
    
    public void release()
    {
        this.content.removeHierarchyListeners(this);
        this.content.screen = null;
    }
    
    //==================================================================================================================
    @Override public void resized(final Rectangle bounds) { bounds.accept(this.content::setBoundsInternal); }
    
    //==================================================================================================================
    @Override
    public void render(final Canvas canvas)
    {
        this.content.renderComponent(canvas, this.content.getX(), this.content.getY());
    }
    
    //==================================================================================================================
    @Override public void hierarchyChanged(final GuiComponent component) { this.close(); }
}
