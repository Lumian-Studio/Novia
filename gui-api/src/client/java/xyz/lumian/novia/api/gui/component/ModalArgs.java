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
package xyz.lumian.novia.api.gui.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;



//**********************************************************************************************************************
/// @param darkenBackground    Whether a darkened blurred background should be drawn behind the layer.
/// @param closeOnDistraction  Whether the layer component should close when it is “distracted”, by that means, clicking
///                            anywhere outside its bounds, focusing a component outside its bounds or when a different
///                            layer opened above it.
/// @param closeOnEscape       If `true`, escape key presses will not be handled by the main [GuiScreen] instance but by
///                            this layer, the escape key will always be handled by the most recent modal layer with
///                            this flag enabled, if a modal is closed on escape all more recent modal layers will be
///                            closed first up to the modal affected by this flag. Any modal, which has this flag
///                            disabled, will pass the escape press through to the next modal layer until one can handle
///                            it; otherwise it will travel all the way up to the screen layer, which will then handle
///                            the escape press.
/// @param wantsAllInput       Whether this layer should capture all mouse and keyboard events even outside its bounds,
///                            this will also cause anything behind the layer to become unfocusable.
/// @param associatedComponent An associated component allows the modal to be positioned relative to that component;
///                            additionally, it will prompt to close the modal once it isn't part of the screen any more
///                            or when it becomes invisible. Any modal depending on another component will also get the
///                            associated component's template and font information during initialisation.
public record ModalArgs(
    boolean                darkenBackground,
    boolean                closeOnDistraction,
    boolean                closeOnEscape,
    boolean                wantsAllInput,
    Optional<GuiComponent> associatedComponent
)
{
    //******************************************************************************************************************
    /// Opens the layer as a dialogue box (not to be confused with Minecraft's 1.21.6
    /// [Dialog](https://minecraft.wiki/w/Dialog) feature), similar to an alert box or a dialogue box with yes and no
    /// buttons.
    ///
    /// A dialogue box behaves much like a [#popup(GuiComponent)], but other than popups it captures all input events
    /// for itself and can only be closed by itself or when the screen is closed (usually by calling
    /// [GuiComponent#hideModal()]). Dialogues can also be opened as their own screen if no other Novia screen is
    /// currently shown.
    /// @param associatedComponent The modal's optional associated component
    public static ModalArgs dialogue(final @Nullable GuiComponent associatedComponent)
    {
        return new ModalArgs(true, false, true, true, Optional.ofNullable(associatedComponent));
    }
    
    /// Opens the component as a popup box, like a tooltip or a context menu.
    ///
    /// A popup box is similar to a [#dialogue(GuiComponent)], with the only difference that it does not put
    /// the screen behind it to rest; meaning, it won't capture all events from the screen behind it, and popups will
    /// also be closed when clicking outside its bounds, another modal was opened, or a component outside it gained
    /// focus.
    ///
    /// Also, other than a [#dialogue(GuiComponent)], a popup is purely a screen element and cannot be opened
    /// if there is no screen currently shown.
    /// @param associatedComponent The modal's optional associated component
    public static ModalArgs popup(final @Nullable GuiComponent associatedComponent)
    {
        return new ModalArgs(false, true, false, false, Optional.ofNullable(associatedComponent));
    }
}
