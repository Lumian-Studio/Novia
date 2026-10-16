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

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

import java.util.*;



//**********************************************************************************************************************
/// An interface declaring its inheritor an automatically translatable object.
public interface ITranslationHolder
{
    //******************************************************************************************************************
    /// {@return the translation key of a translation holder}
    static String of(final ITranslationHolder holder) { return holder.toTranslationKey(); }
    
    /// Gets the translation key of an item.
    /// @param item The [Item] to get the translation key from
    /// @return The translation key of the [Item]
    static String of(final Item item) { return item.getDescriptionId(); }
    
    /// Gets the translation key of a block.
    /// @param block The [Block] to get the translation key from
    /// @return The translation key of the [Block]
    static String of(final Block block) { return block.getDescriptionId(); }
    
    /// Gets the translation key of a creative tab.
    /// @param tabKey The [CreativeModeTab] resource key to get the translation key for
    /// @return The translation key of the [CreativeModeTab]
    static String of(final ResourceKey<CreativeModeTab> tabKey)
    {
        final CreativeModeTab   tab     = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(tabKey);
        final ComponentContents content = tab.getDisplayName().getContents();
        
        if (content instanceof TranslatableContents translatable)
        {
            return translatable.getKey();
        }
        else
        {
            throw new UnsupportedOperationException(
                "creative tab (%s) is not translatable".formatted(tab.getDisplayName().getString()));
        }
    }

    /// Gets the translation key of an entity type.
    /// @param entityType The [EntityType] to get the translation key from
    /// @return The translation key of the [EntityType]
    static String of(final EntityType<?> entityType) { return entityType.getDescriptionId(); }
    
    /// Gets the translation key of an enchantment type.
    /// @param enchantment The [Enchantment] resource key to get the translation key for
    /// @return The translation key of the [Enchantment]
    static String ofEnchantment(final ResourceKey<Enchantment> enchantment)
    {
        return Util.makeDescriptionId("enchantment", enchantment.identifier());
    }
    
    /// Gets the translation key of an entity attribute.
    /// @param attribute The [Attribute] to get the translation key for
    /// @return The translation key of the [Attribute]
    static String of(final Holder<Attribute> attribute) { return attribute.value().getDescriptionId(); }
    
    /// Gets the translation key of a stat type.
    /// @param type The [StatType] to get the translation key for
    /// @return The translation key of the [StatType]
    static String of(final StatType<?> type)
    {
        return ("stat_type." + Objects.requireNonNull(BuiltInRegistries.STAT_TYPE.getKey(type)).toLanguageKey());
    }
    
    /// Gets the translation key of a stat.
    /// @param stat The [Stat] to get the translation key for
    /// @return The translation key of the [Stat]
    static String of(final Stat<Identifier> stat)
    {
        return ("stat." + Objects.requireNonNull(stat.getValue()).toLanguageKey());
    }
    
    /// Gets the translation key of a mob effect.
    /// @param effect The [MobEffect] to get the translation key for
    /// @return The translation key of the [MobEffect]
    static String of(final MobEffect effect) { return effect.getDescriptionId(); }

    /// Converts an identifier to a translation key
    /// @param id The [Identifier] to convert
    /// @return The translation key of the [Identifier]
    static String of(final Identifier id) { return id.toLanguageKey(); }

    /// Gets the translation key of a mob tag key.
    /// @param tag The [TagKey] to get the translation key for
    /// @return The translation key of the [TagKey]
    static String of(final TagKey<?> tag) { return tag.getTranslationKey(); }
    
    //******************************************************************************************************************
    /// {@return the translation key of this translation holder}
    String toTranslationKey();
}
