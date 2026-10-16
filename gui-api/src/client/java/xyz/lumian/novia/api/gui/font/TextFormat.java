/**
 * .-----------------. .----------------.  .----------------.  .----------------.  .----------------.
 * | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
 * | | ____  _____  | || |     ____     | || | ____   ____  | || |     _____    | || |      __      | |
 * | ||_   \|_   _| | || |   .'    `.   | || ||_  _| |_  _| | || |    |_   _|   | || |     /  \     | |
 * | |  |   \ | |   | || |  /  .--.  \  | || |  \ \   / /   | || |      | |     | || |    / /\ \    | |
 * | |  | |\ \| |   | || |  | |    | |  | || |   \ \ / /    | || |      | |     | || |   / ____ \   | |
 * | | _| |_\   |_  | || |  \  `--'  /  | || |    \ ' /     | || |     _| |_    | || | _/ /    \ \_ | |
 * | ||_____|\____| | || |   `.____.'   | || |     \_/      | || |    |_____|   | || ||____|  |____|| |
 * | |              | || |              | || |              | || |              | || |              | |
 * | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
 * '----------------'  '----------------'  '----------------'  '----------------'  '----------------'
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2025 LumiaLights
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.lumian.novia.api.gui.font;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.impl.StyleAccessor;

import java.nio.CharBuffer;
import java.util.*;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// Provides a list of nodes that represent a styling context of a certain portion of text in a string, each node
/// contains data about its current formatting context. This is possible because internally it will consume any type
/// of text you provide it, and whenever it encounters a new styling (either through explicitly set styles or through
/// format codes inside the text), it will create a new text node that spans the text which has the formatting applied.
public class TextFormat
    implements Iterable<TextFormat.Node>
{
    //******************************************************************************************************************
    /// Represents a unit of formatted text that is aliased to an external string.
    /// @param font         The text {@link GuiFont}
    /// @param colour       The text colour
    /// @param shadowColour The text background colour
    /// @param shadowColour The text shadow colour
    /// @param startIndex   The index of the first character that this node aliases
    /// @param endIndex     The index one past the last character that this node aliases
    public record Node(
                  GuiFont   font,
        @Nullable TextColor colour,
        @Nullable TextColor backgroundColour,
        @Nullable Integer   shadowColour,
                  int       startIndex,
                  int       endIndex
    )
    {
        //**************************************************************************************************************
        public Node { Objects.requireNonNull(font, "font"); }
        
        //==============================================================================================================
        public Node withRange(final int start, final int end)
        {
            return new Node(this.font, this.colour, this.backgroundColour, this.shadowColour, start, end);
        }
    }
    
    @FunctionalInterface
    public interface Visitor
    {
        //**************************************************************************************************************
        boolean accept(int index, Node node, CharBuffer content);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static class RangeVisitor
        implements ICharacterVisitor
    {
        //**************************************************************************************************************
        private final Deque<Node>   nodes   = new ArrayDeque<>(2);
        private final StringBuilder builder = new StringBuilder(16);
        
        private @Nullable Style   lastStyle = null;
        private @Nullable GuiFont lastFont  = null;
        private           int     start     = 0;
        private           int     end       = 0;
        
        //**************************************************************************************************************
        public Collection<Node> getNodes()
        {
            if (this.start != this.end)
            {
                this.nodes.addLast(new Node(
                    Objects.requireNonNull(this.lastFont),
                    Objects.requireNonNull(this.lastStyle).getColor(),
                    (this.lastStyle instanceof GuiStyle g_style ? g_style.getBackgroundColor() : null),
                    this.lastStyle.getShadowColor(),
                    this.start,
                    this.end));
                this.start = this.end;
            }
            
            return this.nodes;
        }
        
        public String getContent() { return this.builder.toString(); }
        
        //==============================================================================================================
        @Override
        public boolean accept(final int i, final GuiFont font, final Style style, final int codePoint)
        {
            // start next node
            if (font != this.lastFont)
            {
                if (this.lastStyle != null)
                {
                    this.nodes.addLast(new Node(
                        Objects.requireNonNull(this.lastFont),
                        Objects.requireNonNull(this.lastStyle).getColor(),
                        (this.lastStyle instanceof GuiStyle g_style ? g_style.getBackgroundColor() : null),
                        this.lastStyle.getShadowColor(),
                        this.start,
                        this.end));
                }
                
                this.lastStyle = style;
                this.lastFont  = font;
                this.start     = this.end;
            }
            
            ++this.end;
            this.builder.appendCodePoint(codePoint);
            
            return true;
        }
    }
    
    @SuppressWarnings("ClassCanBeRecord")
    private static class ThreeStateOptional<T>
    {
        //**************************************************************************************************************
        private final static Object EMPTY = new Object();
        
        //**************************************************************************************************************
        private final @Nullable Object object;
        
        //**************************************************************************************************************
        public ThreeStateOptional()                        { this.object = ThreeStateOptional.EMPTY; }
        public ThreeStateOptional(final @Nullable T value) { this.object = value; }
        
        //==============================================================================================================
        @SuppressWarnings("unchecked")
        public @Nullable T getValue()
        {
            if (this.isEmpty())
            {
                throw new IllegalStateException("empty");
            }
            
            return (T) this.object;
        }
        
        public boolean isEmpty() { return (this.object == ThreeStateOptional.EMPTY); }
        
        //==============================================================================================================
        public @Nullable T orElse(final @Nullable T elseValue)
        {
            return (this.isEmpty() ? elseValue : this.getValue());
        }
    }
    
    //******************************************************************************************************************
    /// Describes a text layout that spans a single line without wrapping or maximum adjustments.
    public static final TextLayout.LayoutOptions LAYOUT_OPTIONS = new TextLayout.LayoutOptions() {{
        wordWrap = TextLayout.WordWrap.NO_WRAP;
    }};
    
    //******************************************************************************************************************
    private final List<Node>    nodes   = new ArrayList<>();
    private final StringBuilder content = new StringBuilder();
    
    //******************************************************************************************************************
    /// {@return the content string represented by this text format}
    public String getContent() { return this.content.toString(); }
    
    /// {@return the computed nodes from the content string}
    public List<Node> getNodes() { return new ArrayList<>(this.nodes); }
    
    /// {@return the number of nodes in this text format}
    public int getNodeCount() { return this.nodes.size(); }
    
    /// {@return the number of characters of the text format content}
    public int length() { return this.content.length(); }
    
    /// {@return creates a stream with the nodes of this text format}
    public Stream<Node> stream() { return this.nodes.stream(); }
    
    /// Visits each node in this text format with the given visitor providing the node and a {@link CharBuffer} for the
    /// node's content. Other than {@link #forEachNode(Visitor)}, if the visitor returns {@code false},
    /// the task will be canceled and no further nodes will be visited.
    /// @param visitor The {@link Visitor} to use
    public void visit(final Visitor visitor)
    {
        final CharBuffer buffer = CharBuffer.wrap(this.content);
        
        for (int i = 0; i < this.getNodeCount(); ++i)
        {
            final Node       node    = this.nodes.get(i);
            final CharBuffer content = buffer.subSequence(node.startIndex, node.endIndex);
            
            if (!visitor.accept(i, node, content))
            {
                break;
            }
        }
    }
    
    /// Visits each node in this text format with the given visitor providing the node and a {@link CharBuffer} for the
    /// node's content. Other than {@link #visit(Visitor)}, all nodes will be visited regardless what the visitor
    /// returns.
    /// @param visitor The {@link Visitor} to use
    public void forEachNode(final Visitor visitor)
    {
        final CharBuffer buffer = CharBuffer.wrap(this.content);
        
        for (int i = 0; i < this.getNodeCount(); ++i)
        {
            final Node       node    = this.nodes.get(i);
            final CharBuffer content = buffer.subSequence(node.startIndex, node.endIndex);
            visitor.accept(i, node, content);
        }
    }
    
    //==================================================================================================================
    /// {@return gets whether this text format contains any nodes}
    public boolean isEmpty() { return this.nodes.isEmpty(); }
    
    //==================================================================================================================
    /// Appends the given string to this text format and consumes all formatting codes inside the text.
    /// @param text      The string to append
    /// @param baseFont  The base {@link GuiFont} to use if not overridden by style attributes
    /// @param baseStyle The parent {@link Style} to use
    /// @return `this`
    public TextFormat append(final String text, final GuiFont baseFont, final Style baseStyle)
    {
        this.appendStyled(ICharacterVisitor::visitFormatted, baseFont, baseStyle, text);
        return this;
    }
    
    /// Appends the given string to this text format and consumes all formatting codes inside the text.
    /// @param text     The string to append
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @return `this`
    public TextFormat append(final String text, final GuiFont baseFont)
    {
        this.appendNormal(ICharacterVisitor::visitFormatted, baseFont, text);
        return this;
    }
    
    /// Appends the given {@link FormattedText} to this text format and consumes all formatting codes inside the text.
    /// @param text      The {@link FormattedText} to append
    /// @param baseFont  The base {@link GuiFont} to use if not overridden by style attributes
    /// @param baseStyle The parent {@link Style} to use
    /// @return `this`
    public TextFormat append(final FormattedText text, final GuiFont baseFont, final Style baseStyle)
    {
        this.appendStyled(ICharacterVisitor::visitFormatted, baseFont, baseStyle, text);
        return this;
    }
    
    /// Appends the given {@link FormattedText} to this text format and consumes all formatting codes inside the text.
    /// @param text     The {@link FormattedText} to append
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @return `this`
    public TextFormat append(final FormattedText text, final GuiFont baseFont)
    {
        this.appendNormal(ICharacterVisitor::visitFormatted, baseFont, text);
        return this;
    }
    
    /// Appends the given {@link Component} to this text format and consumes all formatting codes inside the text.
    /// @param text      The {@link Component} to append
    /// @param baseFont  The base {@link GuiFont} to use if not overridden by style attributes
    /// @param baseStyle The parent {@link Style} to use
    /// @return `this`
    public TextFormat append(final Component text, final GuiFont baseFont, final Style baseStyle)
    {
        this.appendStyled(ICharacterVisitor::visitFormatted, baseFont, baseStyle, text);
        return this;
    }
    
    /// Appends the given {@link Component} to this text format and consumes all formatting codes inside the text.
    /// @param text     The {@link Component} to append
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @return `this`
    public TextFormat append(final Component text, final GuiFont baseFont)
    {
        this.appendNormal(ICharacterVisitor::visitFormatted, baseFont, text);
        return this;
    }
    
    /// Appends the given {@link FormattedCharSequence} to this text format and consumes all formatting codes inside the text.
    /// @param text      The {@link FormattedCharSequence} to append
    /// @param baseFont  The base {@link GuiFont} to use if not overridden by style attributes
    /// @param baseStyle The parent {@link Style} to use
    /// @return `this`
    public TextFormat append(final FormattedCharSequence text, final GuiFont baseFont, final Style baseStyle)
    {
        this.appendStyled(ICharacterVisitor::visitFormatted, baseFont, baseStyle, text);
        return this;
    }
    
    /// Appends the given {@link FormattedCharSequence} to this text format and consumes all formatting codes inside the text.
    /// @param text     The {@link FormattedCharSequence} to append
    /// @param baseFont The base {@link GuiFont} to use if not overridden by style attributes
    /// @return `this`
    public TextFormat append(final FormattedCharSequence text, final GuiFont baseFont)
    {
        this.appendNormal(ICharacterVisitor::visitFormatted, baseFont, text);
        return this;
    }
    
    /// Appends the given string to this text format but keeping the format codes. This will effectively create just
    /// a single node.
    /// @param text  The string to append
    /// @param font  The {@link GuiFont} to use
    /// @param style The {@link Style} to use which will also override the font
    /// @return `this`
    public TextFormat appendPlain(final String text, GuiFont font, final Style style)
    {
        if (((StyleAccessor) style).novia$isFontSet())
        {
            final FontDescription font_id = style.getFont();
            
            if (!font_id.equals(font.getDescriptor()))
            {
                font = font.withFont(font_id);
            }
        }
        
        font.setStyle(style);
        this.appendPlain(text, font);
        
        return this;
    }
    
    /// Appends the given string to this text format but keeping the format codes. This will effectively create just
    /// a single node.
    /// @param text The string to append
    /// @param font The {@link GuiFont} to use
    /// @return `this`
    public TextFormat appendPlain(final String text, final GuiFont font)
    {
        this.nodes.add(new Node(font, null, null, null, 0, text.length()));
        this.content.append(text);
        return this;
    }
    
    /// Appends another {@link TextFormat} to this one. If the text format is the same as this one, the content will be
    /// doubled, therefore it might be worth checking beforehand.
    /// @param other The other {@link TextFormat}
    /// @return `this`
    public TextFormat append(final TextFormat other)
    {
        final int offset = this.content.length();
        this.content.append(other.content);
        this.nodes.addAll(other.nodes
            .stream()
            .map(node -> new Node(
                node.font,
                node.colour,
                node.backgroundColour,
                node.shadowColour,
                (node.startIndex + offset),
                (node.endIndex   + offset)))
            .toList());
        return this;
    }
    
    /// Replaces the underlying text content of this text format. If the new content is larger than the previous,
    /// a new node will be added with {@link GuiFont#DEFAULT} and {@link Style#EMPTY}. If it is smaller, the nodes
    /// will be truncated to fit the new content, otherwise the nodes will be left untouched.
    /// @param newContent The new content string to set for this text format
    public void replaceContent(final String newContent)
    {
        final int new_len = newContent.length();
        final int old_len = this.length();
        
        if (new_len > old_len)
        {
            this.append(newContent.substring(new_len - old_len), GuiFont.DEFAULT.get());
        }
        else if (new_len < old_len)
        {
            final var it = this.nodes.listIterator(this.nodes.size());
            
            while (it.hasPrevious())
            {
                final Node node = it.previous();
                it.remove();
                
                if (new_len > node.startIndex)
                {
                    this.nodes.add(node.withRange(node.startIndex, new_len));
                    break;
                }
            }
        }
        
        this.content.replace(0, this.content.length(), newContent);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> void appendNormal(final Function3<ICharacterVisitor, GuiFont, T, Boolean> visitorFunc,
                                  final GuiFont baseFont, final T text)
    {
        final RangeVisitor visitor = new RangeVisitor();
        visitorFunc.apply(visitor, baseFont, text);
        this.nodes  .addAll(visitor.getNodes());
        this.content.append(visitor.getContent());
    }
    
    private <T> void appendStyled(
        final Function4<ICharacterVisitor, GuiFont, Style, T, Boolean> visitorFunc,
        final GuiFont                                                     baseFont,
        final Style                                                       style,
        final T                                                           text
    )
    {
        final RangeVisitor visitor = new RangeVisitor();
        visitorFunc.apply(visitor, baseFont, style, text);
        this.nodes  .addAll(visitor.getNodes());
        this.content.append(visitor.getContent());
    }
    
    //==================================================================================================================
    /// Sets the {@link GuiFont} for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param font       The {@link GuiFont} to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setFont(final GuiFont font, final int startIndex, final int endIndex)
    {
        this.splitAndApplyToRange(Optional.of(font), new ThreeStateOptional<>(), new ThreeStateOptional<>(),
                                  new ThreeStateOptional<>(), startIndex, endIndex);
    }
    
    /// Sets the text colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The text colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setColour(final @Nullable TextColor colour, final int startIndex, final int endIndex)
    {
        this.splitAndApplyToRange(Optional.empty(), new ThreeStateOptional<>(colour), new ThreeStateOptional<>(),
                                  new ThreeStateOptional<>(), startIndex, endIndex);
    }
    
    /// Sets the text colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The text colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setColour(final int colour, final int startIndex, final int endIndex)
    {
        this.setColour(TextColor.fromRgb(colour), startIndex, endIndex);
    }
    
    /// Sets the text background colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The text background colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setColour(final Colour colour, final int startIndex, final int endIndex)
    {
        this.setColour(colour.colour(), startIndex, endIndex);
    }
    
    /// Sets the text background colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The text background colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setBackgroundColour(final @Nullable TextColor colour, final int startIndex, final int endIndex)
    {
        this.splitAndApplyToRange(Optional.empty(), new ThreeStateOptional<>(), new ThreeStateOptional<>(colour),
                                  new ThreeStateOptional<>(), startIndex, endIndex);
    }
    
    /// Sets the text background colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The text background colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setBackgroundColour(final int colour, final int startIndex, final int endIndex)
    {
        this.setBackgroundColour(TextColor.fromRgb(colour), startIndex, endIndex);
    }
    
    /// Sets the text colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The text colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setBackgroundColour(final Colour colour, final int startIndex, final int endIndex)
    {
        this.setBackgroundColour(colour.colour(), startIndex, endIndex);
    }
    
    /// Sets the shadow colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The shadow colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setShadowColour(final @Nullable Integer colour, final int startIndex, final int endIndex)
    {
        this.splitAndApplyToRange(Optional.empty(), new ThreeStateOptional<>(), new ThreeStateOptional<>(),
                                  new ThreeStateOptional<>(colour), startIndex, endIndex);
    }
    
    /// Sets the shadow colour for the given node range. If the range crosses node boundaries, nodes will be
    /// merged and split to adhere to the new formatting.
    /// @param colour     The shadow colour to set for the range
    /// @param startIndex The start index of the range
    /// @param endIndex   The end index of the range
    /// @throws IndexOutOfBoundsException If either start index or end index are out of bounds, or if start index is
    ///                                   greater than end index
    public void setShadowColour(final Colour colour, final int startIndex, final int endIndex)
    {
        this.setShadowColour(colour.colour(), startIndex, endIndex);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void splitAndApplyToRange(final Optional<GuiFont> optFont,
                                      final ThreeStateOptional<TextColor> optColour,
                                      final ThreeStateOptional<TextColor> optBackgroundColour,
                                      final ThreeStateOptional<Integer> optShadowColour,
                                      final int rangeStart, final int rangeEnd)
    {
        if (rangeStart == rangeEnd)
        {
            return;
        }
        
        if (rangeStart < 0 || rangeEnd > this.length() || rangeStart > rangeEnd)
        {
            throw new IndexOutOfBoundsException("start or end index are out of range");
        }
        
        final Deque<Node> new_nodes = new ArrayDeque<>(this.nodes);
        Node intermediary = null;
        
        for (final var node : this.nodes)
        {
            if (rangeStart >= node.endIndex || (rangeEnd < node.endIndex && rangeEnd <= node.startIndex))
            {
                new_nodes.addLast(node);
                continue;
            }
            else if (rangeStart >= node.startIndex)
            {
                // re-add start node up to the start of the range
                new_nodes.addLast(node.withRange(node.startIndex, rangeStart));
                intermediary = new Node(optFont.orElse(node.font), optColour.orElse(node.colour),
                                        optBackgroundColour.orElse(node.backgroundColour),
                                        optShadowColour.orElse(node.shadowColour), rangeStart, -1);
            }
            
            assert (intermediary != null);
            
            if (rangeEnd <= node.endIndex)
            {
                new_nodes.addLast(intermediary.withRange(intermediary.startIndex, rangeEnd));
                
                // make sure, if the end index was not equal, to also add the remaining part
                if (rangeEnd < node.endIndex)
                {
                    new_nodes.addLast(node.withRange(rangeEnd, node.endIndex));
                }
            }
            else
            {
                
                if (
                       (optFont.isPresent()        || intermediary.font != node.font)
                    && (!optColour      .isEmpty() || !Objects.equals(intermediary.colour,       node.colour))
                    && (!optShadowColour.isEmpty() || !Objects.equals(intermediary.shadowColour, node.shadowColour))
                    && (
                        !optBackgroundColour.isEmpty()
                        || !Objects.equals(intermediary.backgroundColour, node.backgroundColour)
                    )
                )
                {
                    new_nodes.addLast(intermediary.withRange(intermediary.startIndex, node.startIndex));
                    intermediary = new Node(optFont.orElse(node.font), optColour.orElse(node.colour),
                                            optBackgroundColour.orElse(node.backgroundColour),
                                            optShadowColour.orElse(node.shadowColour), node.startIndex, -1);
                }
            }
        }
        
        this.nodes.clear();
        this.nodes.addAll(new_nodes);
    }
    
    //==================================================================================================================
    /// Clears this text format's nodes and content.
    public void clear()
    {
        this.nodes  .clear();
        this.content.setLength(0);
    }
    
    //==================================================================================================================
    /// Draws the nodes of this format context.
    /// @param canvas The [Canvas] to draw on
    /// @param x      The x coordinate of the text
    /// @param y      The y coordinate of the text
    public void draw(final Canvas canvas, int x, final int y)
    {
        if (!this.nodes.isEmpty())
        {
            (new TextLayout(this, TextFormat.LAYOUT_OPTIONS)).draw(
                canvas,
                new Rectangle(x, y, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
    }
    
    //==================================================================================================================
    @Override public Iterator<Node> iterator() { return this.nodes.iterator(); }
}
