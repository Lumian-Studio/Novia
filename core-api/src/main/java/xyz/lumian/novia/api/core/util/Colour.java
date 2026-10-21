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
package xyz.lumian.novia.api.core.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



//**********************************************************************************************************************
/// A utility class for storing and manipulating ARGB colour values.
/// @param colour The 4 component ARGB colour value (0xAARRGGBB)
public record Colour(int colour)
    implements Comparable<Integer>
{
    //******************************************************************************************************************
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Named
    {
        String value() default "";
    }

    //******************************************************************************************************************
    /// Ordinary red <span style="color:#FF0000">██</span>
    @Named public static final Colour RED = new Colour(0xFFFF0000);
    
    /// Ordinary green <span style="color:#00FF00">██</span>
    @Named public static final Colour GREEN = new Colour(0xFF00FF00);
    
    /// Ordinary blue <span style="color:#0000FF">██</span>
    @Named public static final Colour BLUE = new Colour(0xFF0000FF);
    
    /// Ordinary yellow <span style="color:#FFFF00">██</span>
    @Named public static final Colour YELLOW = new Colour(0xFFFFFF00);
    
    /// Ordinary purple <span style="color:#800080">██</span>
    @Named public static final Colour PURPLE = new Colour(0xFF800080);
    
    /// Ordinary orange <span style="color:#FF8000">██</span>
    @Named public static final Colour ORANGE = new Colour(0xFFFF8000);
    
    /// Ordinary brown <span style="color:#804000">██</span>
    @Named public static final Colour BROWN = new Colour(0xFF804000);
    
    /// Ordinary pink <span style="color:#FFC0CB">██</span>
    @Named public static final Colour PINK = new Colour(0xFFFFC0CB);
    
    /// Ordinary violet <span style="color:#EE82EE">██</span>
    @Named public static final Colour VIOLET = new Colour(0xFFEE82EE);
    
    /// A vibrant greenish-blue colour <span style="color:#00FFFF">██</span>
    @Named public static final Colour CYAN = new Colour(0xFF00FFFF);
    
    /// A bright reddish-purple colour <span style="color:#FF00FF">██</span>
    @Named public static final Colour MAGENTA = new Colour(0xFFFF00FF);
    
    /// A medium grey hue <span style="color:#808080">██</span>
    @Named public static final Colour GREY = new Colour(0xFF808080);
    
    /// A bright pale violet colour <span style="color:#E6E6FA">██</span>
    @Named public static final Colour LAVENDER = new Colour(0xFFE6E6FA);
    
    /// A soft light shade of pink <span style="color:#A47DAB">██</span>
    @Named public static final Colour LILAC = new Colour(0xFFA47DAB);
    
    /// A bright pale brown colour <span style="color:#D2B48C">██</span>
    @Named public static final Colour TAN = new Colour(0xFFD2B48C);
    
    /// A medium grey hue <span style="color:#C0C0C0">██</span>
    @Named public static final Colour SILVER = new Colour(0xFFC0C0C0);
    
    /// A luxurious warm bright yellow <span style="color:#FFD700">██</span>
    @Named public static final Colour GOLD = new Colour(0xFFFFD700);
    
    /// A bright light greenish-blue colour <span style="color:#40E0D0">██</span>
    @Named public static final Colour TURQUOISE = new Colour(0xFF40E0D0);
    
    /// A dark shade of red <span style="color:#800000">██</span>
    @Named public static final Colour MAROON = new Colour(0xFF800000);
    
    /// A deep dark shade of blue <span style="color:#000080">██</span>
    @Named public static final Colour NAVY_BLUE = new Colour(0xFF000080);
    
    /// A pale brown colour <span style="color:#F5F5DC">██</span>
    @Named public static final Colour BEIGE = new Colour(0xFFF5F5DC);
    
    /// A light yellowish-orange colour <span style="color:#FFDAB9">██</span>
    @Named public static final Colour PEACH = new Colour(0xFFFFDAB9);
    
    /// A bright vivid green <span style="color:#32CD32">██</span>
    @Named public static final Colour LIME = new Colour(0xFF32CD32);
    
    /// A dark pale green <span style="color:#808000">██</span>
    @Named public static final Colour OLIVE = new Colour(0xFF808000);
    
    /// A vibrant greenish-blue colour <span style="color:#008080">██</span>
    @Named public static final Colour TEAL = new Colour(0xFF008080);
    
    /// A rich dark violet colour <span style="color:#4B0082">██</span>
    @Named public static final Colour INDIGO = new Colour(0xFF4B0082);
    
    /// A vibrant greenish-yellow colour <span style="color:#7FFF00">██</span>
    @Named public static final Colour CHARTREUSE = new Colour(0xFF7FFF00);
    
    /// White <span style="color:#FFFFFF">██</span>
    @Named public static final Colour WHITE = new Colour(0xFFFFFFFF);
    
    /// Black <span style="color:#000000">&#x2588;&#x2588;</span>
    @Named public static final Colour BLACK = new Colour(0xFF000000);
    
    /// No colour.
    @Named public static final Colour TRANSPARENT = new Colour(0);
    
    //==================================================================================================================
    public static final StreamCodec<ByteBuf, Colour> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, Colour::colour,
        Colour::new);

    /// Codec for hex string colour values. (#AARRGGBB)
    public static final Codec<Colour> HEX_CODEC;

    /// Codec for colour name strings (see this class' constants for names).
    public static final Codec<Colour> NAME_CODEC;

    /// Codec for integer colour values.
    public static final Codec<Colour> INT_CODEC;

    /// Codec for dynamic properties, either hex strings, name strings or integers.
    /// @see #HEX_CODEC
    /// @see #NAME_CODEC
    /// @see #INT_CODEC
    public static final Codec<Colour> CODEC;

    //------------------------------------------------------------------------------------------------------------------
    private static final Map<String, Colour> NAMED;

    //==================================================================================================================
    static
    {
        NAMED = Arrays
            .stream(Colour.class.getDeclaredFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Named.class))
            .map(field ->
            {
                final Named ann = field.getDeclaredAnnotation(Named.class);

                if (ann != null)
                {
                    final String name = (!ann.value().isEmpty() ? ann.value() : field.getName()).toLowerCase();

                    try
                    {
                        return Pair.of(name, (Colour) field.get(null));
                    }
                    catch (final IllegalAccessException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }

                return Pair.of((String) null, (Colour) null);
            })
            .filter(p -> p.getFirst() != null)
            .collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));

        HEX_CODEC = Codec.STRING.comapFlatMap(
            (str ->
            {
                try
                {
                    return DataResult.success(Colour.fromHexString(str));
                }
                catch (final NumberFormatException ex)
                {
                    return DataResult.error(ex::getMessage);
                }
            }),
            Colour::toString);

        NAME_CODEC = Codec.STRING.flatXmap(
            (str -> Optional
                .ofNullable(NAMED.get(str.toLowerCase()))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "no colour for name " + str))),
            (col -> NAMED
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(col))
                .map(Map.Entry::getKey)
                .findFirst()
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "colour value does not have a name"))));

        INT_CODEC = Codec.INT.xmap(Colour::new, Colour::colour);

        CODEC = Codec.withAlternative(INT_CODEC, Codec.withAlternative(NAME_CODEC, HEX_CODEC));
    }

    //******************************************************************************************************************
    /// Converts a hexadecimal colour string into a [Colour] object.
    /// The colour can either be in 6 figure "#RRGGBB" format or in 2 figure "#CC" channel code.
    /// @param str The hex string
    /// @return The new [Colour]
    /// @throws NumberFormatException If the hex code was invalids
    public static Colour fromHexString(final String str)
    {
        final Matcher m = Pattern.compile("^#([0-9a-fA-F]{6})([0-9a-fA-F]{2})?$").matcher(str);
        
        if (!m.matches())
        {
            throw new NumberFormatException("Invalid hex/rgb(a) colour '" + str + "'");
        }
        
        final int colour = (
            Integer.parseUnsignedInt(m.group(1), 16)
                | (m.group(2) != null
                   ? (Integer.parseUnsignedInt(m.group(2), 16) << 24)
                   : 0xFF000000));
        return new Colour(colour);
    }
    
    /// Creates a new [Colour] object from the given rgb integer with a full alpha channel.
    /// To be able to define the alpha channel, the constructor of [Colour] should be used instead.
    /// @param rgb The rgb value
    /// @return The new [Colour]
    public static Colour fromRgb(final int rgb) { return new Colour(0xFF000000 | rgb); }
    
    //==================================================================================================================
    /// Gets only the alpha channel of a colour.
    /// @param colour The ARGB value
    /// @return The alpha channel value
    public static int getAlphaChannel(final int colour) { return (colour >> 24 & 0xff); }
    
    /// Gets only the red channel of a colour.
    /// @param colour The ARGB value
    /// @return The red channel value
    public static int getRedChannel(final int colour) { return (colour >> 16 & 0xff); }
    
    /// Gets only the green channel of a colour.
    /// @param colour The ARGB value
    /// @return The green channel value
    public static int getGreenChannel(final int colour) { return (colour >> 8 & 0xff); }
    
    /// Gets only the blue channel of a colour.
    /// @param colour The ARGB value
    /// @return The blue channel value
    public static int getBlueChannel(final int colour) { return (colour & 0xff); }
    
    /// Gets the colour's relative luminance value as described here:
    /// [W3C Relative Luminance](https://www.w3.org/WAI/GL/wiki/Relative_luminance)
    /// @param colour The colour value
    /// @return The colour's relative luminance
    public static float calculateLuminance(final int colour)
    {
        final double RsRGB = (Colour.getRedChannel  (colour) / 255.0d);
        final double GsRGB = (Colour.getGreenChannel(colour) / 255.0d);
        final double BsRGB = (Colour.getBlueChannel (colour) / 255.0d);
        final double R     = (RsRGB <= 0.04045 ? (RsRGB / 12.92) : (Math.pow(((RsRGB + 0.055) / 1.055), 2.4)));
        final double G     = (GsRGB <= 0.04045 ? (GsRGB / 12.92) : (Math.pow(((GsRGB + 0.055) / 1.055), 2.4)));
        final double B     = (BsRGB <= 0.04045 ? (BsRGB / 12.92) : (Math.pow(((BsRGB + 0.055) / 1.055), 2.4)));
        
        return (float)((0.2126f * R) + (0.7152f * G) + (0.0722f * B));
    }
    
    //******************************************************************************************************************
    /// Creates a new [Colour] instance from the given channels.
    /// @param red   The red channel
    /// @param green The green channel
    /// @param blue  The blue channel
    public Colour(final int red, final int green, final int blue) { this(red, green, blue, 0xff); }
    
    /// Creates a new [Colour] instance from the given channels.
    /// @param red   The red channel
    /// @param green The green channel
    /// @param blue  The blue channel
    /// @param alpha The alpha channel
    public Colour(final int red, final int green, final int blue, final int alpha)
    {
        this((alpha & 0xff << 24) | (red & 0xff << 16) | (green & 0xff << 8) | (blue & 0xff));
    }
    
    //==================================================================================================================
    /// Gets the colour's relative luminance value. (see [Colour#calculateLuminance(int)])
    /// @return The colour's relative luminance
    /// @see Colour#calculateLuminance(int)
    public float getLuminance() { return Colour.calculateLuminance(this.colour); }

    /// Gets the best fitting contrasting colour of the current colour, which will be either black or white.
    /// @return The contrasting [Colour]
    public Colour getContrasting() { return this.getContrasting(Colour.WHITE, Colour.BLACK); }

    /// Gets the best fitting contrasting colour of the two colours given.
    /// @param colour1 The first option
    /// @param colour2 The second option
    /// @return The better fitting of the two colours in terms of contrast
    public Colour getContrasting(final int colour1, final int colour2)
    {
        final float L1 = Colour.calculateLuminance(colour1);
        final float L2 = Colour.calculateLuminance(colour2);
        final float L3 = this.getLuminance();

        if (L1 > L2)
        {
            return new Colour((((L3 + 0.05f) / (L2 + 0.05f)) > ((L1 + 0.05f) / (L3 + 0.05f))) ? colour2 : colour1);
        }
        else
        {
            return new Colour((((L3 + 0.05f) / (L1 + 0.05f)) > ((L2 + 0.05f) / (L3 + 0.05f))) ? colour1 : colour2);
        }
    }

    /// Gets the best fitting contrasting colour of the two colours given.
    /// @param colour1 The first option
    /// @param colour2 The second option
    /// @return The better fitting of the two colours in terms of contrast
    public Colour getContrasting(final Colour colour1, final Colour colour2)
    {
        return this.getContrasting(colour1.colour, colour2.colour);
    }
    
    /// Inverts the values of the channels.
    /// @return The inverted [Colour]
    public Colour getInverted() { return new Colour(this.colour ^ 0x00FFFFFF); }
    
    //==================================================================================================================
    /// {@return the alpha channel}
    public int alpha() { return Colour.getAlphaChannel(this.colour); }
    
    /// {@return the red channel}
    public int red() { return Colour.getRedChannel(this.colour); }
    
    /// {@return the green channel}
    public int green() { return Colour.getGreenChannel(this.colour); }
    
    /// {@return the blue channel}
    public int blue() { return Colour.getBlueChannel(this.colour); }
    
    /// {@return the alpha channel between 0f and 1f}
    public float alphaNormalised() { return (this.alpha() / 255F); }
    
    /// {@return the red channel between 0f and 1f}
    public float redNormalised() { return (this.red() / 255F); }
    
    /// {@return the green channel between 0f and 1f}
    public float greenNormalised() { return (this.green() / 255F); }
    
    /// {@return the blue channel between 0f and 1f}
    public float blueNormalised() { return (this.blue() / 255F); }
    
    //==================================================================================================================
    /// A new [Colour] instance with the given alpha channel.
    /// @param alpha The new alpha value
    /// @return The new [Colour]
    public Colour withAlpha(final int alpha) { return new Colour((this.colour & 0x00FFFFFF) | (alpha << 24)); }
    
    /// Mixes the alpha channel of this colour and another value.
    /// @param other The other colour value, where all channels but the alpha channel are ignored
    /// @return The new mixed [Colour]
    public Colour withAlphaMixed(final int other)
    {
        final float base_alpha   = ARGB.alphaFloat(other);
        final float target_alpha = this.alpha();
        
        return new Colour(base_alpha != 1.0f
            ? ARGB.color(ARGB.as8BitChannel(base_alpha * target_alpha), this.colour)
            : this.colour);
    }
    
    /// Mixes the alpha channel of this colour and another value.
    /// @param other The other [Colour], where all channels but the alpha channel are ignored
    /// @return The new mixed [Colour]
    public Colour withAlphaMixed(final Colour other) { return this.withAlphaMixed(other.colour); }
    
    /// A new [Colour] instance with the given red channel.
    /// @param red The new red value
    /// @return The new [Colour]
    public Colour withRed(final int red) { return new Colour((this.colour & 0xFF00FFFF) | ((red & 0xFF) << 16)); }
    
    /// A new [Colour] instance with the given green channel.
    /// @param green The new alpha value
    /// @return The new [Colour]
    public Colour withGreen(final int green) { return new Colour((this.colour & 0xFFFF00FF) | ((green & 0xFF) << 8)); }
    
    /// A new [Colour] instance with the given blue channel.
    /// @param blue The new blue value
    /// @return The new [Colour]
    public Colour withBlue(final int blue) { return new Colour((this.colour & 0xFFFFFF00) | (blue & 0xFF)); }
    
    /// A new [Colour] instance with the given alpha channel specified between 0f and 1f (where 1f is fully opaque).
    /// @param opacity The new opacity
    /// @return The new [Colour]
    public Colour withOpacity(final float opacity) { return this.withAlpha(Mth.floor(opacity * 0xFF)); }
    
    /// A new [Colour] instance with the given alpha channel specified between 0f and 1f (where 1f is fully opaque).
    /// Other than [#withOpacity(float)], the `opacity` value is scaled in relation to the current alpha channel.
    ///
    /// For instance, if this colour has an alpha value of 70, setting the opacity to 0.5f will scale it down to half
    /// of 70, so 35.
    ///
    /// @param opacity The new opacity
    /// @return The new [Colour]
    public Colour withOpacityRel(final float opacity)
    {
        if (opacity == 1.0f)
        {
            return this;
        }
        
        final float alpha = ARGB.alphaFloat(this.colour);
        return new Colour(ARGB.color(ARGB.as8BitChannel(opacity * alpha), this.colour));
    }
    
    //==================================================================================================================
    @Override public String toString() { return "#%08x".formatted(this.colour); }

    //==================================================================================================================
    @Override public int compareTo(final Integer o) { return Integer.compare(o, this.colour); }
}
