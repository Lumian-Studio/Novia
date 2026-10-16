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
package xyz.lumian.novia.api.gui.canvas;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fc;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.brush.Brush;
import xyz.lumian.novia.api.gui.component.provided.*;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.font.TextFormat;
import xyz.lumian.novia.api.gui.font.TextLayout;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.renderer.GuiTooltipRenderer;

import java.util.*;
import java.util.function.Supplier;



//**********************************************************************************************************************
/// Describes the base class of all gui templates used to customise rendering of components. This class can be mixed-in
/// to allow templating your own components.
/// @see DefaultGuiTemplate
public interface IGuiTemplate
    extends
        IPaletteProvider,
        
        NVTextBox.Template,
        NVViewport.Template,
        NVSlider.Template,
        NVSimpleButton.Template,
        NVNumericBox.Template,
        NVScrollbar.Template,
        NVLabel.Template,
        NVListBox.Template,
        NVDropdown.Template,
        NVCheckBox.Template,
        GuiTooltipRenderer.Template
{
    //******************************************************************************************************************
    /// The default template used if no explicit template was set. This template does not permit changing colours as it
    /// defines the defaults used for all components.
    /// @throws UnsupportedOperationException If any of the colours in the map is being changed
    IGuiTemplate DEFAULT = new DefaultGuiTemplate()
    {
        @Override
        public @Nullable Colour setColour(ColourId id, int colour)
        {
            throw new UnsupportedOperationException("cannot change default template colours");
        }
        
        @Override
        public @Nullable Colour setColour(ColourId id, @Nullable Colour colour)
        {
            throw new UnsupportedOperationException("cannot change default template colours");
        }
    };
    
    //******************************************************************************************************************
    /// Gets the default brush for this template. The default brush is the brush that is used whenever the [Canvas]
    /// visits the next component to draw.
    /// @return The default [Brush]
    @Contract(pure = true)
    default Supplier<Brush> getDefaultBrush() { return (() -> new Brush(Colour.WHITE)); }
    
    //==================================================================================================================
    @Override
    default void guiTooltipDrawBackground(final Canvas canvas, final Rectangle bounds)
    {
        bounds.pad(-12);
        canvas.drawSprite(GuiTooltipRenderer.BACKGROUND_TEXTURE, bounds, false);
        canvas.drawSprite(GuiTooltipRenderer.FRAME_TEXTURE, bounds, false);
    }
    
    @Override
    default void guiTooltipDrawLines(final Canvas canvas, final int x, int y, final List<FormattedCharSequence> lines)
    {
        final GuiFont font   = canvas.getFont();
        final int     height = Mth.ceil(font.getHeight());
        
        canvas.setColour(canvas.findColour(GuiTooltipRenderer.COLOUR_TEXT));
        
        final TextFormat format = new TextFormat();
        format.append(lines.getFirst(), font);
        format.draw(canvas, x, y);

        y += (height + 2);
        
        for (final var line : lines.subList(1, lines.size()))
        {
            final TextFormat format1 = new TextFormat();
            format1.append(line, font);
            format1.draw(canvas, x, y);
            
            y += height;
        }
    }
    
    //==================================================================================================================
    @Override
    default void nvTextboxDrawContent(final Canvas canvas, final NVTextBox box, final Rectangle bounds,
                                      final long lastSwitchFocusTime)
    {
        final int     first_index = box.getFirstCharacterIndex();
        final GuiFont font        = canvas.getFont();
        final String  text        = font.trimToWidth(box.getText().substring(first_index), bounds.width());
        
        if (!canvas.isActive())
        {
            final Colour colour = canvas.findColour(NVTextBox.COLOUR_TEXT_UNEDITABLE);
            this.nvTextboxDrawText(canvas, box, text, bounds.x(), bounds.y(), colour);
            
            return;
        }
        
        final Colour  text_colour  = canvas.findColour(box.readOnly.get()
            ? NVTextBox.COLOUR_TEXT_UNEDITABLE
            : (box.isTextValid() ? NVTextBox.COLOUR_TEXT_EDITABLE : NVTextBox.COLOUR_TEXT_ERROR));
        final int     text_len     = text.length();
        final int     font_height  = Minecraft.getInstance().font.lineHeight;
        final int     selection_1  = box.getCursorPos();
        final int     selection_2  = box.getSelectionEnd();
        final int     caret_pos    = (selection_1 - first_index);
        final boolean is_caret_set = (caret_pos >= 0 && caret_pos <= text_len);
        
        final int text_y = (((bounds.y() + bounds.getBottom()) / 2 - font_height / 2) + 1);
        int running_x = (bounds.x() + 1);
        
        canvas.setClippingRegion(bounds);
        
        if (!text.isEmpty())
        {
            final String string = (is_caret_set ? text.substring(0, caret_pos) : text);
            this.nvTextboxDrawText(canvas, box, string, running_x, text_y, text_colour);
            running_x += font.getTextWidthFitted(string);
        }
        
        if (!text.isEmpty() && is_caret_set && caret_pos < text_len)
        {
            final String string = text.substring(caret_pos);
            this.nvTextboxDrawText(canvas, box, string, running_x, text_y, text_colour);
        }
        
        if (box.isEmpty() && !box.isFocused() && box.placeholder.isSet())
        {
            this.nvTextboxDrawPlaceholder(canvas, box, running_x, text_y);
        }
        
        final int     box_text_len   = box.getText().length();
        final boolean is_insert_mode = (selection_1 < box_text_len || box_text_len >= box.maxLength.get());
        final int     caret_x        = (!is_caret_set
            ? (caret_pos > 0 ? (bounds.x() + box.getWidth()) : bounds.x())
            : (is_insert_mode ? (running_x - 1) : running_x));
        
        if (!is_insert_mode && box.suggestion.isSet())
        {
            this.nvTextboxDrawSuggestion(canvas, box, (caret_x - 1), text_y);
        }
        
        final int end_caret_pos = Mth.clamp((selection_2 - first_index), 0, text_len);
        
        if (end_caret_pos != caret_pos)
        {
            final int sel_s = (bounds.x() + font.getTextWidthFitted(text.substring(0, end_caret_pos)));
            final int sel_x = (Math.min(Math.min((caret_x + 1), (sel_s - 1)), bounds.width()));
            final int sel_y = (text_y - 1);
            final int sel_w = Math.min((Math.max(sel_s, caret_x) - sel_x), bounds.width());
            final int sel_h = (GuiFont.getDefaultRenderHeight() + 1);
            this.nvTextboxDrawSelection(canvas, box, sel_x, sel_y, sel_w, sel_h);
        }
        
        if (
            box.isFocused()
            && is_caret_set
            && ((Util.getMillis() - lastSwitchFocusTime) / 300L % 2L == 0L)
        )
        {
            if (is_insert_mode)
            {
                canvas.setColour(0xFFD0D0D0);
                canvas.fill(caret_x, (text_y - 1), 1, (font_height + 1));
            }
            else
            {
                canvas.setColour(text_colour);
                canvas.drawText("_", caret_x, text_y);
            }
        }
    }
    
    @Override
    default void nvTextboxDrawBackground(final Canvas canvas, final NVTextBox textBox)
    {
        canvas.drawSprite(
            NVTextBox.TEXTURES.get(canvas.isActive(), textBox.isFocused()),
            0, 0, textBox.getWidth(), textBox.getHeight(),
            false);
    }
    
    @Override
    default void nvTextboxDrawText(final Canvas canvas, final NVTextBox textBox, final String text, final int x,
                                   final int y, final Colour textColour)
    {
        canvas.setColour(textColour);
        canvas.drawText(text, x, y);
    }
    
    @Override
    default void nvTextboxDrawSuggestion(final Canvas canvas, final NVTextBox textBox, final int x, final int y)
    {
        canvas.setColour(canvas.findColour(NVTextBox.COLOUR_TEXT_SUGGESTION));
        canvas.drawText(Objects.requireNonNull(textBox.suggestion.get()), x, y);
    }
    
    @Override
    default void nvTextboxDrawPlaceholder(final Canvas canvas, final NVTextBox textBox, final int x, final int y)
    {
        canvas.setColour(canvas.findColour(NVTextBox.COLOUR_TEXT_PLACEHOLDER));
        canvas.drawText(Objects.requireNonNull(textBox.placeholder.get()), x, y);
    }
    
    @Override
    default void nvTextboxDrawSelection(final Canvas canvas, final NVTextBox textBox, final int x,
                                        final int y, final int width, final int height)
    {
        final Matrix3x2fc     pose         = canvas.getTransform().getMatrix();
        final ScreenRectangle scissor_area = canvas.getClippingRegion().toScreenRect();
        final int             colour_1     = Colour.WHITE.colour();
        final int             colour_2     = 0xFF0000FF;
        final int             x2           = (x + width);
        final int             y2           = (y + height);
        
        canvas.draw(new ColoredRectangleRenderState(
            RenderPipelines.GUI_INVERT,
            TextureSetup.noTexture(),
            pose,
            x, y, x2, y2,
            colour_1, colour_1,
            scissor_area));
        canvas.draw(new ColoredRectangleRenderState(
            RenderPipelines.GUI_TEXT_HIGHLIGHT,
            TextureSetup.noTexture(),
            pose,
            x, y, x2, y2,
            colour_2, colour_2,
            scissor_area));
    }
    
    //==================================================================================================================
    @Override default void nvViewportDrawBackground(Canvas canvas, NVViewport viewport) {}
    
    @Override
    default void nvViewportDrawCorner(final Canvas canvas, final NVViewport viewport, final int width, final int height)
    {
        canvas.drawSprite(NVScrollbar.TEXTURE_SCROLLBAR_BACKGROUND, 0, 0, width, height, false);
    }
    
    //==================================================================================================================
    @Override
    default void nvSliderDrawBackground(final Canvas canvas, final NVSlider slider)
    {
        final Identifier texture = (slider.isFocused() ? NVSlider.TEXTURE_HIGHLIGHTED : NVSlider.TEXTURE);
        canvas.drawSprite(texture, 0, 0, slider.getWidth(), slider.getHeight(), false);
    }
    
    @Override
    default void nvSliderDrawThumb(final Canvas canvas, final NVSlider slider, final Rectangle thumbBounds)
    {
        final Identifier texture = ((slider.isFocused() || slider.isHovered())
            ? NVSlider.TEXTURE_HANDLE_HIGHLIGHT
            : NVSlider.TEXTURE_HANDLE);
        canvas.drawSprite(texture, thumbBounds, false);
    }
    
    @Override
    default void nvSliderDrawText(final Canvas canvas, final NVSlider slider, final Component text)
    {
        canvas.setColour(canvas.findColour(canvas.isActive() ? NVSlider.COLOUR_TEXT : NVSlider.COLOUR_TEXT_INACTIVE));
        canvas.drawScrollableText(text, 2, 0, (slider.getWidth() - 4), slider.getHeight());
    }
    
    //==================================================================================================================
    @Override
    default void nvSimpleButtonDrawBackground(final Canvas canvas, final NVSimpleButton button)
    {
        final Identifier texture = NVSimpleButton.BACKGROUND_TEXTURE.get(
            canvas.isActive(),
            (button.isFocused() || button.isHovered()));
        canvas.drawSprite(texture, 0, 0, button.getWidth(), button.getHeight(), false);
    }
    
    @Override
    default void nvSimpleButtonDrawIcon(final Canvas canvas, final NVSimpleButton button, final Rectangle bounds)
    {
        assert (button.getIcon() != null);
        canvas.drawSprite(button.getIcon(), bounds, false);
    }
    
    @Override
    default void nvSimpleButtonDrawText(final Canvas canvas, final NVSimpleButton button, final Rectangle bounds)
    {
        assert (button.getText() != null);
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVSimpleButton.COLOUR_TEXT
            : NVSimpleButton.COLOUR_TEXT_INACTIVE));
        canvas.drawTextAligned(button.getText(), bounds, Alignment.MIDDLE_CENTRE);
    }
    
    //==================================================================================================================
    @Override
    default void nvNumericBoxDrawArrowButton(final Canvas canvas, final NVNumericBox numericBox,
                                             final NVAbstractButton button, final boolean isUpButton)
    {
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVNumericBox.COLOUR_ARROW
            : NVNumericBox.COLOUR_ARROW_INACTIVE));
        canvas.drawTextAligned((isUpButton ? "▴" : "▾"), button.getLocalBounds(), Alignment.MIDDLE_CENTRE);
    }
    
    //==================================================================================================================
    @Override
    default void nvScrollbarDrawBackground(final Canvas canvas, final NVScrollbar scrollbar,
                                           final Rectangle trackBounds)
    {
        canvas.drawSprite(NVScrollbar.TEXTURE_SCROLLBAR_BACKGROUND, trackBounds, false);
    }
    
    @Override
    default void nvScrollbarDrawThumb(final Canvas canvas, final NVScrollbar scrollbar, final Rectangle bounds)
    {
        if (!canvas.isActive())
        {
            return;
        }
        
        canvas.drawSprite(NVScrollbar.TEXTURE_SCROLLBAR_THUMB, bounds, false);
    }
    
    //==================================================================================================================
    @Override default void nvLabelDrawBackground(Canvas canvas, NVLabel nvLabel) {}
    
    @Override
    default void nvLabelDrawText(final Canvas canvas, final NVLabel label, final FormattedCharSequence text)
    {
        canvas.setColour(canvas.findColour(canvas.isActive() ? NVLabel.COLOUR_TEXT : NVLabel.COLOUR_TEXT_INACTIVE));
        canvas.drawTextAligned(text, label.getLocalBounds(), label.textAlign.get());
    }
    
    //==================================================================================================================
    @Override
    default <T extends INVItemModel>
    void nvListBoxDrawBackground(Canvas canvas, NVListBox<T> nvListBox) {}
    
    //==================================================================================================================
    @Override
    default void nvDropdownDrawMenuBackground(final Canvas canvas, final NVDropdown dropdown, final int width,
                                              final int height)
    {
        final Identifier texture = NVTextBox.TEXTURES.get(true, dropdown.hasFocus());
        canvas.drawSprite(texture, 0, 0, width, height, false);
    }
    
    @Override
    default void nvDropdownDrawMenuOption(final Canvas canvas, final NVDropdown dropdown,
                                          final Component title, final int width, final int height, final int index,
                                          final boolean selected, final boolean hovered, final boolean focused)
    {
        if (hovered || selected)
        {
            canvas.setColour(canvas.findColour(selected
                ? NVDropdown.COLOUR_OPTION_BACKGROUND_SELECTED
                : NVDropdown.COLOUR_OPTION_BACKGROUND_HIGHLIGHT));
            canvas.fill();
        }
        
        final int draw_x     = 5;
        final int draw_width = (width - (draw_x * 2));

        canvas.setColour(canvas.findColour(NVDropdown.COLOUR_OPTION_TEXT));
        canvas.setTransform(AffineTransform.translation(draw_x, 1));
        canvas.setClippingRegion(0, 0, draw_width, height);
        
        final GuiFont font       = canvas.getFont();
        final int     text_width = TextLayout.getTextWidthFitted(font, title);
        
        if (draw_width < text_width)
        {
            final int trim_width = (draw_width - TextLayout.getTextWidthFitted(font, CommonComponents.ELLIPSIS));
            final FormattedCharSequence ordered = Language.getInstance().getVisualOrder(FormattedText.composite(
                TextLayout.trimToWidth(font, title, trim_width),
                CommonComponents.ELLIPSIS));
            canvas.drawTextAligned(ordered, 0, 0, draw_width, height, dropdown.optionAlignment.get());
        }
        else
        {
            canvas.drawTextAligned(title, 0, 0, draw_width, height, dropdown.optionAlignment.get());
        }
    }
    
    @Override
    default void nvDropdownDrawButton(final Canvas canvas, final NVDropdown dropdown, final NVAbstractButton button,
                                      final boolean isOpen)
    {
        final Rectangle  bounds  = button.getLocalBounds();
        final Identifier texture = NVTextBox.TEXTURES.get(canvas.isActive(), dropdown.hasFocus());
        
        canvas.runWithState(() ->
        {
            canvas.setClippingRegion(bounds.withLeftPadding(1));
            canvas.drawSprite(texture, bounds, false);
        });
        
        canvas.setColour(canvas.findColour(canvas.isActive()
            ? NVNumericBox.COLOUR_ARROW
            : NVNumericBox.COLOUR_ARROW_INACTIVE));
        canvas.drawTextAligned((isOpen ? "▴" : "▾"), bounds, Alignment.MIDDLE_CENTRE);
    }
    
    //==================================================================================================================
    @Override default void nvCheckBoxDrawBackground(Canvas canvas, NVCheckBox checkBox) {}
    
    @Override
    default void nvCheckBoxDrawCheckMark(final Canvas canvas, final NVCheckBox checkBox)
    {
        canvas.drawSprite(
            NVCheckBox.TEXTURES.get(
                checkBox.isChecked(),
                (checkBox.isFocused() || checkBox.isHovered()),
                canvas.isActive()),
            0, 0, checkBox.getWidth(), checkBox.getHeight(),
            false);
    }
}
