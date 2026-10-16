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
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;
import xyz.lumian.novia.api.gui.event.GuiEventArgs;
import xyz.lumian.novia.api.gui.event.GuiEventHandler;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.GuiPropertyAttorney;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;

import java.util.function.Function;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// A container component, which allows its content to be scrolled by two scroll-bars if it is too big to be fully
/// contained.
///
/// When hovered over the viewport and no child wants scroll events, this will scroll the content inside the viewport.
/// Using shift while scrolling allows scrolling horizontally.
///
/// The scroll-bars can be selectively disabled or enabled and set to be shown automatically or always.
public class NVViewport
    extends GuiComponent
{
    //******************************************************************************************************************
    /// The display behaviour of the scroll-bars inside the viewport.
    public enum ScrollbarBehaviour
        implements StringRepresentable
    {
        /// Always show the scroll-bar, even if the content fits into the viewport or if nothing is being contained.
        ALWAYS,
        
        /// Only show the scroll-bar when there is content, which overflows the viewport.
        AUTO,
        
        /**
         * Never show the scroll-bar, even if there is overflowing content.
         * Content can only be scrolled by explicitly calling the scroll methods.
         */
        NEVER,
        ;
        
        //**************************************************************************************************************
        public static final Codec<ScrollbarBehaviour> CODEC = StringRepresentable
            .fromEnum(ScrollbarBehaviour::values);
        
        //**************************************************************************************************************
        @Override public String getSerializedName() { return this.name().toLowerCase(); }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        void nvViewportDrawBackground(Canvas canvas, NVViewport viewport);
        void nvViewportDrawCorner(Canvas canvas, NVViewport viewport, int width, int height);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private class Corner
        extends GuiComponent
    {
        //**************************************************************************************************************
        @Override
        public void draw(final Canvas canvas)
        {
            canvas.getTemplate().nvViewportDrawCorner(canvas, NVViewport.this, this.getWidth(), this.getHeight());
        }
    }
    
    //******************************************************************************************************************
    /// See [NVViewport#behaviourHorizontal] and [NVViewport#behaviourVertical].
    public static final ScrollbarBehaviour DEFAULT_SCROLLBAR_BEHAVIOUR = ScrollbarBehaviour.AUTO;
    
    /// See [NVViewport#scrollbarThickness].
    public static final int DEFAULT_SCROLLBAR_THICKNESS = 6;
    
    //******************************************************************************************************************
    /// Describes the behaviour of the horizontal scroll bar (on the bottom).
    /// @see ScrollbarBehaviour
    public final GuiProperty.NonNull<ScrollbarBehaviour> behaviourHorizontal;
    
    /// Describes the behaviour of the vertical scroll bar (on the right).
    /// @see ScrollbarBehaviour
    public final GuiProperty.NonNull<ScrollbarBehaviour> behaviourVertical;
    
    /// Describes the thickness of the scroll bars on the bottom and on the right in pixels.
    public final GuiProperty.NonNull<Integer> scrollbarThickness;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Corner                        cornerComponent;
    private final NVScrollbar                   horizontalScrollbar;
    private final NVScrollbar                   verticalScrollbar;
    private final GuiEventHandler<GuiEventArgs> contentResizedHandler;
    private final GuiEventHandler<GuiEventArgs> contentMovedHandler;
    
    private boolean internalMove = false;
    
    @Nullable
    private GuiComponent content = null;
    
    //******************************************************************************************************************
    /// Creates a new viewport with the given content component and message.
    /// @param content The content component of the viewport
    /// @param message The component's message
    public NVViewport(final @Nullable GuiComponent content, final Component message)
    {
        super(message);
        
        this.behaviourHorizontal = GuiPropertyBuilder
            .nonNull(NVViewport.DEFAULT_SCROLLBAR_BEHAVIOUR)
            .withNoArgSetter(() -> this.updateScrollbars(false))
            .build();
        this.behaviourVertical   = GuiPropertyBuilder
            .nonNull(NVViewport.DEFAULT_SCROLLBAR_BEHAVIOUR)
            .withNoArgSetter(() -> this.updateScrollbars(false))
            .build();
        this.scrollbarThickness  = GuiPropertyBuilder
            .nonNull(NVViewport.DEFAULT_SCROLLBAR_THICKNESS)
            .withNoArgSetter(this::resized)
            .build();
        
        final Function<Boolean, NVScrollbar> scrollbar_factory = (vertical -> Util.make(new NVScrollbar(), bar ->
        {
            bar.setVisible(false);
            bar.setPinned(true);
            bar.vertical.set(vertical);
        }));
        
        this.horizontalScrollbar = this.addChild(scrollbar_factory.apply(false));
        this.verticalScrollbar   = this.addChild(scrollbar_factory.apply(true));
        this.cornerComponent     = this.addChild(new Corner());
        
        this.horizontalScrollbar.valueChanged.subscribe(this::updateOffset);
        this.verticalScrollbar  .valueChanged.subscribe(this::updateOffset);
        
        this.contentResizedHandler = ((sender, args) -> this.contentBoundsChanged(sender, true,  false));
        this.contentMovedHandler   = ((sender, args) -> this.contentBoundsChanged(sender, false, true));
        
        this.setContent(content);
    }
    
    /// Creates a new viewport with the given content component and empty message.
    /// @param content The content component of the viewport
    public NVViewport(final @Nullable GuiComponent content) { this(content, CommonComponents.EMPTY); }
    
    /// Creates a new empty viewport with an empty message.
    public NVViewport() { this(null, CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the content component currently associated with this viewport.
    /// @return The associated [GuiComponent], or `null` if there is no content
    public @Nullable GuiComponent getContent() { return this.content; }
    
    /// Gets the current horizontal offset of the viewport content.
    /// @return The offset or 0 if there is no content
    public int getHorizontalOffset() { return (this.content != null ? (this.content.getX() * -1) : 0); }
    
    /// Gets the current vertical offset of the viewport content.
    /// @return The offset or 0 if there is no content
    public int getVerticalOffset() { return (this.content != null ? (this.content.getY() * -1) : 0); }
    
    /// Gets the content overflow of the content on the left-axis.
    /// @return The overflow
    /// @see NVScrollbar#getOverflow()
    public int getHorizontalOverflow() { return this.horizontalScrollbar.getOverflow(); }
    
    /// Gets the content overflow of the content on the y-axis.
    /// @return The overflow
    /// @see NVScrollbar#getOverflow()
    public int getVerticalOverflow() { return this.verticalScrollbar.getOverflow(); }
    
    /// Gets the width of the view area for the content, this is the width of the viewport minus the thickness
    /// of the scrollbar (if it is shown).
    /// @return The view area width
    public int getViewWidth()
    {
        return Math.max(
            0,
            (this.getWidth() - (this.verticalScrollbar.isVisible() ? this.scrollbarThickness.get() : 0)));
    }
    
    /// Gets the height of the view area for the content, this is the height of the viewport minus the thickness
    /// of the scrollbar (if it is shown).
    /// @return The view area height
    public int getViewHeight()
    {
        return Math.max(
            0,
            (this.getHeight() - (this.horizontalScrollbar.isVisible() ? this.scrollbarThickness.get() : 0)));
    }
    
    /// Gets the bounds of the view area for the viewport.
    /// @return The view bounds
    public Rectangle getViewRect() { return new Rectangle(0, 0, this.getViewWidth(), this.getViewHeight()); }
    
    /// Gets the area of the content component currently visible inside the viewport's view area, relative to
    /// the content's local bounds.
    /// @return The visible area of the content, or an empty [Rectangle] if there is no content
    public Rectangle getVisibleRect()
    {
        if (this.content == null)
        {
            return new Rectangle();
        }
        
        final int offset_x   = (this.content.getX() * -1);
        final int offset_y   = (this.content.getY() * -1);
        final int max_width  = Math.max(0, this.content.getRight());
        final int max_height = Math.max(0, this.content.getBottom());
        
        return new Rectangle(offset_x, offset_y, Math.min(max_width, this.getViewWidth()),
                             Math.min(max_height, this.getViewHeight()));
    }
    
    @Override
    public @Nullable IComponentNavigator getNavigator()
    {
        return (this.content != null ? this.content.getNavigator() : null);
    }
    
    /// Gets the horizontal scroll bar component for the viewport.
    ///
    /// This should REALLY only be used for customising the scrollbar's appearance or delta value, everything else
    /// should remain managed by this viewport.
    /// @return The horizontal [NVScrollbar]
    public NVScrollbar getHorizontalScrollbar() { return this.horizontalScrollbar; }
    
    /// Gets the vertical scroll bar component for the viewport.
    ///
    /// This should REALLY only be used for customising the scrollbar's appearance or delta value, everything else
    /// should remain managed by this viewport.
    /// @return The vertical [NVScrollbar]
    public NVScrollbar getVerticalScrollbar() { return this.verticalScrollbar; }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.VIEWPORT_BEHAVIOR_HORIZONTAL,
                this.behaviourHorizontal,
                ScrollbarBehaviour.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.VIEWPORT_BEHAVIOR_VERTICAL,
                this.behaviourVertical,
                ScrollbarBehaviour.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.VIEWPORT_SCROLLBAR_THICKNESS,
                this.scrollbarThickness,
                Codec.INT));
    }
    
    //==================================================================================================================
    /// Gets whether the horizontal scroll bar is currently shown.
    /// @return `true` if the horizontal scroll bar is shown
    public boolean isHorizontalScrollbarShown() { return this.horizontalScrollbar.isVisible(); }
    
    /// Gets whether the vertical scroll bar is currently shown.
    /// @return `true` if the vertical scroll bar is shown
    public boolean isVerticalScrollbarShown() { return this.verticalScrollbar.isVisible(); }
    
    /// Gets whether the content width is greater than the view width.
    /// @return `true` if the content is overflowing horizontally
    public boolean isOverflowingHorizontally()
    {
        return (this.content != null && this.content.getWidth() > this.getViewWidth());
    }
    
    /// Gets whether the content height is greater than the view height.
    /// @return `true` if the content is overflowing vertically
    public boolean isOverflowingVertically()
    {
        return (this.content != null && this.content.getHeight() > this.getViewHeight());
    }
    
    /// Gets whether the content is overflowing either vertically or horizontally.
    /// @return `true` if the content is overflowing
    public boolean isOverflowing() { return (this.isOverflowingHorizontally() || this.isOverflowingVertically()); }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean isPinningAllowed(final GuiComponent child)
    {
        return (child == this.horizontalScrollbar || child == this.verticalScrollbar);
    }
    
    @Override public boolean isAutoPositioningAllowed(GuiComponent child) { return false; }
    
    //==================================================================================================================
    /// Sets the viewports current content component. If this viewport currently holds a different content-component,
    /// the old one will be removed, and the new one added.
    /// @param content The component to set as content
    public void setContent(final @Nullable GuiComponent content)
    {
        if (this.content == content)
        {
            return;
        }
        
        if (this.content != null)
        {
            this.removeChild(0);
        }
        
        this.content = content;
        
        if (content != null)
        {
            content.getPositioner().ifPresent(pos ->
            {
                throw new IllegalStateException("a viewport's content component must not have a positioner");
            });
            
            content.setPosition(0, 0);
            this.addChild(this.content, 0);
        }
        
        this.updateScrollbars(true);
    }
    
    /// Sets the scroll bar behaviour for both the horizontal and vertical scroll bars.
    /// @param behaviour The new [ScrollbarBehaviour]
    public void setScrollbarBehaviour(final ScrollbarBehaviour behaviour)
    {
        this.setScrollbarBehaviour(behaviour, behaviour);
    }
    
    /// Sets the scroll bar behaviour for both the horizontal and vertical scroll bars.
    /// @param horizontalBehaviour The new horizontal [ScrollbarBehaviour]
    /// @param verticalBehaviour   The new vertical [ScrollbarBehaviour]
    public void setScrollbarBehaviour(final ScrollbarBehaviour horizontalBehaviour,
                                      final ScrollbarBehaviour verticalBehaviour)
    {
        GuiPropertyAttorney.setWithoutNotification(this.behaviourHorizontal, horizontalBehaviour);
        GuiPropertyAttorney.setWithoutNotification(this.behaviourVertical,   verticalBehaviour);
        
        this.updateScrollbars(false);
    }
    
    /// Sets the horizontal scroll offset of the content component (see [NVScrollbar#setOffset(Number)]).
    /// @param offset The offset
    public void setHorizontalOffset(final int offset) { this.horizontalScrollbar.setOffset(offset); }
    
    /// Sets the vertical scroll offset of the content component (see [NVScrollbar#setOffset(Number)]).
    /// @param offset The offset
    public void setVerticalOffset(final int offset) { this.verticalScrollbar.setOffset(offset); }
    
    //==================================================================================================================
    /// Moves the viewport content by the given offset amount in pixels. If the offset is negative, this will scroll
    /// backwards.
    /// @param offsetX The offset to scroll horizontally
    /// @param offsetY The offset to scroll vertically
    public void move(final int offsetX, final int offsetY)
    {
        this.horizontalScrollbar.move(offsetX);
        this.verticalScrollbar  .move(offsetY);
    }
    
    /// Moves the viewport content by the given scroll delta applied `steps` times. If `steps` is negative, this will
    /// scroll backwards.
    /// @param stepsX The number of times delta should be applied to the current horizontal scroll offset
    /// @param stepsY The number of times delta should be applied to the current vertical scroll offset
    public void moveSteps(final int stepsX, final int stepsY)
    {
        this.horizontalScrollbar.movePages(stepsX);
        this.verticalScrollbar  .movePages(stepsY);
    }
    
    /// Moves the viewport content by the given page amount, by that means, `pages` times the view size. If `pages` is
    /// negative, this will scroll backwards.
    /// @param pagesX The number of pages to scroll horizontally
    /// @param pagesY The number of pages to scroll vertically
    public void movePages(final int pagesX, final int pagesY)
    {
        this.horizontalScrollbar.movePages(pagesX);
        this.verticalScrollbar  .movePages(pagesY);
    }
    
    /// Scrolls the viewport all the way to the left.
    public void moveToLeft() { this.horizontalScrollbar.moveToStart(); }
    
    /// Scrolls the viewport all the way to the top.
    public void moveToTop() { this.verticalScrollbar.moveToStart(); }
    
    /// Scrolls the viewport all the way to the right.
    public void moveToRight() { this.horizontalScrollbar.moveToEnd(); }
    
    /// Scrolls the viewport all the way to the bottom.
    public void moveToBottom() { this.verticalScrollbar.moveToEnd(); }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        final int thickness = this.scrollbarThickness.get();
        this.updateScrollbars(false);
        
        if (thickness == 0)
        {
            return;
        }
        
        this.resizeScrollbars();
        this.cornerComponent.setBounds(this
            .getLocalBounds()
            .setTop(this.getHeight() - thickness)
            .setLeft(this.getWidth() - thickness));
    }
    
    private void resizeScrollbars()
    {
        final int       thickness = this.scrollbarThickness.get();
        final Rectangle bounds    = this.getLocalBounds();

        if (this.horizontalScrollbar.isVisible() && this.verticalScrollbar.isVisible())
        {
            this.horizontalScrollbar.setBounds(bounds.removeBottom(thickness).padRight(thickness));
            this.verticalScrollbar  .setBounds(bounds.removeRight(thickness));
        }
        else if (this.horizontalScrollbar.isVisible())
        {
            this.horizontalScrollbar.setBounds(bounds.removeBottom(thickness));
        }
        else if (this.verticalScrollbar.isVisible())
        {
            this.verticalScrollbar.setBounds(bounds.removeRight(thickness));
        }
    }
    
    @Override
    public void onChildAdded(final GuiComponent child)
    {
        if (child == this.content)
        {
            child.sizeChanged    .subscribe(this.contentResizedHandler);
            child.positionChanged.subscribe(this.contentMovedHandler);
        }
    }
    
    @Override
    public void onChildRemoved(final GuiComponent child)
    {
        if (child == this.content)
        {
            child.sizeChanged    .unsubscribe(this.contentResizedHandler);
            child.positionChanged.unsubscribe(this.contentMovedHandler);
        }
    }
    
    //==================================================================================================================
    @Override public void draw(final Canvas canvas) { canvas.getTemplate().nvViewportDrawBackground(canvas, this); }
    
    //==================================================================================================================
    @Override
    public boolean onMouseScroll(final MouseEvent e)
    {
        if (!this.isActive())
        {
            return false;
        }
        
        final boolean is_shift = e.hasShiftDown();
        
        if (is_shift || e.deltaX != 0.0)
        {
            if (!this.horizontalScrollbar.isVisible())
            {
                return false;
            }
            
            final int old_offset = this.horizontalScrollbar.getOffset();
            this.horizontalScrollbar.moveSteps((int) (is_shift ? -e.deltaY : -e.deltaX));
            
            return (old_offset != this.horizontalScrollbar.getOffset());
        }
        
        if (!this.verticalScrollbar.isVisible())
        {
            return false;
        }
        
        final int old_offset = this.verticalScrollbar.getOffset();
        this.verticalScrollbar.moveSteps((int) -e.deltaY);
        
        return (old_offset != this.verticalScrollbar.getOffset());
    }
    
    //==================================================================================================================
    private void updateOffset(final GuiComponent sender, final GuiEventArgs e)
    {
        if (this.content == null)
        {
            return;
        }
        
        final NVScrollbar scrollbar = (NVScrollbar) sender;
        
        final int offset = scrollbar.getOffset();
        this.internalMove = true;
        
        if (!scrollbar.vertical.get())
        {
            this.content.setX(-offset);
        }
        else
        {
            this.content.setY(-offset);
        }
        
        this.internalMove = false;
    }
    
    private void updateScrollbars(final boolean resetOffset)
    {
        if (this.scrollbarThickness.get() == 0)
        {
            this.horizontalScrollbar.setVisible(false);
            this.verticalScrollbar  .setVisible(false);
            this.cornerComponent    .setVisible(false);
            
            return;
        }
        
        final ScrollbarBehaviour h_behaviour = this.behaviourHorizontal.get();
        final ScrollbarBehaviour v_behaviour = this.behaviourVertical  .get();
        
        final boolean show_horizontal = this.horizontalScrollbar.isVisible();
        final boolean show_vertical   = this.verticalScrollbar  .isVisible();
        
        if (h_behaviour != ScrollbarBehaviour.AUTO)
        {
            final int view_height = this.getViewHeight();
            this.horizontalScrollbar.setVisible(h_behaviour == ScrollbarBehaviour.ALWAYS);
            this.verticalScrollbar  .setVisible(v_behaviour == ScrollbarBehaviour.AUTO
                ? (this.content != null && this.content.getHeight() > view_height)
                : v_behaviour == ScrollbarBehaviour.ALWAYS);
        }
        else if (v_behaviour != ScrollbarBehaviour.AUTO)
        {
            final int view_width = this.getViewWidth();
            this.verticalScrollbar  .setVisible(v_behaviour == ScrollbarBehaviour.ALWAYS);
            this.horizontalScrollbar.setVisible(this.content != null && this.content.getWidth() > view_width);
        }
        else
        {
            this.horizontalScrollbar.setVisible(false);
            this.verticalScrollbar  .setVisible(false);
            
            if (this.content != null)
            {
                int view_height = this.getViewHeight();
                int view_width  = this.getViewWidth();
                
                final boolean h_visible = this.horizontalScrollbar.isVisible();
                final boolean v_visible = this.verticalScrollbar  .isVisible();
                
                for (int i = 0; i < 2; ++i)
                {
                    if (this.content.getHeight() > view_height)
                    {
                        this.verticalScrollbar.setVisible(true);
                        view_width = this.getViewWidth();
                    }
                    
                    if (this.content.getWidth() > view_width)
                    {
                        this.horizontalScrollbar.setVisible(true);
                        view_height = this.getViewHeight();
                    }
                    
                    if (
                        h_visible == this.horizontalScrollbar.isVisible()
                        && v_visible == this.verticalScrollbar.isVisible()
                    )
                    {
                        break;
                    }
                }
            }
        }
        
        this.cornerComponent.setVisible(this.horizontalScrollbar.isVisible() && this.verticalScrollbar.isVisible());
        
        final int view_width  = this.getViewWidth();
        final int view_height = this.getViewHeight();
        
        this.horizontalScrollbar.setOverflow(view_width,  (this.content != null ? this.content.getWidth()  : 0));
        this.verticalScrollbar  .setOverflow(view_height, (this.content != null ? this.content.getHeight() : 0));
        
        if (
            (show_horizontal != this.horizontalScrollbar.isVisible()
            || show_vertical != this.verticalScrollbar.isVisible())
        )
        {
            this.resizeScrollbars();
        }
        
        if (resetOffset)
        {
            this.horizontalScrollbar.setOffset(0);
            this.verticalScrollbar  .setOffset(0);
        }
    }
    
    private void contentBoundsChanged(final GuiComponent child, final boolean resized, final boolean moved)
    {
        if (resized)
        {
            this.updateScrollbars(false);
        }
        
        if (moved && !this.internalMove)
        {
            this.internalMove = true;
            
            final int child_offset_x = Math.abs(child.getX());
            final int child_offset_y = Math.abs(child.getY());
            final int h_overflow     = this.getHorizontalOverflow();
            final int v_overflow     = this.getVerticalOverflow();
            
            if (child.getX() > 0 || child.getY() > 0 || child_offset_x > h_overflow || child_offset_y > v_overflow)
            {
                child.setPosition(
                    Math.clamp(child.getX(), -h_overflow, 0),
                    Math.clamp(child.getY(), -v_overflow, 0));
            }
            
            this.horizontalScrollbar.setOffset(child_offset_x);
            this.verticalScrollbar  .setOffset(child_offset_y);
            
            this.internalMove = false;
        }
    }
}
