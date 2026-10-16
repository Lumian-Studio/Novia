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

import org.lwjgl.glfw.GLFW;



//**********************************************************************************************************************
public abstract class KeyboardUtil
{
    //******************************************************************************************************************
    /// Gets the number represented on the number key from the key code (also keypad).
    /// @param keyCode The key code
    /// @return The number from the number key or `-1` if the key was not a number key
    public static int getKeyNumber(final int keyCode)
    {
        return switch (keyCode)
        {
            case GLFW.GLFW_KEY_0, GLFW.GLFW_KEY_KP_0 ->  0;
            case GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_KP_1 ->  1;
            case GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_KP_2 ->  2;
            case GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_KP_3 ->  3;
            case GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_KP_4 ->  4;
            case GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_KP_5 ->  5;
            case GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_KP_6 ->  6;
            case GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_KP_7 ->  7;
            case GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_KP_8 ->  8;
            case GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_KP_9 ->  9;
            default                                  -> -1;
        };
    }

    //******************************************************************************************************************
    private KeyboardUtil() {}
}
