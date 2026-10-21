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
package xyz.lumian.novia.api.config.client;

import net.minecraft.network.chat.contents.TranslatableContents;
import xyz.lumian.novia.api.config.ConfigDefine;
import xyz.lumian.novia.api.core.lang.ILanguageDeclarator;



//**********************************************************************************************************************
public enum ConfigApiLangClient
    implements ILanguageDeclarator<ConfigApiLangClient>
{
    CONFIG_SLIDER_VALUE_TEXT("gui.%s.slider.value.text".formatted(ConfigDefine.ID)),
    CONFIG_EDIT_BUTTON_TEXT("gui.%s.button.edit.text".formatted(ConfigDefine.ID)),
    CONFIG_EDIT_IN_CONFIG_NOTICE("gui.%s.notEditable.text".formatted(ConfigDefine.ID)),
    CONFIG_VARIANT_BUTTON_SELECT_TYPE("gui.%s.variantButton.selectType".formatted(ConfigDefine.ID)),
    CONFIG_SCHEMA_LIST_REMOVE_ITEM("gui.%s.schemaList.removeItem".formatted(ConfigDefine.ID)),
    CONFIG_SCHEMA_CONSTANT("gui.%s.schemaConstant.text".formatted(ConfigDefine.ID)),
    CONFIG_GROUP_UNGROUPED_TITLE("gui.%s.group.ungrouped.title".formatted(ConfigDefine.ID))
    ;
    
    //******************************************************************************************************************
    private final TranslatableContents translatable;
    
    //******************************************************************************************************************
    ConfigApiLangClient(final String key) { this.translatable = new TranslatableContents(key, null, new Object[0]); }
    
    //==================================================================================================================
    @Override public TranslatableContents getTranslatable() { return this.translatable; }
}
