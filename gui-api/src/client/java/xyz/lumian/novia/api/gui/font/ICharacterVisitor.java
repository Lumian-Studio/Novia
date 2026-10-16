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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import xyz.lumian.novia.api.gui.impl.StyleAccessor;

import java.util.Objects;
import java.util.Optional;



//**********************************************************************************************************************
/// A functional interface and compatibility layer on top of [FormattedCharSink] objects that considers
/// [GuiFont] as part of the visitor callback.
///
/// To create a character visitor, one can use [ICharacterVisitor#of(ICharacterVisitor)] or pass it on as
/// a parameter or to a variable. Once acquired, these visitors act as a sort of indirection to [FormattedCharSink]
/// objects, which just means that whenever such is requested,
/// either the [ICharacterVisitor#asStyledVisitor(GuiFont)] or
/// [ICharacterVisitor#asPlainVisitor(GuiFont)] adapter function can be used to adapt to the Minecraft visitor
/// class.
///
/// Additionally, this class provides a few visit functions that can be used instead of the usual
/// [StringDecomposer] way. If, however, none of the visit functions provide the functionality needed you can
/// always pair [StringDecomposer] with this class' adapter functions like in this example where we want to start
/// at index 2:
/// ```java
/// GuiCharacterVisitor visitor = ((index, font, style, codePoint) ->
/// {
///     // do some visitor stuff
///     return true;
/// });
/// TextVisitFactory.visitFormatted(text, 2, Style.Empty, visitor.asStyledVisitor(myFont));
/// ```
@FunctionalInterface
public interface ICharacterVisitor
{
    //******************************************************************************************************************
    /// Helper function that allows creating a [ICharacterVisitor] from an unnamed lambda.
    /// @param visitor The visitor lambda
    /// @return `visitor`
    static ICharacterVisitor of(final ICharacterVisitor visitor)
    {
        return Objects.requireNonNull(visitor, "visitor must not be null");
    }
    
    //******************************************************************************************************************
    /// Processes the current character.
    ///
    /// The `style` object passed alongside the character's font is the original source of data that overrides
    /// the given font for the character, formatting flags that are non-null will be reflected in the font. For this
    /// reason it is usually best to use it for things like colours or other non-font-encoded data only.
    /// @param index     The index of the character
    /// @param font      The [GuiFont] formatting context for the current character
    /// @param style     The [Style] context for the current character
    /// @param codePoint The code point of the character
    /// @return `true` to continue visiting the following characters, otherwise `false`
    boolean accept(int index, GuiFont font, Style style, int codePoint);
    
    //==================================================================================================================
    /// Visits the given string with a plain visitor (see [#asPlainVisitor(GuiFont)]). Format codes will NOT be
    /// consumed.
    /// @param font  The [GuiFont] used for visiting each character
    /// @param style The style overriding the font
    /// @param text  The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visit(GuiFont font, final Style style, final String text)
    {
        if (((StyleAccessor)(Object)style).novia$isFontSet())
        {
            final FontDescription font_id = style.getFont();
            
            if (!font_id.equals(font.getDescriptor()))
            {
                font = font.withFont(font_id);
            }
        }
        
        font.setStyle(style);
        return StringDecomposer.iterate(text, style, this.asPlainVisitor(font));
    }
    
    /// Visits the given string with a plain visitor (see [#asPlainVisitor(GuiFont)]). Format codes will NOT be
    /// consumed.
    /// @param font The [GuiFont] used for visiting each character
    /// @param text The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visit(final GuiFont font, final String text)
    {
        return StringDecomposer.iterate(text, Style.EMPTY, this.asPlainVisitor(font));
    }
    
    /// Visits the given string with a plain visitor backwards (see [#asPlainVisitor(GuiFont)]). Format codes will
    /// NOT be consumed.
    /// @param font  The [GuiFont] used for visiting each character
    /// @param style The style overriding the font
    /// @param text  The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitBackwards(GuiFont font, final Style style, final String text)
    {
        if (((StyleAccessor) style).novia$isFontSet())
        {
            final FontDescription font_id = style.getFont();
            
            if (!font_id.equals(font.getDescriptor()))
            {
                font = font.withFont(font_id);
            }
        }
        
        font.setStyle(style);
        return StringDecomposer.iterateBackwards(text, style, this.asPlainVisitor(font));
    }
    
    /// Visits the given string with a plain visitor backwards (see [#asPlainVisitor(GuiFont)]). Format codes will
    /// NOT be consumed.
    /// @param font The [GuiFont] used for visiting each character
    /// @param text The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitBackwards(final GuiFont font, final String text)
    {
        return StringDecomposer.iterateBackwards(text, Style.EMPTY, this.asPlainVisitor(font));
    }
    
    /// Visits the given string with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format codes WILL be
    /// consumed.
    /// @param baseFont  The base [GuiFont] to use if not overridden by style attributes
    /// @param baseStyle The base [Style] to use if no explicit style was applied through format codes
    ///                  (or if it was reset)
    /// @param text      The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final Style baseStyle, final String text)
    {
        return StringDecomposer.iterateFormatted(text, baseStyle, this.asStyledVisitor(baseFont));
    }
    
