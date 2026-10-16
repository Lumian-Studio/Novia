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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import xyz.lumian.novia.api.core.network.codec.NoviaStreamCodecs;

import java.util.*;



//**********************************************************************************************************************
/// Provides a conversion structure that calculates EM size (relative to [GuiFont#getDefaultRenderHeight()]) based on the
/// given unit and value.
///
/// Consumers of this class will then internally convert the EM size to logical pixels.
/// @param unit  The unit to use to convert to the EM scale
/// @param value The value to convert
public record FontSize(FontSize.Unit unit, float value)
{
    //******************************************************************************************************************
    /// Represents the conversion algorithm to use.
    public enum Unit
        implements StringRepresentable
    {
        //**************************************************************************************************************
        /// Specifies the font's scale in logical pixels.
        PIXEL("px", (1f / GuiFont.getDefaultRenderHeight())),
        
        /// Specifies the font's scale in "fixed points" (ignoring the screen's resolution),
        /// where 1 logical pixel is 6.75pt.
        POINT("pt", (1f / (GuiFont.getDefaultRenderHeight() * 0.75f))),
        
        /// Specifies the font's size in a relative font scale (e.g. 1em: 9 logical pixels, 2em: 18 logical pixels).
        EM("em", 1f),
        ;
        
        //**************************************************************************************************************
        /// Describes the codec for this enum, where the name is the case-insensitive CSS unit literal.
        public static final Codec<Unit> CODEC = StringRepresentable
            .fromEnumWithMapping(Unit::values, String::toLowerCase);
        
        //**************************************************************************************************************
        /// The multiplier used for the scale conversion.
        public final float scaleFactor;
        
        /// The literal used after for this unit, after the number in a style string.
        public final String literal;
        
        //**************************************************************************************************************
        Unit(final String literal, final float scaleFactor)
        {
            this.literal     = Objects.requireNonNull(literal);
            this.scaleFactor = scaleFactor;
        }
        
        //==============================================================================================================
        /// Convert the size value to em scale.
        /// @param sizeValue The value to convert to the em scale
        public float toEm(final float sizeValue) { return (this.scaleFactor * sizeValue); }
        
        /// Convert the size value from the em scale to this scale.
        /// @param emValue The value to convert from the em scale
        public float fromEm(final float emValue) { return (emValue / this.scaleFactor); }
        
        //==============================================================================================================
        @Override public String getSerializedName() { return this.literal; }
    }
    
    //******************************************************************************************************************
    /// The pattern used to parse CSS-inspired font size style strings.
    /// - Pixels: `<number>px`
    /// - Points: `<number>pt`
    /// - Relative scale: `<number>em`
    @Language("RegExp")
    public static final String FONT_SIZE_PATTERN = "^([+-]?(?:[0-9]*[.])?[0-9]+)(px|pt|em)$";
    
    /// The standard Minecraft font size.
    public static final FontSize STANDARD = new FontSize(Unit.EM, 1f);
    
    /// The map codec for this record. It provides the following properties:
    /// * **unit** The conversion unit (px, pt or em)
    /// * **size** The size value
    public static final MapCodec<FontSize> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance
            .group(
                Unit.CODEC
                    .optionalFieldOf("unit", Unit.EM)
                    .forGetter(FontSize::unit),
                Codec.FLOAT
                    .fieldOf("size")
                    .forGetter(FontSize::value))
            .apply(instance, FontSize::new));
    
    /// The codec for this record.
    /// ```json
    /// {
    ///     "unit": <"em"|"px"|"pt">,
    ///     "size": <value>
    /// }
    /// ```
    public static final Codec<FontSize> CODEC = FontSize.MAP_CODEC.codec();
    
    /// The packet codec for this record.
    public static final StreamCodec<FriendlyByteBuf, FontSize> PACKET_CODEC = StreamCodec.composite(
        NoviaStreamCodecs.enumeration(Unit.class), FontSize::unit,
        ByteBufCodecs.FLOAT,                       FontSize::value,
        FontSize::new);
    
    //******************************************************************************************************************
    /// Returns a font size object in logical pixels.
    /// @param value The value in logical pixels
    /// @return The [FontSize]
    /// @see Unit#PIXEL
    public static FontSize pixels(final float value) { return new FontSize(Unit.PIXEL, value); }
    
    /// Returns a font size object in points.
    /// @param value The value in points
    /// @return The [FontSize]
    /// @see Unit#POINT
    public static FontSize points(final float value) { return new FontSize(Unit.POINT, value); }
    
    /// Returns a font size object in relative em unit.
    /// @param value The value in em scale
    /// @return The [FontSize]
    /// @see Unit#EM
    public static FontSize em(final float value) { return new FontSize(Unit.EM, value); }
    
    /// Creates a [FontSize] from the given style string, which is a number and the unit literal.
    /// @param value The style string (e.g. `1px` or `1pt`)
    /// @return A new [FontSize]
    /// @throws IllegalArgumentException If the `value` string was in an invalid format (see [#FONT_SIZE_PATTERN])
    public static FontSize fromString(final @Pattern(FontSize.FONT_SIZE_PATTERN) String value)
    {
        if (!value.matches(FONT_SIZE_PATTERN))
        {
            throw new IllegalArgumentException(value + " is not a valid number or size unit (px, pt, em)");
        }
        
        return new FontSize(
            Arrays
                .stream(Unit.values())
                .filter(e -> e.literal.equalsIgnoreCase(value.substring((value.length() - 2))))
                .findFirst()
                .orElseThrow(),
            Float.parseFloat(value.substring(0, (value.length() - 2))));
    }
    
    //******************************************************************************************************************
    /// Converts [FontSize#value()] to em scale (via [FontSize#unit()]).
    /// @return The converted value
    public float getEmScale() { return this.unit.toEm(this.value); }
}
