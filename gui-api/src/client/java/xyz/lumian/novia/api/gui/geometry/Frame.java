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
/// A geometric utility class providing tools for handling outlines around other shapes such as margins, padding or
/// borders. Frames are immutable.
///
/// ## Precision
/// A frame is always dealing with integers due to Minecraft's limited geometry. Nevertheless, all methods in the
/// Frame class take a [Number] object which can be passed any numeric object that inherits this interface
/// of which all will be internally converted to integers (via [Number#intValue()]). This may lead to loss of precision
/// when dealing with floating point values, as they are truncated to integral values, hence any rounding should be
/// done before passing them on to the Frame API.
///
/// ## Consumable
/// Frames provide a functional layer, that allows its bounding components to be directly embedded into a different
/// invocation or to be transformed into another object entirely. For this occasion, the API provides
/// [#accept(Frame.BoundingConsumer)] and [#transform(Frame.Transformer)] methods.
///
/// ## Java
/// Comparing instances of this class (see [Object#equals(Object)]) will compare the bounding components of this
/// frame and not its identity. It also provides a custom [Object#hashCode()] implementation based on these components.
///
/// Converting this class to string (see [Object#toString()]) will be in the following format:
/// `Frame{left=,top=,right=,bottom=}`
/// @param left   The thickness of the left side of this frame
/// @param top    The thickness of the top side of this frame
/// @param right  The thickness of the right side of this frame
/// @param bottom The thickness of the bottom side of this frame
public record Frame(int left, int top, int right, int bottom)
{
    //******************************************************************************************************************
    /// A functional interface that allows consuming a frame's bounding components.
    @FunctionalInterface
    public interface BoundingConsumer
    {
        //**************************************************************************************************************
        /// Accepts the bounding components.
        /// @param left   The thickness of the left side of the frame
        /// @param top    The thickness of the top side of the frame
        /// @param right  The thickness of the right side of the frame
        /// @param bottom The thickness of the bottom side of the frame
        void accept(int left, int top, int right, int bottom);
    }
    
    /// A functional interface that allows consuming a frame's bounding components and transforming them to a
    /// different object.
    @FunctionalInterface
    public interface Transformer<T>
    {
        //**************************************************************************************************************
        /// Applies the bounding components.
        /// @param left   The thickness of the left side of the frame
        /// @param top    The thickness of the top side of the frame
        /// @param right  The thickness of the right side of the frame
        /// @param bottom The thickness of the bottom side of the frame
        /// @return The new object that this frame's bounding components were transformed to
        T apply(int left, int top, int right, int bottom);
    }
    
    //******************************************************************************************************************
    /// Constructs a new [Frame].
    /// @param left   The thickness of the left side of the frame
    /// @param top    The thickness of the top side of the frame
    /// @param right  The thickness of the right side of the frame
    /// @param bottom The thickness of the bottom side of the frame
    public Frame {}
    
    /// Constructs a new [Frame].
    /// @param allSides The thickness of all sides of the frame
    public Frame(final int allSides) { this(allSides, allSides, allSides, allSides); }
    
    /// Constructs a new [Frame].
    /// @param top          The thickness of the top side of the frame
    /// @param leftAndRight The thickness of the top and bottom side of the frame
    /// @param bottom       The thickness of the bottom side of the frame
    public Frame(final int top, final int leftAndRight, final int bottom)
    {
        this(leftAndRight, top, leftAndRight, bottom);
    }
    
    /// Constructs a new [Frame].
    /// @param leftAndRight The thickness of the left and right side of the frame
    /// @param topAndBottom The thickness of the top and bottom side of the frame
    public Frame(final int leftAndRight, final int topAndBottom)
    {
        this(leftAndRight, topAndBottom, leftAndRight, topAndBottom);
    }
    
    /// Constructs a new empty [Frame] with the thickness of all sides initialised to zero.
    public Frame() { this(0, 0, 0, 0); }
    
