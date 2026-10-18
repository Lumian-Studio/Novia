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
package xyz.lumian.novia.api.gui.component;

import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.InputType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.core.util.NoviaCollectors;
import xyz.lumian.novia.api.gui.GuiApiLang;
import xyz.lumian.novia.api.gui.canvas.*;
import xyz.lumian.novia.api.gui.component.integration.IInputListener;
import xyz.lumian.novia.api.gui.component.integration.INavigable;
import xyz.lumian.novia.api.gui.component.integration.IHierarchyListener;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.event.GuiEventArgs;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Positioner;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.input.MouseEvent;
import xyz.lumian.novia.api.gui.geometry.Restrainer;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.IGuiProperty;

import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.client.gui.components.AbstractWidget;



//**********************************************************************************************************************
/// Represents a (usually) rectangular area on screen that can receive mouse events, this is the
/// replacement for Minecraft's [AbstractWidget] class.
///
/// ## Hierarchy
/// In this library, every component is laid out in a hierarchical structure, that means that every component
/// potentially has a component that is its parent and has other components that function as its children. This relation
/// allows for a more fine-grained workflow in putting components together, such as a drop-down that has a text box and
/// an arrow button. It also allows reasoning about event handling (such as mouse events, focusing etc.) in a more
/// predictable manner, as the order of command is clearly defined.
///
/// A component can ever only have one parent but possibly infinite children components; this makes the screen component
/// hierarchy act like a tree of components. There is one common root among all children at any level from which the
/// rest of the GUI builds upon.
///
/// ## Positioning
/// Gui components are positioned relative to their parent components, if the parent changes position the child
/// component will as well. This system allows a more robust solution at laying out the single parts of a GUI with a
/// more predictive output. Drawing happens in the same domain (in [#draw(Canvas)] and [#drawOnTop(Canvas)]). In both
/// cases the coordination starts at \[0, 0], for positioning this is the top-left corner of the parent component;
/// for drawing this is the top-left corner of the component to be drawn.
///
/// Despite this relative positioning behaviour, components will still keep track of their absolute coordinates on
/// screen, this will be needed internally for event handling for example. These screen coordinates are updated every
/// render pass and can be fetched via [#getScreenX()] and [#getScreenY()] respectively. There's also plenty of
/// conversion utility methods that allow converting relative coordinates to screen coordinates and vice versa.
///
/// ## Templating & Fonts
/// Usually drawing code goes into either [#draw(Canvas)] or [#drawOnTop(Canvas)], however, since this does not allow
/// customisation of the drawing code for single instances of a component this library introduces [IGuiTemplate], which
/// neatly integrates with [Canvas] to allow the component to dedicate their drawing to a separate source. This separate
/// source can be overridden with custom drawing code and then assigned to the component in question to allow it to
/// draw differently than others (via [#setTemplate(IGuiTemplate)]). Components can be in two different template states,
/// that is, "explicitly templated" and "implicitly templated", where explicit means that a component directly defines
/// its own template and implicit means that it has no explicitly set template, but inherits the template from its
/// closest "explicitly templated" parent.
///
/// The same rules apply to fonts, each component has its own attached [GuiFont] object that will be used to draw text
/// inside a component (if there is any text to draw). Both of these are passed on by the given [Canvas] object so that
/// the library doesn't have to browse the entire component hierarchy until it finds one of them. However, needing
/// a template or a font outside of drawing code will require searching through this hierarchy, for which
/// [#getTemplate()] and [#getFont()] can be used.
///
/// ## Event Handling
/// The component hierarchy will handle mouse and keyboard events globally, by which it will traverse the hierarchy
/// to find the component upon which the event applies first (for mouse events the component the mouse is currently over
/// and for keyboard events the component that is currently being focused). Upon finding a component that events can
/// be applied to, it will hand over control to the component to decide whether it wants to handle the event or not;
/// if handling is successful, control will be handed back to the framework and no further component will get the chance
/// to handle that event (unless a parent is an event monitor; see [#isMonitoringChildren()]). If, however, a component
/// rejects handling an event, the event will continue propagating up the hierarchy until a component is found that
/// wants to actually handle the event, this process is also known as "event bubbling".
///
/// For mouse events, it is worth noting that even when the component the mouse is interacting with is a child of a
/// component, this parent component will not receive the event as long as either the child is not rejecting the event
/// or monitoring is disabled for that parent.
///
/// Here is a table of all mouse and keyboard events that a component provides, these can be overridden and used to
/// handle events:
/// <table>
///     <tr>
///         <th>Name</th>
///         <th>Description</th>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseMove(MouseEvent)]</td>
///         <td>Called whenever the mouse moved over the component directly and not over any of its children</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseEnter(MouseEvent)]</td>
///         <td>Called whenever the mouse entered the bounds of the component (not if it is a child)</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseExit(MouseEvent)]</td>
///         <td>Called whenever the mouse left the bounds of the component (also when it entered a child)</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseDrag(MouseEvent)]</td>
///         <td>Called whenever any mouse button was pressed on a component and is now being moved around</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseDown(MouseEvent)]</td>
///         <td>Called whenever any mouse button was pressed on a component</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseUp(MouseEvent)]</td>
///         <td>Called whenever any mouse button was released on a component</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onMouseScroll(MouseEvent)]</td>
///         <td>Called whenever the mouse wheel was scrolled when over a component</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onKeyDown(KeyEvent)]</td>
///         <td>Called whenever a keyboard button was pressed while the component is focused</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onKeyUp(KeyEvent)]</td>
///         <td>Called whenever a keyboard button was released while the component is focused</td>
///     </tr>
///     <tr>
///         <td>[GuiComponent#onInput(KeyEvent)]</td>
///         <td>Called whenever a keyboard button, that corresponds to a text character, was pressed and released</td>
///     </tr>
/// </table>
/// Whenever a component receives any of the aforementioned events it will try to handle it, it is the developer's
/// responsibility to let the hierarchy know an event was successfully handled by returning `true`, in which case
/// the event is finished. If, however, a component returns `false`, this means that an event was not handled and
/// that any of the parents gets the chance of handling the event itself until one returns `true`.
///
/// ## Visibility & Activity
/// Another concept is the visibility and activity state of a component, visibility refers to whether a component should
/// be drawn or not, setting a component to be invisible effectively removes it from the component hierarchy until it
/// is set to be visible again. This means that it is not rendered and also can not receive any input events from either
/// the mouse or the keyboard.
///
/// The activity state is more manual than visibility, while an inactive component usually means it is not interactive
/// it can still receive events, it's the developer's responsibility to make sure to not handle events in an inactive
/// state as well as rendering it differently. The reason for this design decision is that there are scenarios in which
/// a component should still be able to be interacted with if it is inactive. However, focus behaviour will be affected
/// by this flag in that, that a component becomes unfocusable once it is set to be inactive.
///
/// ## Focus Behaviour
/// By default, components are said to be "not wanting focus", that means that a component is not focusable. This
/// behaviour can be changed explicitly by setting [#setWantsFocus(boolean)] to `true`, in which case the component
/// becomes visible to the focus traverser (if it is visible, active and part of the active focus hierarchy).
///
/// The focus traverser is part of the root screen object and combines all component's navigators that take part
/// in focus resolution. The navigator of a component is defined by [#getNavigator()] and [#getNavigationOrder()],
/// which can both be overridden to alter the way that a component is ordered and what children it exposes upon focus
/// resolution. By default, this navigator is using [NaturalNavigator], which uses a combination of HTML and Minecraft's
/// internal focus resolution.
///
/// ## Modals
/// A modal component is a component that is not part of the main component hierarchy, but still part of the active
/// screen. If a component is in a modal state, it will be drawn above any other component that is not a modal, and it
/// is also the first to receive input events. Every modal will become its own layer on the screen, and each layer
/// defines its own component hierarchy. This allows for doing things like context menus or dialogue boxes that are not
/// part of the main hierarchy, but still need to be part of the screen.
/// For more information see [#showModal(ModalArgs)].
///
/// ## GUI Events & Properties
/// A GUI component can also define its own events and properties. A GUI property describes the behaviour of a component
/// that can be changed during its lifetime, these properties should not be used to store data however, such as text
/// in a textbox/label, or items in a list box but purely be defined for purposes that alter its appearance or policies.
/// To define properties, a utility class [GuiProperty] is provided. GUI events on the other hand allow a component to
/// describe an interface for outsiders to respond to certain events that happen inside a component (such as a component
/// changing its bounds or message). An outsider can then subscribe to such an event to get notified when something
/// happens internally. The interface to do this is provided via the [GuiEvent] class.
///
/// It is worth noting that GUI events as well as GUI properties are purely a conventional API for the Novia GUI
/// framework, properties and events can be implemented however desired and do not need to make use of these utility
/// classes, however, for consistency it is recommended to use what is provided. It also allows making use of some other
/// features, as [GuiProperty] integrates with [GuiComponent] via [#getGuiProperties()], such as the GUI developer
/// console providing an overview of a component's properties.
///
/// ## Accessibility
/// Since Minecraft is big on accessibility features, the component class as well integrates with these features.
/// The accessibility API of the Novia GUI framework does not alter much of the way Minecraft deals with accessibility.
/// Components, just as Minecraft's widgets ([AbstractWidget#getMessage()]), come with a message property
/// ([GuiComponent#getMessage()]) that can be used to tell the accessibility manager what a component represents.
/// And just like with widgets, components as well provide a [#getNarrationMessage()] and
/// [#appendNarrations(NarrationElementOutput)] method to fine-tune the narration system.
public class GuiComponent
    implements
        IPaletteProvider,
        INavigable,
        IInputListener
{
    //******************************************************************************************************************
    /// Specifies the descriptions of a [GuiProperty] for a component that can be given to [#getGuiProperties()].
    ///
    /// Do note that `id` should be unique across all properties and all components, a good convention is to use
    /// the component name and the property name paired with the mod ID:
    /// `Identifier.of("modId", "my_component/some_property")`
    /// @param id       A unique [Identifier] for the property
    /// @param property A reference to the [GuiProperty] inside the component
    /// @param codec    An optional [Codec] for the property; leave empty if the property should not be serialised
    ///                 but still be recorded for other features
    /// @param <T>      The type the [GuiProperty] holds
    /// @see #getGuiProperties()
    public record GuiPropertyDescription<T>(
        Identifier         id,
        IGuiProperty<T>    property,
        Optional<Codec<T>> codec
    )
    {
        //**************************************************************************************************************
        /// Constructs a property description with a non-optional codec.
        /// @param id       A unique [Identifier] for the property
        /// @param property A reference to the [GuiProperty] inside the component
        /// @param codec    The [Codec] for the property
        public GuiPropertyDescription(final Identifier id, final IGuiProperty<T> property, final Codec<T> codec)
        {
            this(id, property, Optional.of(codec));
        }

        /// Constructs a property description with an empty codec.
        /// @param id       A unique [Identifier] for the property
        /// @param property A reference to the [GuiProperty] inside the component
        public GuiPropertyDescription(final Identifier id, final IGuiProperty<T> property)
        {
            this(id, property, Optional.empty());
        }

        //==============================================================================================================
        /// Combines the codec of the content with the codec for the [IGuiProperty] and returns the full one.
        /// @return The [Codec] for this property description
        public Optional<Codec<IGuiProperty<T>>> getCodec() { return this.codec.map(this.property::getCodec); }
    }
    
    public record TemplateChangedEventArgs(@Nullable IGuiTemplate newTemplate) implements GuiEventArgs {}
    public record FontChangedEventArgs(@Nullable GuiFont newFont) implements GuiEventArgs {}
    public record FocusEventArgs(InputType navigationType) implements GuiEventArgs {}
    public record ComponentEventArgs(GuiComponent component) implements GuiEventArgs {}
    public record ColourEventArgs(ColourId id) implements GuiEventArgs {}
    
    //------------------------------------------------------------------------------------------------------------------
    enum ComponentFlag
    {
        //**************************************************************************************************************
        VISIBLE           (true),  // Can be drawn and receive input events
        ACTIVE            (true),  // Is active
        PINNED            (false), // Is atop other non-pinned components
        MONITOR           (false), // Should get child events
        WANTS_FOCUS       (false), // Wants to get keyboard events
        FOCUS_PATH        (false), // Component is part of the focus path of the currently focused component
        CONTINUOUS_DRAWING(false), // Should the component re-draw continuously without the need for invalidation
        ;
        
        //**************************************************************************************************************
        public static BitSet createDefaults()
        {
            return Arrays
                .stream(ComponentFlag.values())
                .map(c -> c.defaultValue)
                .collect(NoviaCollectors.toBitset());
        }
        
        //**************************************************************************************************************
        public final boolean defaultValue;
        
        //**************************************************************************************************************
        ComponentFlag(final boolean defaultValue) { this.defaultValue = defaultValue; }
        
        //==============================================================================================================
        public boolean get(final GuiComponent component) { return component.flags.get(this.ordinal()); }
        
        //==============================================================================================================
        public void set(final GuiComponent component, final boolean value)
        {
            component.flags.set(this.ordinal(), value);
        }
    }
    
    //******************************************************************************************************************
    @Nullable
    static GuiComponent FOCUSED = null;
    
    //******************************************************************************************************************
    /// {@return the currently focused component or `null` if no component is focused}
    public static @Nullable GuiComponent getCurrentFocused() { return GuiComponent.FOCUSED; }
    
    //------------------------------------------------------------------------------------------------------------------
    private static int clampToPinBounds(final int value, final int size, final int pins, boolean isPinned)
    {
        final int pin_start = (size - pins);
        
        if (isPinned)
        {
            return Math.clamp(value, pin_start, (size - 1));
        }
        
        return Math.clamp(value, 0, (pin_start - 1));
    }
    
    //******************************************************************************************************************
    /// Triggered whenever the size of the component changed.
    public final GuiEvent.Simple sizeChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the position (inside the parent) of the component changed.
    public final GuiEvent.Simple positionChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the component's visibility flag changed. This will not trigger if any of the parents changed
    /// their visibility (see [IHierarchyListener] instead).
    public final GuiEvent.Simple visibilityChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the component's activity flag changed. This will not trigger if any of the parents changed
    /// their activity state.
    public final GuiEvent.Simple activityChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the associated [IGuiTemplate] for this component changed. This will only trigger for
    /// the component that changed its immediate template and not for children that inherit this template.
    public final GuiEvent<TemplateChangedEventArgs> templateChanged = new GuiEvent<>();
    
    /// Triggered whenever the associated [GuiFont] for this component changed. This will only trigger for
    /// the component that changed its immediate font and not for children that inherit this font.
    public final GuiEvent<FontChangedEventArgs> fontChanged = new GuiEvent<>();
    
    /// Triggered whenever the component received focus.
    public final GuiEvent<FocusEventArgs> focused = new GuiEvent<>();
    
    /// Triggered whenever the component lost focus.
    public final GuiEvent.Simple blurred = new GuiEvent.Simple();
    
    /// Triggered whenever the component message changed.
    public final GuiEvent.Simple messageChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the tooltip changed.
    public final GuiEvent.Simple tooltipChanged = new GuiEvent.Simple();
    
    /// Triggered whenever a child has been added to this component.
    public final GuiEvent<ComponentEventArgs> childAdded = new GuiEvent<>();
    
    /// Triggered whenever a child has been removed from this component.
    public final GuiEvent<ComponentEventArgs> childRemoved = new GuiEvent<>();
    
    /// Triggered whenever the parent component changed.
    public final GuiEvent.Simple parentChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the navigation order of this component changed.
    public final GuiEvent.Simple navigationOrderChanged = new GuiEvent.Simple();
    
    /// Triggered whenever the component's pin state changed (either through being pinned or unpinned).
    public final GuiEvent.Simple pinStateChanged = new GuiEvent.Simple();
    
    /// Triggered whenever one of the component's colours changed.
    public final GuiEvent<ColourEventArgs> colourChanged = new GuiEvent<>();
    
    //==================================================================================================================
    /// A map for additional component properties that can be used to attach custom user data.
    ///
    /// Since this map is fully controlled by the user and not used anywhere else in this mod's GUI code,
    /// the data attached should be end-user-defined and not at library scope.
    /// This is so that it can be used for particular scenarios like e.g. in customised component drawing ect.
    ///
    /// If there needs to be re-usable data attached to a component, as in a custom library component,
    /// it should be provided as a class field.
    public final Object2ObjectMap<String, Value> properties = new Object2ObjectOpenHashMap<>();

    //------------------------------------------------------------------------------------------------------------------
    @Nullable GuiComponent  parent   = null;
    @Nullable IGuiTemplate  template = null;
    @Nullable GuiFont       font     = null;
    @Nullable ScreenInterop screen   = null;
    
    //------------------------------------------------------------------------------------------------------------------
    private final List<GuiComponent>      children           = new ArrayList<>();
    private final Set<IHierarchyListener> hierarchyListeners = new HashSet<>();
    private final BitSet                  flags              = ComponentFlag.createDefaults();
    private final Rectangle bounds  = new Rectangle();
    private final Palette   palette = new Palette(null);
    
    @Nullable
    private Positioner positioner   = null;
    
    @Nullable
    private Restrainer restrainer   = null;
    
    @Nullable
    private Tooltip    tooltip      = null;
    
    private Duration   tooltipDelay = Duration.ZERO;
    private int        screenX      = 0;
    private int        screenY      = 0;
    private int        numPinned    = 0;
    private float      opacity      = 1.0f;
    private int        focusOrder   = 0;
    private Component  message;
    
    //******************************************************************************************************************
    /// Constructs a new GUI component.
    /// @param message The initial message of the component
    public GuiComponent(final Component message)
    {
        this.message = Objects.requireNonNull(message, "message must not be null");
    }
    
    /// Constructs a new GUI component with an empty message.
    public GuiComponent() { this(CommonComponents.EMPTY); }
    
    //==================================================================================================================
    /// Gets the bounds of this component where left and y represent the location of the component in the parent.
    /// @return The bounds of this component
    public final Rectangle getBounds() { return new Rectangle(this.bounds); }
    
    /// Gets the local bounds of this component where left and y will both be 0.
    /// @return The local bounds of this component
    public final Rectangle getLocalBounds() { return this.getBounds().resetPos(); }
    
    /// Gets the positioner of this component.
    /// @return The [Positioner] of this component or an empty [Optional] if there is none set
    public final Optional<Positioner> getPositioner() { return Optional.ofNullable(this.positioner); }

    /// Gets the restrainer of this component.
    /// @return The [Restrainer] of this component or an empty [Optional] if there is none set
    public final Optional<Restrainer> getRestrainer() { return Optional.ofNullable(this.restrainer); }

    /// Gets the screen bounds of this component where left and y represent the location of the component on the screen.
    /// @return The local bounds of this component
    public final Rectangle getScreenBounds()
    {
        return new Rectangle(this.screenX, this.screenY, this.getWidth(), this.getHeight());
    }
    
    /// Gets this component's bounds as [ScreenRectangle].
    /// @return The [ScreenRectangle]
    public final ScreenRectangle getScreenRect()
    {
        return new ScreenRectangle(this.screenX, this.screenY, this.getWidth(), this.getHeight());
    }
    
    /// {@return the position of this component in the parent component}
    public final Point getPosition() { return this.bounds.getPosition(); }
    
    /// {@return the accessibility message for this component}
    public final Component getMessage() { return this.message; }
    
    /// Gets the opacity of this component.
    ///
    /// Opacity of 0 is different from an invisible component, as the component will still be drawn and receive input
    /// events.
    /// @return The opacity
    public final float getOpacity() { return this.opacity; }
    
    /// Gets the component (child or itself) that is located at the given coordinate, or `null` if the coordinate is
    /// outside the bounds of this component, or the hit test failed for the given coordinates.
    ///
    /// This does not check whether the component is active but will ignore invisible components.
    /// @param x The X coordinate
    /// @param y The Y coordinate
    /// @return The found [GuiComponent], otherwise `null`
    public final @Nullable GuiComponent getComponentAt(final int x, final int y)
    {
        if (!this.containsPoint(x, y) || !this.isVisible() || !this.hitTest(x, y))
        {
            return null;
        }
        
        for (int i = (this.children.size() - 1); i >= 0; --i)
        {
            final GuiComponent child  = this.children.get(i);
            final GuiComponent target = child.getComponentAt((x - child.getX()), (y - child.getY()));
            
            if (target != null)
            {
                return target;
            }
        }
        
        return this;
    }
    
    /// Gets the component (child or itself) that is located at the given coordinate, or `null` if the coordinate is
    /// outside the bounds of this component.
    ///
    /// This does not check whether the component is active but will ignore invisible components.
    /// @param x The X coordinate
    /// @param y The Y coordinate
    /// @return The found [GuiComponent], or `null` if this component was invisible, or the coordinates were
    ///         out of bounds
    public final @Nullable GuiComponent getComponentAt(final double x, final double y)
    {
        return this.getComponentAt((int) x, (int) y);
    }
    
    /// Gets the component (child or itself) that is located at the given coordinate, or `null` if the coordinate is
    /// outside the bounds of this component.
    ///
    /// This does not check whether the component is active but will ignore invisible components.
    /// @param location The location to look for the component
    /// @return The found [GuiComponent], or `null` if this component was invisible, or the coordinates were
    ///         out of bounds
    public final @Nullable GuiComponent getComponentAt(final Point location)
    {
        return this.getComponentAt(location.x(), location.y());
    }
    
    /// {@return the x coordinate of this component inside the parent}
    public final int getX() { return this.bounds.x(); }
    
    /// {@return the y coordinate of this component inside the parent}
    public final int getY() { return this.bounds.y(); }
    
    /// {@return the x coordinate of this component on the right-hand side inside the parent}
    public final int getRight() { return this.bounds.getRight(); }
    
    /// {@return the y coordinate of this component on the bottom side inside the parent}
    public final int getBottom() { return this.bounds.getBottom(); }
    
    /// {@return the width of this component}
    public final int getWidth() { return this.bounds.width(); }
    
    /// {@return the height of this component}
    public final int getHeight() { return this.bounds.height(); }
    
    /// {@return the absolute x coordinate of this component in screen coordinates}
    public final int getScreenX() { return this.screenX; }
    
    /// {@return the absolute y coordinate of this component in screen coordinates}
    public final int getScreenY() { return this.screenY; }
    
    /// {@return the centre point of this component on the x axis}
    public final int getCentreX() { return this.bounds.getCentreX(); }
    
    /// {@return the absolute x coordinate of this component on the right-hand side in screen coordinates}
    public final int getScreenRight() { return (this.screenX + this.getWidth()); }
    
    /// {@return the absolute y coordinate of this component on the bottom side in screen coordinates}
    public final int getScreenBottom() { return (this.screenY + this.getHeight()); }
    
    /// {@return the centre point of this component on the y axis}
    public final int getCentreY() { return this.bounds.getCentreY(); }
    
    /// {@return the centre point of this component}
    public final Point getCentre() { return this.bounds.getCentre(); }
    
    /// {@return the absolute position of this component in screen coordinates}
    public final Point getScreenPosition() { return new Point(this.screenX, this.screenY); }
    
    /// {@return the number of child components}
    public final int getChildCount() { return this.children.size(); }
    
    /// Gets the parent component of this component.
    /// @return The parent component or `null` if this component is the top-level component
    public final @Nullable GuiComponent getParent() { return this.parent; }
    
    /// Gets the top-level component in the component hierarchy. (the component without a parent)
    /// @return The top-level component, or itself if this component is the top-level component
    public final GuiComponent getTopLevelComponent()
    {
        GuiComponent component = this;
        
        while (component.parent != null)
        {
            component = component.parent;
        }
        
        return component;
    }
    
    /// Gets the associated tooltip for this component or `null` if there is no tooltip for this component.
    /// @return The tooltip or `null`
    public final @Nullable Tooltip getTooltip() { return this.tooltip; }
    
    /// Gets the duration the tooltip takes to pop up when hovering over the component.
    /// @return The tooltip's pop-up [Duration]
    public final Duration getTooltipDelay() { return this.tooltipDelay; }
    
    /// Gets the child component at the given index, or an empty optional if there is no child at the given index.
    ///
    /// Do note that the position of children is not as deterministic as it might seem, children might change their
    /// z-index as they see fit.
    /// @param index The index to get
    /// @return The child component or -1 if no component found
    public final Optional<GuiComponent> getChildAt(final int index)
    {
        if (index < 0 || index >= this.children.size())
        {
            return Optional.empty();
        }
        
        return Optional.of(this.children.get(index));
    }
    
    /// {@return the list of all child components}
    public final List<GuiComponent> getChildren() { return new ArrayList<>(this.children); }
    
    @Override
    public @Nullable IComponentNavigator getNavigator()
    {
        return (this.hasChildren() ? NaturalNavigator.INSTANCE : null);
    }
    
    @Override public final int getNavigationOrder() { return this.focusOrder; }
    
    /// Gets the narration title message for the screen narrator, or `null` if the narration title is disabled
    /// for this component.
    /// @return The narration message
    public @Nullable MutableComponent getNarrationMessage()
    {
        return Component.translatable(
            (this.isActive()
                ? GuiApiLang.GUI_NARRATION_COMPONENT_TITLE
                : GuiApiLang.GUI_NARRATION_COMPONENT_TITLE_INACTIVE).toTranslationKey(),
            this.message);
    }
    
    /// Finds the first template that can be applied to this component. This will search all the way up the component
    /// hierarchy until a template could be found, if none was found this will return [IGuiTemplate#DEFAULT].
    ///
    /// For [GuiScreen] objects that have their own explicit template specified, all modal layers of that screen
    /// will get the given template as root template.
    ///
    /// Do not modify a template this way, if the template that is returned is an inherited template from a parent
    /// component, this will result in other children of that parent, that also inherit this template, to adapt these
    /// changes as well. Instead, find a way to copy the template and explicitly set it via
    /// [#setTemplate(IGuiTemplate)]; or even better, make sure to set it up entirely upon construction.
    ///
    /// Additionally, since this has to scan the entire hierarchy until it finds a template, it should not be used
    /// to get the template inside any of the draw calls, [Canvas] has [Canvas#getTemplate()]
    /// for exactly this reason.
    /// @return The [IGuiTemplate] applied to this component
    public final IGuiTemplate getTemplate()
    {
        return Objects.requireNonNullElse(this.findParentObject(comp -> comp.template), IGuiTemplate.DEFAULT);
    }
    
    /// Finds the first GUI font that can be applied to this component. This will search all the way up the component
    /// hierarchy until a font could be found, if none was found this will return [GuiFont#DEFAULT].
    ///
    /// For [GuiScreen] objects that have their own explicit font specified, all modal layers of that screen
    /// will get the given font as base font.
    ///
    /// Do note that the [GuiFont] returned is a copy of the font that was found, that means it is not possible
    /// to modify the font directly but requires a call to [#setFont(GuiFont)].
    ///
    /// Additionally, since this has to scan the entire hierarchy until it finds a font, it should not be used
    /// to get the font inside any of the draw calls, [Canvas] has [Canvas#getFont()]
    /// for exactly this reason.
    /// @return The [IGuiTemplate] applied to this component
    public final GuiFont getFont()
    {
        final GuiFont font = this.findParentObject(comp -> comp.font);
        return (font != null ? new GuiFont(font) : GuiFont.DEFAULT.get());
    }
    
    @Override public Palette getPalette() { return this.palette; }
    
    /// Can be overridden to let the component system know that the component provides a few GUI properties.
    ///
    /// This is not strictly necessary for the component to function but allows some additional features such as
    /// serialisation and advanced integration for development utils, or anything related to property management.
    /// @return A map of [GuiProperty] objects mapped to a unique string identifier
    public Stream<GuiPropertyDescription<?>> getGuiProperties() { return Stream.empty(); }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> @Nullable T findParentObject(final Function<GuiComponent, @Nullable T> getter)
    {
        for (GuiComponent comp = this; comp != null; comp = comp.parent)
        {
            final T object = getter.apply(comp);
            
            if (object != null)
            {
                return object;
            }
        }
        
        return null;
    }
    
    //==================================================================================================================
    /// Determines whether this component is currently being focused.
    /// @return `true` if this element is focused
    public final boolean isFocused() { return (GuiComponent.FOCUSED == this); }
    
    /// Determines whether this component OR any of its children (at any depth) currently hold focus.
    /// @return `true` if this element or any of its children is focused
    public final boolean hasFocus() { return ComponentFlag.FOCUS_PATH.get(this); }
    
    /// Whether the mouse cursor is currently hovering over this component.
    ///
    /// If the mouse is currently in a drag gesture, this will not be the component it is currently over but instead the
    /// component, which is currently being dragged.
    /// @return `true` if this element is focused
    public final boolean isHovered()
    {
        final GuiScreen screen = GuiScreen.CURRENT_SCREEN;
        return (screen != null && screen.getHovered() == this);
    }
    
    /// Determines whether this component is currently being dragged.
    /// @return `true` if this component is currently being dragged
    public final boolean isDragging()
    {
        final GuiScreen screen = GuiScreen.CURRENT_SCREEN;
        return (screen != null && screen.isInDrag() && screen.getHovered() == this);
    }
    
    /// Gets whether this component is currently active.
    ///
    /// This will return `false` if either this component is marked inactive, or if any of the parent components
    /// is inactive.
    ///
    /// Because this needs to go upwards to find out if any component is inactive, this should not be used to check the
    /// activity state for rendering, the [Canvas] provided during rendering will automatically provide the most
    /// recent activity state with [Canvas#isActive()].
    /// @return `true` if this component is active
    public final boolean isActive()
    {
        for (GuiComponent comp = this; comp.hasActiveFlag(); comp = comp.parent)
        {
            if (comp.parent == null)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /// Gets whether this component has the activity flag set, by that means, if the component is considered active on
    /// its own.
    ///
    /// Do note that this should not be used to test whether the component is currently active, as this will only return
    /// the activity state of this component and not whether it is actually active, considering the activity of its
    /// parent components. For cases like these, [#isActive()] should be used instead, which will check the entire
    /// parent hierarchy.
    /// @return `true` if the activity flag for this component is set
    public final boolean hasActiveFlag() { return ComponentFlag.ACTIVE.get(this); }
    
    /// Gets whether this component is currently visible.
    ///
    /// This only checks whether the internal visibility flag is set for this component; it does not check whether
    /// the entire parent hierarchy is visible or not. If you want to know whether it is actually visible for drawing,
    /// use [#isDrawing()] instead.
    /// @return `true` if this component is visible
    public final boolean isVisible() { return ComponentFlag.VISIBLE.get(this); }
    
    /// Determine whether this component is focusable.
    ///
    /// Note that a component denying focus does not mean children can't get the focus.
    /// @return `true` if this component is focusable
    public final boolean wantsFocus() { return ComponentFlag.WANTS_FOCUS.get(this); }
    
    /// A simple check, which determines whether the mouse is currently over this component or not. Since this will
    /// check the screen bounds of the components, this will return `true` even if the component is outside its
    /// parent bounds, there are a few alternatives such as [Canvas#clipRegionContains(Point)] when inside the
    /// drawing routine of the component, which will check its current visible bounds.
    ///
    /// If child components should be ignored, to check whether the mouse is inside the bounds regardless,
    /// [#isMouseOver(double, double, boolean)] with `ignoreChildren` set to `true` should be used instead.
    ///
    /// This does not check whether the child is active or visible.
    ///
    /// Do note that this might not be accurate if the component is not part of an active screen, additionally.
    ///
    /// @param mouseX The absolute (screen-space) mouse position X coordinate
    /// @param mouseY The absolute (screen-space) mouse position Y coordinate
    /// @return `true` if the mouse is hovering over this component
    public final boolean isMouseOver(final double mouseX, final double mouseY)
    {
        return this.isMouseOver(mouseX, mouseY, false);
    }
    
    /// A simple check, which determines whether the mouse is currently over this component or not. Since this will
    /// check the screen bounds of the components, this will return `true` even if the component is outside its
    /// parent bounds, there are a few alternatives such as [Canvas#clipRegionContains(Point)] when inside the
    /// drawing routine of the component, which will check its current visible bounds.
    ///
    /// If child components should be ignored, to check whether the mouse is inside the bounds regardless,
    /// [#isMouseOver(double, double, boolean)] with `ignoreChildren` set to `true` should be used instead.
    ///
    /// This does not check whether the child is active or visible.
    ///
    /// Do note that this might not be accurate if the component is not part of an active screen.
    /// @param mousePos The mouse position
    /// @return `true` if the mouse is hovering over this component
    public final boolean isMouseOver(final Point mousePos)
    {
        return this.isMouseOver(mousePos.x(), mousePos.y());
    }
    
    /// A simple check, which determines whether the mouse is currently over this component or not. Since this will
    /// check the screen bounds of the components, this will return `true` even if the component is outside its parent
    /// bounds, there are a few alternatives such as [Canvas#clipRegionContains(Point)] when inside the drawing routine
    /// of the component, which will check its current visible bounds.
    ///
    /// This does not check whether the child is active or visible.
    ///
    /// Do note that this might not be accurate if the component is not part of an active screen.
    /// @param mouseX         The absolute (screen-space) mouse position X coordinate
    /// @param mouseY         The absolute (screen-space) mouse position Y coordinate
    /// @param ignoreChildren If `false`, this will return `false` if the cursor is above a child component
    /// @return `true` if the mouse is hovering over this component
    public final boolean isMouseOver(final double mouseX, final double mouseY, final boolean ignoreChildren)
    {
        if (
            (mouseX >= this.screenX && mouseX < this.getScreenRight())
            && (mouseY >= this.screenY && mouseY < this.getScreenBottom())
        )
        {
            return (
                ignoreChildren
                || this.children.stream().noneMatch(child -> child.isMouseOver(mouseX, mouseY, true))
            );
        }
        
        return false;
    }
    
    /// A simple check, which determines whether the mouse is currently over this component or not. Since this will
    /// check the screen bounds of the components, this will return `true` even if the component is outside its parent
    /// bounds, there are a few alternatives such as [Canvas#clipRegionContains(Point)] when inside the drawing routine
    /// of the component, which will check its current visible bounds.
    ///
    /// This does not check whether the child is active or visible.
    ///
    /// Do note that this might not be accurate if the component is not part of an active screen.
    ///
    /// @param mousePos       The mouse position
    /// @param ignoreChildren If `false`, this will return `false` if the cursor is above a child component
    /// @return `true` if the mouse is hovering over this component
    public final boolean isMouseOver(final Point mousePos, final boolean ignoreChildren)
    {
        return this.isMouseOver(mousePos.x(), mousePos.y(), ignoreChildren);
    }
    
    /// Determines whether this component, besides its own, is monitoring mouse events also from children.
    ///
    /// If a child somewhere down the hierarchy gets a mouse event, this will also receive that mouse event.
    /// @return `true` if this component is monitoring mouse events from children
    public final boolean isMonitoringChildren() { return ComponentFlag.MONITOR.get(this); }
    
    /// Checks whether this component is currently on screen, by that means, it is part of the hierarchy of the
    /// currently displayed screen. This ignores whether it is currently active or visible and just determines whether
    /// it is part of the hierarchy.
    /// @return `true` if this component is part of the screen component hierarchy
    public final boolean isOnScreen()
    {
        final GuiComponent top = this.getTopLevelComponent();
        return (top.isScreenContainer() && Objects.requireNonNull(top.screen).isShowing());
    }
    
    /// Checks whether this component as well as any of its parents are visible and that it is part of the active
    /// screen.
    /// @return `true` if this component is visible on screen
    public final boolean isDrawing()
    {
        for (GuiComponent comp = this; comp.isVisible(); comp = comp.parent)
        {
            if (comp.parent == null)
            {
                return (comp.isScreenContainer() && Objects.requireNonNull(comp.screen).isShowing());
            }
        }
        
        return false;
    }
    
    /// Gets whether this component has any child components.
    /// @return `true` if this component has child components
    public final boolean hasChildren() { return !this.children.isEmpty(); }
    
    /// Gets whether this component is a pinned component in the parent.
    ///
    /// To know more about pinned components, see [#setPinned].
    /// @return `true` if this component is pinned
    public final boolean isPinned() { return ComponentFlag.PINNED.get(this); }
    
    /// Whether the narrator should consider this component narratable.
    /// @return `true` if this component is narratable
    public final boolean isNarratable() { return this.isVisible(); }
    
    /// Determines whether this component can be focused at all.
    ///
    /// Other than [#canBeNavigatedTo()], this will return `true` even if focus order is below 0.
    ///
    /// Note that this does not check whether the component is part of an active component hierarchy but only check
    /// whether this component qualifies as focusable on its own.
    /// @return `true` if this component can be focused
    public final boolean canBeFocused() { return (this.hasActiveFlag() && this.isVisible() && this.wantsFocus()); }
    
    /// Determines whether this component can be focused by navigating through the keyboard.
    ///
    /// Other than [#canBeFocused()], this will return `false` if focus order is below 0.
    ///
    /// Please note that this does not take into account if [#getNavigator()] is overridden, as at this point the
    /// navigation order is unpredictable.
    ///
    /// Also note that this does not check whether the component is part of an active component hierarchy but only check
    /// whether this component qualifies as navigable on its own.
    /// @return `true` if this component can be focused through keyboard navigation
    public final boolean canBeNavigatedTo() { return (this.canBeFocused() && this.getNavigationOrder() >= 0); }
    
    /// Gets whether this component contains a given component (by identity) as a child.
    ///
    /// This will either check only its immediate children or if `recursive` is `true`, check the children
    /// at any depth.
    /// @param component The component to find
    /// @param recursive Whether to search at any depth
    /// @return `true` if the component contains the given child, otherwise `false`
    public final boolean containsChild(final @Nullable GuiComponent component, final boolean recursive)
    {
        if (component == null)
        {
            return false;
        }
        
        if (recursive)
        {
            for (final var child : this.children)
            {
                if (child == component || child.containsChild(component, true))
                {
                    return true;
                }
            }
        }
        
        return this.children.stream().anyMatch(child -> (child == component));
    }
    
    /// Gets whether this component contains a given component (by identity) as a child.
    ///
    /// This will recursively check the children at any depth. If instead only the immediate children should be checked,
    /// use [#containsChild(GuiComponent, boolean)] instead with `recursive` set to `true`.
    /// @param component The component to find
    /// @return `true` if the component contains the given child, otherwise `false`
    public final boolean containsChild(final @Nullable GuiComponent component)
    {
        return this.containsChild(component, true);
    }
    
    /// Gets whether the given point lies within the bounds of this component.
    /// @param x The coordinate on the left-axis
    /// @param y The coordinate on the y-axis
    /// @return `true` if the point lies within the bounds of this component
    public final boolean containsPoint(final int x, final int y)
    {
        return (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight());
    }
    
    /// Gets whether the given point lies within the bounds of this component.
    /// @param point The point
    /// @return `true` if the point lies within the bounds of this component
    public final boolean containsPoint(final Point point) { return point.transform(this::containsPoint); }
    
    /// Gets whether the given screen point lies within the screen bounds of this component.
    /// @param screenX The screen coordinate on the left-axis
    /// @param screenY The screen coordinate on the y-axis
    /// @return `true` if the point lies within the bounds of this component
    public final boolean containsScreenPoint(final int screenX, final int screenY)
    {
        return (
            screenX >= this.screenX && screenX < (this.screenX + this.getWidth())
            && screenY >= this.screenY && screenY < (this.screenY + this.getHeight()));
    }
    
    /// Gets whether the given screen point lies within the screen bounds of this component.
    /// @param screenPoint The screen point
    /// @return `true` if the point lies within the bounds of this component
    public final boolean containsScreenPoint(final Point screenPoint)
    {
        return screenPoint.transform(this::containsScreenPoint);
    }
    
    /// Can be overridden to determine if a child can be pinned.
    /// @param child The child component in question
    /// @return `true` if the parent allows pinning this child, otherwise `false`
    public boolean isPinningAllowed(final GuiComponent child) { return true; }
    
    /// Can be overridden to determine if a child can be added.
    /// @param child The component to add
    /// @return `true` if the parent allows adding this child
    public boolean isAddingChildAllowed(final GuiComponent child) { return true; }
    
    /// Can be overridden to determine if a child can be removed.
    /// @param child The component to remove
    /// @return `true` if the parent allows removing this child
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isRemovingChildAllowed(final GuiComponent child) { return true; }
    
    /// Can be overridden to determine whether children of this component are allowed to use [Positioner] to
    /// automatically determine their bounds inside this component.
    /// @param child The component to auto-position
    /// @return `true` if auto-positioning is allowed
    public boolean isAutoPositioningAllowed(final GuiComponent child) { return true; }
    
    /// Can be overridden to determine whether this component is allowed to get promoted to a modal layer.
    ///
    /// This is useful for components that might not work as a modal component, or that might make it difficult to
    /// integrate in a well functioning way, like [GuiScreen].
    /// @return `true` if this component can be made a modal, `false` otherwise
    public boolean isModalPromotionAllowed() { return true; }
    
    /// Determines whether this component is a screen container, that is, if this is the root content component of a
    /// screen.
    /// @return `true` if this is a screen container
    public boolean isScreenContainer() { return (this.screen != null); }
    
    /// Determines whether this component is currently shown as a modal component and not part of another parent.
    /// @return `true` if this is a modal component
    public boolean isModal() { return (this.screen != null && this.screen.getLayerForComponent(this) != null); }
    
    //==================================================================================================================
    /// Converts a given point on the left-axis, relative to this component, into screen coordinates.
    /// @param relativeX The point relative to this component
    /// @return The absolute point
    public final int toScreenX(final int relativeX) { return (this.screenX + relativeX); }
    
    /// Converts a given point on the y-axis, relative to this component, into screen coordinates.
    /// @param relativeY The point relative to this component
    /// @return The absolute point
    public final int toScreenY(final int relativeY) { return (this.screenY + relativeY); }
    
    /// Converts a given point on the left-axis, relative to this component, into screen coordinates.
    /// @param relativeX The point relative to this component
    /// @return The absolute point
    public final double toScreenX(final double relativeX)
    {
        return (this.screenX + relativeX);
    }
    
    /// Converts a given point on the y-axis, relative to this component, into screen coordinates.
    /// @param relativeY The point relative to this component
    /// @return The absolute point
    public final double toScreenY(final double relativeY)
    {
        return (this.screenY + relativeY);
    }
    
    /// Converts a given point, relative to this component, into screen coordinates.
    /// @param relativeX The point on the left-axis relative to this component
    /// @param relativeY The point on the y-axis relative to this component
    /// @return The absolute point
    public final Point toScreenPos(final int relativeX, final int relativeY)
    {
        return new Point(this.toScreenX(relativeX), this.toScreenY(relativeY));
    }
    
    /// Converts a given point, relative to this component, into screen coordinates.
    /// @param relativeX The point on the left-axis relative to this component
    /// @param relativeY The point on the y-axis relative to this component
    /// @return The absolute point
    public final Point toScreenPos(final double relativeX, final double relativeY)
    {
        return new Point((int) this.toScreenX(relativeX), (int) this.toScreenY(relativeY));
    }
    
    /// Converts a given point, relative to this component, into screen coordinates.
    /// @param relativePos The relative point
    /// @return The absolute point
    public final Point toScreenPos(final Point relativePos)
    {
        return relativePos.withTranslation(this.screenX, this.screenY);
    }
    
    /// Converts a given point on the left-axis, absolute to the screen, into a local component point.
    /// @param absoluteX The absolute point on the screen
    /// @return The component local point
    public final int toRelativeX(final int absoluteX) { return (absoluteX - this.screenX); }
    
    /// Converts a given point on the y-axis, absolute to the screen, into a local component point.
    /// @param absoluteY The absolute point on the screen
    /// @return The component local point
    public final int toRelativeY(final int absoluteY) { return (absoluteY - this.screenY); }
    
    /// Converts a given point on the left-axis, absolute to the screen, into a local component point.
    /// @param absoluteX The absolute point on the screen
    /// @return The component local point
    public final int toRelativeX(final double absoluteX) { return (int) (absoluteX - this.screenX); }
    
    /// Converts a given point on the y-axis, absolute to the screen, into a local component point.
    /// @param absoluteY The absolute point on the screen
    /// @return The component local point
    public final int toRelativeY(final double absoluteY) { return (int) (absoluteY - this.screenY); }
    
    /// Converts a given point, absolute to the screen, into local component coordinates.
    /// @param absoluteX The point on the left-axis relative absolute to the screen
    /// @param absoluteY The point on the y-axis relative absolute to the screen
    /// @return The component local point
    public final Point toRelativePos(final int absoluteX, final int absoluteY)
    {
        return new Point(this.toRelativeX(absoluteX), this.toRelativeY(absoluteY));
    }
    
    /// Converts a given point, absolute to the screen, into local component coordinates.
    /// @param absoluteX The point on the left-axis relative absolute to the screen
    /// @param absoluteY The point on the y-axis relative absolute to the screen
    /// @return The component local point
    public final Point toRelativePos(final double absoluteX, final double absoluteY)
    {
        return new Point((int) this.toRelativeX(absoluteX), (int) this.toRelativeY(absoluteY));
    }
    
    /// Converts a given point, absolute to the screen, into local component coordinates.
    /// @param absolutePos The absolute point
    /// @return The component local point
    public final Point toRelativePos(final Point absolutePos)
    {
        return absolutePos.withTranslation(-this.screenX, -this.screenY);
    }
    
    /// Gets the index of the given child component.
    ///
    /// Components are compared by identity and not by contents, this will only return a valid index if exactly
    /// the given component instance is part of the tree.
    /// @param child The child component
    /// @return The index of the component, or -1 if no such component is currently part of the child hierarchy
    public final int indexOfChild(final @Nullable GuiComponent child)
    {
        return IntStream
            .range(0, this.children.size())
            .filter(i -> this.children.get(i) == child)
            .findFirst()
            .orElse(-1);
    }
    
    /// Tests whether the mouse actually executes events on this component. If this returns `false`, the event
    /// will fall through to the component behind it.
    ///
    /// By default, this will always return `true` which means that as long as the cursor is over this component it
    /// will always pick this component for handling a mouse event, but overriding it allows fine-tuning this behaviour.
    /// @param x The relative left-coordinate to test
    /// @param y The relative y-coordinate to test
    /// @return `true` if the mouse actually executes events on this component at the given location inside the
    ///         component's bounds
    public boolean hitTest(final int x, final int y) { return true; }
    
    //==================================================================================================================
    /// Sets whether this component should be visible; an invisible component will not receive input events.
    /// @param shouldBeVisible The visibility
    public final void setVisible(final boolean shouldBeVisible)
    {
        if (this.isVisible() == shouldBeVisible)
        {
            return;
        }
        
        ComponentFlag.VISIBLE.set(this, shouldBeVisible);
        
        if (!shouldBeVisible && this.hasFocus())
        {
            if (this.parent != null)
            {
                this.parent.focus();
            }
            
            this.blur();
        }
        
        this.onVisibilityChanged();
        this.visibilityChanged.post(this);
        this.sendHierarchyVisibilityChangeNotification();
        
        if (this.isOnScreen())
        {
            Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
        }
    }
    
    /// Sets whether this component should be active. An inactive component will still receive input events,
    /// it is the developers duty to check for the component's activity status themselves.
    /// @param shouldBeActive The activity state
    public final void setActive(final boolean shouldBeActive)
    {
        if (ComponentFlag.ACTIVE.get(this) == shouldBeActive)
        {
            return;
        }
        
        ComponentFlag.ACTIVE.set(this, shouldBeActive);
        
        if (!shouldBeActive && this.hasFocus())
        {
            if (this.parent != null)
            {
                this.parent.focus();
            }
            
            this.blur();
        }
        
        this.sendActivityChangeNotification();
        this.children.forEach(GuiComponent::sendActivityChangeNotification);
        this.activityChanged.post(this);
    }
    
    /// Sets the opacity of this component.
    /// @param opacity The new opacity value (0-1, where 0 means fully transparent and 1 means fully opaque)
    public final void setOpacity(final float opacity) { this.opacity = Math.clamp(opacity, 0.0f, 1.0f); }
    
    /// Sets the left coordinate of this component local to the parent component. If this component has a [Positioner],
    /// this will do nothing.
    /// @param x The left coordinate
    public final void setX(final int x)
    {
        this.setBounds(x, this.bounds.y(), this.bounds.width(), this.bounds.height());
    }
    
    /// Sets the y coordinate of this component local to the parent component. If this component has a [Positioner],
    /// this will do nothing.
    /// @param y The y coordinate
    public final void setY(final int y)
    {
        this.setBounds(this.bounds.x(), y, this.bounds.width(), this.bounds.height());
    }
    
    /// Sets the width of this component. If this component has a [Positioner], this will do nothing.
    /// @param width The component width
    public final void setWidth(final int width)
    {
        this.setBounds(this.bounds.x(), this.bounds.y(), width, this.bounds.height());
    }
    
    /// Sets the height of this component. If this component has a [Positioner], this will do nothing.
    /// @param height The component height
    public final void setHeight(final int height)
    {
        this.setBounds(this.bounds.x(), this.bounds.y(), this.bounds.width(), height);
    }
    
    /// Sets the position of this component local to the parent component. The position is relative to the top-left
    /// corner of the parent component. If this component has a [Positioner], this will do nothing.
    /// @param x The left coordinate
    /// @param y The y coordinate
    public final void setPosition(final int x, final int y)
    {
        this.setBounds(x, y, this.bounds.width(), this.bounds.height());
    }
    
    /// Sets the position of this component local to the parent component. The position is relative to the top-left
    /// corner of the parent component. If this component has a [Positioner], this will do nothing.
    /// @param position The new position
    public final void setPosition(final Point position) { position.accept(this::setPosition); }
    
    /// Sets the size of this component. If this component has a [Positioner], this will do nothing.
    /// @param width  The new width
    /// @param height The new height
    public final void setDimensions(final int width, final int height)
    {
        this.setBounds(this.bounds.x(), this.bounds.y(), width, height);
    }
    
    /// Sets the bounds of this component inside the parent component. The position is relative to the top-left
    /// corner of the parent component. If this component has a [Positioner], this will do nothing.
    /// @param x      The left coordinate relative to the parent component
    /// @param y      The y coordinate relative to the parent component
    /// @param width  The width
    /// @param height The height
    public final void setBounds(final int x, final int y, int width, int height)
    {
        if (this.positioner == null || (this.parent != null && !this.parent.isAutoPositioningAllowed(this)))
        {
            this.setBoundsInternal(x, y, width, height);
        }
    }
    
    /// Sets the bounds of this component inside the parent component. The position is relative to the top-left
    /// corner of the parent component. If this component has a [Positioner], this will do nothing.
    /// @param bounds The new bounds inside the parent component
    public final void setBounds(final Rectangle bounds) { bounds.accept(this::setBounds); }
    
    /// Sets the component's message.
    ///
    /// A component message basically allows a narrator to narrate the component with the given [Component] object.
    /// Custom components can also use the message for other purposes like rendering it on screen and use it as narration
    /// at the same time.
    ///
    /// By default, a component will use the component's message as narration title via [#getNarrationMessage()].
    /// @param message The new message of the component
    public final void setMessage(final Component message)
    {
        Objects.requireNonNull(message, "message must not be null");
        
        if (!Objects.equals(this.message, message))
        {
            this.message = message;
            
            this.onMessageChanged();
            this.messageChanged.post(this);
        }
    }
    
    /// Sets the tooltip's message for this component. If the tooltip message is `null`, no tooltip will be shown.
    /// @param tooltip The new tooltip
    public final void setTooltip(final @Nullable Tooltip tooltip)
    {
        if (!Objects.equals(this.tooltip, tooltip))
        {
            this.tooltip = tooltip;
            
            final GuiComponent top_level = this.getTopLevelComponent();
            
            if (top_level.isScreenContainer() && Objects.requireNonNull(top_level.screen).isShowing())
            {
                top_level.screen.updateTooltip(this);
            }
            
            this.onTooltipChanged();
            this.tooltipChanged.post(this);
        }
    }
    
    @Override
    public @Nullable Colour setColour(final ColourId id, final @Nullable Colour colour)
    {
        final Colour old_colour = IPaletteProvider.super.setColour(id, colour);
        
        if (!Objects.equals(old_colour, colour))
        {
            this.onColoursChanged();
            this.colourChanged.post(this, new ColourEventArgs(id));
        }
        
        return old_colour;
    }
    
    /// Sets the template of this component to the new one, or clears the template by passing `null`.
    ///
    /// This will raise [#templateChanged] for this and invoke [#onTemplateChanged(IGuiTemplate)]
    /// for all children up to the end of the hierarchy or when a component provides its own explicit template.
    /// @param template The new [IGuiTemplate] or `null` to clear the template
    public final void setTemplate(final @Nullable IGuiTemplate template)
    {
        if (this.template != template)
        {
            this.template = template;
            
            this.sendTemplateChangeNotification(template);
            this.templateChanged.post(this, new TemplateChangedEventArgs(template));
        }
    }
    
    /// Sets the font of this component to the new one, or clears the font by passing `null`.
    ///
    /// This will raise [#fontChanged] for this component and invoke [#onFontChanged(GuiFont)] for all
    /// children up to the end of the hierarchy or when a component provides its own explicit font.
    /// @param font The new [GuiFont] or `null` to clear the font
    public final void setFont(final GuiFont font)
    {
        if (!Objects.equals(this.font, font))
        {
            this.font = font;
            
            this.sendFontChangeNotification(font);
            this.fontChanged.post(this, new FontChangedEventArgs(font));
        }
    }
    
    /// Sets the positioner for this component.
    ///
    /// A positioner allows a parent to automatically adjust the bounds of a child component based on its positioner.
    /// It should be preferred to, nonetheless, set children bounds manually in [#resized()], however, in a more
    /// dynamic context it can be useful to let this be done automatically.
    ///
    /// This will happen before [#resized()] is called.
    /// @param positioner The [Positioner]
    public final void setPositioner(final @Nullable Positioner positioner)
    {
        if (!Objects.equals(this.positioner, positioner))
        {
            this.positioner = positioner;
            
            if (this.parent != null && this.parent.isAutoPositioningAllowed(this))
            {
                if (positioner != null)
                {
                    positioner
                        .getBounds(this.parent.getLocalBounds())
                        .accept(this::setBoundsInternal);
                }
                else
                {
                    this.parent.resized();
                }
            }
        }
    }

    /// Sets a component restrainer that, upon resizing, limits the component in what size it can adapt.
    /// @param restrainer The [Restrainer]
    public final void setRestrainer(final @Nullable Restrainer restrainer)
    {
        if (!Objects.equals(this.restrainer, restrainer))
        {
            this.restrainer = restrainer;
            this.setBoundsInternal(this.getX(), this.getY(), this.getWidth(), this.getHeight());

            if (this.parent != null)
            {
                this.parent.resized();
            }
        }
    }
    
    /// Sets the time the tooltip waits before showing upon hovering over this component.
    /// @param tooltipDelay The delay duration
    public final void setTooltipDelay(final Duration tooltipDelay) { this.tooltipDelay = tooltipDelay; }
    
    /// Sets whether this component wants to get keyboard focus or not.
    ///
    /// Setting a component to not wanting focus does not stop its children from being able to receive focus.
    /// @param wantsFocus Whether this component wants to get keyboard focus
    public final void setWantsFocus(final boolean wantsFocus) { ComponentFlag.WANTS_FOCUS.set(this, wantsFocus); }
    
    /// Sets this component to be a pinned component in its parent.
    ///
    /// A pinned component is a component that will always be in front of all other non-pinned components, where their
    /// z-index cannot be smaller than that of unpinned components.
    ///
    /// The z-index among pinned components can be less or greater, but will always be greater than that of unpinned
    /// components.
    ///
    /// This will send this component all the way to the front of all other components, pinned and unpinned and upon
    /// unpinning to the front of all other unpinned components.
    /// @param shouldBePinned `true` if this component should be pinned
    public final void setPinned(final boolean shouldBePinned)
    {
        if (shouldBePinned == this.isPinned())
        {
            return;
        }
        
        ComponentFlag.PINNED.set(this, shouldBePinned);
        
        if (this.parent != null)
        {
            if (shouldBePinned)
            {
                if (!this.parent.isPinningAllowed(this))
                {
                    ComponentFlag.PINNED.set(this, false);
                    return;
                }
                
                ++this.parent.numPinned;
                this.setZIndex(this.parent.getChildCount() - 1);
            }
            else
            {
                final int order = this.parent.numPinned--;
                this.setZIndex(this.parent.getChildCount() - order);
            }
            
            this.pinStateChanged.post(this);
        }
    }
    
    /// Sets the navigation order of this component inside the parent (see [#getNavigationOrder()]).
    /// @param navigationOrder The navigation order
    public final void setNavigationOrder(final int navigationOrder)
    {
        if (this.focusOrder != navigationOrder)
        {
            this.focusOrder = navigationOrder;
            this.navigationOrderChanged.post(this);
        }
    }
    
    /// Sets this component's z-index in the parent component.
    ///
    /// If the new index is below zero, will put it all the way in the back, if it is above the number of children,
    /// will put it all the way to the front.
    /// @param newIndex The new z-index of this component
    public final void setZIndex(int newIndex)
    {
        if (this.parent == null)
        {
            return;
        }
        
        final int old_index = this.parent.indexOfChild(this);
        newIndex = GuiComponent.clampToPinBounds(
            newIndex,
            this.parent.getChildCount(),
            this.parent.numPinned,
            this.isPinned());
        
        if (old_index == newIndex)
        {
            return;
        }
        
        this.parent.setChildZIndex(old_index, newIndex);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    /// Sets whether this component, besides its own, is monitoring mouse events also from children.
    /// If a child somewhere down the hierarchy gets a mouse event, this will also receive that mouse event.
    ///
    /// Monitoring components only get events from children if one of them successfully handled it,
    /// otherwise the monitoring component will get it as an ordinary event. If a component got a monitored event,
    /// then [MouseEvent#target()] will return the component, which handled it and not the monitoring component.
    /// @param shouldMonitor If this is `true`, this component will be monitoring mouse events from children
    @SuppressWarnings("SameParameterValue")
    protected final void setMonitorChildren(final boolean shouldMonitor)
    {
        ComponentFlag.MONITOR.set(this, shouldMonitor);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    final void setBoundsInternal(final int x, final int y, int width, int height)
    {
        if (this.restrainer == null)
        {
            width  = Math.max(0, width);
            height = Math.max(0, height);
        }
        else
        {
            width  = Math.clamp(width,  this.restrainer.minWidth(),  this.restrainer.maxWidth());
            height = Math.clamp(height, this.restrainer.minHeight(), this.restrainer.maxHeight());
        }
        
        final boolean resized = (width != this.getWidth() || height != this.getHeight());
        final boolean moved   = (x     != this.getX()     || y      != this.getY());
        
        if (resized || moved)
        {
            this.bounds.setBounds(x, y, width, height);
            
            if (this.isDrawing())
            {
                Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
            }
            
            for (final var child : this.children)
            {
                if (child.positioner != null && this.isAutoPositioningAllowed(child))
                {
                    child.positioner
                        .getBounds(bounds)
                        .accept(child::setBoundsInternal);
                }
            }
            
            if (resized)
            {
                this.resized();
                this.sizeChanged.post(this);
            }
            
            if (moved)
            {
                this.moved();
                this.positionChanged.post(this);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setChildZIndex(final int oldIndex, int newIndex)
    {
        newIndex = GuiComponent.clampToPinBounds(
            newIndex,
            (this.getChildCount() + 1),
            this.numPinned,
            this.children.get(oldIndex).isPinned());
        
        if (oldIndex == newIndex)
        {
            return;
        }
        
        this.children.add(newIndex, this.children.remove(oldIndex));
        
        if (this.isDrawing())
        {
            Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
        }
    }
    
    //==================================================================================================================
    /// Attaches a hierarchy listener to this component (see [IHierarchyListener]).
    /// @param listener The [IHierarchyListener] to add
    public void addHierarchyListeners(final IHierarchyListener listener)
    {
        this.hierarchyListeners.add(listener);
    }
    
    /// Detaches a hierarchy listener from this component, if it exists (see [IHierarchyListener]).
    /// @param listener The [IHierarchyListener] to remove
    public void removeHierarchyListeners(final IHierarchyListener listener)
    {
        this.hierarchyListeners.remove(listener);
    }
    
    //==================================================================================================================
    /// Tries to give this component focus.
    public void focus() { this.updateFocusState(InputType.NONE, true, true); }
    
    /// If this component or a child (at any depth) of this component is currently focused, will blur that component.
    public void blur() { this.updateFocusState(InputType.NONE, true, false); }
    
    //==================================================================================================================
    /// Appends narration messages to the builder.
    ///
    /// When overriding this method, make sure to call the super method to allow the default narrations to be added.
    /// @param builder The narration builder
    public void appendNarrations(final NarrationElementOutput builder)
    {
        if (!this.isNarratable())
        {
            return;
        }
        
        if (this.getNarrationMessage() != null)
        {
            builder.add(NarratedElementType.TITLE, this.getNarrationMessage());
        }
        
        this.appendCustomNarrations(builder);
        this.children.forEach(child -> child.appendNarrations(builder));
        
        if (this.tooltip != null)
        {
            this.tooltip.updateNarration(builder);
        }
    }
    
    /// Pushes this component the given amount to the front in its parent component.
    ///
    /// If this component is not a child component, nothing will be done.
    /// @param amount The number of indices this should be pushed to the front, if 0 or less, nothing will be done and
    ///               if the amount is higher than the remaining slots this will push it all the way to the front
    public final void sendForward(final int amount)
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.setZIndex(this.parent.indexOfChild(this) + amount);
    }
    
    /// Pushes this component one index to the front in its parent component. If this component is not a child component
    /// or is already at the top, nothing will be done.
    public final void sendForward() { this.sendForward(1); }
    
    /// Pushes this component the given amount to the back in its parent component. If this component is not a child
    /// component, nothing will be done.
    /// @param amount The number of indices this should be pushed to the back, if 0 or less, nothing will be done and if
    ///               the amount is higher than the remaining slots this will push it all the way to the back
    public final void sendBackward(final int amount)
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.setZIndex(this.parent.indexOfChild(this) - amount);
    }
    
    /// Pushes this component one index to the back in its parent component. If this component is not a child component
    /// or is already at the bottom, nothing will be done.
    public final void sendBackward() { this.sendBackward(1); }
    
    /// Pushes this component all the way to the top in its parent component. If this component is not a child
    /// component, nothing will be done.
    public final void sendToFront()
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.sendForward(this.parent.children.size());
    }
    
    /// Pushes this component all the way to the bottom in its parent component. If this component is not a child
    /// component, nothing will be done.
    public final void sendToBack()
    {
        if (this.parent == null)
        {
            return;
        }
        
        this.sendBackward(this.parent.children.size());
    }
    
    public void appendCustomNarrations(NarrationElementOutput builder) {}
    
    /// Adds a child component to this component and updates the component hierarchy.
    ///
    /// If the given component is already a child of this component, this does nothing. If the given component has a
    /// different parent, the component will first be removed from its old parent and then added to this component.
    ///
    /// The specified z-index determines the z order of the component in relation to all other children, where a higher z
    /// order means the component will be treated as above all other components with lower z-indices. If the component is
    /// pinned, it will be added to the pin range of the child list, and it will be added at least above all non-pinned
    /// components regardless of the specified z-index.
    ///
    /// Components are treated by reference and not equality, by that means, two children with the same data but
    /// different instances will not be the same component.
    /// @param child  The new component to add
    /// @param zIndex The desired z-index of the component
    /// @return The added, or the already contained child component
    public final <T extends GuiComponent> T addChild(final T child, int zIndex)
    {
        Objects.requireNonNull(child, "child component must not be null");
        
        if (child.parent == this)
        {
            return child;
        }
        
        if (!this.isAddingChildAllowed(child))
        {
            return child;
        }
        
        if (child.parent != null)
        {
            child.parent.removeChild(child);
        }
        
        if (child.isPinned())
        {
            if (this.isPinningAllowed(child))
            {
                ++this.numPinned;
            }
            else
            {
                child.setPinned(false);
            }
        }
        
        if (zIndex < 0)
        {
            zIndex = this.getChildCount();
        }
        
        zIndex = GuiComponent.clampToPinBounds(
            zIndex,
            (this.getChildCount() + 1),
            this.numPinned,
            child.isPinned());
        this.children.add(zIndex, child);
        
        child.parent = this;
        child.onParentChanged();
        child.parentChanged.post(this);
        child.sendHierarchyChangeNotification();
        
        if (this.isDrawing())
        {
            Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
        }
        
        child.getPositioner().ifPresent(positioner ->
        {
            if (!this.bounds.isEmpty())
            {
                positioner
                    .getBounds(this.bounds)
                    .accept(child::setBoundsInternal);
            }
        });
        
        this.onChildAdded(child);
        this.childAdded.post(this, new ComponentEventArgs(child));
        
        return child;
    }
    
    /// Adds a list of child components to this component and updates the component hierarchy. The same rules as with
    /// [#addChild(GuiComponent, int)] apply.
    /// @param children The list of components to add
    /// @return The number of children that were added
    public final int addAllChildren(final Collection<? extends GuiComponent> children)
    {
        Objects.requireNonNull(children, "children collection must not be null");
        
        final List<? extends GuiComponent> new_list = children
            .stream()
            .filter(component ->
            {
                Objects.requireNonNull(component, "component must not be null");
                return (component.parent != this && this.isAddingChildAllowed(component));
            })
            .peek(component ->
            {
                if (component.isPinned() && !this.isPinningAllowed(component))
                {
                    component.setPinned(false);
                }
            })
            .toList();
        
        if (new_list.isEmpty())
        {
            return 0;
        }
        
        final List<GuiComponent> old_list = new ArrayList<>(this.children);
        
        this.children.clear();
        this.children.addAll(Stream
            .concat(old_list.stream(), new_list.stream())
            .sorted(Comparator
                .comparing(GuiComponent::isPinned)
                .thenComparing(e -> (e.parent != this)))
            .toList());

        new_list.forEach(child ->
        {
            final GuiComponent c_parent = child.getParent();
            
            if (c_parent != null)
            {
                c_parent.removeChild(child);
            }
            
            if (child.isPinned())
            {
                ++this.numPinned;
            }
            
            child.parent = this;
            child.onParentChanged();
            child.parentChanged.post(this);
            child.sendHierarchyChangeNotification();
            
            child.getPositioner().ifPresent(positioner ->
            {
                if (!this.bounds.isEmpty())
                {
                    positioner
                        .getBounds(this.bounds)
                        .accept(child::setBoundsInternal);
                }
            });
            
            this.onChildAdded(child);
            this.childAdded.post(this, new ComponentEventArgs(child));
        });
        
        if (this.isDrawing())
        {
            Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
        }
        
        return new_list.size();
    }
    
    /// Adds a child component to this component and updates the component hierarchy.
    ///
    /// If the given component is already a child of this component, this does nothing. If the given component has a
    /// different parent, the component will first be removed from its old parent and then added to this component.
    ///
    /// This will add the component at the front of all other components. If this component has pinned components and the
    /// new child is not pinned, it will be added at the front of all non-pinned components but behind all pinned
    /// components.
    ///
    /// Components are treated by reference and not equality, by that means, two children with the same data but
    /// different instances will not be the same component.
    /// @param child The new component to add
    /// @return The added, or the already contained child component
    public final <T extends GuiComponent> T addChild(final T child) { return this.addChild(child, -1); }
    
    /// Removes the given child component from this component and updates the component hierarchy. If there is no
    /// component at the given index, this does nothing.
    /// @param index The index of the child component to remove
    /// @return The removed component or `null` if there was no component at the given index
    public final @Nullable GuiComponent removeChild(final int index)
    {
        if (index < 0 || index >= this.children.size())
        {
            return null;
        }
        
        final GuiComponent child = this.children.get(index);
        
        if (!this.isRemovingChildAllowed(child))
        {
            return null;
        }
        
        this.children.remove(index);
        this.removeChildInternal(child);
        
        if (this.isDrawing())
        {
            Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
        }
        
        return child;
    }
    
    /// Removes the given child component from this component and updates the component hierarchy. If the given
    /// component is not a child of this component, this does nothing.
    /// @param child The child to remove
    /// @return The removed component or `null` if the given component was not a child of this
    public final @Nullable GuiComponent removeChild(final GuiComponent child)
    {
        Objects.requireNonNull(child, "child must not be null");
        return this.removeChild(this.indexOfChild(child));
    }
    
    /// Removes all child components from this component and updates the component hierarchy.
    /// @return The number of children that were removed
    public final int removeAllChildren()
    {
        int removed = 0;
        
        for (int i = (this.getChildCount() - 1); i >= 0; --i)
        {
            final GuiComponent child = this.children.get(i);
            
            if (!this.isRemovingChildAllowed(child))
            {
                continue;
            }
            
            this.children.remove(i);
            this.removeChildInternal(child);
            ++removed;
        }
        
        if (removed > 0)
        {
            if (this.isDrawing())
            {
                Objects.requireNonNull(GuiScreen.CURRENT_SCREEN).updateHoverState();
            }
        }
        
        return removed;
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void removeChildInternal(final GuiComponent child)
    {
        if (child.isPinned())
        {
            --this.numPinned;
        }
        
        if (child.hasFocus())
        {
            Objects.requireNonNull(GuiComponent.FOCUSED).updateFocusedComponent(InputType.NONE, false);
            this.focus();
        }
        
        child.parent = null;
        child.parentChanged.post(this);
        child.sendHierarchyChangeNotification();
        
        this.onChildRemoved(child);
        this.childRemoved.post(this, new ComponentEventArgs(child));
    }
    
    //==================================================================================================================
    void sendActivityChangeNotification()
    {
        if (this.hasActiveFlag())
        {
            this.onActivityChanged();
            this.children.forEach(GuiComponent::sendActivityChangeNotification);
        }
    }
    
    void sendTemplateChangeNotification(final @Nullable IGuiTemplate template)
    {
        this.onTemplateChanged(template);
        this.onColoursChanged();
        this.children
            .stream()
            .filter(child -> (child.template == null))
            .forEach(child -> child.sendTemplateChangeNotification(template));
    }
    
    void sendFontChangeNotification(final @Nullable GuiFont font)
    {
        this.onFontChanged(font);
        this.children
            .stream()
            .filter(child -> (child.font == null))
            .forEach(child -> child.sendFontChangeNotification(font));
    }
    
    void sendHierarchyChangeNotification()
    {
        this.onHierarchyChanged();
        this.hierarchyListeners.forEach(listener -> listener.hierarchyChanged(this));
        this.children.forEach(GuiComponent::sendHierarchyChangeNotification);
    }
    
    void sendHierarchyVisibilityChangeNotification()
    {
        this.onHierarchyVisibilityChanged();
        this.hierarchyListeners.forEach(listener -> listener.visibilityChanged(this));
        this.children.forEach(GuiComponent::sendHierarchyVisibilityChangeNotification);
    }
    
    //==================================================================================================================
    /// Called whenever this component's own visibility flag has changed.
    public void onVisibilityChanged() {}
    
    /// Called whenever this component's or any of its parent's activity state has changed.
    public void onActivityChanged() {}
    
    /// Called whenever the message of this component changed.
    public void onMessageChanged() {}
    
    /// Called whenever the component's internal colours or when the component's associated template changes.
    public void onColoursChanged() {}
    
    /// Called whenever the component's associated template changes. This event will propagate all the way down
    /// the child hierarchy for every component until it encounters an explicit template.
    ///
    /// Do note that this event is not triggered if this component is inheriting its template from a parent and the
    /// template changes through component hierarchy updates, like when it gets a new parent. For these cases overriding
    /// [#onHierarchyChanged()] will be necessary.
    /// @param template The new [IGuiTemplate] that is applied to this component
    public void onTemplateChanged(@Nullable IGuiTemplate template) {}
    
    /// Called whenever the component's associated font changes. This event will propagate all the way down
    /// the child hierarchy for every component until it encounters an explicit font.
    ///
    /// Do note that this event is not triggered if this component is inheriting its font from a parent and the font
    /// changes through component hierarchy updates, like when it gets a new parent. For these cases overriding
    /// [#onHierarchyChanged()] will be necessary.
    /// @param font The new [GuiFont] that is applied to this component
    public void onFontChanged(@Nullable GuiFont font) {}
    
    /// Called whenever this component's tooltip has changed.
    public void onTooltipChanged() {}
    
    /// Called every draw call, useful to update things based on timing.
    ///
    /// This method can be ignored as everything in here can just ordinarily be done in `#draw(Canvas)`, however,
    /// it is useful for keeping the draw method clean or when something needs to be updated before the component
    /// is starting to draw.
    ///
    /// It is also worth noting that this will be called regardless of the component being drawn to the canvas
    /// or not. If the component is invisible, has zero bounds or is not inside the draw area of its parents, this
    /// will be called regardless.
    /// @param mousePos The position of the mouse during the tick
    /// @param delta    The time between the last render call and this one
    public void onDeltaTick(Point mousePos, float delta) {}
    
    /// Called whenever this component has either lost or gained focus.
    /// @param type The cause of the focus change
    public void onFocusChanged(InputType type) {}
    
    /// Called whenever the focus of this or any of its children (at any depth) migrated in our out of the component.
    /// This will not be called if the focus just switched between components inside the hierarchy of this component.
    /// @param target The component, which changed its focus
    /// @param type   The type of focus change event
    public void onFocusMigrated(GuiComponent target, InputType type) {}
    
    /// Called whenever the component has opened as a modal, not if a modal has been opened as a screen.
    public void onModalOpened() {}
    
    /// Called whenever the component has closed its modal state.
    public void onModalClosed() {}
    
    /// Called whenever the immediate parent component changed.
    public void onParentChanged() {}
    
    /// Called whenever the parent hierarchy changed.
    public void onHierarchyChanged() {}
    
    /// Called whenever the visibility of a parent changed.
    public void onHierarchyVisibilityChanged() {}
    
    /// Called whenever a child component was added.
    /// @param child The child [GuiComponent] that was added
    public void onChildAdded(GuiComponent child) {}
    
    /// Called whenever a child component was removed.
    /// @param child The child [GuiComponent] that was removed
    public void onChildRemoved(GuiComponent child) {}
    
    /// Called whenever the focus of a child component (at any depth) has changed.
    /// @param child The child that changed focus
    /// @param type  The type of focus change
    public void onChildFocusChanged(GuiComponent child, InputType type) {}
    
    //==================================================================================================================
    /// Called whenever the bounds of this component have changed. This method is used to set the bounds of child
    /// components.
    public void resized() {}
    
    /// Called whenever the position of this component have changed inside the parent.
    public void moved() {}
    
    //==================================================================================================================
    /// Shows this component in a modal state, by that means, above all other screens currently shown. If there is
    /// currently no Novia specific screen opened, this will return [ModalResult.Code#SCREEN_NOT_OPEN].
    ///
    /// If this component is already open as a modal component, nothing will happen and
    /// [ModalResult.Code#ALREADY_SHOWING] will be returned. If this component is part of another component,
    /// it will be removed from that component and treated as a modal component.
    ///
    /// Modals are bound to the screen they were opened on, so if the main screen is closed, the modal component will
    /// close as well.
    ///
    /// Note: It is recommended for modals, that should not cover the entire screen, to set a [Positioner] for that
    /// component; this allows declaring the positioning behaviour of a modal component. The positioner of a modal will
    /// always get the bounds of the entire screen if the modal has no associated component, otherwise it will get the
    /// screen bounds of the associated component.
    ///
    /// For more details see [ModalArgs].
    /// @param args The args that define how the modal component should behave
    /// @return If the modal opened successfully, the returned [ModalResult] will complete once
    ///         it closed, otherwise if it couldn't be opened, [ModalResult#getCode()] will return a
    ///         non-empty optional, and the future will never complete.
    ///         This future is not asynchronous to the render thread,
    ///         thus it would not be advisable to also wait for it on the render thread.
    public ModalResult showModal(final ModalArgs args)
    {
        if (GuiScreen.CURRENT_SCREEN == null)
        {
            return new ModalResult(ModalResult.Code.SCREEN_NOT_OPEN);
        }
        
        return GuiScreen.CURRENT_SCREEN.showModal(this, args);
    }
    
    /// If this component is currently a modal component, will remove it from its modal state.
    /// If it is a screen, it will close it and reopen the associated previous screen (if one was set).
    /// @return `true` if the component actually was open as a modal
    public final boolean hideModal()
    {
        if (GuiScreen.CURRENT_SCREEN == null)
        {
            return false;
        }
        
        return GuiScreen.CURRENT_SCREEN.hideModal(this);
    }
    
    //==================================================================================================================
    /// Draws the component below its children.
    ///
    /// Do note that this is only called if the component is visible, has non-zero bounds and is inside the draw area
    /// of its parents. If you need periodical updates regardless its visibility, it is preferable to do this in
    /// [#onDeltaTick(Point, float)].
    /// @param canvas The drawing context
    public void draw(Canvas canvas) {}
    
    /// Draws the component above its children (but below its later siblings inside the parent).
    ///
    /// Do note that this is only called if the component is visible, has non-zero bounds and is inside the draw area
    /// of its parents. If you need periodical updates regardless its visibility, it is preferable to do this in
    /// [#onDeltaTick(Point, float)].
    ///
    /// This will not affect mouse events to children but only rendering, any child that is getting drawn over will still
    /// receive mouse events.
    /// @param canvas The drawing context
    public void drawOnTop(Canvas canvas) {}
    
    //==================================================================================================================
    /// Used internally to render the component on screen. This should normally not be called manually as components
    /// will call this themselves.
    /// @param canvas  The [Canvas] object to draw onto
    /// @param screenX The absolute left coordinate of this component on screen
    /// @param screenY The absolute y coordinate of this component on screen
    public final void renderComponent(final Canvas canvas, final int screenX, final int screenY)
    {
        if (
            !this.isVisible()
            || this.bounds.isEmpty()
            || !canvas.frameIntersects(screenX, screenY, this.getWidth(), this.getHeight())
        )
        {
            this.renderUpdateHierarchy(screenX, screenY, canvas.mousePos, canvas.deltaTime);
            return;
        }
        
        this.screenX = screenX;
        this.screenY = screenY;
        
        this.onDeltaTick(canvas.mousePos, canvas.deltaTime);

        CanvasAttorney.pushFrame(canvas, this);
        {
            // draw the contents of the component
            this.draw(canvas);
            
            // draw all the children
            for (final var child : this.children)
            {
                child.renderComponent(canvas, (screenX + child.getX()), (screenY + child.getY()));
            }
            
            // reset the internal state buffer and draw on top of the component and all its children
            canvas.resetState();
            this.drawOnTop(canvas);
        }
        CanvasAttorney.popFrame(canvas);
    }
    
    //==================================================================================================================
    void updateFocusState(final InputType type, final boolean parent, final boolean focus)
    {
        if (!focus)
        {
            if (this.hasFocus())
            {
                Objects.requireNonNull(GuiComponent.FOCUSED).updateFocusedComponent(type, false);
            }
        }
        else if (this.isDrawing())
        {
            if (this.wantsFocus() && this.isActive())
            {
                this.updateFocusedComponent(type, true);
                return;
            }
            
            if (this.hasFocus() && Objects.requireNonNull(GuiComponent.FOCUSED).isDrawing())
            {
                return;
            }
            
            final IComponentNavigator navigator = this.getNavigator();
            
            if (navigator != null)
            {
                final Optional<GuiComponent> child = navigator
                    .findFirst(this, new FocusNavigationEvent.TabNavigation(true));
                
                if (child.isPresent())
                {
                    child.orElseThrow().updateFocusState(type, false, true);
                    return;
                }
            }
            
            if (parent && this.parent != null)
            {
                this.parent.updateFocusState(type, true, true);
            }
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void renderUpdateHierarchy(final int screenX, final int screenY, final Point mousePos,
                                       final float deltaTime)
    {
        this.screenX = screenX;
        this.screenY = screenY;
        
        this.onDeltaTick(mousePos, deltaTime);
        
        for (final var child : this.children)
        {
            child.renderUpdateHierarchy((screenX + child.getX()), (screenY + child.getY()), mousePos, deltaTime);
        }
    }
    
    private void updateFocusedComponent(final InputType type, final boolean focus)
    {
        if (!focus)
        {
            GuiComponent.FOCUSED = null;
        }
        else if (!this.isFocused())
        {
            final GuiComponent focused = GuiComponent.FOCUSED;
            GuiComponent.FOCUSED = this;
            
            if (focused != null)
            {
                focused.onFocusChanged(type);
                focused.blurred.post(focused);
                focused.updateFocusPath(focused, type);
            }
        }
        else
        {
            return;
        }
        
        this.onFocusChanged(type);
        
        if (focus)
        {
            this.focused.post(this, new FocusEventArgs(type));
        }
        else
        {
            this.blurred.post(this);
        }
        
        this.updateFocusPath(this, type);
    }
    
    private void updateFocusPath(final GuiComponent changed, final InputType type)
    {
        final boolean is_focused = (this.isFocused() || this.containsChild(GuiComponent.FOCUSED));
        
        if (is_focused != ComponentFlag.FOCUS_PATH.get(this))
        {
            ComponentFlag.FOCUS_PATH.set(this, is_focused);
            this.onFocusMigrated(changed, type);
        }
        
        if (this.parent != null)
        {
            this.parent.onChildFocusChanged(changed, type);
            this.parent.updateFocusPath(changed, type);
        }
    }
}
