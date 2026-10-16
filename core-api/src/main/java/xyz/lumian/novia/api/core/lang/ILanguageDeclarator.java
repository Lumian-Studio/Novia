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
package xyz.lumian.novia.api.core.lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;

import java.util.*;
import java.util.function.UnaryOperator;



//**********************************************************************************************************************
/// Interface to be inherited by language enumerations.
public interface ILanguageDeclarator<E extends Enum<E> & ILanguageDeclarator<E>>
    extends
        Component,
        ITranslationHolder
{
    //******************************************************************************************************************
    /// Gets the translatable object of the current declarator.
    /// @return The [TranslatableContents] object
    TranslatableContents getTranslatable();
    
    @Override default ComponentContents     getContents()        { return this.getTranslatable(); }
    @Override default Style                 getStyle()           { return Style.EMPTY; }
    @Override default List<Component>       getSiblings()        { return List.of(); }
    @Override default String                toTranslationKey()   { return this.getTranslatable().getKey(); }
    @Override default FormattedCharSequence getVisualOrderText() { return this.asMutableText().getVisualOrderText(); }

    //==================================================================================================================
    /// Gets this declarator as mutable component.
    /// @return The new [MutableComponent]
    default MutableComponent asMutableText() { return MutableComponent.create(this.getTranslatable()); }
    
    //==================================================================================================================
    default MutableComponent withStyle(final UnaryOperator<Style> styleUpdater)
    {
        return this.asMutableText().withStyle(styleUpdater);
    }

    default MutableComponent withStyle(final Style styleOverride)
    {
        return this.asMutableText().withStyle(styleOverride);
    }

    default MutableComponent withStyle(final ChatFormatting ...formatting)
    {
        return this.asMutableText().withStyle(formatting);
    }
    
    default MutableComponent withStyle(final ChatFormatting formatting)
    {
        return this.asMutableText().withStyle(formatting);
    }

    default MutableComponent withColor(final int color)   { return this.asMutableText().withColor(color); }
    default MutableComponent append(final String text)    { return this.asMutableText().append(text); }
    default MutableComponent append(final Component text) { return this.asMutableText().append(text); }
    
    //==================================================================================================================
    /// Gets the translation component for the current translation key with the given late-bound arguments.
    /// @param args The arguments to bind to this translation component
    /// @return The translatable text component
    default MutableComponent withArgs(final Object ...args)
    {
        return Component.translatable(this.getTranslatable().getKey(), args);
    }
    
    /// Gets the translation component for the current translation key with the given fallback.
    /// @param fallback The fallback string
    /// @return The translatable text component
    default MutableComponent withFallback(final String fallback)
    {
        return Component.translatable(this.getTranslatable().getKey(), fallback);
    }
    
    /// Gets the translation component for the current translation key with the given fallback and late-bound arguments.
    /// @param fallback The fallback string
    /// @param args     The arguments to bind to this translation component
    /// @return The translatable text component
    default MutableComponent withFallback(final String fallback, final Object ...args)
    {
        return Component.translatableWithFallback(this.getTranslatable().getKey(), fallback, args);
    }
    
    //==================================================================================================================
    /// Creates a mapping that can be used in conjunction with [LanguageMap#fromMapped(List, LanguageMap.Mapped\[\])].
    /// @param defaultTranslation The translation used for [net.minecraft.locale.Language#DEFAULT]
    /// @param translations       All other translations
    /// @return A new [xyz.lumian.novia.api.core.lang.LanguageMap.Mapped] mapping
    @SuppressWarnings("unchecked")
    default LanguageMap.Mapped<E> mapped(final String defaultTranslation, final String ...translations)
    {
        return new LanguageMap.Mapped<>((E) this, defaultTranslation, List.of(translations));
    }
}
