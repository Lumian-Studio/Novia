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
package xyz.lumian.novia.api.gui.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import xyz.lumian.novia.api.core.data.lang.NoviaLanguageProvider;
import xyz.lumian.novia.api.core.lang.LanguageMap;
import xyz.lumian.novia.api.gui.GuiApiLang;
import xyz.lumian.novia.api.gui.GuiDefine;

import java.util.*;



//**********************************************************************************************************************
public class GuiApiDataProvider
    implements DataGeneratorEntrypoint
{
    //******************************************************************************************************************
    private static final LanguageMap<GuiApiLang> LANGUAGE_MAP;
    
    //==================================================================================================================
    static
    {
        LANGUAGE_MAP = new LanguageMap<>(GuiApiLang.class);
        LANGUAGE_MAP.fromMapped(List.of("de_de"),
            GuiApiLang.GUI_NARRATION_COMPONENT_TITLE.mapped("GUI component %s", "GUI Komponente %s"),
            GuiApiLang.GUI_NARRATION_COMPONENT_TITLE_INACTIVE
                .mapped("Inactive GUI component %s", "Inaktive GUI Komponente %s"),
            GuiApiLang.GUI_GO_BACK_BUTTON           .mapped("< Back", "< Zurück"),
            GuiApiLang.GUI_COMPONENT_SLIDER_MESSAGE .mapped("Value: %s", "Wert: %s"),
            GuiApiLang.GUI_BOOLEAN_VALUE_FALSE      .mapped("False", "Falsch"),
            GuiApiLang.GUI_BOOLEAN_VALUE_TRUE       .mapped("True", "Wahr"),
            GuiApiLang.GUI_DROPDOWN_PLACEHOLDER     .mapped("Select...", "Auswählen...")
        );
        
        LANGUAGE_MAP.defaultsTo("de_de",
            "de_at",
            "de_ch");
    }
    
    //******************************************************************************************************************
    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator fabricDataGenerator)
    {
        final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(NoviaLanguageProvider.simple(GuiDefine.ID, LANGUAGE_MAP));
    }
}
