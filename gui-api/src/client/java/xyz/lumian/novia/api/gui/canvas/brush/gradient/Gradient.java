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
package xyz.lumian.novia.api.gui.canvas.brush.gradient;

import net.minecraft.util.ARGB;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.brush.VertexPalette;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.Objects;



//**********************************************************************************************************************
/// Specifies a colour gradient that can be used to apply a colour interpolation curve onto a shape.
public class Gradient
{
    //******************************************************************************************************************
    private final int       startColour;
    private final int       endColour;
    private final float     skew;
    private final Direction direction;

    //******************************************************************************************************************
    /// Constructs a new gradient.
    /// @param startColour The colour the gradient starts at
    /// @param endColour   The colour the gradient ends at
    /// @param direction   The direction the gradient interpolates in, either vertical or horizontal
    /// @param skew        The skew of the interpolation
    public Gradient(final int startColour, final int endColour, final Direction direction, final float skew)
    {
        this.startColour = startColour;
        this.endColour   = endColour;
        this.direction   = Objects.requireNonNull(direction, "direction must not be null");
        this.skew        = skew;

        if (this.skew <= 0.0f)
        {
            throw new IllegalArgumentException("skew value must not be equal or less than 0");
        }
    }
    
    /// Constructs a new gradient.
    /// @param startColour The colour the gradient starts at
    /// @param endColour   The colour the gradient ends at
    /// @param direction   The direction the gradient interpolates in, either vertical or horizontal
    /// @param skew        The skew of the interpolation
    public Gradient(final Colour startColour, final Colour endColour, final Direction direction, final float skew)
    {
        this(startColour.colour(), endColour.colour(), direction, skew);
    }

    /// Constructs a new gradient with a skew of 1.0f.
    /// @param startColour The colour the gradient starts at
    /// @param endColour   The colour the gradient ends at
    /// @param direction   The direction the gradient interpolates in, either vertical or horizontal
    public Gradient(final int startColour, final int endColour, final Direction direction)
    {
        this(startColour, endColour, direction, 1.0f);
    }
    
    /// Constructs a new gradient with a skew of 1.0f.
    /// @param startColour The colour the gradient starts at
    /// @param endColour   The colour the gradient ends at
    /// @param direction   The direction the gradient interpolates in, either vertical or horizontal
    public Gradient(final Colour startColour, final Colour endColour, final Direction direction)
    {
        this(startColour, endColour, direction, 1.0f);
    }
    
    //==================================================================================================================
    /// Clones the current gradient with the given colours replaced.
    /// @param startColour The start colour to replace
    /// @param endColour   The end colour to replace
    public Gradient withColours(final int startColour, final int endColour)
    {
        return new Gradient(startColour, endColour, this.direction, this.skew);
    }

    /// Clones the current gradient with the given colours replaced.
    /// @param startColour The start colour to replace
    /// @param endColour   The end colour to replace
    public Gradient withColours(final Colour startColour, final Colour endColour)
    {
        return new Gradient(startColour, endColour, this.direction, this.skew);
    }

    /// Clones the current gradient with the given opacity level applied to the colour interpolation.
    /// @param opacity The opacity level to apply to the gradients colours
    public Gradient withOpacity(final float opacity)
    {
        return this.withColours(
            new Colour(this.startColour).withOpacityRel(opacity),
            new Colour(this.endColour)  .withOpacityRel(opacity));
    }

    //==================================================================================================================
    /// {@return the start colour the interpolation starts with}
    public int startColour() { return this.startColour; }
    
    /// {@return the end colour the interpolation ends with}
    public int endColour() { return this.endColour; }
    
    /// {@return the skew that is applied to the distribution of the colour interpolation}
    public float skew() { return this.skew; }
    
    /// {@return the direction the interpolation flows in}
    public Direction direction() { return this.direction; }

    //==================================================================================================================
    /// Gets the colour at the given delta value, where 0 is [#startColour()] and 1 is [#endColour].
    /// @param delta The interpolation delta value
    public int getColour(float delta)
    {
        delta = Math.clamp(delta, 0f, 1f);

        if (this.skew != 1f && delta > 0f)
        {
            delta = (float) Math.exp(Math.log(delta) / this.skew);
        }

        return ARGB.linearLerp(delta, this.startColour, this.endColour);
    }
    
