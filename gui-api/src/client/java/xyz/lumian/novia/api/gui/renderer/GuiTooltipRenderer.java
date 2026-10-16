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
package xyz.lumian.novia.api.gui.renderer;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Vector2ic;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.font.TextLayout;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
public final class GuiTooltipRenderer
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        void guiTooltipDrawBackground(Canvas canvas, Rectangle bounds);
        void guiTooltipDrawLines(Canvas canvas, int x, int y, List<FormattedCharSequence> lines);
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_TEXT = ColourId.reserve();
    
    public static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("tooltip/background");
    public static final Identifier FRAME_TEXTURE      = Identifier.withDefaultNamespace("tooltip/frame");
    
    //==================================================================================================================
    public static void draw(final Canvas canvas, final Component text, final int x, final int y)
    {
		GuiTooltipRenderer.draw(canvas, Collections.singletonList(text), x, y);
	}
 
	public static void draw(final Canvas canvas, final List<Component> lines, final int x, final int y)
    {
		GuiTooltipRenderer.draw(canvas, lines.stream().map(Component::getVisualOrderText).toList(),
                                DefaultTooltipPositioner.INSTANCE, x, y);
	}
 
	public static void drawOrdered(final Canvas canvas, final List<? extends FormattedCharSequence> text,
                                   final int x, final int y)
    {
		GuiTooltipRenderer.draw(canvas, new ArrayList<>(text), DefaultTooltipPositioner.INSTANCE, x, y);
	}
    
    public static void draw(final Canvas canvas, final List<FormattedCharSequence> lines,
                            final ClientTooltipPositioner positioner, final int x, final int y)
    {
        final GuiFont font = canvas.getFont();
     
        int width  = 0;
        int height = (lines.size() == 1 ? -2 : 0);

        for (final var line : lines)
        {
            final int k = TextLayout.getTextWidthFitted(font, line);
            
            if (k > width)
            {
                width = k;
            }
            
            height += Mth.ceil(font.getHeight());
        }
        
        final Vector2ic pos_vec = positioner
            .positionTooltip(canvas.getScaledWindowWidth(), canvas.getScaledWindowHeight(), x, y, width, height);
        
        final IGuiTemplate template = canvas.getTemplate();
        template.guiTooltipDrawBackground(canvas, new Rectangle(pos_vec.x(), pos_vec.y(), width, height));
        template.guiTooltipDrawLines(canvas, pos_vec.x(), pos_vec.y(), lines);
    }
    
    //******************************************************************************************************************
    private GuiTooltipRenderer() {}
}
