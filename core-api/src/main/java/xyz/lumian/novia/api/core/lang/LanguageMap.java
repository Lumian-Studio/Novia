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

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.locale.Language;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// A specialised map implementation for Minecraft language translation keys.
public class LanguageMap<E extends Enum<E> & ILanguageDeclarator<E>>
    extends HashMap<String, EnumMap<E, String>>
{
    //******************************************************************************************************************
    /// A mapping for all translations in all languages mapped to a translation key.
    /// @param constant           The translation key enum constant
    /// @param defaultTranslation The translation for the Minecraft default language
    /// @param translations       The (in order) translations for all other languages
    public record Mapped<E extends Enum<E> & ILanguageDeclarator<E>>(
        E            constant,
        String       defaultTranslation,
        List<String> translations
    ) {}
    
    //******************************************************************************************************************
    private final Class<E>                         enumClass;
    private final Object2ObjectMap<String, String> lang2LangFallbacks = new Object2ObjectOpenHashMap<>();
    private final Map<String, Map<String, String>> rawTranslations    = new Object2ObjectOpenHashMap<>();
    
    //******************************************************************************************************************
    /// Constructs a new [LanguageMap].
    /// @param enumClass The translation enumeration class
    public LanguageMap(final Class<E> enumClass)
    {
        this.enumClass = Objects.requireNonNull(enumClass);
    }
    
    //==================================================================================================================
    /// Gets a translation value for the given language code and translation key.
    /// @param langCode       The language code
    /// @param translationKey The translation key constant
    /// @param searchDefault  Whether, if a translation could not be found, its default language should be searched
    /// @return The translation text, or `null` if no translation was found
    public @Nullable String getTranslation(final String langCode, final E translationKey, final boolean searchDefault)
    {
        final EnumMap<E, String> translations = this.get(Objects.requireNonNull(langCode));
        
        if (translations != null)
        {
            final String result = translations.get(Objects.requireNonNull(translationKey));
            
            if (result != null)
            {
                return result;
            }
        }
        
        if (!searchDefault || !this.lang2LangFallbacks.containsKey(langCode))
        {
            return null;
        }
        
        final String alias_lang = this.lang2LangFallbacks.get(langCode);
        return this.get(alias_lang).get(translationKey);
    }
    
    /// Gets a translation value for the given language code and translation key, or searches its aliased language for
    /// a translation.
    ///
    /// @param langCode       The language code
    /// @param translationKey The translation key constant
    /// @return The translation text, or `null` if no translation was found
    public @Nullable String getTranslation(final String langCode, final E translationKey)
    {
        return this.getTranslation(langCode, translationKey, true);
    }
    
    //==================================================================================================================
    /// Adds a language alias for a regional language code (e.g. mapping en_gb to en_us, so that the British region
    /// automatically inherits translations for the American region)
    ///
    /// @param defaultLanguage The default language the regional code inherits translations from
    /// @param targetLanguages All regional variants that inherit from the default language
    /// @throws IllegalArgumentException If the default language had not been registered in the map, or if the target
    ///                                  and default languages are the same
    public void defaultsTo(final String defaultLanguage, final String ...targetLanguages)
    {
        if (!this.containsKey(Objects.requireNonNull(defaultLanguage)))
        {
            throw new IllegalArgumentException("default language '%s' is not registered".formatted(defaultLanguage));
        }
        
        for (final var target : targetLanguages)
        {
            if (target.equals(defaultLanguage))
            {
                throw new IllegalArgumentException("defaulted language code must not refer to itself");
            }
            
            this.lang2LangFallbacks.put(target, defaultLanguage);
        }
    }
    
    //==================================================================================================================
    /// Adds a translation for the given language code and translation key.
    /// @param langCode       The language code
    /// @param translationKey The translation key associated with the translation
    /// @param translation    The actual translation string
    /// @throws TranslationRegisterException If there already is a translation for the given translation key
    public void addTranslation(final String langCode, final E translationKey, final String translation)
    {
        Objects.requireNonNull(langCode,       "lang code must not be null");
        Objects.requireNonNull(translationKey, "translation key must not be null");
        Objects.requireNonNull(translation,    "translation string must not be null");
        
        final EnumMap<E, String> translation_map = this.computeIfAbsent(langCode, (_ -> new EnumMap<>(this.enumClass)));
        this.addTranslationInternal(translation_map, langCode, translationKey, translation);
    }
    
    /// Adds a set of translations for the given language code.
    /// @param langCode     The language code
    /// @param translations The set of translations mapped to their keys
    /// @throws TranslationRegisterException If there already is at least one translation for the mapped translation key
    public void addAllTranslations(final String langCode, final Map<E, String> translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation map must not be null");
        
        final EnumMap<E, String> translation_map = this.computeIfAbsent(langCode, (_ -> new EnumMap<>(this.enumClass)));
        translations.forEach((key, val) -> this.addTranslationInternal(translation_map, langCode, key, val));
    }
    
    /// Adds a set of translations for the given language code.
    /// @param langCode     The language code
    /// @param translations The pairs of translations mapped to their keys
    /// @throws TranslationRegisterException If there already is at least one translation for the mapped translation key
    @SafeVarargs
    public final void addAllTranslations(final String langCode, final Pair<E, String>...translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation list must not be null");
        
        final EnumMap<E, String> translation_map = this.computeIfAbsent(langCode, (_ -> new EnumMap<>(this.enumClass)));
        Arrays.stream(translations)
            .forEach(p -> this.addTranslationInternal(translation_map, langCode, p.getFirst(), p.getSecond()));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void addTranslationInternal(final EnumMap<E, String> map, final String langCode, final E translationKey,
                                        final String translation)
    {
        if (map.containsKey(translationKey))
        {
            throw new TranslationRegisterException(String
                .format("duplicate translation key '%s' for language '%s'", translationKey, langCode));
        }
        
        map.put(translationKey, translation);
    }
    
    //==================================================================================================================
    /// Adds a translation that is not part of the language enum.
    /// @param langCode       The language code
    /// @param translationKey The raw translation key
    /// @param translation    The translation for the key
    /// @throws TranslationRegisterException If a translation for the given translation key was already registered or
    ///                                      if the language enum already specifies the translation key
    public void addRawTranslation(final String langCode, final String translationKey, final String translation)
    {
        Objects.requireNonNull(langCode,       "lang code must not be null");
        Objects.requireNonNull(translationKey, "translation key must not be null");
        Objects.requireNonNull(translation,    "translation string must not be null");
        
        final Map<String, String> map = this.rawTranslations
            .computeIfAbsent(langCode, (_ -> new HashMap<>()));
        this.addRawTranslationInternal(map, langCode, translationKey, translation);
    }
    
    /// Adds a set of translations that are not part of the language enum.
    /// @param langCode     The language code
    /// @param translations The translations mapped to their raw keys
    /// @throws TranslationRegisterException If a translation for the given translation key was already registered or
    ///                                      if the language enum already specifies the translation key
    public void addAllRawTranslations(final String langCode, final Map<String, String> translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation map must not be null");
        
        final Map<String, String> map = this.rawTranslations
            .computeIfAbsent(langCode, (_ -> new HashMap<>()));
        translations.forEach((key, val) -> this.addRawTranslationInternal(map, langCode, key, val));
    }
    
    /// Adds a set of translations that are not part of the language enum.
    /// @param langCode     The language code
    /// @param translations The translation pairs with the translations mapped to their raw keys
    /// @throws TranslationRegisterException If a translation for the given translation key was already registered or
    ///                                      if the language enum already specifies the translation key
    @SafeVarargs
    public final void addAllRawTranslations(final String langCode, final Pair<String, String> ...translations)
    {
        Objects.requireNonNull(langCode,     "lang code must not be null");
        Objects.requireNonNull(translations, "translation list must not be null");
        
        final Map<String, String> map = this.rawTranslations
            .computeIfAbsent(langCode, (_ -> new HashMap<>()));
        Arrays.stream(translations)
            .forEach(p -> this.addRawTranslationInternal(map, langCode, p.getFirst(), p.getSecond()));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void addRawTranslationInternal(final Map<String, String> map, final String langCode,
                                           final String translationKey, final String translation)
    {
        if (map.containsKey(translationKey))
        {
            throw new TranslationRegisterException(
                "duplicate translation key '%s' for language '%s'".formatted(translationKey, langCode));
        }
        
        if (Arrays.stream(this.enumClass.getEnumConstants()).anyMatch(e -> e.toTranslationKey().equals(translationKey)))
        {
            throw new TranslationRegisterException(
                "invalid raw translation key '%s', already specified as translation declaration in %s"
                    .formatted(translationKey, this.enumClass.getSimpleName()));
        }
        
        map.put(translationKey, translation);
    }
    
    //==================================================================================================================
    /// Adds all translations for all languages given.
    /// The order of languages in `langCodes` is directly tied to the order of translations in [Mapped#translations].
    ///
    /// @param langCodes    A list of language codes to add translations to
    /// @param translations A set of [Mapped] translations to add to the map
    /// @throws TranslationRegisterException If a duplicate mapping was found
    /// @throws IllegalArgumentException If the Minecraft default language was added to `langCodes`
    @SafeVarargs
    public final void fromMapped(final List<String> langCodes, final Mapped<E> ...translations)
    {
        final String mc_default = Language.DEFAULT;
        
        this.addAllTranslations(mc_default, Arrays.stream(translations)
            .collect(Collectors.toMap(Mapped::constant, Mapped::defaultTranslation)));
        
        for (int i = 0; i < langCodes.size(); i++)
        {
            final String lang_code = langCodes.get(i);
            
            if (lang_code.equals(mc_default))
            {
                throw new IllegalArgumentException("default language '%s' is implied and therefore cannot be specified"
                    .formatted(mc_default));
            }
            
            final EnumMap<E, String> translation_map = this
                .computeIfAbsent(lang_code, (_ -> new EnumMap<>(this.enumClass)));
            
            for (final var mapped : translations)
            {
                if (translation_map.containsKey(mapped.constant()))
                {
                    throw new TranslationRegisterException("duplicate key '%s'"
                        .formatted(mapped.constant().toTranslationKey()));
                }
                
                final String translation = (mapped.translations.size() > i ? mapped.translations.get(i) : null);
                
                if (translation != null)
                {
                    translation_map.put(mapped.constant, translation);
                }
            }
        }
    }
    
    /// Adds all translations for all languages given.
    /// The order of languages in `langCodes` is directly tied to the order of translations in [Mapped#translations].
    ///
    /// Other than [#fromMapped(List, Mapped\[\])], this allows adding raw translation keys from a separate language
    /// enumeration.
    ///
    /// @param langCodes    A list of language codes to add translations to
    /// @param translations A set of [Mapped] translations to add to the map
    /// @throws TranslationRegisterException If a duplicate mapping was found
    /// @throws IllegalArgumentException If the Minecraft default language was added to `langCodes`
    @SafeVarargs
    public final <E2 extends Enum<E2> & ILanguageDeclarator<E2>> void fromMappedDeclarator(
        final List<String> langCodes,
        final Mapped<E2>   ...translations
    )
    {
        final String mc_default = Language.DEFAULT;
        
        this.addAllRawTranslations(mc_default, Arrays.stream(translations)
            .collect(Collectors.toMap((t -> t.constant.toTranslationKey()), Mapped::defaultTranslation)));
        
        for (int i = 0; i < langCodes.size(); i++)
        {
            final String lang_code = langCodes.get(i);
            
            if (lang_code.equals(mc_default))
            {
                throw new IllegalArgumentException("default language '%s' is implied and therefore cannot be specified"
                    .formatted(mc_default));
            }
            
            final Map<String, String> translation_map = this.rawTranslations
                .computeIfAbsent(lang_code, (_ -> new HashMap<>()));
            
            for (final var mapped : translations)
            {
                final String key = mapped.constant.toTranslationKey();
                
                if (translation_map.containsKey(key))
                {
                    throw new TranslationRegisterException("duplicate key '%s'".formatted(key));
                }
                
                final String translation = (mapped.translations.size() > i ? mapped.translations.get(i) : null);
                
                if (translation != null)
                {
                    translation_map.put(key, translation);
                }
            }
        }
    }
    
    //==================================================================================================================
    /// Creates a Java Stream with all translations mapped to their keys.
    /// @param langCode The language code of the translations to stream
    /// @return A Java [Stream] with all the translations
    public Stream<Pair<String, String>> streamLanguage(final String langCode)
    {
        if (!this.containsKey(langCode))
        {
            throw new IllegalArgumentException("lang code '%s' is not registered".formatted(langCode));
        }
        
        final Map<String, String> empty_map       = new HashMap<>();
        final EnumMap<E, String>  translation_map = this.get(langCode);
        final EnumMap<E, String>  defaults_map    = (this.lang2LangFallbacks.containsKey(langCode)
            ? this.get(this.lang2LangFallbacks.get(langCode))
            : null);
        
        return Stream.concat(
            Arrays
                .stream(this.enumClass.getEnumConstants())
                .flatMap(constant ->
                {
                    final String translation = translation_map.get(constant);
                    
                    if (translation == null)
                    {
                        if (defaults_map != null)
                        {
                            final String defaulted = defaults_map.get(constant);
                            
                            if (defaulted != null)
                            {
                                return Stream.of(Pair.of(constant.toTranslationKey(), defaulted));
                            }
                        }
                        
                        return Stream.empty();
                    }
                    
                    return Stream.of(Pair.of(constant.toTranslationKey(), translation_map.get(constant)));
                }),
            this.rawTranslations
                .getOrDefault(langCode, empty_map)
                .entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), e.getValue())));
    }
}
