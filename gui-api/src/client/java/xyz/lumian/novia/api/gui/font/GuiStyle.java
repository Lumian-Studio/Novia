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
package xyz.lumian.novia.api.gui.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.impl.StyleAccessor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;



//**********************************************************************************************************************
public class GuiStyle
    extends Style
{
    //******************************************************************************************************************
    public static final GuiStyle EMPTY = new GuiStyle(null, null, null, null, null, null, null, null, null, null, null,
                                                      null, null, null, null);

    //==================================================================================================================
    public static final MapCodec<GuiStyle>                             MAP_CODEC;
    public static final Codec<GuiStyle>                                CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, GuiStyle> STREAM_CODEC;

    //==================================================================================================================
    static {
        MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
            .group(
                TextColor.CODEC
                    .optionalFieldOf("colour")
                    .forGetter((style) -> Optional.ofNullable(style.getColor())),
                ExtraCodecs.ARGB_COLOR_CODEC
                    .optionalFieldOf("shadow_color")
                    .forGetter((style) -> Optional.ofNullable(style.getShadowColor())),
                Codec.BOOL
                    .optionalFieldOf("bold")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isBold())),
                Codec.BOOL
                    .optionalFieldOf("italic")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isItalic())),
                Codec.BOOL
                    .optionalFieldOf("underlined")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isUnderlined())),
                Codec.BOOL
                    .optionalFieldOf("strikethrough")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isStrikethrough())),
                Codec.BOOL
                    .optionalFieldOf("obfuscated")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$isObfuscated())),
                ClickEvent.CODEC
                    .optionalFieldOf("click_event")
                    .forGetter((style) -> Optional.ofNullable(style.getClickEvent())),
                HoverEvent.CODEC
                    .optionalFieldOf("hover_event")
                    .forGetter((style) -> Optional.ofNullable(style.getHoverEvent())),
                Codec.STRING
                    .optionalFieldOf("insertion")
                    .forGetter((style) -> Optional.ofNullable(style.getInsertion())),
                GuiFont.FONT_DESCRIPTION_CODEC
                    .optionalFieldOf("font")
                    .forGetter((style) -> Optional.ofNullable(((StyleAccessor) style).novia$getFontId())),
                Codec.BOOL
                    .optionalFieldOf("shaded")
                    .forGetter((style) -> Optional.ofNullable(style.shaded)),
                Codec.FLOAT
                    .optionalFieldOf("size")
                    .forGetter((style) -> Optional.ofNullable(style.getSize())),
                Codec.FLOAT
                    .optionalFieldOf("tracking")
                    .forGetter((style) -> Optional.ofNullable(style.getTracking())),
                TextColor.CODEC
                    .optionalFieldOf("background_color")
                    .forGetter((style) -> Optional.ofNullable(style.getBackgroundColor())))
            .apply(instance, GuiStyle::of));
        CODEC        = MAP_CODEC.codec();
        STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    }

    //******************************************************************************************************************
    private static <T> GuiStyle with(final GuiStyle    newStyle,
                                     final @Nullable T oldAttribute,
                                     final @Nullable T newAttribute)
    {
        return ((oldAttribute != null && newAttribute == null && newStyle.equals(GuiStyle.EMPTY))
            ? GuiStyle.EMPTY
            : newStyle);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static GuiStyle of(final Optional<TextColor>       color,
                               final Optional<Integer>         shadowColor,
                               final Optional<Boolean>         bold,
                               final Optional<Boolean>         italic,
                               final Optional<Boolean>         underlined,
                               final Optional<Boolean>         strikethrough,
                               final Optional<Boolean>         obfuscated,
                               final Optional<ClickEvent>      clickEvent,
                               final Optional<HoverEvent>      hoverEvent,
                               final Optional<String>          insertion,
                               final Optional<FontDescription> font,
                               final Optional<Boolean>         shaded,
                               final Optional<Float>           size,
                               final Optional<Float>           tracking,
                               final Optional<TextColor>       backgroundColor)
    {
        final GuiStyle style = new GuiStyle(
            color          .orElse(null),
            shadowColor    .orElse(null),
            bold           .orElse(null),
            italic         .orElse(null),
            underlined     .orElse(null),
            strikethrough  .orElse(null),
            obfuscated     .orElse(null),
            clickEvent     .orElse(null),
            hoverEvent     .orElse(null),
            insertion      .orElse(null),
            font           .orElse(null),
            shaded         .orElse(null),
            size           .orElse(null),
            tracking       .orElse(null),
            backgroundColor.orElse(null));
        return (style.equals(GuiStyle.EMPTY) ? GuiStyle.EMPTY : style);
    }

    //******************************************************************************************************************
    final @Nullable Boolean   shaded;
    final @Nullable Float     size;
    final @Nullable Float     tracking;
    final @Nullable TextColor backgroundColor;

    //******************************************************************************************************************
    public GuiStyle(final @Nullable TextColor       color,
                    final @Nullable Integer         shadowColor,
                    final @Nullable Boolean         bold,
                    final @Nullable Boolean         italic,
                    final @Nullable Boolean         underlined,
                    final @Nullable Boolean         strikethrough,
                    final @Nullable Boolean         obfuscated,
                    final @Nullable ClickEvent      clickEvent,
                    final @Nullable HoverEvent      hoverEvent,
                    final @Nullable String          insertion,
                    final @Nullable FontDescription font,
                    final @Nullable Boolean         shaded,
                    final @Nullable Float           size,
                    final @Nullable Float           tracking,
                    final @Nullable TextColor       backgroundColor)
    {
        super(color, shadowColor, bold, italic, underlined, strikethrough, obfuscated, clickEvent, hoverEvent,
              insertion, font);

        this.shaded          = shaded;
        this.size            = size;
        this.tracking        = tracking;
        this.backgroundColor = backgroundColor;
    }

    public GuiStyle(final Style style, final @Nullable Boolean shaded, final @Nullable Float size,
                    final @Nullable Float tracking, final @Nullable TextColor backgroundColor)
    {
        this((StyleAccessor) style, style, shaded, size, tracking, backgroundColor);
    }

    //------------------------------------------------------------------------------------------------------------------
    private GuiStyle(final StyleAccessor styleAcc, final Style style, final @Nullable Boolean shaded,
                     final @Nullable Float size, final @Nullable Float tracking,
                     final @Nullable TextColor backgroundColor)
    {
        super(
            style   .getColor(),
            style   .getShadowColor(),
            styleAcc.novia$isBold(),
            styleAcc.novia$isItalic(),
            styleAcc.novia$isUnderlined(),
            styleAcc.novia$isStrikethrough(),
            styleAcc.novia$isObfuscated(),
            style   .getClickEvent(),
            style   .getHoverEvent(),
            style   .getInsertion(),
            styleAcc.novia$getFontId()
        );
        
        this.shaded          = shaded;
        this.size            = size;
        this.tracking        = tracking;
        this.backgroundColor = backgroundColor;
    }

    //==================================================================================================================
    /// {@return the font size}
    public @Nullable Float getSize() { return this.size; }

    /// {@return the tracking factor}
    public @Nullable Float getTracking() { return this.tracking; }
    
    public @Nullable TextColor getBackgroundColor() { return this.backgroundColor; }
    
    //==================================================================================================================
    public boolean isShaded() { return (this.shaded == Boolean.TRUE); }
    
    @Override public boolean isEmpty() { return (this == GuiStyle.EMPTY); }
    
    //==================================================================================================================
    public GuiStyle withColor(final @Nullable TextColor color)
    {
        return new GuiStyle(super.withColor(color), this.shaded, this.size, this.tracking, this.backgroundColor);
    }

    public GuiStyle withColor(final @Nullable ChatFormatting color)
    {
        return (this.withColor(color != null ? TextColor.fromLegacyFormat(color) : null));
    }

    public GuiStyle withColor(final int rgbColor) { return this.withColor(TextColor.fromRgb(rgbColor)); }
    
    public GuiStyle withColor(final @Nullable Colour color)
    {
        return this.withColor(color != null ? TextColor.fromRgb(color.colour()) : null);
    }
    
    public GuiStyle withShadowColor(final int shadowColor)
    {
        return new GuiStyle(super.withShadowColor(shadowColor), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }
    
    public GuiStyle withShadowColor(final Colour colour)
    {
        return this.withShadowColor(colour.colour());
    }

    public GuiStyle withBold(final @Nullable Boolean bold)
    {
        return new GuiStyle(super.withBold(bold), this.shaded, this.size, this.tracking, this.backgroundColor);
    }

    public GuiStyle withItalic(final @Nullable Boolean italic)
    {
        return new GuiStyle(super.withItalic(italic), this.shaded, this.size, this.tracking, this.backgroundColor);
    }

    public GuiStyle withUnderlined(final @Nullable Boolean underline)
    {
        return new GuiStyle(super.withUnderlined(underline), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle withStrikethrough(final @Nullable Boolean strikethrough)
    {
        return new GuiStyle(super.withStrikethrough(strikethrough), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle withObfuscated(final @Nullable Boolean obfuscated)
    {
        return new GuiStyle(super.withObfuscated(obfuscated), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle withClickEvent(final @Nullable ClickEvent clickEvent)
    {
        return new GuiStyle(super.withClickEvent(clickEvent), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle withHoverEvent(final @Nullable HoverEvent hoverEvent)
    {
        return new GuiStyle(super.withHoverEvent(hoverEvent), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle withInsertion(final @Nullable String insertion)
    {
        return new GuiStyle(super.withInsertion(insertion), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle withFont(final @Nullable FontDescription font)
    {
        return new GuiStyle(super.withFont(font), this.shaded, this.size, this.tracking, this.backgroundColor);
    }
    
    public GuiStyle withFontResource(final @Nullable Identifier fontId)
    {
        return this.withFont(fontId != null ? new FontDescription.Resource(fontId) : null);
    }

    public GuiStyle withShadow(final @Nullable Boolean shadow)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.shaded, shadow)
            ? GuiStyle.with(
                new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                             style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                             style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                             this.getHoverEvent(), this.getInsertion(), this.getFont(), shadow, this.size,
                             this.tracking, this.backgroundColor),
                this.shaded, shadow)
            : this);
    }

    public GuiStyle withSize(final @Nullable Float size)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.size, size)
            ? GuiStyle.with(
                new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                             style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                             style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                             this.getHoverEvent(), this.getInsertion(), this.getFont(), this.shaded, size,
                             this.tracking, this.backgroundColor),
                this.size, size)
            : this);
    }

    public GuiStyle withTracking(final @Nullable Float tracking)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.tracking, tracking)
            ? GuiStyle.with(
            new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                         style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                         style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                         this.getHoverEvent(), this.getInsertion(), this.getFont(), this.shaded, this.size,
                         tracking, this.backgroundColor),
            this.tracking, tracking)
            : this);
    }
    
    public GuiStyle withBackgroundColor(final @Nullable TextColor backgroundColor)
    {
        final StyleAccessor style_acc = (StyleAccessor) this;
        return (!Objects.equals(this.backgroundColor, backgroundColor)
            ? GuiStyle.with(
                new GuiStyle(this.getColor(), this.getShadowColor(), style_acc.novia$isBold(),
                             style_acc.novia$isItalic(), style_acc.novia$isUnderlined(),
                             style_acc.novia$isStrikethrough(), style_acc.novia$isObfuscated(), this.getClickEvent(),
                             this.getHoverEvent(), this.getInsertion(), this.getFont(), this.shaded, this.size,
                             this.tracking, backgroundColor),
                this.backgroundColor, backgroundColor)
            : this);
    }
    
    public GuiStyle withBackgroundColor(final @Nullable ChatFormatting color)
    {
        return this.withBackgroundColor(color != null ? TextColor.fromLegacyFormat(color) : null);
    }

    public GuiStyle withBackgroundColor(final int rgbColor)
    {
        return this.withBackgroundColor(TextColor.fromRgb(rgbColor));
    }
    
    public GuiStyle withBackgroundColor(final @Nullable Colour color)
    {
        return this.withBackgroundColor(color != null ? TextColor.fromRgb(color.colour()) : null);
    }

    public GuiStyle applyFormat(final ChatFormatting formatting)
    {
        if (formatting == ChatFormatting.RESET) return GuiStyle.EMPTY;
        return new GuiStyle(super.applyFormat(formatting), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle applyLegacyFormat(final ChatFormatting formatting)
    {
        if (formatting == ChatFormatting.RESET) return GuiStyle.EMPTY;
        return new GuiStyle(super.applyLegacyFormat(formatting), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle applyFormats(final ChatFormatting ...formattings)
    {
        if (Arrays.stream(formattings).anyMatch(e -> e == ChatFormatting.RESET)) return GuiStyle.EMPTY;
        return new GuiStyle(super.applyFormats(formattings), this.shaded, this.size, this.tracking,
                            this.backgroundColor);
    }

    public GuiStyle applyTo(final GuiStyle parent)
    {
        if (this == GuiStyle.EMPTY)
        {
            return parent;
        }

        final StyleAccessor accessor        = (StyleAccessor) this;
        final StyleAccessor parent_accessor = (StyleAccessor) parent;
        
        return (parent != GuiStyle.EMPTY
            ? new GuiStyle(
                this.getOrParent(Style        ::getColor,              this,     parent),
                this.getOrParent(Style        ::getShadowColor,        this,     parent),
                this.getOrParent(StyleAccessor::novia$isBold,          accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isItalic,        accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isUnderlined,    accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isStrikethrough, accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isObfuscated,    accessor, parent_accessor),
                this.getOrParent(Style        ::getClickEvent,         this,     parent),
                this.getOrParent(Style        ::getHoverEvent,         this,     parent),
                this.getOrParent(Style        ::getInsertion,          this,     parent),
                this.getOrParent(StyleAccessor::novia$getFontId,       accessor, parent_accessor),
                this.getOrParent(GuiStyle     ::isShaded,              this,     parent),
                this.getOrParent(GuiStyle     ::getSize,               this,     parent),
                this.getOrParent(GuiStyle     ::getTracking,           this,     parent),
                this.getOrParent(GuiStyle     ::getBackgroundColor,    this,     parent))
            : this);
    }

    @Override
    public GuiStyle applyTo(final Style parent)
    {
        if (this == GuiStyle.EMPTY)
        {
            return new GuiStyle(parent, null, null, null, null);
        }

        final Boolean   shadow;
        final Float     size;
        final Float     tracking;
        final TextColor backgroundColor;
        
        if (parent instanceof GuiStyle g_style)
        {
            shadow          = this.getOrParent(GuiStyle::isShaded,           this, g_style);
            size            = this.getOrParent(GuiStyle::getSize,            this, g_style);
            tracking        = this.getOrParent(GuiStyle::getTracking,        this, g_style);
            backgroundColor = this.getOrParent(GuiStyle::getBackgroundColor, this, g_style);
        }
        else
        {
            shadow          = this.shaded;
            size            = this.size;
            tracking        = this.tracking;
            backgroundColor = this.backgroundColor;
        }
        
        final StyleAccessor accessor        = (StyleAccessor) this;
        final StyleAccessor parent_accessor = (StyleAccessor) parent;
        
        return (!parent.isEmpty()
            ? new GuiStyle(
                this.getOrParent(Style        ::getColor,              this,     parent),
                this.getOrParent(Style        ::getShadowColor,        this,     parent),
                this.getOrParent(StyleAccessor::novia$isBold,          accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isItalic,        accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isUnderlined,    accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isStrikethrough, accessor, parent_accessor),
                this.getOrParent(StyleAccessor::novia$isObfuscated,    accessor, parent_accessor),
                this.getOrParent(Style        ::getClickEvent,         this,     parent),
                this.getOrParent(Style        ::getHoverEvent,         this,     parent),
                this.getOrParent(Style        ::getInsertion,          this,     parent),
                this.getOrParent(StyleAccessor::novia$getFontId,       accessor, parent_accessor),
                shadow,
                size,
                tracking,
                backgroundColor)
            : this);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T, O> @Nullable T getOrParent(final Function<O, @Nullable T> getter, final O primary, final O parent)
    {
        final T first = getter.apply(primary);
        return (first != null ? first : getter.apply(parent));
    }

    //==================================================================================================================
    @Override
    public String toString()
    {
        final BiFunction<String, @Nullable Object, String> appender = ((id, in) -> "," + id + '=' + in);
        final String                             string   = super.toString();

        return (
            string.substring(0, (string.length() - 1))
                + appender.apply("shaded",          this.shaded)
                + appender.apply("size",            this.size)
                + appender.apply("tracking",        this.tracking)
                + appender.apply("backgroundColor", this.backgroundColor)
            + "}"
        );
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof GuiStyle other)) return false;

        return (
            super.equals(o)
            && Objects.equals(this.shaded,          other.shaded)
            && Objects.equals(this.size,            other.size)
            && Objects.equals(this.tracking,        other.tracking)
            && Objects.equals(this.backgroundColor, other.backgroundColor)
        );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), this.shaded, this.size, this.tracking, this.backgroundColor);
    }
}
