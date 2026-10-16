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

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.ObjectInfos;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.impl.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// The gui font class describes an abstraction layer over Minecraft's [font providers](https://minecraft.wiki/w/Font).
///
/// ## Formatting
/// Every instance of a Novia font provides an extensive context of formatting tools, such as boldness or spacing.
/// At the gist of it all is the [Format] enum, which provides all flags that can be used to format
/// a font object. Additionally, the formats come with a [Format#SHADED] flag which, other than how Minecraft
/// usually renders shadows as part of the draw call, a way to tell a font that it should draw shadows beneath the text
/// that it is rendered with.
///
/// A font also comes with extra spacing options, most notably the tracking factor that adds a uniform spacing between
/// each of the glyphs in a text.
///
/// The Minecraft [Style] class is mostly interconvertible with Novia fonts, except a few members such as mouse events
/// or colours. However, it is worth noting that a [Style] instance does not contain all formatting options that a
/// Novia font provides, for things like scale, tracking and shading, Novia provides a [GuiStyle] class which is a child
/// of [Style] and defines all the missing properties.
///
/// ## Metrics & Scaling
/// Since Minecraft uses fixed font scaling (as can be seen with {@link Font#lineHeight}), a Novia font allows
/// text to be rendered in different sizes, for this to work the library gathers and collects metrics about a font.
///
/// Those metrics are not necessarily the real font's metrics, but rather [font provider](https://minecraft.wiki/w/Font)
/// specified metrics that are adjusted to the specific instance they are used in. These justified metrics are
/// attainable through a font's [#getAscent()], [#getDescent()] and [#getHeight()] methods, which are the specific font
/// instance's adjusted metrics, such as with applied font scaling. It is worth noting that for a single Novia font
/// instance, there may be multiple providers that are used, which creates an automatic font fallback environment in
/// case there are glyphs missing in one of them. For this reason, the metrics of such a font object are the combined
/// maximums of all providers.
///
/// To define a font's metrics, Minecraft uses its providers with a set of options that declare how a font is measured
/// internally. Many providers (such as bitmap and truetype) have a font size attribute that describes what the internal
/// resolution will be and how they are fitted onto the atlas grid.
public final class GuiFont
{
    //******************************************************************************************************************
    /// Specifies the formatting flags for the font.
    public enum Format
    {
        BOLD(ChatFormatting.BOLD),
        ITALIC(ChatFormatting.ITALIC),
        STRIKETHROUGH(ChatFormatting.STRIKETHROUGH),
        UNDERLINED(ChatFormatting.UNDERLINE),
        OBFUSCATED(ChatFormatting.OBFUSCATED),
        SHADED(null),
        ;
        
        //**************************************************************************************************************
        private static final MapCodec<BitSet> MAP_CODEC = new MapCodec<>()
        {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops)
            {
                return Arrays
                    .stream(Format.values())
                    .map(format -> ops.createString(format.name().toLowerCase()));
            }
            
            @Override
            public <T> DataResult<BitSet> decode(final DynamicOps<T> ops, final MapLike<T> input)
            {
                final BitSet flags = new BitSet(Format.values().length);
                
                for (final var format : Format.values())
                {
                    final T value_result = input.get(format.name().toLowerCase());
                    
                    if (value_result == null)
                    {
                        continue;
                    }
                    
                    final DataResult<Boolean> flag = ops.getBooleanValue(value_result);
                    
                    if (flag.isError())
                    {
                        return DataResult.error(() -> "not a boolean value");
                    }
                    
                    flags.set(format.ordinal(), flag.getOrThrow());
                }
                
                return DataResult.success(flags);
            }
            
            @Override
            public <T> RecordBuilder<T> encode(final BitSet input, final DynamicOps<T> ops,
                                               final RecordBuilder<T> prefix)
            {
                final T true_bool = ops.createBoolean(true);
                
                Arrays
                    .stream (Format.values())
                    .filter (format -> input.get(format.ordinal()))
                    .forEach(format -> prefix.add(format.name().toLowerCase(), true_bool));
                
                return prefix;
            }
        };
        
