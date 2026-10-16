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
package xyz.lumian.novia.api.gui.component.provided;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.component.ComponentUtil;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.property.GuiProperty;

import java.util.stream.Stream;



//**********************************************************************************************************************
/// An abstract base class for widgets that can be pressed.
///
/// Buttons using this base class will be rendered with the default Minecraft button texture as background, custom
/// implementations can implement this class to render custom things on top of the button texture (e.g. [NVSimpleButton]
/// for buttons providing rendering text on top).
public abstract class NVAbstractButton
    extends GuiComponent
{
    //******************************************************************************************************************
    /// See [NVAbstractButton#playClickSound].
    public static final boolean DEFAULT_SHOULD_PLAY_SOUND = true;
    
    //******************************************************************************************************************
    /// Describes whether the iconic click sound should play upon clicking the button.
    public final GuiProperty.NonNull<Boolean> playClickSound;
    
    //==================================================================================================================
    /// Triggered whenever the button was clicked.
    public final GuiEvent.Simple clicked = new GuiEvent.Simple();
    
    //******************************************************************************************************************
    /// Constructs a new abstract button.
    /// @param message The component message
    public NVAbstractButton(final Component message)
    {
        super(message);
        
        this.playClickSound = GuiProperty.nonNull(NVAbstractButton.DEFAULT_SHOULD_PLAY_SOUND);
        this.setWantsFocus(true);
    }
    
    public NVAbstractButton() { this(CommonComponents.EMPTY); }
    
    //==================================================================================================================
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.ABSTRACT_BUTTON_PLAY_SOUND,
                this.playClickSound,
                Codec.BOOL));
    }
    
    //==================================================================================================================
    /// Explicitly executes the button's associated click handler.
    public void makePress()
    {
        if (this.playClickSound.get())
        {
            ComponentUtil.playClickSound();
        }
        
        this.onPress();
        this.clicked.post(this);
    }
    
    //==================================================================================================================
    @Override
    public boolean onMouseDown(final MouseEvent e)
    {
        if (!this.isActive() || !e.isLeftButtonDown())
        {
            return false;
        }
        
        this.makePress();
        return true;
    }
    
    @Override
    public boolean onKeyDown(final KeyEvent e)
    {
        if (this.isActive() && e.isSelection())
        {
            this.makePress();
			return true;
        }
        
        return false;
    }
    
    //==================================================================================================================
    /// Can be overridden to let child implementations also listen to click events internally.
    public void onPress() {}
}
