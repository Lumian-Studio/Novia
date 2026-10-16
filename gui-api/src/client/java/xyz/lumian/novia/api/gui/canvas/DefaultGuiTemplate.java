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
package xyz.lumian.novia.api.gui.canvas;

import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.component.provided.*;
import xyz.lumian.novia.api.gui.event.NoviaGuiEvent;
import xyz.lumian.novia.api.gui.renderer.GuiTooltipRenderer;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
/// Provides the Novia default template for [IGuiTemplate], which can be overridden to style components on top of
/// the default style. Overriding this class should usually be preferred to [IGuiTemplate], as this provides
/// a reasonable default colour palette.
///
/// If you want to also set defaults for your own third-party components, you can do this by subscribing to the
/// [NoviaGuiEvent#DEFAULT_PALETTE_INITIALISATION] event. A good way to make sure that all templates share the
/// same default colours in a mod is to provide a basic custom template that all more specifically targeted
/// templates inherit from.
///
/// Do not mixin your own component's template functions into this class, this should be done with
/// the [IGuiTemplate] class.
public class DefaultGuiTemplate
    implements IGuiTemplate
{
    //******************************************************************************************************************
    private static final Map<ColourId, Colour> DEFAULT_COLOURS;
    
    //==================================================================================================================
    static
    {
        final Colour fg_active   = Colour.WHITE;
        final Colour fg_inactive = new Colour(0xFF707070);
        
        final Map<ColourId, Colour> colours = new HashMap<>(Map.of(
            // Tooltip
            GuiTooltipRenderer.COLOUR_TEXT, fg_active,
            
            // NVLabel
            NVLabel.COLOUR_TEXT, fg_active,
            NVLabel.COLOUR_TEXT_INACTIVE, fg_inactive,
            
            // NVDropdown
            NVDropdown.COLOUR_OPTION_TEXT, fg_active,
            NVDropdown.COLOUR_OPTION_BACKGROUND_HIGHLIGHT, Colour.WHITE.withAlpha(0x77),
            NVDropdown.COLOUR_OPTION_BACKGROUND_SELECTED, Colour.WHITE.withAlpha(0x77),
            
            // NVNumericBox
            NVNumericBox.COLOUR_ARROW, fg_active,
            NVNumericBox.COLOUR_ARROW_INACTIVE, fg_inactive
        ));
        colours.putAll(Map.of(
            // NVTextBox
            NVTextBox.COLOUR_TEXT_EDITABLE, fg_active,
            NVTextBox.COLOUR_TEXT_UNEDITABLE, fg_inactive,
            NVTextBox.COLOUR_TEXT_ERROR, Colour.RED,
            NVTextBox.COLOUR_TEXT_SUGGESTION, new Colour(0xFF808080),
            NVTextBox.COLOUR_TEXT_PLACEHOLDER, new Colour(0xFF707070),
            
            // NVSlider
            NVSlider.COLOUR_TEXT, fg_active,
            NVSlider.COLOUR_TEXT_INACTIVE, fg_inactive,
            
            // NVSimpleButton
            NVSimpleButton.COLOUR_TEXT, fg_active,
            NVSimpleButton.COLOUR_TEXT_INACTIVE, fg_inactive
        ));
        
        // add third-party defaults
        final Map<ColourId, Colour> user_colours = new HashMap<>();
        NoviaGuiEvent.DEFAULT_PALETTE_INITIALISATION.invoker().setColours(user_colours);
        
        final Set<ColourId> ids = colours.keySet();
        colours.putAll(user_colours
            .entrySet()
            .stream()
            .filter(e -> !ids.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        
        DEFAULT_COLOURS = new HashMap<>(colours);
    }
    
    //******************************************************************************************************************
    /// {@return a copy of the default colour map}
    public static Map<ColourId, Colour> getDefaultColours()
    {
        return new HashMap<>(DefaultGuiTemplate.DEFAULT_COLOURS);
    }
    
    //******************************************************************************************************************
    private final Palette palette;
    
    //******************************************************************************************************************
    /// Constructs a new default template.
    public DefaultGuiTemplate() { this.palette = new Palette(DefaultGuiTemplate.DEFAULT_COLOURS, null); }
    
    //==================================================================================================================
    @Override public Palette getPalette() { return this.palette; }
}
