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
package xyz.lumian.novia.api.gui.geometry;



//**********************************************************************************************************************
public record Restrainer(int minWidth, int maxWidth, int minHeight, int maxHeight)
{
    //******************************************************************************************************************
    public static Restrainer min(final int minWidth, final int minHeight)
    {
        return new Restrainer(minWidth, Integer.MAX_VALUE, minHeight, Integer.MAX_VALUE);
    }

    public static Restrainer max(final int maxWidth, final int maxHeight)
    {
        return new Restrainer(0, maxWidth, 0, maxHeight);
    }

    //******************************************************************************************************************
    public Restrainer
    {
        if (minWidth < 0 || minHeight < 0)
        {
            throw new IllegalArgumentException("minWidth and minHeight can't be less than 0");
        }

        if (minWidth > maxWidth || minHeight > maxHeight)
        {
            throw new IllegalArgumentException("minWidth and minHeight can't be greater than maxWidth and maxHeight");
        }
    }

    //==================================================================================================================
    public int clampWidth(final int width) { return Math.clamp(width, this.minWidth, this.maxWidth); }
    public int clampHeight(final int height) { return Math.clamp(height, this.minHeight, this.maxHeight); }
}
