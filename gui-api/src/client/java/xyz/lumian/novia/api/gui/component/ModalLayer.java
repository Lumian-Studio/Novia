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
import xyz.lumian.novia.api.gui.geometry.Rectangle;



//**********************************************************************************************************************
final class ModalLayer
    extends ContentLayer
{
    //******************************************************************************************************************
    public static final GuiComponent END = new GuiComponent();
    
    //******************************************************************************************************************
    final ModalArgs args;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Rectangle   targetBounds = new Rectangle();
    private final ModalResult result;
    
    //******************************************************************************************************************
    public ModalLayer(final GuiComponent  content, final ScreenInterop screen, final ModalArgs args,
                      final ModalResult result)
    {
        super(screen, content);
        this.args   = args;
        this.result = result;
    }
    
    //==================================================================================================================
    @Override
    public @Nullable GuiComponent getComponentAt(final int screenX, final int screenY)
    {
        final GuiComponent comp = super.getComponentAt(screenX, screenY);
        return (this.args.wantsAllInput() && comp == null ? ModalLayer.END : comp);
    }
    
    //==================================================================================================================
    @Override
    public void resized(final Rectangle bounds)
    {
        this.args.associatedComponent().ifPresentOrElse(
            (comp -> this.setComponentBounds(comp.getScreenX(), comp.getScreenY(), comp.getWidth(), comp.getHeight())),
            (() -> bounds.accept(this::setComponentBounds)));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setComponentBounds(final int x, final int y, final int width, final int height)
    {
        if (
               this.targetBounds.x()      != x
            || this.targetBounds.y()      != y
            || this.targetBounds.width()  != width
            || this.targetBounds.height() != height
        )
        {
            this.targetBounds.setBounds(x, y, width, height);
            this.content.getPositioner().ifPresentOrElse(
                (pos -> pos.getBounds(x, y, width, height).accept(this.content::setBoundsInternal)),
                (() -> this.content.setBoundsInternal(x, y, width, height)));
        }
    }
    
    //==================================================================================================================
    @Override
    public void init()
    {
        super.init();
        this.args.associatedComponent().ifPresent(comp ->
        {
            if (this.content.template == null)
            {
                this.content.template = comp.getTemplate();
            }
            
            if (this.content.font == null)
            {
                this.content.font = comp.getFont();
            }
        });
        
        this.content.onModalOpened();
    }
    
    @Override
    public void release()
    {
        this.result.complete(this.content);
        this.content.onModalClosed();
        super.release();
    }
    
    //==================================================================================================================
    @Override
    public void render(final Canvas canvas)
    {
        this.args.associatedComponent().ifPresent(comp ->
            this.setComponentBounds(comp.getScreenX(), comp.getScreenY(), comp.getWidth(), comp.getHeight()));
        
        if (this.args.darkenBackground())
        {
            canvas.drawDarkening(0, 0, this.screen.width, this.screen.height);
        }
        
        super.render(canvas);
    }
    
    //==================================================================================================================
    @Override public void visibilityChanged(final GuiComponent component) { this.close(); }
    
    //==================================================================================================================
    @Override public void close() { this.screen.removeLayer(this); }
}