    //==================================================================================================================
    /// Gets a new [Frame] with the left side set to the given thickness `left`.
    /// @param left The new thickness for the left side
    /// @return The new [Frame]
    public Frame withLeft(final int left) { return new Frame(left, this.top, this.right, this.bottom); }
    
    /// Gets a new [Frame] with the top side set to the given thickness `top`.
    /// @param top The new thickness for the top side
    /// @return The new [Frame]
    public Frame withTop(final int top) { return new Frame(this.left, top, this.right, this.bottom); }
    
    /// Gets a new [Frame] with the right side set to the given thickness `right`.
    /// @param right The new thickness for the right side
    /// @return The new [Frame]
    public Frame withRight(final int right) { return new Frame(this.right, this.top, right, this.bottom); }
    
    /// Gets a new [Frame] with the bottom side set to the given thickness `bottom`.
    /// @param bottom The new thickness for the bottom side
    /// @return The new [Frame]
    public Frame withBottom(final int bottom) { return new Frame(this.left, this.top, this.right, bottom); }
    
    /// Gets a new [Frame] with the given sides expanded by the given amounts, negative values will reduce the
    /// thickness.
    /// @param left   The amount to expand the left
    /// @param top    The amount to expand the top
    /// @param right  The amount to expand the right
    /// @param bottom The amount to expand the bottom
    /// @return The new [Frame]
    public Frame withExpansion(final int left, final int top, final int right, final int bottom)
    {
        return new Frame((this.left + left), (this.top + top), (this.right + right), (this.bottom + bottom));
    }
    
    /// Gets a new [Frame] with the given sides expanded by the given amount, negative values will reduce the
    /// thickness.
    /// @param allSides The amount to expand on all sides
    /// @return The new [Frame]
    public Frame withExpansion(final int allSides)
    {
        return new Frame((this.left + allSides), (this.top + allSides), (this.right + allSides),
                         (this.bottom + allSides));
    }
    
    /// Gets a new [Frame] with the given sides expanded by the given amount, negative values will reduce the
    /// thickness.
    /// @param leftAndRight The amount to expand on the left and right side
    /// @param topAndBottom The amount to expand on the top and bottom side
    /// @return The new [Frame]
    public Frame withExpansion(final int leftAndRight, final int topAndBottom)
    {
        return new Frame((this.left + leftAndRight), (this.top + topAndBottom), (this.right + leftAndRight),
                         (this.bottom + topAndBottom));
    }
    
    /// Gets a new [Frame] with the left side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the left side
    /// @return The new [Frame]
    public Frame leftExpanded(final int amount)
    {
        return new Frame((this.left + amount), this.top, this.right, this.bottom);
    }
    
    /// Gets a new [Frame] with the top side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the top side
    /// @return The new [Frame]
    public Frame topExpanded(final int amount)
    {
        return new Frame(this.left, (this.top + amount), this.right, this.bottom);
    }
    
    /// Gets a new [Frame] with the right side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the right side
    /// @return The new [Frame]
    public Frame rightExpanded(final int amount)
    {
        return new Frame(this.left, this.top, (this.right + amount), this.bottom);
    }
    
    /// Gets a new [Frame] with the bottom side expanded by the given amount, negative values will reduce the thickness.
    /// @param amount The amount to expand on the bottom side
    /// @return The new [Frame]
    public Frame bottomExpanded(final int amount)
    {
        return new Frame(this.left, this.top, this.right, (this.bottom + amount));
    }
    
    //==================================================================================================================
    /// Accepts this frame's bounding components with the given consumer.
    /// @param consumer The [BoundingConsumer] to accept
    public void accept(final BoundingConsumer consumer)
    {
        consumer.accept(this.left, this.top, this.right, this.bottom);
    }
    
    /// Applies this frame's bounding components to the given transformer and returns the result of this operation.
    /// @param transformer The [Transformer] to apply this frame's bounding components to
    /// @return The result of this operation
    /// @param <T> The result type of this operation
    public <T> T transform(final Transformer<T> transformer)
    {
        return transformer.apply(this.left, this.top, this.right, this.bottom);
    }
}