    /// {@return the gradient as a 4 vertex [VertexPalette]}
    public VertexPalette getPalette()
    {
        return switch (this.direction)
        {
            case VERTICAL   -> new VertexPalette(this.startColour, this.startColour, this.endColour, this.endColour);
            case HORIZONTAL -> new VertexPalette(this.startColour, this.endColour, this.startColour, this.endColour);
        };
    }
    
    //==================================================================================================================
    /// Partitions the gradient to only a portion of the gradient where the start and end colour become a colour on
    /// the colour interpolation curve of the gradient.
    /// @param x           The x coordinate of area the gradient applies to
    /// @param y           The y coordinate of area the gradient applies to
    /// @param width       The width of area the gradient applies to
    /// @param height      The height of area the gradient applies to
    /// @param deltaXStart The x coordinate of the start of the partition area
    /// @param deltaYStart The y coordinate of the start of the partition area
    /// @param deltaXEnd   The x coordinate of the end of the partition area
    /// @param deltaYEnd   The y coordinate of the end of the partition area
    /// @return The partitioned [Gradient]
    public Gradient partition(final int x,           final int y,
                              final int width,       final int height,
                              final int deltaXStart, final int deltaXEnd,
                              final int deltaYStart, final int deltaYEnd)
    {
        return switch (this.direction)
        {
            case HORIZONTAL -> this.partition(x, (x + width),  deltaXStart, deltaXEnd);
            case VERTICAL   -> this.partition(y, (y + height), deltaYStart, deltaYEnd);
        };
    }
    
    /// Partitions the gradient to only a portion of the gradient where the start and end colour become a colour on
    /// the colour interpolation curve of the gradient.
    /// @param area        The area the gradient applies to
    /// @param deltaXStart The x coordinate of the start of the partition area
    /// @param deltaYStart The y coordinate of the start of the partition area
    /// @param deltaXEnd   The x coordinate of the end of the partition area
    /// @param deltaYEnd   The y coordinate of the end of the partition area
    /// @return The partitioned [Gradient]
    public Gradient partition(final Rectangle area,
                              final int deltaXStart, final int deltaXEnd,
                              final int deltaYStart, final int deltaYEnd)
    {
        return area.transform((x, y, w, h) ->
            this.partition(x, y, w, h, deltaXStart, deltaXEnd, deltaYStart, deltaYEnd));
    }
    
    /// Partitions the gradient to only a portion of the gradient where the start and end colour become a colour on
    /// the colour interpolation curve of the gradient.
    /// @param area    The area the gradient applies to
    /// @param subArea The area inside `subArea` that should be partitioned
    /// @return The partitioned [Gradient]
    public Gradient partition(final Rectangle area, final Rectangle subArea)
    {
        return area.transform((x, y, w, h) ->
            this.partition(x, y, w, h, subArea.x(), subArea.getRight(), subArea.y(), subArea.getBottom()));
    }
    
    //------------------------------------------------------------------------------------------------------------------
    public Gradient partition(final int axisStart, final int axisEnd, final int deltaStart, final int deltaEnd)
    {
        if (deltaStart > deltaEnd)
        {
            throw new IllegalArgumentException("start delta must not be greater than end delta");
        }
        
        if (deltaStart >= axisEnd || deltaStart < axisStart)
        {
            throw new IllegalArgumentException("start delta must be inside the given area");
        }
        
        if (deltaEnd >= axisEnd)
        {
            throw new IllegalArgumentException("end delta must be inside the given area");
        }
        
        final int start = this.getColour((float) deltaStart / axisEnd);
        final int end   = this.getColour((float) deltaEnd   / axisEnd);
        
        return this.withColours(start, end);
    }
    
    //==================================================================================================================
    @Override
    public String toString()
    {
        return "Gradient{startColour=%d, endColour=%d, skew=%f, direction=%s}"
            .formatted(this.startColour, this.endColour, this.skew, this.direction);
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)                      return true;
        if (!(obj instanceof Gradient other)) return false;
        
        return (
               this.startColour == other.startColour
            && this.endColour   == other.endColour
            && this.skew        == other.skew
            && this.direction   == other.direction
        );
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.startColour, this.endColour, this.skew, this.direction);
    }
}
