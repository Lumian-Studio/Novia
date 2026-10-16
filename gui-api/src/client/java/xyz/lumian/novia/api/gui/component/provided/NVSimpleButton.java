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
package xyz.lumian.novia.api.gui.component.provided;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.util.RefUtils;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.font.TextLayout;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;

import java.util.*;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// An [NVAbstractButton] implementation that can render a given text, and an optional icon on top of the button
/// background.
public class NVSimpleButton
    extends NVAbstractButton
{
    //******************************************************************************************************************
    public enum IconPlacement
        implements StringRepresentable
    {
        /// Places the icon left of the text.
        LEFT,
        
        /// Places the icon right of the text.
        RIGHT,
        
        /// Places the icon above the text.
        ABOVE,
        
        /// Places the icon below the text.
        BELOW,
        ;
        
        //**************************************************************************************************************
        public static final Codec<IconPlacement> CODEC = StringRepresentable.fromEnum(IconPlacement::values);
        
        //**************************************************************************************************************
        @Override public String getSerializedName() { return this.name().toLowerCase(); }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the button's background.
        /// @param canvas The [Canvas]
        /// @param button The [NVSimpleButton]
        void nvSimpleButtonDrawBackground(Canvas canvas, NVSimpleButton button);
        
        /// Draws the button's icon
        /// @param canvas The [Canvas]
        /// @param button The [NVSimpleButton]
        /// @param bounds The available space for the icon
        void nvSimpleButtonDrawIcon(Canvas canvas, NVSimpleButton button, Rectangle bounds);
        
        /// Draws the button's text.
        /// @param canvas The [Canvas]
        /// @param button The [NVSimpleButton]
        /// @param bounds The available space for the text
        void nvSimpleButtonDrawText(Canvas canvas, NVSimpleButton button, Rectangle bounds);
    }
    
    //******************************************************************************************************************
    public static final ColourId COLOUR_TEXT          = ColourId.reserve();
    public static final ColourId COLOUR_TEXT_INACTIVE = ColourId.reserve();
    
    //==================================================================================================================
    /// The textures used to render the button.
    public static final WidgetSprites BACKGROUND_TEXTURE = Optional.of(Identifier.withDefaultNamespace("widget"))
        .map(widgets -> new WidgetSprites(
            widgets.withSuffix("/button"),
            widgets.withSuffix("/button_disabled"),
            widgets.withSuffix("/button_highlighted")))
        .orElseThrow();
    
    //==================================================================================================================
    /// See [NVSimpleButton#iconPlacement].
    public static final IconPlacement DEFAULT_PLACEMENT = IconPlacement.LEFT;
    
    /// See [NVSimpleButton#iconSize].
    public static final int DEFAULT_ICON_SIZE = 16;
    
    /// See [NVSimpleButton#iconMargin].
    public static final int DEFAULT_ICON_MARGIN = 2;
    
    //******************************************************************************************************************
    /// Describes the placement of the icon next to the button text. (if both are set)
    public final GuiProperty.NonNull<IconPlacement> iconPlacement;
    
    /// Describes the size of the icon texture that should be rendered on the button.
    public final GuiProperty.NonNull<Integer> iconSize;
    
    /// Describes the margin between the icon and text if both are set.
    public final GuiProperty.NonNull<Integer> iconMargin;
    
    //==================================================================================================================
    /// Triggered whenever the displayed icon changes.
    public final GuiEvent.Simple iconChangedEvent = new GuiEvent.Simple();
    
    /// Triggered whenever the displayed text changes.
    public final GuiEvent.Simple textChangedEvent = new GuiEvent.Simple();
    
    //==================================================================================================================
    private Rectangle iconBounds = new Rectangle();
    private Rectangle textBounds = new Rectangle();
    
    @Nullable
    private Identifier textureId = null;
    
    @Nullable
    private Component text = null;
    
    //******************************************************************************************************************
    /// Constructs a new button with the given text and action.
    /// @param text    The text displayed on the button
    /// @param message The component message
    public NVSimpleButton(final @Nullable Component text, final Component message)
    {
        super(message);
        
        this.iconPlacement = GuiPropertyBuilder.nonNull(NVSimpleButton.DEFAULT_PLACEMENT)
            .withNoArgSetter(this::updateIconStyle)
            .build();
        this.iconMargin    = GuiPropertyBuilder.nonNull(NVSimpleButton.DEFAULT_ICON_MARGIN)
            .withNoArgSetter(this::updateIconStyle)
            .build();
        this.iconSize      = GuiPropertyBuilder.nonNull(NVSimpleButton.DEFAULT_ICON_SIZE)
            .withNoArgSetter(this::updateIconSize)
            .withValidator(RefUtils.greaterThan(0))
            .build();
        
        this.text = text;
    }
    
    /// Constructs a new button with the given text and action.
    /// @param text The text displayed on the button
    public NVSimpleButton(final Component text) { this(text, CommonComponents.EMPTY); }
    
    /// Constructs a new button without an action and no text.
    public NVSimpleButton() { this(null, CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the display icon texture for this button
    /// @return The texture [Identifier] or `null` if none was set
    public @Nullable Identifier getIcon() { return this.textureId; }
    
    /// Gets the text displayed on the button.
    /// @return The button [Component] or `null` if none was set
    public @Nullable Component getText() { return this.text; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.concat(
            super.getGuiProperties(),
            Stream.of(
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.SIMPLE_BUTTON_ICON_SIZE,
                    this.iconSize,
                    Codec.INT),
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.SIMPLE_BUTTON_ICON_PLACEMENT,
                    this.iconMargin,
                    Codec.INT),
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.SIMPLE_BUTTON_ICON_MARGIN,
                    this.iconPlacement,
                    IconPlacement.CODEC)));
    }
    
    //==================================================================================================================
    /// Sets the icon textures to draw on the button.
    /// @param textureId The texture [Identifier]
    public void setIcon(final @Nullable Identifier textureId)
    {
        if (!Objects.equals(this.textureId, textureId))
        {
            final Identifier old = this.textureId;
            this.textureId = textureId;
            
            if (old == null || textureId == null)
            {
                this.updateContentBounds(this.getFont());
            }
            
            this.onIconChanged();
            this.iconChangedEvent.post(this);
        }
    }
    
    /// Sets the text displayed on the button.
    /// @param text The button [Component]
    public void setText(final @Nullable Component text)
    {
        if (!Objects.equals(this.text, text))
        {
            this.text = text;
            this.updateContentBounds(this.getFont());
            
            this.onTextChanged();
            this.textChangedEvent.post(this);
        }
    }
    
    //==================================================================================================================
    @Override public void resized() { this.updateContentBounds(this.getFont()); }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvSimpleButtonDrawBackground(canvas, this);
        
        if (this.textureId != null)
        {
            template.nvSimpleButtonDrawIcon(canvas, this, new Rectangle(this.iconBounds));
        }
        
        if (this.text != null)
        {
            template.nvSimpleButtonDrawText(canvas, this, new Rectangle(this.textBounds));
        }
    }
    
    //==================================================================================================================
    @Override
    public void onFontChanged(final @Nullable GuiFont font)
    {
        this.updateContentBounds(Objects.requireNonNullElseGet(font, this::getFont));
    }
    
    /// If the component's parents change, we might also get a different font
    @Override public void onHierarchyChanged() { this.onFontChanged(this.getFont()); }
    
    //==================================================================================================================
    /// Called when the displayed icon changed.
    public void onIconChanged() {}
    
    /// Called when the displayed text changed.
    public void onTextChanged() {}
    
    //==================================================================================================================
    private void updateIconStyle()
    {
        if (this.textureId != null && this.text != null)
        {
            this.updateContentBounds(this.getFont());
        }
    }
    
    private void updateIconSize()
    {
        if (this.textureId != null)
        {
            this.updateContentBounds(this.getFont());
        }
    }
    
    private void updateContentBounds(final GuiFont font)
    {
        final boolean has_icons = (this.textureId != null);
        
        if (!has_icons && this.text == null)
        {
            return;
        }
        
        final IconPlacement placement   = this.iconPlacement.get();
        final int           icon_size   = (has_icons ? this.iconSize.get() : 0);
        final int           font_height = Minecraft.getInstance().font.lineHeight;
        
        switch (placement)
        {
            case LEFT:
            case RIGHT:
            {
                int width      = 0;
                int height     = 0;
                int text_width = 0;
                
                if (this.text != null)
                {
                    text_width = TextLayout.getTextWidthFitted(font, this.text);
                    width      = text_width;
                    height     = font_height;
                }
                
                if (has_icons)
                {
                    width  += icon_size;
                    height  = Math.max(height, this.iconSize.get());
                    
                    if (this.text != null)
                    {
                        width += this.iconMargin.get();
                    }
                }
                
                final Rectangle content_bounds = new Rectangle(0, 0, width, height).centre(this.getLocalBounds());
                
                if (placement == IconPlacement.LEFT)
                {
                    this.iconBounds = content_bounds.removeLeft(icon_size);
                    this.textBounds = content_bounds;
                }
                else
                {
                    this.textBounds = content_bounds.removeLeft(text_width);
                    this.iconBounds = content_bounds;
                }
            } break;
            
            case ABOVE:
            case BELOW:
            {
                int width       = 0;
                int height      = 0;
                int text_height = 0;
                
                if (this.text != null)
                {
                    text_height = font_height;
                    width       = TextLayout.getTextWidthFitted(font, this.text);
                    height      = text_height;
                }
                
                if (has_icons)
                {
                    width   = Math.max(width, this.iconSize.get());
                    height += icon_size;
                    
                    if (this.text != null)
                    {
                        height += this.iconMargin.get();
                    }
                }
                
                final Rectangle content_bounds = new Rectangle(0, 0, width, height).centre(this.getLocalBounds());
                
                if (placement == IconPlacement.ABOVE)
                {
                    this.iconBounds = content_bounds.removeTop(icon_size);
                    this.textBounds = content_bounds;
                }
                else
                {
                    this.textBounds = content_bounds.removeTop(text_height);
                    this.iconBounds = content_bounds;
                }
            } break;
        }
    }
}
