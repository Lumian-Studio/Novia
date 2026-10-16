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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// An interface allowing to define customisable area shapes. An area shape internally never deals in absolute block
/// position but rather in relative terms depending on its shape.
public interface IAreaShape
    extends Iterable<Vec3i>
{
    //******************************************************************************************************************
    /// Gets a position inside the shape based on its ordinal position.
    /// @param ordinal The ordinal (0: first offset; (size - 1): last offset)
    /// @return The offset, or `null` if outside bounds
    @Nullable Vec3i getOffset(int ordinal);
    
    //==================================================================================================================
    /// {@return a stream with all blocks}
    Stream<Vec3i> stream();
    
    /// {@return a specialised iterator allowing to iterate over every offset in the shape}
    @Override Iterator<Vec3i> iterator();
    
    /// {@return the number of blocks this shape encompasses}
    int size();
    
    //==================================================================================================================
    /// Builds a set of block positions based on the current shape.
    /// @param startPos The starting [BlockPos]
    /// @return The set of [BlockPos]
    ImmutableList<BlockPos> build(BlockPos startPos);

    //==================================================================================================================
    /// Expands the shape in a given direction.
    /// @param direction The direction to expand the shape
    /// @param amount    The amound of blocks to expand
    /// @return The new shape
    IAreaShape expand(Direction direction, int amount);
    
    /// Reduces the shape in a given direction.
    /// @param direction The direction to reduce the shape
    /// @param amount    The amound of blocks to reduce
    /// @return The new shape
    IAreaShape reduce(Direction direction, int amount);
    
    /// Gives the shape an offset.
    /// @param direction The direction to where to offset the shape
    /// @param amount    The amound of blocks to offset
    /// @return The newly offset shape
    IAreaShape move(Direction direction, int amount);
}
