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
package xyz.lumian.novia.api.gui.component.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputWithModifiers;
import xyz.lumian.novia.api.gui.component.GuiComponent;

import java.util.*;



//**********************************************************************************************************************
/// Provides data about the action of a keyboard input event, such as the key code, when available the pressed character,
/// and the given inputs. This is used for component events in the [GuiComponent] class such as for
/// [GuiComponent#onKeyDown(KeyEvent)].
public final class KeyEvent
    extends AbstractInputEvent<KeyEvent>
    implements InputWithModifiers
{
    //******************************************************************************************************************
    /// The keyboard device that has triggered this event.
    public final KeyboardHandler device = Minecraft.getInstance().keyboardHandler;
    
    /// Either the key code of the key, which has been pressed, or the character that has been typed.
    public final int input;
    
    /// The modifiers that have been pressed when this event has been triggered.
    public final int modifiers;
    
    /// The scan code of the key, which has been pressed, or empty.
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Integer> scanCode;
    
    //******************************************************************************************************************
    /// Constructs a new non-character key keyboard event.
    /// @param source    The [GuiComponent] that originally triggered the event
    /// @param input     The key code of the key that was pressed (see [org.lwjgl.glfw.GLFW]
    /// @param scanCode  The scan code of the key being pressed
    /// @param modifiers The additional modifier flags of the modifier keys that have been pressed in addition
    public KeyEvent(final GuiComponent source, final int input, final int scanCode, final int modifiers)
    {
        super(source);
    
        this.input     = input;
        this.modifiers = modifiers;
        this.scanCode  = Optional.of(scanCode);
    }

    /// Constructs a new character key keyboard event.
    /// @param source    The [GuiComponent] that originally triggered the event
    /// @param codepoint The codepoint of the key that was pressed
    public KeyEvent(final GuiComponent source, final int codepoint)
    {
        super(source);
        
        this.input     = codepoint;
        this.modifiers = 0;
        this.scanCode  = Optional.empty();
    }
    
    //==================================================================================================================
    /// Gets the character represented by [#input]. If this is not part of a char input event, using this might overflow
    /// the original input.
    /// @return A character
    public char character() { return (char) this.input; }
    
    @Override public @InputConstants.Value int input()     { return this.input; }
    @Override public @Modifiers            int modifiers() { return this.modifiers; }
}