        //**************************************************************************************************************
        public final @Nullable ChatFormatting minecraftFormat;
        
        //**************************************************************************************************************
        Format(final @Nullable ChatFormatting minecraftFormat) { this.minecraftFormat = minecraftFormat; }
    }
    
    //******************************************************************************************************************
    /// A codec that can be used to serialise font objects.
    /// ```json
    /// {
    ///     "font": "[modid:]<path>",
    ///     "size": <em | { "unit": <"em"|"px"|"pt">, "size": <value> }>
    ///     "tracking": <factor>,
    ///     ["<formatting>": true ...]
    /// }
    /// ```
    public static final Codec<GuiFont> CODEC;
    
    public static final Codec<FontDescription> FONT_DESCRIPTION_CODEC;
    
    //==================================================================================================================
    /// The currently set default font (see [Mojangles](https://minecraft.fandom.com/de/wiki/Mojangles)).
    public static final Supplier<GuiFont> DEFAULT = GuiFont::getCachedDefault;
    
    /// [GNU Unifont](https://minecraft.wiki/w/GNU_Unifont)
    public static final Supplier<GuiFont> UNICODE = GuiFont::getCachedUnicode;
    
    /// [Standard Galactic Alphabet](https://minecraft.wiki/w/Standard_Galactic_Alphabet)
    public static final Supplier<GuiFont> ALT = GuiFont::getCachedAlt;
    
    /// [Illageralt](https://minecraft.wiki/w/Illageralt)
    public static final Supplier<GuiFont> ILLAGERALT = GuiFont::getCachedIllageralt;
    
    //------------------------------------------------------------------------------------------------------------------
    private static @Nullable GuiFont CACHED_DEFAULT    = null;
    private static @Nullable GuiFont CACHED_UNICODE    = null;
    private static @Nullable GuiFont CACHED_ALT        = null;
    private static @Nullable GuiFont CACHED_ILLAGERALT = null;
    
    private static @Nullable EffectGlyph WHITE_GLYPH = null;
    
    //==================================================================================================================
    static
    {
        FONT_DESCRIPTION_CODEC = Codec.withAlternative(
            FontDescription.CODEC,
            ObjectInfos.CODEC.codec().xmap(ObjectInfo::fontDescription, (desc -> switch (desc)
            {
                case FontDescription.AtlasSprite  sprite -> new AtlasSprite(sprite.atlasId(), sprite.spriteId());
                case FontDescription.PlayerSprite player -> new PlayerSprite(player.profile(), player.hat());
                default -> throw new RuntimeException("welp, this should never have happened");
            })));
        
        CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                FONT_DESCRIPTION_CODEC
                    .fieldOf("font")
                    .forGetter(GuiFont::getDescriptor),
                Format.MAP_CODEC
                    .forGetter(font -> font.formats),
                Codec.either(Codec.FLOAT, FontSize.CODEC)
                    .fieldOf("size")
                    .forGetter(font -> Either.left(font.fontSize)),
                Codec.FLOAT
                    .fieldOf("tracking")
                    .forGetter(GuiFont::getTrackingFactor))
            .apply(instance, GuiFont::new));
    }
    
    //******************************************************************************************************************
    /// Finds a font from its descriptor of the font register.
    /// @param fontDesc The descriptor of the font
    /// @return An optional [GuiFont], or an empty optional if no font was found for that descriptor
    public static Optional<GuiFont> findFont(final FontDescription fontDesc)
    {
        return GuiFont.getGlyphSource(fontDesc).map(source -> new GuiFont(fontDesc, source));
    }
    
    /// Finds a font from its ID of the font register.
    /// @param fontId The ID of the font
    /// @return An optional [GuiFont], or an empty optional if no font was found for that descriptor
    public static Optional<GuiFont> findFont(final Identifier fontId)
    {
        final FontDescription desc = new FontDescription.Resource(fontId);
        return GuiFont.getGlyphSource(new FontDescription.Resource(fontId)).map(source -> new GuiFont(desc, source));
    }
    
    /// Gets the standard render height of text in Minecraft, this is the default size that font in Minecraft uses to
    /// render as text on screen. This is like [#getDefaultFontHeight()] but includes extra space for rendering the
    /// underline.
    /// @return [Font#lineHeight]
    public static int getDefaultRenderHeight() { return 9; }
    
    /// Gets the standard font height of text in Minecraft.
    /// @return [GuiFont#getDefaultRenderHeight()] - 1 (underline height)
    public static int getDefaultFontHeight() { return 8; }
    
    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("resource")
    private static Optional<GlyphSource> getGlyphSource(final FontDescription fontId)
    {
        final FontManagerAccessor manager = (FontManagerAccessor)((MinecraftClientAccessor) Minecraft.getInstance())
            .novia$getFontManager();
        final GlyphSource   missing  = manager.novia$getMissingFontSet().source(false);
        final Font.Provider provider = manager.novia$getFontProvider();
        final GlyphSource source = provider.glyphs(fontId);
        
        if (GuiFont.WHITE_GLYPH == null)
        {
            GuiFont.WHITE_GLYPH = manager.novia$getFontSet(Minecraft.DEFAULT_FONT).whiteGlyph();
        }
        
        return Optional.ofNullable(source != missing ? source : null);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static GuiFont getCachedDefault()
    {
        if (GuiFont.CACHED_DEFAULT == null)
        {
            GuiFont.CACHED_DEFAULT = GuiFont.findFont(Minecraft.DEFAULT_FONT).orElseThrow();
        }
        
        return new GuiFont(GuiFont.CACHED_DEFAULT);
    }
    
    private static GuiFont getCachedUnicode()
    {
        if (GuiFont.CACHED_UNICODE == null)
        {
            GuiFont.CACHED_UNICODE = GuiFont.findFont(Minecraft.UNIFORM_FONT).orElseThrow();
        }
        
        return new GuiFont(GuiFont.CACHED_UNICODE);
    }
    
    private static GuiFont getCachedAlt()
    {
        if (GuiFont.CACHED_ALT == null)
        {
            GuiFont.CACHED_ALT = GuiFont.findFont(Minecraft.ALT_FONT).orElseThrow();
        }
        
        return new GuiFont(GuiFont.CACHED_ALT);
    }
    
    private static GuiFont getCachedIllageralt()
    {
        if (GuiFont.CACHED_ILLAGERALT == null)
        {
            GuiFont.CACHED_ILLAGERALT = GuiFont.findFont(Identifier.withDefaultNamespace("illageralt")).orElseThrow();
        }
        
        return new GuiFont(GuiFont.CACHED_ILLAGERALT);
    }
    
    //******************************************************************************************************************
    private final BitSet          formats;
    private final GlyphSource     source;
    private final FontDescription descriptor;
    
    private float fontSize = 1f;
    private float tracking = 0f;
    
    //******************************************************************************************************************
    /// Creates a new font from the given font ID, with the given size and formatting. If a font could not be found by
    /// its ID, the default font will be used.
    /// @param descriptor The [FontDescription] of the font
    /// @param fontSize   The size of the font
    /// @param formats    The formatting to use
    public GuiFont(final FontDescription descriptor, final FontSize fontSize, final Format ...formats)
    {
        this(descriptor);
        this.fontSize = fontSize.getEmScale();
        this.addFormatting(formats);
    }
    
    /// Creates a new font with the default font.
    /// @param fontSize The size of the font
    /// @param formats  The formatting to use
    public GuiFont(final FontSize fontSize, final Format ...formats)
    {
        this();
        this.fontSize = fontSize.getEmScale();
        this.addFormatting(formats);
    }
    
    /// Creates a new font from the given font ID with the default size and no formatting. If a font could not be found
    /// by its ID, the default font will be used.
    /// @param fontDesc The [FontDescription] of the font
    public GuiFont(final FontDescription fontDesc)
    {
        final Optional<GlyphSource> attempt = GuiFont.getGlyphSource(fontDesc);
        final GlyphSource           source  = attempt.orElse(GuiFont.DEFAULT.get().source);
        final FontDescription       desc    = (attempt.isPresent()
            ? fontDesc
            : new FontDescription.Resource(Minecraft.DEFAULT_FONT));
        
        this.formats    = new BitSet(Format.values().length);
        this.source     = source;
        this.descriptor = desc;
    }
    
    /// Constructs a new copy of the given font object.
    /// @param other The [GuiFont] to copy from
    public GuiFont(final GuiFont other)
    {
        this(other.descriptor, other.source);
        this.formats.or(other.formats);
        this.fontSize = other.fontSize;
        this.tracking = other.tracking;
    }
    
    /// Constructs a new font from the given [Style]. If font ID is null or couldn't be found, the default font
    /// will be used.
    /// @param style The [Style]
    public GuiFont(final Style style)
    {
        this(style.getFont());
        this.setStyle(style);
    }
    
    /// Constructs a new font with the default font, the default size and no formatting.
    public GuiFont() { this(new FontDescription.Resource(Minecraft.DEFAULT_FONT)); }
    
    //------------------------------------------------------------------------------------------------------------------
    private GuiFont(final FontDescription descriptor, final GlyphSource source)
    {
        this.descriptor = descriptor;
        this.source     = source;
        this.formats    = new BitSet(Format.values().length);
    }
    
    private GuiFont(final FontDescription fontDesc, final BitSet formats, final Either<Float, FontSize> scale,
                    final float tracking)
    {
        this(fontDesc);
        
        this.formats.or(formats);
        this.fontSize = scale.map(Function.identity(), FontSize::getEmScale);
        this.tracking = tracking;
    }
    
    //==================================================================================================================
    /// Gets the descriptor of this font.
    /// @return The [FontDescription]
    public FontDescription getDescriptor() { return this.descriptor; }
    
    /// Gets the (possibly) cached glyph for the given code point. If no glyph could be found, it will return
    /// [SpecialGlyphs#MISSING] (an in-built glyph represented by a white square).
    /// @param codePoint The code point to look up the glyph for
    /// @return The [BakedGlyph]
    public BakedGlyph getGlyph(final int codePoint) { return this.source.getGlyph(codePoint); }
    
    /// Gets the (possibly) cached glyph for the given code point. If no glyph could be found, it will return
    /// an empty optional.
    /// @param codePoint The code point to look up the glyph for
    /// @return An [Optional] containing the [BakedGlyph], or empty if no glyph was found
    public Optional<BakedGlyph> getOptionalGlyph(final int codePoint)
    {
        final BakedGlyph glyph = this.getGlyph(codePoint);
        return Optional.ofNullable(glyph.info() != SpecialGlyphs.MISSING ? glyph : null);
    }
    
    /// {@return the bitmask for the formats currently being used by this font}
    public byte getFormatFlags() { return this.formats.toByteArray()[0]; }
    
    /// Gets the obfuscated version of a given glyph.
    /// @param random The [RandomSource]
    /// @param width  The width
    /// @return The obfuscated [BakedGlyph]
    public BakedGlyph getRandomGlyph(final RandomSource random, int width)
    {
        return this.source.getRandomGlyph(random, width);
    }
    
    /// Gets the baked rectangle glyph used to draw the lines for things like underlined or strike-through texts.
    /// @return The rectangle [BakedGlyph]
    public EffectGlyph getWhiteGlyph() { return Objects.requireNonNull(GuiFont.WHITE_GLYPH); }
    
    /// Gets the current size for this font relative to [#getDefaultRenderHeight()].
    /// @param unit The unit to convert the size to
    /// @return The font size
    public float getSize(final FontSize.Unit unit) { return unit.fromEm(this.fontSize); }
    
    /// Gets the current size for this font relative to [#getDefaultRenderHeight()] in em unit
    /// (where 1f means default render height).
    /// This is different to [#getHeight()].
    /// @return The font size
    public float getSizeEm() { return this.fontSize; }
    
    /// Gets this font's current tracking space, which is the tracking factor times the font's height.
    /// @return The tracking space
    public float getTrackingSpace() { return (this.getHeight() * this.tracking); }
    
    /// Gets this font's tracking factor, where 0 means default and anything above is added space
    /// (see [#getTrackingFactor()]).
    /// @return The tracking factor
    public float getTrackingFactor() { return this.tracking; }
    
    /// Gets the scaled height of what size is needed to fully display the entire height of a text glyph including its
    /// underline.
    /// @return The scaled height of the font
    public float getHeight()
    {
        final FontMetrics metrics = ((FontSetAccessor) this.source).novia$getMetrics();
        return (metrics.height() * this.fontSize + this.fontSize);
    }
    
    /// Gets the scaled ascent of the font, this is the distance between the top of the font down to the baseline.
    /// For fonts with multiple providers, this will be the maximum scaled ascent of all providers.
    /// @return The scaled ascent of the font
    public float getAscent()
    {
        final FontMetrics metrics = ((FontSetAccessor) this.source).novia$getMetrics();
        return (metrics.ascent() * this.fontSize);
    }
    
    /// Gets the scaled descent of the font, this is the distance between the bottom of the font up to the baseline.
    /// For fonts with multiple providers, this will be the maximum scaled descent of all providers.
    /// @return The scaled descent of the font
    public float getDescent()
    {
        final FontMetrics metrics = ((FontSetAccessor) this.source).novia$getMetrics();
        return (metrics.descent() * this.fontSize + this.fontSize);
    }
    
    /// Gets the width of the code point with the current font (ignoring tracking).
    /// @param codePoint The code point of the character to measure
    /// @return The exact width of the character
    public float getCharWidth(final int codePoint)
    {
        final GlyphInfo info = this.getGlyph(codePoint).info();
        return (
            (((GlyphMetricsExtension) info).novia$getLogicalAdvance() * this.fontSize)
            + (this.isBold() ? (info.getBoldOffset() * this.fontSize) : 0f)
        );
    }
    
    /// Gets the width of the code point with the current font, but rounded up so the char fits perfectly.
    /// @param codePoint The code point of the character to measure
    /// @return The adjusted width of the character
    public int getCharWidthFitted(final int codePoint) { return Mth.ceil(this.getCharWidth(codePoint)); }
    
    /// Gets the width of the text with the current font. Formatting codes will not be consumed and considered part
    /// of the text, if this is not the desired behaviour use [TextLayout] instead.
    /// @param text The text to measure
    /// @return The exact width of the text
    public float getTextWidth(final String text)
    {
        final TextLayout.WidthCollectingVisitor visitor = new TextLayout.WidthCollectingVisitor();
        visitor.visit(this, text);
        return visitor.getWidth();
    }
    
    /// Gets the width of the text with the current font, but rounded up so all text fits perfectly.
    /// Formatting codes will not be consumed and considered part of the text, if this is not the desired behaviour
    /// use [TextLayout] instead.
    /// @param text The text to measure
    /// @return The fitted width of the text
    public int getTextWidthFitted(final String text) { return Mth.ceil(this.getTextWidth(text)); }
    
    /// Gets the string length of the text given this font and the specified width maximum. Formatting codes will not be
    /// consumed and considered part of the text, if this is not the desired behaviour use [TextLayout] instead.
    /// @param text     The text to get the length of
    /// @param maxWidth The maximum width the text should have
    /// @return The length of the text (UTF code-points will be counted as one)
    public int getTextLength(final String text, final int maxWidth)
    {
        final TextLayout.WidthLimitingVisitor visitor = new TextLayout.WidthLimitingVisitor(maxWidth);
        visitor.visit(this, text);
        return visitor.getLength();
    }
    
    //==================================================================================================================
    public String trimToWidth(final String text, final int maxWidth)
    {
        return text.substring(0, this.getTextLength(text, maxWidth));
    }
    
    public String trimToWidthBackwards(final String text, final int maxWidth)
    {
        final MutableFloat total    = new MutableFloat();
        final MutableInt   end      = new MutableInt(text.length());
        final float        tracking = this.getTrackingSpace();
        
        ICharacterVisitor
            .of((i, _, _, codePoint) ->
            {
                final float width = total.addAndGet(this.getCharWidth(codePoint) + tracking);
    
                if (width > ((float) maxWidth))
                {
                    return false;
                }
    
                end.setValue(i);
                return true;
            })
            .visitBackwards(this, text);

        return text.substring(end.intValue());
    }
    
    //==================================================================================================================
    /// Gets this font as [GuiStyle] object with all the formatting data converted.
    /// @param ignoreFontId If `true`, the font ID will not be applied to the style
    /// @return The new [GuiStyle]
    public GuiStyle asStyle(final boolean ignoreFontId)
    {
        return new GuiStyle(null, null, this.isBold(), this.isItalic(), this.isUnderlined(), this.isStrikethrough(),
                            this.isObfuscated(), null, null, null, (ignoreFontId ? null : this.getDescriptor()),
                            this.isShaded(), this.fontSize, this.tracking, null);
    }
    
    /// Gets this font as [GuiStyle] object with all the formatting data converted.
    /// @return The new [GuiStyle]
    public GuiStyle asStyle() { return this.asStyle(false); }
    
    //==================================================================================================================
    /// {@return whether the font is bold}
    public boolean isBold() { return this.formats.get(Format.BOLD.ordinal()); }
    
    /// {@return whether the font is italic}
    public boolean isItalic() { return this.formats.get(Format.ITALIC.ordinal()); }
    
    /// {@return whether the font is underlined}
    public boolean isUnderlined() { return this.formats.get(Format.UNDERLINED.ordinal()); }
    
    /// {@return whether the font is strikethrough}
    public boolean isStrikethrough() { return this.formats.get(Format.STRIKETHROUGH.ordinal()); }
    
    /// {@return whether the font is obfuscated}
    public boolean isObfuscated() { return this.formats.get(Format.OBFUSCATED.ordinal()); }
    
    /// {@return whether the text draws a shadow beneath}
    public boolean isShaded() { return this.formats.get(Format.SHADED.ordinal()); }
    
    /// {@return `true` if no formatting flag was set}
    public boolean isPlain() { return this.formats.isEmpty(); }
    
    /// {@return `true` if no formatting flag was set, and if tracking and font size are at their defaults}
    public boolean isDefaultFormatted() { return (this.isPlain() && this.fontSize == 1f && this.tracking == 0f); }
    
    //==================================================================================================================
    /// Copies the formatting of this font and creates a new font object with the given font descriptor to use instead.
    /// If the given font ID does not exist, [#DEFAULT] will be applied instead.
    /// @param fontDesc The [FontDescription] of the font to use
    /// @return The new [GuiStyle]
    public GuiFont withFont(final FontDescription fontDesc)
    {
        if (fontDesc.equals(this.getDescriptor()))
        {
            return new GuiFont(this);
        }
        
        final GuiFont font = new GuiFont(fontDesc);
        font.formats.or(this.formats);
        font.fontSize = this.fontSize;
        font.tracking = this.tracking;
        
        return font;
    }
    
    /// Copies this font and specified whether it should be bold or not.
    /// @param bold `true` if the font should be bold
    /// @return The new font
    public GuiFont withBold(final boolean bold)
    {
        final GuiFont font = new GuiFont(this);
        font.setBold(bold);
        return font;
    }
    
    /// Copies this font and specified whether it should be italicised or not.
    /// @param italic `true` if the font should be italic
    /// @return The new font
    public GuiFont withItalicisation(final boolean italic)
    {
        final GuiFont font = new GuiFont(this);
        font.setItalic(italic);
        return font;
    }
    
    /// Copies this font and specified whether it should be underlined or not.
    /// @param underlined `true` if the font should be underlined
    /// @return The new font
    public GuiFont withUnderline(final boolean underlined)
    {
        final GuiFont font = new GuiFont(this);
        font.setUnderlined(underlined);
        return font;
    }
    
    /// Copies this font and specified whether it should be strikethrough or not.
    /// @param strikethrough `true` if the font should be strikethrough
    /// @return The new font
    public GuiFont withStrikethrough(final boolean strikethrough)
    {
        final GuiFont font = new GuiFont(this);
        font.setStrikethrough(strikethrough);
        return font;
    }
    
    /// Copies this font and specified whether it should be obfuscated or not.
    /// @param obfuscated `true` if the font should be obfuscated
    /// @return The new font
    public GuiFont withObfuscation(final boolean obfuscated)
    {
        final GuiFont font = new GuiFont(this);
        font.setObfuscated(obfuscated);
        return font;
    }
    
    /// Copies this font and specifies whether it should be shaded or not.
    /// @param shaded `true` if the font should be shaded
    /// @return The new font
    public GuiFont withShadow(final boolean shaded)
    {
        final GuiFont font = new GuiFont(this);
        font.setShaded(shaded);
        return font;
    }
    
    /// Copies this font and sets the returned font's size.
    /// @param em The size of the font in em unit
    /// @return The new font
    public GuiFont withSize(final float em)
    {
        final GuiFont font = new GuiFont(this);
        font.setSize(em);
        return font;
    }
    
    /// Copies this font and sets the returned font's size.
    /// @param size The [FontSize] size of the font
    /// @return The new font
    public GuiFont withSize(final FontSize size)
    {
        final GuiFont font = new GuiFont(this);
        font.setSize(size);
        return font;
    }
    
    /// Copies this font and sets the returned font's tracking factor.
    /// @param tracking The tracking factor
    /// @return The new font
    public GuiFont withTrackingFactor(final float tracking)
    {
        final GuiFont font = new GuiFont(this);
        font.setTrackingFactor(tracking);
        return font;
    }
    
    /// Copies this font and sets the flags to the given `formats`.
    /// @param formats The set of [Format] flags to apply to the font
    /// @return The new font
    public GuiFont withFormatting(final Format ...formats)
    {
        final GuiFont font = new GuiFont(this);
        font.setFormatting(formats);
        return font;
    }
    
    /// Copies this font and sets the flags to the given `formats`.
    ///
    /// This is similar to [#withFormatting(Format...)], with the difference that any pre-existing flags won't be
    /// cleared and instead appended to.
    /// @param formats The set of [Format] flags to apply to the font
    /// @return The new font
    public GuiFont withFormattingPreserved(final Format ...formats)
    {
        final GuiFont font = new GuiFont(this);
        font.addFormatting(formats);
        return font;
    }
    
    /// Copies this font and sets all format flags off.
    /// @return The new plain font
    public GuiFont withPlain()
    {
        final GuiFont font = new GuiFont(this);
        font.clearFormatting();
        return font;
    }
    
    //==================================================================================================================
    /// Sets the font to be bold.
    /// @param bold `true` if the font should be bold
    public void setBold(final boolean bold) { this.setFormat(Format.BOLD, bold); }
    
    /// Sets the font to be italic.
    /// @param italic `true` if the font should be italic
    public void setItalic(final boolean italic) { this.setFormat(Format.ITALIC, italic); }
    
    /// Sets the font to be underlined.
    /// @param underlined `true` if the font should be underlined
    public void setUnderlined(final boolean underlined) { this.setFormat(Format.UNDERLINED, underlined); }
    
    /// Sets the font to be strikethrough.
    /// @param strikethrough `true` if the font should be strikethrough
    public void setStrikethrough(final boolean strikethrough) { this.setFormat(Format.STRIKETHROUGH, strikethrough); }
    
    /// Sets the font to be obfuscated.
    /// @param obfuscated `true` if the font should be obfuscated
    public void setObfuscated(final boolean obfuscated) { this.setFormat(Format.OBFUSCATED, obfuscated); }
    
    /// Sets the font to be shaded.
    /// @param shaded `true` if the font should be shaded
    public void setShaded(final boolean shaded) { this.setFormat(Format.SHADED, shaded); }
    
    /// Sets a specific [Format] flag for this font.
    /// @param format  The [Format] flag to enable/disable
    /// @param enabled `true` if the flag should be enabled, otherwise `false`
    public void setFormat(final Format format, final boolean enabled)
    {
        this.formats.set(format.ordinal(), enabled);
    }
    
    /// Sets a set of [Format] flags to be enabled for this font; all flags that are not specified will be disabled.
    /// @param formats The set of [Format] flags to enable
    public void setFormatting(final Format ...formats)
    {
        this.clearFormatting();
        this.addFormatting(formats);
    }
    
    /// Adds a set of [Format] flags that should be enabled for this font; additionally to the current format.
    /// @param formats The set of [Format] flags to enable
    public void addFormatting(final Format ...formats)
    {
        for (final var format : formats)
        {
            this.formats.set(format.ordinal(), true);
        }
    }
    
    /// Sets the font's scale override. The scale is applied relative to [#getDefaultFontHeight()], by that means, 1em
    /// is considered 9 pixels so 2em means 18 pixels and so on.
    /// @param size The size of the font
    /// @see FontSize
    public void setSize(final FontSize size) { this.fontSize = size.getEmScale(); }
    
    /// Sets the font size in em scale. The scale is applied relative to [#getDefaultFontHeight()],
    /// by that means, 1em is considered 9 pixels so 2em means 18 pixels and so on.
    /// @param em The size of the font
    public void setSize(final float em) { this.fontSize = em; }
    
    /// Sets this font's tracking factor.
    ///
    /// The tracking factor is additional space that is added between glyphs uniformly. By default, this is 0,
    /// which means no extra space is added, and anything above will be extra added space (see [#getTrackingSpace()]).
    ///
    /// Do not that this is less kerning than font tracking, which applies the spacing uniformly across the characters.
    /// @param trackingFactor The tracking factor
    public void setTrackingFactor(final float trackingFactor) { this.tracking = trackingFactor; }
    
    /// Sets the formatting of this font to the given [Style] or [GuiStyle]. Font ID will be ignored, use
    /// [GuiFont#GuiFont(Style)] instead.
    ///
    /// When applying a style to a font, it is worth noting that styles that do not explicitly set a formatting
    /// attribute, will, for those values, not override the font. By that means, if an attribute of a style is
    /// internally `null`, this font will retain its current value for that style attribute,
    /// otherwise it will be overridden regardless of its value.
    /// @param style The [Style] to apply
    public void setStyle(final Style style)
    {
        if (style.isEmpty())
        {
            return;
        }
        
        final StyleAccessor accessor = (StyleAccessor) style;
        
        final Boolean bold          = accessor.novia$isBold();
        final Boolean italic        = accessor.novia$isItalic();
        final Boolean underline     = accessor.novia$isUnderlined();
        final Boolean strikethrough = accessor.novia$isStrikethrough();
        final Boolean obfuscated    = accessor.novia$isObfuscated();
        
        if (bold          != null) this.setBold         (bold);
        if (italic        != null) this.setItalic       (italic);
        if (underline     != null) this.setUnderlined   (underline);
        if (strikethrough != null) this.setStrikethrough(strikethrough);
        if (obfuscated    != null) this.setObfuscated   (obfuscated);
        
        if (style instanceof GuiStyle g_style)
        {
            if (g_style.shaded   != null) this.setShaded        (g_style.shaded);
            if (g_style.size     != null) this.setSize          (g_style.size);
            if (g_style.tracking != null) this.setTrackingFactor(g_style.tracking);
        }
    }
    
    /// Clears all format flags and makes this a plain font.
    public void clearFormatting() { this.formats.clear(); }
    
    //==================================================================================================================
    @Override
    public String toString()
    {
        return MoreObjects
            .toStringHelper(this)
            .add("descriptor",    this.getDescriptor())
            .add("bold",          this.isBold())
            .add("italic",        this.isItalic())
            .add("underline",     this.isUnderlined())
            .add("strikethrough", this.isStrikethrough())
            .add("obfuscated",    this.isObfuscated())
            .add("shaded",        this.isShaded())
            .add("size",          this.getSizeEm())
            .add("tracking",      this.getTrackingSpace())
            .toString();
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                     return true;
        if (!(obj instanceof GuiFont other)) return false;
        
        return (
            this.descriptor.equals(other.descriptor)
            && this.formats.equals(other.formats)
            && this.fontSize == other.fontSize
            && this.tracking == other.tracking
        );
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.descriptor, this.formats, this.fontSize, this.tracking);
    }
}
