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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.CanvasAttorney;
import xyz.lumian.novia.api.gui.canvas.IPaletteProvider;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.renderer.GuiTooltipRenderer;

import java.util.*;



//**********************************************************************************************************************
public class TooltipLayer
    extends ScreenLayer
{
    //******************************************************************************************************************
    @Nullable
    private GuiComponent target = null;
    
    private List<FormattedCharSequence> lines = new ArrayList<>();
    private long                        delay;
    private long                        tooltipTime;
    private boolean                     isFocusMode;
    
    //******************************************************************************************************************
    TooltipLayer(final ScreenInterop screen) { super(screen); }
    
    //==================================================================================================================
    public @Nullable GuiComponent getTarget() { return target; }
    
    //==================================================================================================================
    public void set(final @Nullable GuiComponent target, final boolean hovered)
    {
        final Minecraft mc = Minecraft.getInstance();
        
        if (target != null && (hovered || target.isFocused() && mc.getLastInputType().isKeyboard()))
        {
            final Tooltip tooltip = target.getTooltip();
            
            if (tooltip != null)
            {
                this.target      = target;
                this.isFocusMode = !hovered;
                this.lines       = tooltip.toCharSequence(mc);
                this.delay       = target.getTooltipDelay().toMillis();
                this.tooltipTime = Util.getMillis();
                
                return;
            }
        }
        
        this.target = null;
    }
    
    public void updateTarget(final GuiComponent target)
    {
        Objects.requireNonNull(target, "target must not be null");
        
        if (this.target != target)
        {
            return;
        }
        
        final Tooltip tooltip = target.getTooltip();
        
        if (tooltip == null)
        {
            this.target = null;
            return;
        }
        
        this.lines       = tooltip.toCharSequence(Minecraft.getInstance());
        this.delay       = this.target.getTooltipDelay().toMillis();
        this.tooltipTime = Util.getMillis();
    }
    
    //==================================================================================================================
    @Override public void init() {}
    @Override public void release() {}
    
    @Override public IPaletteProvider getPalette() { return this.getLayerTemplate(); }
    
    //==================================================================================================================
    @Override public void resized(final Rectangle bounds) {}
    
    //==================================================================================================================
    @Override
    public void render(final Canvas canvas)
    {
        if (this.target == null)
        {
            return;
        }
        
        if ((Util.getMillis() - this.tooltipTime) > this.delay)
        {
            final ScreenRectangle   focus      = this.target.getScreenRect();
            final ClientTooltipPositioner positioner = (this.isFocusMode
                ? new BelowOrAboveWidgetTooltipPositioner(focus)
                : new MenuTooltipPositioner(focus));

            CanvasAttorney.setLayer(canvas, this);
            GuiTooltipRenderer.draw(canvas, this.lines, positioner, canvas.mousePos.x(), canvas.mousePos.y());
        }
    }
    
    //==================================================================================================================
    @Override public void close() { this.screen.onClose(); }
}