    /// Visits the given string with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format codes WILL be consumed.
    /// This uses a styled visitor (see [#asStyledVisitor(GuiFont)]).
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final String text)
    {
        return this.visitFormatted(baseFont, Style.EMPTY, text);
    }
    
    /// Visits the given [FormattedText] with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format
    /// codes WILL be consumed.
    /// @param baseFont  The base [GuiFont] to use if not overridden by style attributes
    /// @param baseStyle The base [Style] to use if no explicit style was applied through format codes
    ///                  (or if it was reset)
    /// @param text      The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final Style baseStyle, final FormattedText text)
    {
        return StringDecomposer.iterateFormatted(text, baseStyle, this.asStyledVisitor(baseFont));
    }
    
    /// Visits the given [FormattedText] with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format
    /// codes WILL be consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final FormattedText text)
    {
        return this.visitFormatted(baseFont, Style.EMPTY, text);
    }
    
    /// Visits the given text with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format codes WILL be
    /// consumed.
    /// @param baseFont  The base [GuiFont] to use if not overridden by style attributes
    /// @param baseStyle The base [Style] to use if no explicit style was applied through format codes
    ///                  (or if it was reset)
    /// @param text      The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final Style baseStyle, final Component text)
    {
        final FormattedCharSink visitor = this.asStyledVisitor(baseFont);
        return text
            .visit(
                ((style, string) -> (StringDecomposer.iterateFormatted(string, style, visitor)
                    ? Optional.empty()
                    : FormattedText.STOP_ITERATION)),
                baseStyle)
            .isPresent();
    }
    
    /// Visits the given text with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format codes WILL be
    /// consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final Component text)
    {
        return this.visitFormatted(baseFont, Style.EMPTY, text);
    }
    
    /// Visits the given text with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format codes WILL be
    /// consumed.
    /// @param baseFont  The base [GuiFont] to use if not overridden by style attributes
    /// @param baseStyle The base [Style] to use if no explicit style was applied through format codes
    ///                  (or if it was reset)
    /// @param text      The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final Style baseStyle, final FormattedCharSequence text)
    {
        final FormattedCharSink visitor = this.asStyledVisitor(baseFont);
        return text.accept((i, style, codePoint) -> visitor.accept(i, style.applyTo(baseStyle), codePoint));
    }
    
    /// Visits the given text with a styled visitor (see [#asStyledVisitor(GuiFont)]). Format codes WILL be
    /// consumed.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @param text     The text to visit the characters from
    /// @return `true` if the entire text was visited, otherwise `false`
    default boolean visitFormatted(final GuiFont baseFont, final FormattedCharSequence text)
    {
        return text.accept(this.asStyledVisitor(baseFont));
    }
    
    //==================================================================================================================
    /// Converts this visitor to a Minecraft [FormattedCharSink] which tracks the characters current style and
    /// applies its attributes to the given base font.
    ///
    /// If a character has its own style attributes that are not internally `null`, the given base font will be
    /// overridden with these attributes regardless of their value, otherwise the style attribute of the base font will
    /// be used instead.
    /// @param baseFont The base [GuiFont] to use if not overridden by style attributes
    /// @return A Minecraft [FormattedCharSink]
    default FormattedCharSink asStyledVisitor(final GuiFont baseFont)
    {
        return new FormattedCharSink()
        {
            //**********************************************************************************************************
            private GuiFont font      = new GuiFont(baseFont);
            private Style   lastStyle = Style.EMPTY;
            
            //**********************************************************************************************************
            @Override
            public boolean accept(final int i, final Style style, final int codePoint)
            {
                // if our cached style is different (this should rarely be the case, depending on how heavy we style),
                // we want to recreate the gui style
                if (!style.equals(this.lastStyle))
                {
                    // we got some overrides
                    if (!style.isEmpty())
                    {
                        final GuiFont new_font = baseFont.withFont(Objects.requireNonNullElse(
                            ((StyleAccessor) style).novia$getFontId(),
                            baseFont.getDescriptor()));
                        new_font.setStyle(style);
                        this.font = new_font;
                    }
                    
                    // we go back to our base font
                    else
                    {
                        this.font = new GuiFont(baseFont);
                    }
                    
                    // also make sure to cache the current style for the next iteration
                    this.lastStyle = style;
                }
                
                return ICharacterVisitor.this.accept(i, this.font, style, codePoint);
            }
        };
    }
    
    /// Converts this visitor to a Minecraft [FormattedCharSink] which applies the same font style attributes to
    /// all characters, regardless if they have their own style attached (the style parameter will still have the latest
    /// style but the font parameter will be fixed).
    /// @param font The [GuiFont] used for visiting each character
    /// @return A Minecraft [FormattedCharSink]
    default FormattedCharSink asPlainVisitor(final GuiFont font)
    {
        return ((i, style1, codePoint) -> this.accept(i, font, style1, codePoint));
    }
}
