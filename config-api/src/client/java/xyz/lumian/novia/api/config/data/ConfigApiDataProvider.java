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
package xyz.lumian.novia.api.config.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.locale.Language;
import xyz.lumian.novia.api.config.ConfigDefine;
import xyz.lumian.novia.api.config.client.ConfigApiLangClient;
import xyz.lumian.novia.api.core.data.lang.NoviaLanguageProvider;
import xyz.lumian.novia.api.core.lang.LanguageMap;

import java.util.*;



//**********************************************************************************************************************
public class ConfigApiDataProvider
    implements DataGeneratorEntrypoint
{
    //******************************************************************************************************************
    private static final LanguageMap<ConfigApiLangClient> LANGUAGE_MAP;
    
    //==================================================================================================================
    static
    {
        LANGUAGE_MAP = new LanguageMap<>(ConfigApiLangClient.class);
        LANGUAGE_MAP.fromMapped(
            List.of("de_de"),
            
            ConfigApiLangClient.CONFIG_SLIDER_VALUE_TEXT.mapped(
                "Value: %s",
                "Wert: %s"
            ),
            ConfigApiLangClient.CONFIG_EDIT_BUTTON_TEXT.mapped(
                "Edit",
                "Bearbeiten"
            ),
            ConfigApiLangClient.CONFIG_EDIT_IN_CONFIG_NOTICE.mapped(
                "Edit in file",
                "Nur in Datei"
            ),
            ConfigApiLangClient.CONFIG_VARIANT_BUTTON_SELECT_TYPE.mapped(
                "Choose type",
                "Format auswählen"
            ),
            ConfigApiLangClient.CONFIG_SCHEMA_LIST_REMOVE_ITEM.mapped(
                "Remove item",
                "Eintrag entfernen"
            ),
            ConfigApiLangClient.CONFIG_SCHEMA_CONSTANT.mapped(
                "Constant",
                "Konstante"
            ),
            ConfigApiLangClient.CONFIG_GROUP_UNGROUPED_TITLE.mapped(
                "Ungrouped",
                "Ungruppiert"
            )
        );
        
        LANGUAGE_MAP.defaultsTo(Language.DEFAULT,
            "en_gb", "en_au", "en_ca", "en_nz");
        LANGUAGE_MAP.defaultsTo("de_de",
            "de_at", "de_ch");
    }
    
    //******************************************************************************************************************
    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator fabricDataGenerator)
    {
        final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(NoviaLanguageProvider.simple(ConfigDefine.ID, ConfigApiDataProvider.LANGUAGE_MAP));
    }
}
