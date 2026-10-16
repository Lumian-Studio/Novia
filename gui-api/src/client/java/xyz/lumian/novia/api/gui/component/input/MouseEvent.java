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
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.component.GuiComponent;


//**********************************************************************************************************************
/// Provides data about the action of a mouse input event, such as the left mouse button or the mouse-wheel.
/// This is used for component events in the [GuiComponent] class such as for
/// [GuiComponent#onMouseDown(MouseEvent)].
public final class MouseEvent
    extends AbstractInputEvent<MouseEvent>
    implements InputWithModifiers
{
    //******************************************************************************************************************
    public static MouseEvent event(final GuiComponent component, final MouseButtonEvent e,
                                   final double deltaX, final double deltaY)
    {
        return new MouseEvent(component, new Point((int) e.x(), (int) e.y()), deltaX, deltaY, e.button(),
                              e.modifiers());
    }
    
    public static MouseEvent noClickEvent(final GuiComponent component, final double mouseX, final double mouseY,
                                          final double deltaX, final double deltaY)
    {
        final MouseHandler device = Minecraft.getInstance().mouseHandler;
        final int          button;
        
        if (device.isLeftPressed())
        {
            button = GLFW.GLFW_MOUSE_BUTTON_LEFT;
        }
        else if (device.isRightPressed())
        {
            button = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        }
        else if (device.isMiddlePressed())
        {
            button = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
        }
        else
        {
            button = 0;
        }
        
        return new MouseEvent(component, new Point((int) mouseX, (int) mouseY), deltaX, deltaY, button, 0);
    }
    
    //******************************************************************************************************************
    /// The mouse device that has triggered this event.
    public final MouseHandler device = Minecraft.getInstance().mouseHandler;
    
    /// Determines a change on the left-axis since the last mouse event.
    ///
    /// For scroll events this will determine the horizontal scroll amount, for drag events the change in mouse position
    /// since the last drag event and for any other mouse event 0.
    public final double deltaX;
    
    /// Determines a change on the y-axis since the last mouse event.
    ///
    /// For scroll events this will determine the vertical scroll amount, for drag events the change in mouse position
    /// since the last drag event and for any other mouse event 0.
    public final double deltaY;
    
    /// The mouse position in screen coordinates.
    public final Point mousePos;
    
    public final int mouseButton;
    
    public final int modifiers;
    
    //******************************************************************************************************************
    /// Constructs a new mouse button event.
    /// @param source   The [GuiComponent] that originally triggered the event
    /// @param mousePos The position of the cursor in screen coordinates
    /// @param deltaX   The amount the position has changed from the last move on the left-axis
    /// @param deltaY   The amount the position has changed from the last move on the y-axis
    /// @param button   The code of the mouse button that has been pressed (see [GLFW])
    private MouseEvent(final GuiComponent source, final Point mousePos,
                       final double deltaX, final double deltaY,
                       final int button, final int modifier)
    {
        super(source);
    
        this.mousePos    = mousePos;
        this.deltaX      = deltaX;
        this.deltaY      = deltaY;
        this.mouseButton = button;
        this.modifiers   = modifier;
    }
    
    //==================================================================================================================
    @Override public @InputConstants.Value int input() { return this.mouseButton; }
    
    @Override public @Modifiers int modifiers() { return this.modifiers; }
    
    //==================================================================================================================
    /// {@return the mouse coordinates relative to the [#target()] component}
    public Point localMousePos() { return this.target().toRelativePos(this.mousePos); }
    
    /// {@return the X mouse coordinate relative to the [#target()] component}
    public int localMouseX() { return this.target().toRelativeX(this.mousePos.x()); }
    
    /// {@return the Y mouse coordinate relative to the [#target()] component}
    public int localMouseY() { return this.target().toRelativeY(this.mousePos.y()); }
    
    //==================================================================================================================
    /// {@return whether the left mouse button was pressed when the event triggered}
    public boolean isLeftButtonDown() { return this.mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT; }
    
    /// {@return whether the right mouse button was pressed when the event triggered}
    public boolean isRightButtonDown() { return this.mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT; }
    
    /// {@return whether the middle mouse button was pressed when the event triggered}
    public boolean isMiddleButtonDown() { return this.mouseButton == GLFW.GLFW_MOUSE_BUTTON_MIDDLE; }
    
    //==================================================================================================================
    /// Disables the cursor. [#enableCursor()] should be called after every call to this one, otherwise the cursor won't
    /// reappear.
    public void disableCursor()
    {
        GLFW.glfwSetInputMode(
            Minecraft.getInstance().getWindow().handle(),
            GLFW.GLFW_CURSOR,
            GLFW.GLFW_CURSOR_DISABLED);
    }
    
    /// Re-enables the cursor. This should be called after every call to [#disableCursor()], otherwise the cursor won't
    /// reappear.
    public void enableCursor()
    {
        GLFW.glfwSetInputMode(
            Minecraft.getInstance().getWindow().handle(),
            GLFW.GLFW_CURSOR,
            GLFW.GLFW_CURSOR_NORMAL);
    }
}
