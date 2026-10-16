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

import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;



//**********************************************************************************************************************
public interface IInputListener
{
    //******************************************************************************************************************
    /// Called whenever the cursor moved across a component.
    ///
    /// It is worth nothing that during a drag motion, this will not be called for any component; for cases like these,
    /// [#onMouseDrag(MouseEvent)] should be overridden instead.
    /// @param e The mouse event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onMouseMove(MouseEvent e) { return false; }
    
    /// Called whenever the cursor enters the bounds of a component.
    ///
    /// This will only be called if no other component is currently being dragged; or after a drag operation completed
    /// if the component below the cursor is different from the component being dragged.
    ///
    /// This event does not bubble up and can only be handled for the target.
    /// @param e The mouse event
    default void onMouseEnter(MouseEvent e) {}
    
    /// Called whenever the cursor exits the bounds of a component.
    ///
    /// This will only be called if no other component is currently being dragged; or after a drag operation completed
    /// if the component below the cursor is different from the component being dragged.
    ///
    /// This event does not bubble up and can only be handled for the target.
    /// @param e The mouse event
    default void onMouseExit(MouseEvent e) {}
    
    /// Called whenever the cursor is over a component with any mouse button pressed (mouse button is down but not yet
    /// released).
    ///
    /// The return value will also determine whether the component gets focus on mouse down and if it can receive
    /// drag events; if it returns `false`, it will neither be focused nor start drag motions.
    /// @param e The mouse event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onMouseDown(MouseEvent e) { return false; }

    /// Called whenever a mouse button is released.
    ///
    /// This will get sent to the component, which started the drag event (started by [#onMouseDown(MouseEvent)]),
    /// and not the component the mouse button was actually released upon.
    /// If there is no component, which is currently being dragged (e.g. drag started outside the screen bounds),
    /// no component will receive this event.
    /// @param e The mouse event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onMouseUp(MouseEvent e) { return false; }
    
    /// Called upon scrolling the mouse wheel either vertically or horizontally upon hovering over a component.
    /// @param e The mouse event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onMouseScroll(MouseEvent e) { return false; }
    
    /// Called between the click and release of the mouse button, whenever the mouse is moving.
    ///
    /// This will only be called for the component the mouse initially clicked on, even if the mouse is leaving the
    /// component's bounds.
    /// @param e The mouse event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onMouseDrag(MouseEvent e) { return false; }
    
    /// Called whenever a button on the keyboard was pressed while the component was focused.
    /// @param e The keyboard event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onKeyDown(KeyEvent e) { return false; }
    
    /// Called whenever a button on the keyboard was released while the component was focused.
    /// @param e The keyboard event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onKeyUp(KeyEvent e) { return false; }
    
    /// Called whenever a character key was typed while the component was focused.
    /// @param e The keyboard event
    /// @return `true` if the component handled the event, `false` if the parent should handle it
    default boolean onInput(KeyEvent e) { return false; }
}
