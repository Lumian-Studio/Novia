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
package xyz.lumian.novia.api.gui.component.provided;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.RefUtils;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.IGuiTemplate;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.component.StatefulGuiComponent;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;

import java.util.*;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// A slider like component, which can be used to modify the position of other components upon scrolling.
///
/// Scroll bars provide input listeners for drag events where the mouse can pull the thumb around to scroll as well as
/// mouse wheel listeners that allow scrolling upon rotating the wheel when it is hovered.
///
/// By default, this component does not participate in the focus chain of the component hierarchy.
///
/// This is a stateful GUI component, the value it contains represents the normalised scroll offset of the specified
/// overflow area, it can be converted between number qualified [Value] objects.
public class NVScrollbar
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the scroll bar's background.
        /// @param canvas      The [Canvas]
        /// @param scrollbar   The [NVScrollbar]
        /// @param trackBounds The bounds of the track that the thumb can slide in
        void nvScrollbarDrawBackground(Canvas canvas, NVScrollbar scrollbar, Rectangle trackBounds);
        
        /// Draws the scroll bar's thumb.
        /// @param canvas    The [Canvas]
        /// @param scrollbar The [NVScrollbar]
        /// @param bounds    The bounds of the thumb
        void nvScrollbarDrawThumb(Canvas canvas, NVScrollbar scrollbar, Rectangle bounds);
    }
    
    /// Represents the scroll space handler.
    /// @param view    The minimised size (in pixels) that is being viewed
    /// @param content The content size (in pixels) of the area that can be scrolled
    public record ScrollSpace(int view, int content)
    {
        //**************************************************************************************************************
        public ScrollSpace
        {
            if (view < 0)
            {
                throw new IllegalArgumentException("view space must be positive");
            }
            
            if (content < 0)
            {
                throw new IllegalArgumentException("content space must be positive");
            }
        }
        
        //==============================================================================================================
        public int getOverflow() { return Math.max(0, (this.content - this.view)); }
        
        public int getThumbSize(final int totalSize, final int minThumbSize)
        {
            if (totalSize == 0)
            {
                return minThumbSize;
            }
            
            if (this.content == 0)
            {
                return totalSize;
            }
            
            return Math.clamp(
                (int) Math.round((double) this.view * totalSize / this.content),
                Math.min(minThumbSize, (totalSize - 1)),
                totalSize);
        }
        
        //==============================================================================================================
        public boolean isEmpty() { return (this.content == 0); }
        
        public boolean isOverflowing() { return (this.content > this.view); }
    }
    
    //******************************************************************************************************************
    /// See [NVScrollbar#delta].
    public static final int DEFAULT_DELTA = 10;
    
    /// See [NVScrollbar#vertical].
    public static final boolean DEFAULT_IS_VERTICAL = true;
    
    /// See [NVScrollbar#minThumbSize].
    public static final int DEFAULT_MIN_THUMB_SIZE = 16;
    
    //==================================================================================================================
    /// The texture used for the scroll bar thumb.
    public static final Identifier TEXTURE_SCROLLBAR_THUMB = Identifier.withDefaultNamespace("widget/scroller");
    
    /// The texture used for the background of the scroll bar.
	public static final Identifier TEXTURE_SCROLLBAR_BACKGROUND
        = Identifier.withDefaultNamespace("widget/scroller_background");
    
    public static final int PULL_START_DELAY_MS = 500;
    public static final int PULL_APPLY_DELAY_MS = 100;
    
    //******************************************************************************************************************
    /// Describes the scroll delta used to scroll with the mouse wheel or the keyboard.
    ///
    /// The scroll delta is a value that determines how much the scroll bar thumb will jump upon scrolling
    /// with the mouse wheel or the keyboard, based on the given height of the content to scroll.
    ///
    /// A value of 0 means no scrolling at all with these devices.
    public final GuiProperty.NonNull<Integer> delta;
    
    /// Describes the orientation of the scrollbar, if `true` this is a vertical scrollbar (top to bottom),
    /// or `false` if horizontal (left to right).
    public final GuiProperty.NonNull<Boolean> vertical;
    
    /// Describes the minimum size the thumb should have if the scroll space gets too big. This value can only take
    /// positive values starting at 1.
    ///
    /// It is best to leave this at the specified default; however, for exceptional cases it is provided.
    public final GuiProperty.NonNull<Integer> minThumbSize;
    
    //==================================================================================================================
    /// Triggered whenever the available scroll space of the scroll-bar changed.
    public final GuiEvent.Simple scrollSpaceChanged = new GuiEvent.Simple();
    
    //==================================================================================================================
    private final Rectangle trackBounds = new Rectangle();
    private final Rectangle thumbBounds = new Rectangle();
    
    private int         offset   = 0;
    private int         overflow = 0;
    private boolean     dragging = false;
    private boolean     using    = false;
    private boolean     canPull  = false;
    private long        startMs  = 0;
    private long        applyMs  = 0;
    private ScrollSpace space;
    
    //******************************************************************************************************************
    /// Constructs a new vertical scroll bar.
    /// @param scrollSpace The initial [ScrollSpace]
    /// @param message The scrollbar message
    public NVScrollbar(final ScrollSpace scrollSpace, final Component message)
    {
        super(message);
        
        this.vertical     = GuiPropertyBuilder.nonNull(NVScrollbar.DEFAULT_IS_VERTICAL)
            .withNoArgSetter(this::resized)
            .build();
        this.delta        = GuiProperty.nonNull(NVScrollbar.DEFAULT_DELTA);
        this.minThumbSize = GuiPropertyBuilder.nonNull(NVScrollbar.DEFAULT_MIN_THUMB_SIZE)
            .withNoArgSetter(this::updateThumbBounds)
            .withValidator(RefUtils.greaterThan(0))
            .build();
        
        this.space = Objects.requireNonNull(scrollSpace, "scroll space must not be null");
    }
    
    /// Constructs a new vertical scroll bar.
    /// @param scrollSpace The initial [ScrollSpace]
    public NVScrollbar(final ScrollSpace scrollSpace) { this(scrollSpace, CommonComponents.EMPTY); }
    
    /// Constructs a new vertical scrollbar with an empty scroll space.
    public NVScrollbar() { this(new ScrollSpace(0, 0), CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the current scroll offset as number qualified [Value] object. The value returned is a
    /// normalised float (see [#getOffsetNormalised()]).
    /// @return The current scroll offset [Value]
    @Override public Value getValue() { return new Value(this.getOffsetNormalised()); }
    
    /// Gets the current offset from the target's origin point.
    ///
    /// If the scroll bar's thumb is all the way on the top this will return 0, if it is all the way on the bottom
    /// this will return [#getOverflow()].
    /// @return The offset of the overflow
    public int getOffset() { return this.offset; }
    
    /// Gets the current offset as normalised number with values ranging between 0 (included) and 1 (included), where
    /// 0 means the start of the scroll bar and 1 means the end.
    /// @return The normalised scroll bar offset
    public float getOffsetNormalised() { return (this.overflow > 0 ? (this.offset / (float) this.overflow) : 0f); }
    
    /// Gets the maximum overflow the scroll bar can scroll to.
    /// @return The maximum overflow
    public int getOverflow() { return this.overflow; }
    
    @Override public @Nullable IComponentNavigator getNavigator() { return null; }
    
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SCROLLBAR_DELTA,
                this.delta,
                Codec.INT),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SCROLLBAR_VERTICAL,
                this.vertical,
                Codec.BOOL),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.SCROLLBAR_MIN_THUMB_SIZE,
                this.minThumbSize,
                Codec.INT)
        );
    }
    
    //------------------------------------------------------------------------------------------------------------------
    /// Track start position.
    protected int getTrackStart() { return 0; }
    
    /// Track end position.
    protected int getTrackEnd() { return (this.vertical.get() ? this.getHeight() : this.getWidth()); }
    
    /// Track size.
    protected int getTrackSize() { return Math.max(0, (this.getTrackEnd() - this.getTrackStart())); }
    
    //==================================================================================================================
    /// Sets the scroll offset from the given [Value] object (see [#getOffsetNormalised()]). If the
    /// [Value] object is not a number this will do nothing.
    /// @param value The scroll offset value
    @Override
    public void setValue(final Value value)
    {
        if (!value.isNumber())
        {
            return;
        }
        
        this.setOffsetNormalised(value.getNumber().floatValue());
    }
    
    /// Sets the scroll offset of the target on this scrollbar. The value is between 0 and [#getOverflow()];
    /// anything above or below will be clamped.
    /// @param offset The scroll offset
    public void setOffset(final Number offset)
    {
        final int new_offset = Math.clamp(offset.intValue(), 0, this.overflow);
        
        if (this.offset != new_offset)
        {
            this.offset = new_offset;
            
            this.updateThumbBounds();
            this.sendChangeNotification();
        }
    }
    
    /// Sets the scroll offset of the target on this scrollbar to the given normalised value
    /// (see [#getOffsetNormalised()]).
    /// @param offsetNormalised The normalised scroll offset
    public void setOffsetNormalised(final float offsetNormalised)
    {
        this.setOffset(this.overflow * Math.clamp(offsetNormalised, 0f, 1f));
    }
    
    /// Updates the overflow based on the given view and content space, where view space is the container of the content
    /// and content space is the space of the content.
    ///
    /// This will not be automatically determined upon resizing, the developer is responsible to update this manually.
    /// @param viewSpace    The maximum size of the area that is visible of the clipped content
    /// @param contentSpace The maximum size of the content
    public void setOverflow(final int viewSpace, final int contentSpace)
    {
        this.setOverflow(new ScrollSpace(viewSpace, contentSpace));
    }
    
    /// Updates the overflow based on the given view and content space, where view space is the container of the content
    /// and content space is the space of the content.
    ///
    /// This will not be automatically determined upon resizing, the developer is responsible to update this manually.
    /// @param scrollSpace The [ScrollSpace]
    public void setOverflow(final ScrollSpace scrollSpace)
    {
        if (scrollSpace.equals(this.space))
        {
            return;
        }
        
        this.space    = scrollSpace;
        this.overflow = scrollSpace.getOverflow();
        
        if (this.offset > this.overflow)
        {
            this.setOffset(this.overflow);
        }
        else
        {
            this.updateThumbBounds();
        }
        
        this.onScrollSpaceChanged();
        this.scrollSpaceChanged.post(this);
    }
    
    //==================================================================================================================
    /// Moves the offset by the given offset amount in pixels. If `offset` is negative, this will scroll backwards.
    /// @param offset The offset to scroll
    public void move(final int offset) { this.setOffset(this.offset + offset); }
    
    /// Moves the offset by the given number of [#delta] applied `steps` times.
    /// If `steps` is negative, this will scroll backwards.
    /// @param steps The number of times delta should be applied to the current scroll offset
    public void moveSteps(final int steps) { this.move(this.delta.get() * steps); }
    
    /// Moves the offset by the given page amount, by that means, `pages` times the view size.
    /// If `pages` is negative, this will scroll backwards.
    /// @param pages The number of pages to scroll
    public void movePages(final int pages) { this.move(this.space.view * pages); }
    
    /// Scrolls all the way to the start.
    public void moveToStart() { this.setOffset(0); }
    
    /// Scrolls all the way to the end.
    public void moveToEnd() { this.setOffset(this.overflow); }
    
    //==================================================================================================================
    @Override
    public boolean onMouseDown(final MouseEvent e)
    {
        if (!this.isActive())
        {
            this.using = false;
            return false;
        }
        
        this.canPull = false;
        this.using   = true;
        
        final int mouse_pos;
        final int thumb_start;
        final int thumb_end;
        
        if (this.vertical.get())
        {
            mouse_pos   = e.localMouseY();
            thumb_start = this.thumbBounds.y();
            thumb_end   = this.thumbBounds.getBottom();
        }
        else
        {
            mouse_pos   = e.localMouseX();
            thumb_start = this.thumbBounds.x();
            thumb_end   = this.thumbBounds.getRight();
        }
        
        if (mouse_pos < thumb_start)
        {
            this.movePages(-1);
            this.canPull  = true;
            this.startMs = Util.getMillis();
            this.applyMs = Util.getMillis();
        }
        else if (mouse_pos >= thumb_end)
        {
            this.movePages(1);
            this.canPull = true;
            this.startMs = Util.getMillis();
            this.applyMs = Util.getMillis();
        }
        else
        {
            this.dragging = true;
        }
        
        return true;
    }
    
    @Override
    public boolean onMouseScroll(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.moveSteps((int) -e.deltaY);
        return true;
    }
    
    @Override
    public boolean onMouseDrag(final MouseEvent e)
    {
        if (!this.isActive())
        {
            this.using    = false;
            this.dragging = false;
            
            return false;
        }
        
        if (!this.dragging)
        {
            return this.using;
        }
        
        final Point local_mouse_pos = e.localMousePos();
        
        final int    m_pos;
        final double m_delta;
        final int    m_area;
        
        if (this.vertical.get())
        {
            m_pos   = local_mouse_pos.y();
            m_delta = e.deltaY;
            m_area  = (this.getTrackSize() - this.thumbBounds.height());
        }
        else
        {
            m_pos   = local_mouse_pos.x();
            m_delta = e.deltaX;
            m_area  = (this.getTrackSize() - this.thumbBounds.width());
        }
        
        if (m_pos < this.getTrackStart())
        {
            this.moveToStart();
        }
        else if (m_pos > this.getTrackEnd())
        {
            this.moveToEnd();
        }
        else if (m_area > 0)
        {
            this.move((int) Math.round(m_delta * this.overflow / m_area));
        }
        
        return true;
    }
    
    @Override
    public boolean onMouseUp(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        this.dragging = false;
        this.canPull  = false;
        
        return true;
    }
    
    //==================================================================================================================
    @Override
    public void onDeltaTick(final Point mousePos, final float delta)
    {
        if (!this.canPull)
        {
            return;
        }
        
        final Point local_mouse_pos = this.toRelativePos(mousePos);
        
        if (this.thumbBounds.contains(local_mouse_pos))
        {
            this.canPull = false;
            return;
        }
        
        final long now = Util.getMillis();
        
        if (
            (now - this.startMs) > NVScrollbar.PULL_START_DELAY_MS
            && (now - this.applyMs) > NVScrollbar.PULL_APPLY_DELAY_MS
        )
        {
            final int mouse_pos;
            final int thumb_start;
            final int thumb_end;
            
            if (this.vertical.get())
            {
                mouse_pos   = local_mouse_pos.y();
                thumb_start = this.thumbBounds.y();
                thumb_end   = this.thumbBounds.getBottom();
            }
            else
            {
                mouse_pos   = local_mouse_pos.x();
                thumb_start = this.thumbBounds.x();
                thumb_end   = this.thumbBounds.getRight();
            }
            
            if (mouse_pos < thumb_start)
            {
                this.movePages(-1);
            }
            else if (mouse_pos > thumb_end)
            {
                this.movePages(1);
            }
            
            this.applyMs = now;
        }
    }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        final IGuiTemplate template = canvas.getTemplate();
        template.nvScrollbarDrawBackground(canvas, this, new Rectangle(this.trackBounds));
        template.nvScrollbarDrawThumb(canvas, this, new Rectangle(this.thumbBounds));
    }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        if (this.vertical.get())
        {
            this.trackBounds.setBounds(0, this.getTrackStart(), this.getWidth(), this.getTrackSize());
        }
        else
        {
            this.trackBounds.setBounds(this.getTrackStart(), 0, this.getTrackSize(), this.getHeight());
        }
        
        this.updateThumbBounds();
    }
    
    //==================================================================================================================
    /// Called whenever the available scroll space of the scroll-bar changed.
    public void onScrollSpaceChanged() {}
    
    //==================================================================================================================
    private void updateThumbBounds()
    {
        final int track_size = this.getTrackSize();
        final int thumb_size = (!this.space.isEmpty()
            ? this.space.getThumbSize(track_size, this.minThumbSize.get())
            : track_size);
        
        int thumb_pos = this.getTrackStart();
        
        if (this.space.isOverflowing())
        {
            thumb_pos += (int) Math.round((double) this.offset * (track_size - thumb_size) / this.overflow);
        }
        
        if (this.vertical.get())
        {
            this.thumbBounds.setBounds(0, thumb_pos, this.getWidth(), thumb_size);
        }
        else
        {
            this.thumbBounds.setBounds(thumb_pos, 0, thumb_size, this.getHeight());
        }
    }
}
