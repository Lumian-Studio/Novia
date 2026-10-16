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

import com.google.common.collect.Sets;
import xyz.lumian.novia.api.gui.component.GuiComponent;

import java.util.Objects;
import java.util.Set;



//**********************************************************************************************************************
/// A lightweight event system that allows subscribing and unsubscribing handlers that get posted when the event
/// was triggered.
///
/// An event lets subscribers know that there was a change inside the component, therefore an event should never be
/// triggered by anything other than its owning component. This also applies to parent components, which should never
/// trigger events for children.
/// @param <EventArgs> The [GuiEventArgs] implementation to use
public sealed class GuiEvent<EventArgs extends GuiEventArgs>
    permits GuiEvent.Simple
{
    //******************************************************************************************************************
    /// A convenience [GuiEvent] that avoid having to deal with empty [GuiEventArgs] objects.
    public static final class Simple
        extends GuiEvent<GuiEventArgs>
    {
        //**************************************************************************************************************
        public Simple() { super(); }
        
        //==============================================================================================================
        /// Posts the event to all subscribed handlers.
        /// @param sender The component sending the event
        public void post(final GuiComponent sender) { this.post(sender, GuiEventArgs.EMPTY); }
    }
    
    //******************************************************************************************************************
    final Set<GuiEventHandler<EventArgs>> handlers = Sets.newIdentityHashSet();
    
    //******************************************************************************************************************
    /// Subscribes to the event with the given handler.
    ///
    /// Do note that, if the handler should be unsubscribed from the event later on,
    /// you need the original handler object. As is the case for lambdas and method references, this means you will need
    /// to keep that handler object stored somewhere, or it will not be possible to unsubscribe.
    /// @param handler The [GuiEventHandler] for this event
    /// @return `true` if the given handler was not already subscribed to this event, otherwise `false`
    public boolean subscribe(final GuiEventHandler<EventArgs> handler)
    {
        return this.handlers.add(Objects.requireNonNull(handler, "handler must not be null"));
    }
    
    /// Unsubscribes a handler from this event.
    ///
    /// Do note that you need the original handler object. As is the case for lambdas and method references,
    /// you will need to pass the original handler object you subscribed with earlier, or it will not be possible
    /// to unsubscribe.
    /// @param handler The [GuiEventHandler] for this event
    /// @return `true` if the given handler could be unsubscribed from this event, otherwise `false`
    public boolean unsubscribe(final GuiEventHandler<EventArgs> handler)
    {
        return this.handlers.remove(Objects.requireNonNull(handler, "handler must not be null"));
    }
    
    //==================================================================================================================
    /// Posts the event to all subscribed handlers.
    /// @param sender The component sending the event
    /// @param args   The [EventArgs] for this event
    public void post(final GuiComponent sender, final EventArgs args)
    {
        this.handlers.forEach(handler -> handler.handle(sender, args));
    }
}
