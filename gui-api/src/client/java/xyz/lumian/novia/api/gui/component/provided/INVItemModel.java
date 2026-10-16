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

import net.minecraft.client.gui.components.Tooltip;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.*;



//**********************************************************************************************************************
/// Provides the item model used to display and manage the list box items. Every item can have its own drawing code as
/// well as its own components that can be added to it.
public interface INVItemModel
{
    //******************************************************************************************************************
    /// Override to add child components to this item instance.
    /// @return The list of children
    default List<GuiComponent> getChildren() { return List.of(); }
    
    //==================================================================================================================
    /// Called whenever this item instance got its bounds changed; can be used to update the bounds of child components.
    ///
    /// Do note that the position of `bounds` is relative to the list box top-left corner, but the positioning of
    /// children is relative to the top-left corner of the item; hence for positioning children [Rectangle#resetPos()]
    /// should be used.
    /// @param bounds The new bounds of the item
    default void resized(Rectangle bounds) {}
    
    //==================================================================================================================
    /// Called when the item has been clicked.
    /// @param mousePos The mouse position relative to the item's bounds
    /// @param selected `true` if this item is currently selected
    /// @return `true` if the item should be selected (if enabled)
    default boolean onClick(Point mousePos, boolean selected) { return true; }
    
    //==================================================================================================================
    /// Gets the tooltip for this item, or null if no tooltip.
    /// @return The tooltip for this item
    default @Nullable Tooltip getTooltip(boolean selected) { return null; }
    
    //==================================================================================================================
    /// Draws the item to the canvas.
    ///
    /// Do note that the position of `bounds` is relative to the list box top-left corner, but the drawing
    /// is relative to the top-left corner of the item;
    /// hence for drawing to the canvas [Rectangle#resetPos()] should be used.
    /// @param canvas   The canvas to draw to
    /// @param bounds   The bounds of the item inside the list box
    /// @param index    The index of the item
    /// @param selected `true` if this item is selected
    /// @param hovered  `true` if the mouse is hovering over this item
    /// @param focused  `true` if this item is focused
    default void draw(Canvas canvas, Rectangle bounds, int index, boolean selected, boolean hovered, boolean focused) {}
}
