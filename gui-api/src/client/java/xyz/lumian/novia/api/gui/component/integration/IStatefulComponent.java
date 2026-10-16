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
package xyz.lumian.novia.api.gui.component.integration;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import xyz.lumian.novia.api.core.serialisation.IValueConvertible;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.event.GuiEvent;



//**********************************************************************************************************************
/// A base class used for [GuiComponent] objects to handle state in an API stable way. All implementations
/// provide listeners that track changes and the [IValueConvertible] for interoperability with [Value]
/// objects.
public interface IStatefulComponent
    extends IValueConvertible
{
    //******************************************************************************************************************
    /// Gets the event used to notify when state changes occurred.
    /// @return The change [GuiEvent]
    GuiEvent.Simple getChangeEvent();
    
    //==================================================================================================================
    /// Disables change notifications for the component.
    ///
    /// When muting a component, the caller of this method has to make sure to always unmute this component after the
    /// necessary changes have been made; otherwise the component is at risk of never notifying its subscribers anymore.
    void mute();
    
    /// Re-enables change notifications for the component.
    void unmute();
    
    //==================================================================================================================
    default <T> DataResult<T> write(final DynamicOps<T> dynOps)
    {
        return Value.CODEC.encodeStart(dynOps, this.getValue());
    }
    
    default <T> DataResult<Value> read(final DynamicOps<T> dynOps, final T data)
    {
        final DataResult<Value> result = Value.CODEC.parse(dynOps, data);
        
        if (result.isError())
        {
            return result;
        }
        
        this.setValue(result.getOrThrow());
        return result;
    }
}
