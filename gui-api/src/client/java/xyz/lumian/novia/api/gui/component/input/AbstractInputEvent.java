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

import xyz.lumian.novia.api.gui.component.GuiComponent;

import java.util.*;
import java.util.function.BiFunction;



//**********************************************************************************************************************
public sealed abstract class AbstractInputEvent<Self extends AbstractInputEvent<Self>>
    permits
        MouseEvent,
        KeyEvent
{
    //******************************************************************************************************************
    /// The component, which this event has been initially triggered on.
    public final GuiComponent source;
    
    //------------------------------------------------------------------------------------------------------------------
    private GuiComponent target;
    
    //******************************************************************************************************************
    public AbstractInputEvent(final GuiComponent source)
    {
        this.source = source;
        this.target = source;
    }
    
    //==================================================================================================================
    /// Gets the final component on which this event has been successfully handled.
    ///
    /// Do note that this is only accurate upon completion of all handlers, by that means, only when this event is being
    /// monitored from the outside (such as through a monitoring parent) the returned target is available,
    /// otherwise this will always be the current handler component.
    /// @return The handler component
    public final GuiComponent target() { return this.target; }
    
    //==================================================================================================================
    /// Posts the event to the component hierarchy.
    /// @param listener The listener function
    /// @return `true` if any one of the components handled this event successfully
    @SuppressWarnings("unchecked")
    public final boolean post(final BiFunction<GuiComponent, Self, Boolean> listener)
    {
        Objects.requireNonNull(listener, "input listener must not be null");
        final Self self = (Self) this;
        
        while (!listener.apply(this.target, self))
        {
            final GuiComponent parent = this.target.getParent();
            
            if (parent == null)
            {
                return false;
            }
            
            this.target = parent;
        }

        for (GuiComponent parent = this.target.getParent(); parent != null; parent = parent.getParent())
        {
            if (parent.isMonitoringChildren())
            {
                listener.apply(parent, self);
            }
        }
        
        return true;
    }
}
