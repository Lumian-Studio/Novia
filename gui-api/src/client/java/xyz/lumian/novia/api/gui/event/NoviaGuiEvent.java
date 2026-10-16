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
package xyz.lumian.novia.api.gui.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.DefaultGuiTemplate;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.canvas.Palette;

import java.util.*;



//**********************************************************************************************************************
public class NoviaGuiEvent
{
    //******************************************************************************************************************
    @FunctionalInterface
    public interface PaletteInit
    {
        //**************************************************************************************************************
        void setColours(Map<ColourId, Colour> palette);
    }
    
    //******************************************************************************************************************
    /// Triggered whenever a new [DefaultGuiTemplate] is initialised, to set up the template's default colour palette.
    ///
    /// This should not be used to set default colours for a custom template, instead this can be used for third-party
    /// library gui components, to set up the default colours a component should come with. If you want to set up
    /// default colours for a specific custom template, you can set the colours inside the template's constructor with
    /// [IGuiTemplate#setColour(ColourId, Colour)] or [Palette#setAllColours(Map)].
    ///
    /// Note to library developers: Do not override another library's default component colours, this is purely meant
    /// for your own components, otherwise this could mess them up and render your library unusable.
    public static final Event<PaletteInit> DEFAULT_PALETTE_INITIALISATION = EventFactory.createArrayBacked(
        PaletteInit.class,
        (listeners -> (palette -> Arrays.stream(listeners).forEach(listener -> listener.setColours(palette)))));
}
