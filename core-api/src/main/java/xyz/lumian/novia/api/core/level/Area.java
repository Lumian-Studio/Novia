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
package xyz.lumian.novia.api.core.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.level.shapes.CuboidShape;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;



//**********************************************************************************************************************
public class Area
{
    //******************************************************************************************************************
    public static Area cuboid(final BlockPos pos1, final BlockPos pos2)
    {
        final int x = Math.min(pos1.getX(), pos2.getX());
        final int y = Math.min(pos1.getY(), pos2.getY());
        final int z = Math.min(pos1.getZ(), pos2.getZ());
        return new Area(new BlockPos(x, y, z), CuboidShape.fromPoints(pos1, pos2));
    }

    public static Area cuboid(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        return Area.cuboid(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
    }
    
    public static Area cuboid(final BlockPos startPos, final int xCount, final int yCount, final int zCount)
    {
        return Area.cuboid(startPos, startPos.offset(xCount, yCount, zCount));
    }

    //******************************************************************************************************************
    private final IAreaShape shape;
    private final BlockPos   startPos;
    
    private @Nullable ImmutableList<BlockPos> cache = null;
    
    //******************************************************************************************************************
    public Area(final BlockPos startPos, final IAreaShape shape)
    {
        this.shape    = shape;
        this.startPos = startPos;
    }

    //==================================================================================================================
    public IAreaShape getShape() { return this.shape; }
    
    /// Returns a cached list of blocks currently held by this [Area] object.
    /// @return The [ImmutableList] of [BlockPos]
    public ImmutableList<BlockPos> getBlocks()
    {
        if (this.cache == null)
        {
            this.cache = this.shape.build(this.startPos);
        }
        
        return this.cache;
    }
    
    //==================================================================================================================
    /// Applies a [Consumer] to each block in the specified area.
    /// @param consumer The [Consumer]
    public void apply(final Consumer<BlockPos> consumer)
    {
        Objects.requireNonNull(consumer);
        StreamSupport.stream(this.shape.spliterator(), false)
            .forEach(offset -> consumer.accept(this.startPos.offset(offset)));
    }

    //==================================================================================================================
    public boolean intersectsWith(final Area other)
    {
        if (this == Objects.requireNonNull(other))
        {
            return true;
        }
        
        final ImmutableList<BlockPos> other_blocks = other.getBlocks();
        return this.getBlocks().stream().anyMatch(other_blocks::contains);
    }
    
    public boolean fullyContains(final Area other)
    {
        if (this == Objects.requireNonNull(other))
        {
            return true;
        }
        
        return this.getBlocks().containsAll(other.getBlocks());
    }

    //==================================================================================================================
    public ImmutableList<BlockPos> intersection(final Area other)
    {
        return ImmutableList.copyOf(Sets.intersection(
            Sets.newHashSet(this.getBlocks()),
            Sets.newHashSet(other.getBlocks())));
    }
    
    public ImmutableList<BlockPos> difference(final Area other)
    {
        return ImmutableList.copyOf(Sets.difference(
            Sets.newHashSet(this.getBlocks()),
            Sets.newHashSet(other.getBlocks())));
    }

    public ImmutableList<BlockPos> union(final Area other)
    {
        return ImmutableList.copyOf(Sets.union(
            Sets.newHashSet(this.getBlocks()),
            Sets.newHashSet(other.getBlocks())));
    }

    //==================================================================================================================
    public Area expanded(final Direction direction, final int amount)
    {
        if (amount == 0) return this;
        return new Area(this.startPos, this.shape.expand(direction, amount));
    }

    public Area reduced(final Direction direction, final int amount)
    {
        if (amount == 0) return this;
        return new Area(this.startPos, this.shape.reduce(direction, amount));
    }

    public Area moved(final Direction direction, final int amount)
    {
        if (amount == 0) return this;
        return new Area(this.startPos, this.shape.move(direction, amount));
    }
}
