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
package xyz.lumian.novia.api.gui;


import net.minecraft.resources.Identifier;



//**********************************************************************************************************************
public abstract class GuiApiId
{
    //******************************************************************************************************************
    public static abstract class Component
    {
        //**************************************************************************************************************
        public static final Identifier CHECKBOX     = GuiApiId.of("check_box");
        public static final Identifier DROPDOWN     = GuiApiId.of("dropdown");
        public static final Identifier LABEL        = GuiApiId.of("label");
        public static final Identifier LABEL_BUTTON = GuiApiId.of("label_button");
        public static final Identifier LIST_BOX      = GuiApiId.of("list_box");
        public static final Identifier NUMERIC_BOX   = GuiApiId.of("numeric_box");
        public static final Identifier SCROLLBAR     = GuiApiId.of("scrollbar");
        public static final Identifier SIMPLE_BUTTON = GuiApiId.of("simple_button");
        public static final Identifier SLIDER        = GuiApiId.of("slider");
        public static final Identifier TEXT_BOX      = GuiApiId.of("text_box");
        public static final Identifier VIEWPORT      = GuiApiId.of("viewport");
    }
    
    public static abstract class GuiProperty
    {
        //**************************************************************************************************************
        // VIEWPORT
        public static final Identifier VIEWPORT_BEHAVIOR_HORIZONTAL = GuiApiId.of("viewport/behavior_horizontal");
        public static final Identifier VIEWPORT_BEHAVIOR_VERTICAL   = GuiApiId.of("viewport/behavior_vertical");
        public static final Identifier VIEWPORT_SCROLLBAR_THICKNESS = GuiApiId.of("viewport/scrollbar_thickness");
        
        // TEXT BOX
        public static final Identifier TEXT_BOX_TEXT_PROVIDER         = GuiApiId.of("text_box/text_provider");
        public static final Identifier TEXT_BOX_MAX_LENGTH            = GuiApiId.of("text_box/max_length");
        public static final Identifier TEXT_BOX_PREDICATE_STOPS_INPUT = GuiApiId.of("text_box/predicate_stops_input");
        public static final Identifier TEXT_BOX_TEXT_PREDICATE        = GuiApiId.of("text_box/text_predicate");
        public static final Identifier TEXT_BOX_SUGGESTION            = GuiApiId.of("text_box/suggestion");
        public static final Identifier TEXT_BOX_PLACEHOLDER           = GuiApiId.of("text_box/placeholder");
    
        // SLIDER
        public static final Identifier SLIDER_RANGE         = GuiApiId.of("slider/range");
        public static final Identifier SLIDER_TEXT_PROVIDER = GuiApiId.of("slider/text_provider");
    
        // SIMPLE BUTTON
        public static final Identifier SIMPLE_BUTTON_ICON_SIZE      = GuiApiId.of("simple_button/icon_size");
        public static final Identifier SIMPLE_BUTTON_ICON_PLACEMENT = GuiApiId.of("simple_button/icon_placement");
        public static final Identifier SIMPLE_BUTTON_ICON_MARGIN    = GuiApiId.of("simple_button/icon_margin");
    
        // SCROLLBAR
        public static final Identifier SCROLLBAR_DELTA          = GuiApiId.of("scrollbar/delta");
        public static final Identifier SCROLLBAR_VERTICAL       = GuiApiId.of("scrollbar/vertical");
        public static final Identifier SCROLLBAR_MIN_THUMB_SIZE = GuiApiId.of("scrollbar/min_thumb_size");
        
        // NUMERIC BOX
        public static final Identifier NUMERIC_BOX_RANGE  = GuiApiId.of("numeric_box/range");
        public static final Identifier NUMERIC_BOX_FORMAT = GuiApiId.of("numeric_box/format");
        
        // LIST BOX
        public static final Identifier LIST_BOX_VERTICAL        = GuiApiId.of("list_box/vertical");
        public static final Identifier LIST_BOX_SELECTION_MODE  = GuiApiId.of("list_box/selection_mode");
        public static final Identifier LIST_BOX_DEFAULT_ITEM    = GuiApiId.of("list_box/default_item");
        public static final Identifier LIST_BOX_ALWAYS_SELECTED = GuiApiId.of("list_box/always_selected");
        public static final Identifier LIST_BOX_CAN_DESELECT    = GuiApiId.of("list_box/can_deselect");
        public static final Identifier LIST_BOX_CAN_FOCUS_ITEMS = GuiApiId.of("list_box/can_focus_items");
        public static final Identifier LIST_BOX_ITEM_SIZE       = GuiApiId.of("list_box/item_size");
        
        // LABEL
        public static final Identifier LABEL_TEXT_ALIGN = GuiApiId.of("label/text_align");
        public static final Identifier LABEL_TRIM_FUNC  = GuiApiId.of("label/trim_function");
        
        // IMAGE
        public static final Identifier IMAGE_EXPLICIT_SCALING = GuiApiId.of("image/explicit_scaling");
        public static final Identifier IMAGE_SPRITE_UV        = GuiApiId.of("image/sprite_uv");
        
        // DROPDOWN
        public static final Identifier DROPDOWN_DEFAULT_OPT     = GuiApiId.of("dropdown/default_opt");
        public static final Identifier DROPDOWN_OPT_ALIGNMENT   = GuiApiId.of("dropdown/opt_alignment");
        public static final Identifier DROPDOWN_SHOW_SUGGESTION = GuiApiId.of("dropdown/show_suggestion");
        
        // ABSTRACT BUTTON
        public static final Identifier ABSTRACT_BUTTON_PLAY_SOUND = GuiApiId.of("abstract_button/play_sound");
    }
    
    public static abstract class RenderPipeline
    {
        public static final Identifier GUI          = GuiApiId.of("pipeline/gui");
        public static final Identifier GUI_TEXTURED = GuiApiId.of("pipeline/gui_textured");
    }
    
    //******************************************************************************************************************
    public static Identifier of(final String path) { return Identifier.fromNamespaceAndPath(GuiDefine.ID, path); }
}
