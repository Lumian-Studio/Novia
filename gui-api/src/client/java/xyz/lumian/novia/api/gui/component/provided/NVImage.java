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

import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.geometry.UvMapping;
import xyz.lumian.novia.api.gui.property.GuiProperty;

import java.util.Objects;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// Represents a simple component that only draws a texture on screen that can be specified with the [NVImage.Sprites]
/// record.
public class NVImage
    extends GuiComponent
{
    //******************************************************************************************************************
    /// Describes the texture to be used, when:
    /// @param active         the component is active but neither focused nor hovered
    /// @param inactive       the component is inactive
    /// @param focused        the component is active and focused but not hovered
    /// @param hovered        the component is active and hovered but not focused
    /// @param hoveredFocused the component is active and hovered as well as focused
    public record Sprites(
        Identifier active,
        Identifier inactive,
        Identifier focused,
        Identifier hovered,
        Identifier hoveredFocused
    )
    {
        //**************************************************************************************************************
        public Sprites(final Identifier active, final Identifier inactive, final Identifier focused,
                       final Identifier hovered)
        {
            this(active, inactive, focused, hovered, hovered);
        }
        
        public Sprites(final Identifier active, final Identifier inactive, final Identifier hoveredOrFocused)
        {
            this(active, inactive, hoveredOrFocused, hoveredOrFocused, hoveredOrFocused);
        }
        
        public Sprites(final Identifier active, final Identifier inactive)
        {
            this(active, inactive, active, active, active);
        }
        
        public Sprites(final Identifier all) { this(all, all, all, all, all); }
        
        //==============================================================================================================
        /// Gets the ID of the texture.
        /// @param active  Whether the component is active
        /// @param hovered Whether the component is hovered
        /// @param focused Whether the component is focused
        /// @return The sprite [Identifier] associated with the given state of the component
        public Identifier getId(final boolean active, final boolean hovered, final boolean focused)
        {
            if (active)
            {
                if (focused)
                {
                    return (hovered ? this.hoveredFocused : this.focused);
                }
                
                return (hovered ? this.hovered : this.active);
            }
            
            return this.inactive;
        }
    }
    
    //******************************************************************************************************************
    @Nullable
    public static final GuiSpriteScaling DEFAULT_EXPLICIT_SCALING = null;
    public static final UvMapping        DEFAULT_SPRITE_UV        = UvMapping.FULL;
    
    //==================================================================================================================
    /// The default texture used if no texture is set for the image component.
    public static final Identifier DEFAULT_TEXTURE_ID = GuiApiId.of("widget/no_image");
    
    //******************************************************************************************************************
    /// Raised whenever the displayed sprites have changed.
    public final GuiEvent.Simple spritesChanged = new GuiEvent.Simple();
    
    //==================================================================================================================
    /// Describes the explicit scaling of the sprite in this image, or `null` to apply automatic scaling as described
    /// for the sprite's texture atlas entry.
    public final GuiProperty<@Nullable GuiSpriteScaling> explicitScaling;
    
    /// Describes how the UV coordinates are mapped to the rendered sprite ([UvMapping#FULL] by default).
    public final GuiProperty.NonNull<UvMapping> spriteUv;
    
    //==================================================================================================================
    @Nullable
    private Sprites sprites;

    //******************************************************************************************************************
    /// Constructs a new image component with the given sprites.
    /// @param sprites The sprites to use or `null` to not use any sprites
    public NVImage(final @Nullable Sprites sprites)
    {
        this.explicitScaling = GuiProperty.nullable(NVImage.DEFAULT_EXPLICIT_SCALING);
        this.spriteUv        = GuiProperty.nonNull (NVImage.DEFAULT_SPRITE_UV);
        this.sprites         = sprites;
    }
    
    /// Constructs a new image component with the given sprite.
    /// @param spriteId The ID of the sprite to use
    public NVImage(final Identifier spriteId) { this(new Sprites(spriteId)); }
    
    /// Constructs a new empty image component.
    public NVImage() { this((Sprites) null); }

    //==================================================================================================================
    /// Gets the sprites that are currently set for this image component.
    /// @return The [Sprites]
    public @Nullable Sprites getSprites() { return this.sprites; }
    
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.concat(
            super.getGuiProperties(),
            Stream.of(
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.IMAGE_EXPLICIT_SCALING,
                    this.explicitScaling,
                    GuiSpriteScaling.CODEC),
                new GuiPropertyDescription<>(
                    GuiApiId.GuiProperty.IMAGE_SPRITE_UV,
                    this.spriteUv,
                    UvMapping.CODEC)));
    }
    
    //==================================================================================================================
    /// Gets whether this image component currently has sprites set.
    /// @return `true` if there are sprites set for this component
    public boolean hasSprite() { return (this.sprites != null); }
    
    //==================================================================================================================
    /// Sets the sprites to be used for this image component.
    /// @param sprites The sprites to use or `null` to not use any sprites
    public void setSprites(final @Nullable Sprites sprites)
    {
        if (!Objects.equals(this.sprites, sprites))
        {
            this.sprites = sprites;
            
            this.onSpritesChanged();
            this.spritesChanged.post(this);
        }
    }
    
    /// Sets the sprite to the given sprite ID.
    /// @param spriteId The ID of the sprite to use
    public void setImage(final Identifier spriteId)
    {
        final Sprites new_sprites = new Sprites(spriteId);
        
        if (!new_sprites.equals(this.sprites))
        {
            this.sprites = new_sprites;
            this.spritesChanged.post(this);
        }
    }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        final Rectangle  bounds    = this.getLocalBounds();
        final UvMapping  uv        = this.spriteUv.get();
        final Identifier sprite_id = (this.sprites != null
            ? this.sprites.getId(canvas.isActive(), this.isHovered(), this.isFocused())
            : NVImage.DEFAULT_TEXTURE_ID);
        
        this.explicitScaling
            .ifEmpty(()      -> canvas.drawSprite(sprite_id, bounds, uv, false))
            .ifSet  (scaling -> canvas.drawSprite(canvas.findSprite(new SpriteId(AtlasIds.GUI, sprite_id)), scaling,
                                                  bounds, uv, false));
    }
    
    //==================================================================================================================
    /// Can be overridden to get notified whenever the image component's sprites changed.
    public void onSpritesChanged() {}
}
