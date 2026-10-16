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
package xyz.lumian.novia.api.gui.canvas;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;



//**********************************************************************************************************************
/// Describes a numeric value that represents a colour component for the entire component system. Each colour ID is
/// unique and cannot be defined twice.
public final class ColourId
    extends Number
    implements Comparable<Integer>
{
    //******************************************************************************************************************
    /// Describes a range of colour IDs.
    public static final class Range
    {
        //**************************************************************************************************************
        /// The list of colour IDs associated with this range.
        private final ImmutableList<ColourId> ids;
        
        //**************************************************************************************************************
        Range(final int start, final int count)
        {
            this.ids = IntStream
                .range(start, (start + count))
                .mapToObj(ColourId::new)
                .collect(ImmutableList.toImmutableList());
        }
        
        //==============================================================================================================
        /// Gets a colour ID at the given index from the range.
        /// @param index The index of the ID
        /// @return The [ColourId]
        /// @throws IndexOutOfBoundsException If the index is not inside the range
        public ColourId get(final int index) { return this.ids.get(index); }

        /// Gets the entire list of colour IDs inside this range.
        /// @return The immutable list containing the colour IDs
        public List<ColourId> getIDs() { return this.ids; }
    }
    
    //******************************************************************************************************************
    private static final AtomicInteger CURRENT_ID = new AtomicInteger(0);
    
    //==================================================================================================================
    /// Allocates a new ID for use with the component system.
    /// @return The new [ColourId]
    public static ColourId reserve() { return new ColourId(CURRENT_ID.getAndIncrement()); }

    /// Allocates a range of new colour IDs for use with the component system.
    /// @param count The number of IDs to reserver
    /// @return The new ID [Range]
    public static Range reserveRange(final int count) { return new Range(CURRENT_ID.getAndAdd(count), count); }

    /// Gets the current number of allocated colour IDs.
    /// @return The number of IDs
    public static int getSlotCount() { return CURRENT_ID.get(); }
    
    //******************************************************************************************************************
    /// The integral ID of this colour ID object.
    public final int id;
    
    //******************************************************************************************************************
    ColourId(final int id) { this.id = id; }
    
    //==================================================================================================================
    @Override public int    intValue()    { return this.id; }
    @Override public long   longValue()   { return this.id; }
    @Override public float  floatValue()  { return this.id; }
    @Override public double doubleValue() { return this.id; }
    
    //==================================================================================================================
    @Override public String toString() { return ("ColourId#" + this.id); }
    
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                      return true;
        if (!(obj instanceof ColourId other)) return false;
        return (this.id == other.id);
    }
    
    @Override public int hashCode() { return this.id; }

    //==================================================================================================================
    @Override public int compareTo(final Integer o) { return Integer.compare(this.id, o); }
}
