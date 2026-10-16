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

import net.minecraft.client.gui.navigation.FocusNavigationEvent;

import java.util.*;



//**********************************************************************************************************************
/// Describes how a component should react to focus navigation events.
public interface IComponentNavigator
{
    //******************************************************************************************************************
    /// Gets the next component to focus in the navigator's specified traversal order, if there is a focused component.
    /// @param container The container to search for focusable components
    /// @param nav       The type and direction of navigation to use when navigating
    /// @return The next component to focus, or an empty {@link Optional} if no focusable component could be found
    Optional<GuiComponent> findNext(GuiComponent container, FocusNavigationEvent nav);
    
    /// Gets the first component to focus in the navigator's specified traversal order.
    /// @param container The container to search for focusable components
    /// @param nav       The type and direction of navigation to use when navigating
    /// @return The first component to focus, or an empty {@link Optional} if no focusable component could be found
    Optional<GuiComponent> findFirst(GuiComponent container, FocusNavigationEvent nav);
    
    /// Gets all components that want focus in the navigator's specified traversal order.
    /// @param container The container to search for focusable components
    /// @param nav       The type and direction of navigation to use when navigating
    /// @return The list of components
    List<GuiComponent> getAll(GuiComponent container, FocusNavigationEvent nav);
}
