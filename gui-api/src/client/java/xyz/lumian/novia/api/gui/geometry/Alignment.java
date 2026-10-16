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

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;



//**********************************************************************************************************************
public enum Alignment
    implements StringRepresentable
{
    LEFT(1),
    CENTRE(2),
    RIGHT(4),
    TOP(8),
    MIDDLE(16),
    BOTTOM(32),
    
    TOP_LEFT     (TOP.flags    | LEFT.flags),
    TOP_CENTRE   (TOP.flags    | CENTRE.flags),
    TOP_RIGHT    (TOP.flags    | RIGHT.flags),
    MIDDLE_LEFT  (MIDDLE.flags | LEFT.flags),
    MIDDLE_CENTRE(MIDDLE.flags | CENTRE.flags),
    MIDDLE_RIGHT (MIDDLE.flags | RIGHT.flags),
    BOTTOM_LEFT  (BOTTOM.flags | LEFT.flags),
    BOTTOM_CENTRE(BOTTOM.flags | CENTRE.flags),
    BOTTOM_RIGHT (BOTTOM.flags | RIGHT.flags),
    ;
    
    //******************************************************************************************************************
    public static final Codec<Alignment> CODEC = StringRepresentable.fromEnum(Alignment::values);
    
    //******************************************************************************************************************
    /// The flags that are given for the constant.
    public final int flags;
    
    //******************************************************************************************************************
    Alignment(final int flags) { this.flags = flags; }
    
    //==================================================================================================================
    /// {@return for compound flags, whether the given flag <code>alignment</code> is implicated}
    public boolean has(final Alignment alignment)
    {
        return ((this.flags & alignment.flags) == alignment.flags);
    }
    
    //==================================================================================================================
    /// Aligns the given target bounds so that it is aligned to the given container area.
    /// @param containerX      The position of the area that the target area should be aligned to on the left-axis
    /// @param containerY      The position of the area that the target area should be aligned to on the y-axis
    /// @param containerWidth  The width of the area that the target area should be aligned to
    /// @param containerHeight The height of the area that the target area should be aligned to
    /// @param targetWidth     The width of the to be aligned area
    /// @param targetHeight    The height of the to be aligned area
    /// @return A new [Rectangle] aligned to the given container rect
    public Rectangle align(final int containerX, final int containerY,
                           final int containerWidth, final int containerHeight,
                           final int targetWidth, final int targetHeight)
    {
        int x = containerX;
        int y = containerY;
        
        if (this.has(Alignment.CENTRE))
        {
            x += (int) Math.round((containerWidth - targetWidth) * 0.5);
        }
        else if (this.has(Alignment.RIGHT))
        {
            x += (containerWidth - targetWidth);
        }
        
        if (this.has(Alignment.MIDDLE))
        {
            y += (int) Math.round((containerHeight - targetHeight) * 0.5);
        }
        else if (this.has(Alignment.BOTTOM))
        {
            y += (containerHeight - targetHeight);
        }
        
        return new Rectangle(x, y, targetWidth, targetHeight);
    }
    
    /// Aligns `target` so that it is aligned relatively to the given container bounds.
    /// @param containerX      The position of the area that the target area should be aligned to on the left-axis
    /// @param containerY      The position of the area that the target area should be aligned to on the y-axis
    /// @param containerWidth  The width of the area that the target area should be aligned to
    /// @param containerHeight The height of the area that the target area should be aligned to
    /// @param target          The [Rectangle] to be aligned
    /// @return A new [Rectangle] aligned to the given container rect
    public Rectangle align(final int containerX, final int containerY,
                           final int containerWidth, final int containerHeight,
                           final Rectangle target)
    {
        return target.transform((x, y, w, h) ->
            this.align(containerX, containerY, containerWidth, containerHeight, w, h));
    }
    
    /// Aligns the target area so that it is aligned relatively to `container` based on the given alignment flags.
    /// @param container    The containing area that the target area should be aligned to
    /// @param targetWidth  The width of the to be aligned area
    /// @param targetHeight The height of the to be aligned area
    /// @return A new [Rectangle] aligned to the given container rect
    public Rectangle align(final Rectangle container, final int targetWidth, final int targetHeight)
    {
        return container.transform((x, y, w, h) -> this.align(x, y, w, h, targetWidth, targetHeight));
    }
    
    /// Aligns `target` so that it is aligned relatively to `container` based on the given alignment flags.
    /// @param container The [Rectangle] to align to
    /// @param target    The [Rectangle] to be aligned
    /// @return A new [Rectangle] aligned to the given container rect
    public Rectangle align(final Rectangle container, final Rectangle target)
    {
        return container.transform((cx, cy, cw, ch) -> target.transform((tx, ty, tw, th) ->
            this.align(cx, cy, cw, ch, tw, th)));
    }
    
    //==================================================================================================================
    @Override public String getSerializedName() { return this.name().toLowerCase(); }
}
