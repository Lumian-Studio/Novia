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
package xyz.lumian.novia.api.config.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.config.ConfigApiIds;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Direction;
import xyz.lumian.novia.api.gui.canvas.brush.gradient.Gradient;
import xyz.lumian.novia.api.gui.component.ComponentUtil;
import xyz.lumian.novia.api.gui.component.provided.INVItemModel;
import xyz.lumian.novia.api.gui.component.provided.NVListBox;
import xyz.lumian.novia.api.gui.component.provided.NVViewport;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;



//**********************************************************************************************************************
public class TabList
    extends NVListBox<TabList.Item>
{
    //******************************************************************************************************************
    public class Item
        implements INVItemModel
    {
        //**************************************************************************************************************
        private static final Identifier[] TEXTURES = {
            ConfigApiIds.Sprite.CONFIG_TAB_BUTTON,
            ConfigApiIds.Sprite.CONFIG_TAB_BUTTON_INTERMEDIATE,
            ConfigApiIds.Sprite.CONFIG_TAB_BUTTON_SELECTED
        };
        
        private static final Colour COLOUR_FG_UNHOVERED          = new Colour(0xFF303031);
        private static final Colour COLOUR_BG_UNSELECTED_HOVERED = new Colour(0xFFCCCCCC);
        private static final Colour COLOUR_SHADING               = new Colour(0x77000000);
        
        //**************************************************************************************************************
        private final Component title;
        private final Colour    colour;

        private int spriteIndex = 0;
        
        //**************************************************************************************************************
        public Item(final Component title, final Colour colour)
        {
            this.title  = title.copy().setStyle(Style.EMPTY.withShadowColor(0x33000000));
            this.colour = colour;
        }
        
        //==============================================================================================================
        @Override
        public void draw(final Canvas canvas, final Rectangle bounds, final int index, final boolean selected,
                         final boolean hovered, final boolean focused)
        {
            final int     offset          = -bounds.y();
            final int     bounds_bottom   = bounds.getBottom();
            final int     view_bottom     = TabList.this.getHeight();
            final boolean out_on_top      = (offset > 0);
            final boolean out_on_bottom   = (bounds_bottom > view_bottom);
            final int     indent          = (selected
                ? (out_on_bottom || out_on_top ? 2 : this.spriteIndex)
                : this.spriteIndex);
            final int     selected_sprite = ((selected || out_on_top || out_on_bottom) ? 2 : 0);
            
            final Colour foreground;
            final Colour background;
            
            if (selected)
            {
                foreground = Colour.WHITE;
                background = this.colour;
            }
            else
            {
                foreground = Item.COLOUR_FG_UNHOVERED;
                background = (hovered ? Item.COLOUR_BG_UNSELECTED_HOVERED : Colour.WHITE);
            }

            final Rectangle local_bounds = new Rectangle(bounds)
                .resetPos()
                .padLeft(indent);

            canvas.setColour(background);
            canvas.drawSprite(TEXTURES[this.spriteIndex], local_bounds, true);
            
            canvas.setColour(foreground);
            canvas.getFont().setShaded(false);
            canvas.drawTextAligned(this.title, local_bounds.withLeftPadding(5), Alignment.MIDDLE);
            
            if (!selected)
            {
                if (out_on_top)
                {
                    canvas.setGradient(new Gradient(COLOUR_SHADING, new Colour(0), Direction.VERTICAL));
                    canvas.fill(local_bounds.padTop(offset));
                }
                else if (out_on_bottom)
                {
                    canvas.setGradient(new Gradient(new Colour(0), COLOUR_SHADING, Direction.VERTICAL));
                    canvas.fill(local_bounds.padBottom(bounds_bottom - view_bottom));
                }
                
                if (this.spriteIndex == 2 && !TabList.this.isItemSelectedAt(index - 1))
                {
                    canvas.setColour(COLOUR_SHADING.withAlpha(0x80));
                    canvas.fill(bounds.withLocalPos().setHeight(1).padLeft(1));
                }
            }
            else if (bounds.y() >= bounds.height())
            {
                canvas.setColour(COLOUR_SHADING.withAlpha(0x80));
                canvas.fill(bounds.withLocalPos().setHeight(1).padLeft(1));
            }
            
            if (this.spriteIndex > selected_sprite)
            {
                if (canvas.deltaTime < 1f)
                {
                    --this.spriteIndex;
                }
            }
            else if (this.spriteIndex < selected_sprite)
            {
                if (canvas.deltaTime < 1f)
                {
                    ++this.spriteIndex;
                }
            }
        }

        @Override
        public boolean onClick(final Point mousePos, final boolean selected)
        {
            if (!selected)
            {
                ComponentUtil.playClickSound();
            }

            return true;
        }
    }
    
    //******************************************************************************************************************
    public TabList()
    {
        this.alwaysSelected.set(true);
        this.itemSize.set(22);
        this.getViewport().behaviourVertical.set(NVViewport.ScrollbarBehaviour.ALWAYS);
    }
    
    //==================================================================================================================
    public void addTab(final Component title, final Colour colour) { this.addItem(new Item(title, colour)); }
}
