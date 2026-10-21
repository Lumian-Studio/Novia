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
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.config.ConfigApiIds;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.provided.NVLabel;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Frame;
import xyz.lumian.novia.api.gui.geometry.Rectangle;



//**********************************************************************************************************************
public class DefaultConfigScreenLookAndFeel
    implements ConfigScreenLookAndFeel
{
    //******************************************************************************************************************
    public static final Frame CLIENT_PADDING          = new Frame(7);
    public static final int   CLIENT_HEADER_HEIGHT    = 20;
    public static final int   CLIENT_BORDER_THICKNESS = 6;
    public static final int   CLIENT_MAX_WIDTH        = 700;
    public static final int   CLIENT_MAX_HEIGHT       = 400;
    
    public static final int   TAB_LIST_WIDTH   = 100;
    public static final Frame TAB_LIST_PADDING = new Frame(
        CLIENT_BORDER_THICKNESS,
        CLIENT_HEADER_HEIGHT,
        0,
        CLIENT_BORDER_THICKNESS);
    
    public static final Frame OPTION_LIST_PADDING = CLIENT_PADDING.withExpansion(10, 0);
    
    public static final Frame EXTRA_PANEL_PADDING = new Frame(
        CLIENT_BORDER_THICKNESS, CLIENT_HEADER_HEIGHT,
        CLIENT_BORDER_THICKNESS, CLIENT_BORDER_THICKNESS);
    
    public static final Identifier TAB_BACKGROUND_TEXTURE         = ConfigApiIds.Sprite.CONFIG_BACKGROUND_TABS;
    public static final Identifier OPTIONS_BACKGROUND_TEXTURE     = ConfigApiIds.Sprite.CONFIG_BACKGROUND_OPTIONS;
    public static final Identifier EXTRA_PANEL_BACKGROUND_TEXTURE = ConfigApiIds.Sprite.CONFIG_BACKGROUND_EXTRA;
    
    //******************************************************************************************************************
    private final Rectangle configPanelClientBounds = new Rectangle();
    private final Rectangle tabAreaBounds           = new Rectangle();
    private final Rectangle optionAreaBounds        = new Rectangle();
    private final Rectangle editPanelClientBounds   = new Rectangle();
    
    //******************************************************************************************************************
    @Override public Rectangle getConfigPanelClientBounds() { return this.configPanelClientBounds; }
    
    @Override
    public Rectangle getTabListBounds()
    {
        return this.tabAreaBounds.withPadding(TAB_LIST_PADDING.leftExpanded(-2));
    }
    
    @Override
    public Rectangle getOptionListBounds()
    {
        return this.optionAreaBounds
            .withPadding(4, CLIENT_HEADER_HEIGHT, CLIENT_BORDER_THICKNESS, CLIENT_BORDER_THICKNESS)
            .withPadding(OPTION_LIST_PADDING);
    }
    
    //==================================================================================================================
    @Override public GuiFont getDefaultFont() { return GuiFont.DEFAULT.get().withShadow(true); }

    //==================================================================================================================
    @Override public Rectangle getEditPanelClientBounds() { return this.editPanelClientBounds; }
    
    @Override
    public Rectangle getEditPanelListBounds()
    {
        return this
            .editPanelClientBounds
            .withPadding(4, CLIENT_HEADER_HEIGHT, CLIENT_BORDER_THICKNESS, CLIENT_BORDER_THICKNESS)
            .withPadding(OPTION_LIST_PADDING);
    }
    
    //==================================================================================================================
    @Override
    public Rectangle getTitleBounds(final Component text, final Rectangle clientBounds)
    {
        return clientBounds.withHeight(CLIENT_HEADER_HEIGHT);
    }
    
    //==================================================================================================================
    @Override
    public NVLabel createTitleWidget(final Component text)
    {
        final NVLabel label = new NVLabel(text);
        label.textAlign.set(Alignment.MIDDLE_CENTRE);
        label.setColour(NVLabel.COLOUR_TEXT, Colour.BLACK);
        label.setFont(this.getDefaultFont().withShadow(false));
        
        return label;
    }
    
    //==================================================================================================================
    @Override
    public void resize(final Rectangle screenBounds)
    {
        this.configPanelClientBounds
            .setSize(screenBounds)
            .constrainToMax(CLIENT_MAX_WIDTH, CLIENT_MAX_HEIGHT)
            .pad(CLIENT_PADDING)
            .centre(screenBounds);
        
        final Rectangle tab_bounds = this.optionAreaBounds
            .setBounds(this.configPanelClientBounds)
            .removeLeft(TAB_LIST_WIDTH + CLIENT_BORDER_THICKNESS);
        this.tabAreaBounds.setBounds(tab_bounds);
        
        this.editPanelClientBounds
            .setBounds(screenBounds)
            .constrainToMax(400, 400)
            .pad(CLIENT_PADDING)
            .centre(screenBounds);
    }
    
    //==================================================================================================================
    @Override
    public void drawBackground(final Canvas canvas)
    {
        canvas.drawSprite(TAB_BACKGROUND_TEXTURE, this.tabAreaBounds, false);
        canvas.drawSprite(OPTIONS_BACKGROUND_TEXTURE, this.optionAreaBounds, false);
    }
    
    @Override
    public void drawEditPanelBackground(final Canvas canvas)
    {
        canvas.drawSprite(EXTRA_PANEL_BACKGROUND_TEXTURE, this.editPanelClientBounds, false);
    }
}
