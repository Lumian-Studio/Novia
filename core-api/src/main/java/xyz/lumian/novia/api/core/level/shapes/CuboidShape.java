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
package xyz.lumian.novia.api.core.level.shapes;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.level.IAreaShape;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// Describes a cuboidal area of position vectors, also directly integrates with [BlockPos].
///
/// The basic layout of this class describes the bounds of the cuboid shape and an optional pivot point that describes
/// where in the cuboidal area the start position is, for cases where the cuboid needs to be computed relative to
/// a given position. By default, this pivot starts by \[0, 0, 0], so the bottom left corner on the front.
/// @param xCount The number of blocks on the x-axis
/// @param yCount The number of blocks on the y-axis
/// @param zCount The number of blocks on the z-axis
/// @param pivot  The pivot point of the cuboid
public record CuboidShape(int xCount, int yCount, int zCount, Vec3i pivot)
    implements IAreaShape
{
    //******************************************************************************************************************
    public static class CuboidIterator
        implements Iterator<Vec3i>
    {
        //**************************************************************************************************************
        private final CuboidShape cuboid;
        private final int         size;
        
        private int i;
        
        //**************************************************************************************************************
        public CuboidIterator(final CuboidShape cuboid)
        {
            this.cuboid = cuboid;
            this.size   = cuboid.size();
            this.i      = 0;
        }
        
        //==============================================================================================================
        @Override public boolean hasNext() { return (this.i < this.size); }
        
        //==============================================================================================================
        @Override public Vec3i next() { return this.cuboid.getOffsetUnchecked(this.i++); }
    }
    
    //******************************************************************************************************************
    /// Create a new [CuboidShape] from the given two positions, where all positions between `startPos` and `endPos`
    /// (inclusive) are considered part of the cuboid.
    /// @param startPos The start position
    /// @param endPos   The end position
    /// @param origin   The origin position, where [CuboidShape#pivot()] will be the difference between `origin` and the
    ///                 minimum in all 3 dimensions.
    public static CuboidShape fromPointsWithOrigin(final Vec3i startPos, final Vec3i endPos, final Vec3i origin)
    {
        final int min_x = Math.min(startPos.getX(), endPos.getX());
        final int min_y = Math.min(startPos.getY(), endPos.getY());
        final int min_z = Math.min(startPos.getZ(), endPos.getZ());
        final int max_x = Math.max(startPos.getX(), endPos.getX());
        final int max_y = Math.max(startPos.getY(), endPos.getY());
        final int max_z = Math.max(startPos.getZ(), endPos.getZ());
        
        final Vec3i pivot = new Vec3i((origin.getX() - min_x), (origin.getY() - min_y), (origin.getZ() - min_z));
        return new CuboidShape(((max_x - min_x) + 1), ((max_y - min_y) + 1), ((max_z - min_z) + 1), pivot);
    }
    
    /// Create a new [CuboidShape] from the given two positions and a pivot of \[0, 0, 0], where all positions between
    /// `startPos` and `endPos` (inclusive) are considered part of the cuboid.
    /// @param startPos The start position
    /// @param endPos   The end position
    public static CuboidShape fromPoints(final Vec3i startPos, final Vec3i endPos)
    {
        return CuboidShape.fromPoints(startPos, endPos, Vec3i.ZERO);
    }
    
    /// Create a new [CuboidShape] from the given two positions and the given pivot, where all positions between
    /// `startPos` and `endPos` (inclusive) are considered part of the cuboid.
    /// @param startPos The start position
    /// @param endPos   The end position
    /// @param pivot    The pivot of the cuboid, relative to \[0, 0, 0] of the cuboid
    public static CuboidShape fromPoints(final Vec3i startPos, final Vec3i endPos, final Vec3i pivot)
    {
        final int min_x = Math.min(startPos.getX(), endPos.getX());
        final int min_y = Math.min(startPos.getY(), endPos.getY());
        final int min_z = Math.min(startPos.getZ(), endPos.getZ());
        final int max_x = Math.max(startPos.getX(), endPos.getX());
        final int max_y = Math.max(startPos.getY(), endPos.getY());
        final int max_z = Math.max(startPos.getZ(), endPos.getZ());
        
        return new CuboidShape(((max_x - min_x) + 1), ((max_y - min_y) + 1), ((max_z - min_z) + 1), pivot);
    }
    
    //******************************************************************************************************************
    /// @throws IllegalArgumentException If `xCount`, `yCount` or `zCount` are less or equal to zero
    public CuboidShape
    {
        if (xCount <= 0)
        {
            throw new IllegalArgumentException("x dimension must be above zero");
        }
        
        if (yCount <= 0)
        {
            throw new IllegalArgumentException("y dimension must be above zero");
        }
        
        if (zCount <= 0)
        {
            throw new IllegalArgumentException("z dimension must be above zero");
        }
    }

    //==================================================================================================================
    @Override
    @Nullable
    public Vec3i getOffset(final int ordinal)
    {
        if (ordinal < 0 || ordinal >= this.size())
        {
            return null;
        }
        
        return this.getOffsetUnchecked(ordinal);
    }
    
    /// Same as [#getOffset(int)] but does not prevent you from going outside bounds.
    /// @param ordinal The ordinal of the position
    /// @return The offset
    public Vec3i getOffsetUnchecked(final int ordinal)
    {
        final int x_div = (ordinal / this.xCount);
        final int z_div = (x_div   / this.zCount);
        return new Vec3i(
            (ordinal - (x_div * this.xCount) - this.pivot.getX()),
            (z_div                           - this.pivot.getY()),
            (x_div   - (z_div * this.zCount) - this.pivot.getZ()));
    }
    
    //==================================================================================================================
    /// Streams all given positions relative to this cuboid's [#pivot()] in the cuboidal area.
    /// @return The [Stream] providing all [Vec3i] positions
    @Override
    public Stream<Vec3i> stream()
    {
        return IntStream.range(0, this.size()).mapToObj(this::getOffsetUnchecked);
    }
    
    @Override public Iterator<Vec3i> iterator() { return new CuboidIterator(this); }
    
    @Override public int size() { return (this.xCount * this.yCount * this.zCount); }
    
    //==================================================================================================================
    @Override
    public ImmutableList<BlockPos> build(final BlockPos startPos)
    {
        return this.stream()
            .map(startPos::offset)
            .collect(ImmutableList.toImmutableList());
    }

    //==================================================================================================================
    @Override
    public IAreaShape expand(final Direction direction, final int amount)
    {
        if (amount == 0)
        {
            return this;
        }
        
        final int x_count = Math.max(1, (this.xCount + Math.abs(direction.getStepX()) * amount));
        final int y_count = Math.max(1, (this.yCount + Math.abs(direction.getStepY()) * amount));
        final int z_count = Math.max(1, (this.zCount + Math.abs(direction.getStepZ()) * amount));
        
        final Vec3i pivot;
        
        if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
        {
            final int pivot_x = (x_count - this.xCount);
            final int pivot_y = (y_count - this.yCount);
            final int pivot_z = (z_count - this.zCount);
            pivot = this.pivot.offset(pivot_x, pivot_y, pivot_z);
        }
        else pivot = this.pivot;
        
        return new CuboidShape(x_count, y_count, z_count, pivot);
    }

    @Override
    public IAreaShape reduce(final Direction direction, final int amount)
    {
        return this.expand(direction, -amount);
    }

    @Override
    public IAreaShape move(final Direction direction, final int amount)
    {
        final int pivot_x = (direction.getStepX() * amount);
        final int pivot_y = (direction.getStepY() * amount);
        final int pivot_z = (direction.getStepZ() * amount);
        return new CuboidShape(this.xCount, this.yCount, this.zCount, this.pivot.offset(pivot_x, pivot_y, pivot_z));
    }
}
