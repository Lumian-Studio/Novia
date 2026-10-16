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

import net.minecraft.network.chat.Component;
import xyz.lumian.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumian.novia.api.gui.event.GuiEvent;



//**********************************************************************************************************************
/// A [GuiComponent] that provides (usually) mutable state, which can be set and read.
///
/// This class is not a requirement for components that handle state but is recommended for API compatibility.
/// @see GuiComponent
/// @see IStatefulComponent
public abstract class StatefulGuiComponent
    extends GuiComponent
    implements IStatefulComponent
{
    //******************************************************************************************************************
    /// Triggered whenever the state of a stateful component changed.
    public final GuiEvent.Simple valueChanged = new GuiEvent.Simple();
    
    //==================================================================================================================
    private boolean muted = false;
    
    //******************************************************************************************************************
    /// Constructs a new stateful GUI component.
    /// @param message The component's initial message
    public StatefulGuiComponent(final Component message) { super(message); }
    
    /// Constructs a new stateful GUI component with an empty message.
    public StatefulGuiComponent() {}
    
    //==================================================================================================================
    @Override public GuiEvent.Simple getChangeEvent() { return this.valueChanged; }
    
    //==================================================================================================================
    /// Gets whether this component's change listeners are muted.
    /// @return [true] if this component is muted
    public final boolean isMuted() { return this.muted; }
    
    //==================================================================================================================
    @Override public final void mute()   { this.muted = true; }
    @Override public final void unmute() { this.muted = false; }
    
    //==================================================================================================================
    /// Triggers change notifications for all change listeners attached to this component. If this component is
    /// currently muted, this doesn't do anything.
    public final void sendChangeNotification()
    {
        if (this.muted)
        {
            return;
        }
        
        this.onValueChanged();
        this.valueChanged.post(this);
    }
    
    //==================================================================================================================
    /// Called whenever the component is notified of a state change.
    public void onValueChanged() {}
}
