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
package xyz.lumian.novia.api.gui.util;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;



//**********************************************************************************************************************
public abstract class MouseUtil
{
    //******************************************************************************************************************
    public static Point getScaledMousePos()
    {
        final Minecraft    client = Minecraft.getInstance();
        final MouseHandler mouse  = client.mouseHandler;
        final Window       window = client.getWindow();
        
        return new Point(
            (int) (mouse.xpos() * (double) window.getGuiScaledWidth()  / (double) window.getWidth()),
            (int) (mouse.ypos() * (double) window.getGuiScaledHeight() / (double) window.getHeight()));
    }
    
    public static Point getMousePos()
    {
        final MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        return new Point((int) mouse.xpos(), (int) mouse.ypos());
    }
    
    public static boolean hitTest(final GuiComponent component, final int mouseX, final int mouseY)
    {
        final Rectangle screen_bounds = component.getScreenBounds();
        
        if (!screen_bounds.contains(mouseX, mouseY))
        {
            return false;
        }
        
        return component.hitTest((mouseX - screen_bounds.x()), (mouseY - screen_bounds.y()));
    }
    
    public static boolean hitTest(final GuiComponent component, final double mouseX, final double mouseY)
    {
        return MouseUtil.hitTest(component, (int) mouseX, (int) mouseY);
    }
    
    public static boolean hitTest(final GuiComponent component, final Point mousePos)
    {
        return MouseUtil.hitTest(component, mousePos.x(), mousePos.y());
    }

    //******************************************************************************************************************
    private MouseUtil() {}
}
