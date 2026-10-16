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
package xyz.lumian.novia.api.gui.component.provided;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.GuiApiId;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.canvas.ColourId;
import xyz.lumian.novia.api.gui.component.*;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.event.GuiEventArgs;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Point;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.property.GuiProperty;
import xyz.lumian.novia.api.gui.property.GuiPropertyBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// A text-box with a button that can only contain a pre-determined set of options, and a menu that lists the possible
/// options that can be selected.
///
/// This is a stateful GUI component, the value it contains represents the value of the option that is currently
/// selected, it can be converted between [Value] objects.
public class NVDropdown
    extends StatefulGuiComponent
{
    //******************************************************************************************************************
    public class Option
        implements INVItemModel
    {
        //**************************************************************************************************************
        public static Option forValue(final NVDropdown dropdown, final Value value)
        {
            final String name = value.asString();
            return dropdown.new Option(name, value, Component.literal(name));
        }
        
        //**************************************************************************************************************
        private final String    name;
        private final Value     value;
        private final Component title;
        private final Tooltip   tooltip;
        
        //**************************************************************************************************************
        public Option(final String name, final Value value, final Component title)
        {
            this.name    = Objects.requireNonNull(name, "name must not be null");
            this.value   = Objects.requireNonNull(value, "value must not be null");
            this.title   = Objects.requireNonNull(title, "title must not be null");
            this.tooltip = Tooltip.create(title);
        }
        
        //==============================================================================================================
        @Override
        public @Nullable Tooltip getTooltip(final boolean selected) {return this.tooltip;}
        
        public String getName() { return this.name; }
        
        public Value getValue() { return this.value; }
        
        public Component getTitle() { return this.title; }
        
        //==============================================================================================================
        @Override
        public boolean onClick(final Point mousePos, final boolean selected)
        {
            ComponentUtil.playClickSound();
            return INVItemModel.super.onClick(mousePos, selected);
        }
        
        //==============================================================================================================
        @Override
        public void draw(final Canvas canvas, final Rectangle bounds, final int index, final boolean selected,
                         final boolean hovered, final boolean focused)
        {
            canvas.getTemplate().nvDropdownDrawMenuOption(canvas, NVDropdown.this, this.title, bounds.width(),
                                                          bounds.height(), index, selected, hovered, focused);
        }
    }
    
    public interface Template
    {
        //**************************************************************************************************************
        /// Draws the background of the dropdown.
        /// @param canvas   The [Canvas]
        /// @param dropdown The [NVDropdown]
        /// @param width    The width of the dropdown
        /// @param height   The height of the dropdown
        void nvDropdownDrawMenuBackground(Canvas canvas, NVDropdown dropdown, int width, int height);
        
        /// Draws a single option inside the dropdown menu.
        /// @param canvas   The [Canvas]
        /// @param dropdown The [NVDropdown]
        /// @param title    The text of the option
        /// @param width    The width of the dropdown
        /// @param height   The height of the dropdown
        /// @param index    The index of the option in the option list
        /// @param selected Whether the option is selected
        /// @param hovered  Whether the option is hovered
        /// @param focused  Whether the option is focused
        void nvDropdownDrawMenuOption(Canvas canvas, NVDropdown dropdown, Component title,
                                      int width, int height, int index, boolean selected, boolean hovered,
                                      boolean focused);
        
        /// Draws the arrow button of the dropdown.
        /// @param canvas   The [Canvas]
        /// @param dropdown The [NVDropdown]
        /// @param button   The [NVAbstractButton] component
        /// @param isOpen   Whether the menu is currently open
        void nvDropdownDrawButton(Canvas canvas, NVDropdown dropdown, NVAbstractButton button, boolean isOpen);
    }
    
    public record OptionEventArgs(NVDropdown.Option option)
        implements GuiEventArgs {}
    
    //------------------------------------------------------------------------------------------------------------------
    private class Menu
        extends GuiComponent
    {
        //**************************************************************************************************************
        private final NVListBox<Option> listBox;
        
        //**************************************************************************************************************
        public Menu(final List<Option> options)
        {
            this.listBox = new NVListBox<>();
            this.listBox.selectionMode.set(NVListBox.SelectionMode.SINGLE);
            this.listBox.itemSize.set(NVDropdown.ITEM_HEIGHT);
            this.listBox.addAllItems(options);
            this.listBox.refreshList();
            this.listBox.selectItem(NVDropdown.this.selected);
            this.listBox.selectionChanged.subscribe((sender, args) -> this.hideModal());
            this.addChild(this.listBox);
            
            this.setPositioner(this::calculateBounds);
        }
        
        //==============================================================================================================
        public Integer getSelected() { return this.listBox.getIndexOfFirstSelectedItem(); }
        
        //==============================================================================================================
        @Override
        public void resized() {this.listBox.setBounds(this.getLocalBounds().pad(1));}
        
        //==============================================================================================================
        @Override
        public void draw(final Canvas canvas)
        {
            canvas.getTemplate().nvDropdownDrawMenuBackground(canvas, NVDropdown.this, this.getWidth(),
                                                              this.getHeight());
        }
        
        //==============================================================================================================
        private Rectangle calculateBounds(final int x, final int y, final int width, final int height)
        {
            final int window_height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            final int new_y         = (y + height - 1);
            
            return new Rectangle(x, new_y, width, (NVDropdown.ITEM_HEIGHT * this.listBox.getItemCount() + 2))
                .constrainToMax(Integer.MAX_VALUE, (window_height - new_y - 2))
                .constrainToMin(0, NVDropdown.MIN_LIST_HEIGHT);
        }
    }
    
    private class DropDownButton
        extends NVAbstractButton
    {
        //**************************************************************************************************************
        private boolean open = false;
        
        //**************************************************************************************************************
        public DropDownButton() { this.setWantsFocus(false); }
        
        //==============================================================================================================
        public void setOpen(final boolean isOpen) { this.open = isOpen; }
        
        //==============================================================================================================
        @Override
        public boolean hitTest(final int x, final int y) {return (x > 0 && x < this.getWidth());}
        
        //==============================================================================================================
        @Override
        public void draw(final Canvas canvas)
        {
            canvas.getTemplate().nvDropdownDrawButton(canvas, NVDropdown.this, this, this.open);
        }
    }
    
    //******************************************************************************************************************
    /// The colour used for drawing the text in the drop-down menu.
    public static final ColourId COLOUR_OPTION_TEXT = ColourId.reserve();
    
    /// The colour used for drawing the highlight beneath the text in the drop-down menu.
    public static final ColourId COLOUR_OPTION_BACKGROUND_HIGHLIGHT = ColourId.reserve();
    
    /// The colour used for drawing the selection highlight beneath the text in the drop-down menu.
    public static final ColourId COLOUR_OPTION_BACKGROUND_SELECTED = ColourId.reserve();
    
    //==================================================================================================================
    /// See [NVDropdown#showSuggestion].
    public static final boolean DEFAULT_SHOW_SUGGESTION = true;
    
    /// See [NVDropdown#optionAlignment].
    public static final Alignment DEFAULT_OPTION_ALIGNMENT = Alignment.MIDDLE_LEFT;
    
    //==================================================================================================================
    private static final int ITEM_HEIGHT     = 15;
    private static final int MIN_LIST_HEIGHT = (ITEM_HEIGHT * 3);
    
    //******************************************************************************************************************
    /// Describes the ID of the option, which should be automatically selected. If this property is set and has an empty
    /// string, or an ID not referring to a valid option, it will always select the first option. If it is not set,
    /// the drop-down can be in an empty state. If no options are in the list, none will be selected.
    public final GuiProperty<String> defaultOption;
    
    /// Describes whether the text box should show the first closest match to the given input if it doesn't match any
    /// option at that point.
    public final GuiProperty.NonNull<Boolean> showSuggestion;
    
    /// Describes the alignment of options inside the drop-down list.
    public final GuiProperty.NonNull<Alignment> optionAlignment;
    
    //==================================================================================================================
    /// Triggered whenever a new option has been added to the drop-down.
    public final GuiEvent<OptionEventArgs> optionAdded = new GuiEvent<>();
    
    /// Triggered whenever an option has been removed from the drop-down.
    public final GuiEvent<OptionEventArgs> optionRemoved = new GuiEvent<>();
    
    //==================================================================================================================
    private final List<Option>   options;
    private final NVTextBox      input;
    private final DropDownButton arrowButton;
    
    private int     selected = -1;
    private boolean isOpen   = false;
    
    //******************************************************************************************************************
    /// Creates new drop-down with the given options pre-initialised.
    /// @param values  The pre-initialised options
    /// @param message The component message
    public NVDropdown(final Collection<Pair<Component, Value>> values, final Component message)
    {
        super(message);
        
        this.options = values
            .stream()
            .map(p -> new Option(p.getFirst().getString(), p.getSecond(), p.getFirst()))
            .collect(Collectors.toList());
        
        this.defaultOption   = GuiPropertyBuilder.nullable((String) null)
            .withSetter(v -> this.setDefaultSelectedOption())
            .build();
        this.showSuggestion  = GuiPropertyBuilder.nonNull(NVDropdown.DEFAULT_SHOW_SUGGESTION)
            .withSetter(this::updateSuggestionMode)
            .build();
        this.optionAlignment = GuiPropertyBuilder.nonNull(NVDropdown.DEFAULT_OPTION_ALIGNMENT)
            .build();
        
        this.input = this.addChild(new NVTextBox());
        this.input.textPredicate.set(this::hasOption);
        this.input.valueChanged.subscribe(this::textChanged);
        
        this.arrowButton = this.addChild(new DropDownButton());
        this.arrowButton.clicked.subscribe(this::toggleMenu);
        
        this.setDefaultSelectedOption();
    }
    
    /// Creates new drop-down with the given options pre-initialised.
    /// @param values The pre-initialised options
    public NVDropdown(final Collection<Pair<Component, Value>> values) { this(values, CommonComponents.EMPTY); }
    
    /// Creates new empty drop-down.
    public NVDropdown() { this(Collections.emptyList()); }
    
    //==================================================================================================================
    /// Gets the currently selected value from the drop-down. If there is no selected option, this will throw a
    /// [UndefinedComponentStateException], so make sure to test with [#hasSelectedOption()] first.
    /// @return The currently selected [Value]
    /// @throws UndefinedComponentStateException If there is no selected option
    @Override
    public Value getValue()
    {
        if (this.selected < 0)
        {
            throw new UndefinedComponentStateException("no option was selected");
        }
        
        return this.options.get(this.selected).value;
    }
    
    /// Gets the internal text box, this should only be used for styling purposes.
    /// @return The internal [NVTextBox]
    public NVTextBox getTextBox() { return this.input; }
    
    /// Gets the text of the currently selected option as string.
    /// @return The selected text
    public Optional<Option> getSelectedOption() { return this.getOption(this.selected); }
    
    /// Gets the option at the specified index, or an empty optional if the index is out of bounds.
    /// @param index The index of the option
    /// @return An [Optional] containing the [Option]
    public Optional<Option> getOption(final int index)
    {
        if (index < 0 || index > this.getOptionCount())
        {
            return Optional.empty();
        }
        
        return Optional.of(this.options.get(index));
    }
    
    /// Gets the option with the specified index, or an empty optional if there is none.
    /// @param optionName The name of the option
    /// @return An [Optional] containing the [Option]
    public Optional<Option> getOption(final String optionName)
    {
        return Optional
            .ofNullable(this.getOptionFor(optionName, Option::getName))
            .map(Pair::getSecond);
    }
    
    /// Gets the first option that is found containing the given value, or an empty optional if there is none.
    /// @param value The value of the option
    /// @return An [Optional] containing the [Option]
    public Optional<Option> getOption(final Value value)
    {
        return Optional
            .ofNullable(this.getOptionFor(value, Option::getValue))
            .map(Pair::getSecond);
    }
    
    /// {@return the number of options in the drop-down}
    public int getOptionCount() { return this.options.size(); }
    
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Stream<GuiPropertyDescription<?>> getGuiProperties()
    {
        return Stream.of(
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.DROPDOWN_DEFAULT_OPT,
                this.defaultOption,
                Codec.STRING),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.DROPDOWN_OPT_ALIGNMENT,
                this.optionAlignment,
                Alignment.CODEC),
            new GuiPropertyDescription<>(
                GuiApiId.GuiProperty.DROPDOWN_SHOW_SUGGESTION,
                this.showSuggestion,
                Codec.BOOL)
        );
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> @Nullable Pair<Integer, Option> getOptionFor(final T optionComponent, final Function<Option, T> getter)
    {
        return IntStream
            .range(0, this.options.size())
            .mapToObj(i -> Pair.of(i, this.options.get(i)))
            .filter(p -> getter.apply(p.getSecond()).equals(optionComponent))
            .findFirst()
            .orElse(null);
    }
    
    //==================================================================================================================
    /// Determines whether this drop-down contains an option with the given name.
    /// @param optionName The unique name of the option
    /// @return `true` if an option with that name exists
    public boolean hasOption(final String optionName) { return (this.getOption(optionName).isPresent()); }
    
    /// Determines whether this drop-down contains an option with the given name.
    /// @param option The [Option] to check the name of
    /// @return `true` if the option exists
    public boolean hasOption(final Option option) { return this.hasOption(option.name); }
    
    /// Determines whether this drop-down contains an option with the given value.
    /// @param value The [Value] the option has to contain
    /// @return `true` if an option with that [Value] exists
    public boolean hasOption(final Value value) { return (this.getOption(value).isPresent()); }
    
    /// Determines whether the drop-down has any options to pick from.
    /// @return `true` if there is at least one option
    public boolean hasAnyOptions() { return !this.options.isEmpty(); }
    
    /// Whether this drop down currently has an option selected or not.
    /// @return `true` if there is an option selected
    public boolean hasSelectedOption() { return (this.selected > -1); }
    
    //==================================================================================================================
    /// Sets the option to be selected based on its associated value.
    /// @param value The [Value] the option to be selected has
    @Override
    public void setValue(final Value value)
    {
        Objects.requireNonNull(value, "value must not be null");
        this.setSelectedOption(value);
    }
    
    /// Sets the selected option from its unique name.
    /// @param optionName The name of the option
    public void setSelectedOption(final String optionName)
    {
        Objects.requireNonNull(optionName, "option name must not be null");
        
        final Pair<Integer, Option> opt = this.getOptionFor(optionName, (opt2 -> opt2.name));
        
        if (opt == null || opt.getFirst() == this.selected)
        {
            return;
        }
        
        this.setSelectedOption(opt.getFirst());
    }
    
    /// Sets the selected option by its given name.
    /// @param option The option to get the name of
    public void setSelectedOption(final Option option) { this.setSelectedOption(option.name); }
    
    /// Sets the selected option to the first one with the given value.
    /// @param value The value of the option to select
    public void setSelectedOption(final Value value)
    {
        Optional
            .ofNullable(this.getOptionFor(value, Option::getValue))
            .ifPresent(p -> this.setSelectedOption(p.getFirst()));
    }
    
    /// Sets the selected option by its index in the option list.
    /// @param index The index of the option to select
    private void setSelectedOption(final int index)
    {
        if (index < 0 || index >= this.getOptionCount() || index == this.selected)
        {
            return;
        }
        
        this.selected = index;
        
        this.updateText();
        this.sendChangeNotification();
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void setDefaultSelectedOption()
    {
        this.defaultOption.ifSet(optionName ->
        {
            if (this.selected < 0)
            {
                Optional
                    .ofNullable(this.getOptionFor(optionName, Option::getName))
                    .ifPresent(p -> this.setSelectedOption(p.getFirst()));
            }
        });
    }
    
    //==================================================================================================================
    /// Inserts the given option with the given value and text to the drop-down list at the specified index.
    /// @param index  The index to insert the option at
    /// @param option The option to add
    /// @return `true` if the option was added, `false` if an option by that name already existed
    /// @throws IndexOutOfBoundsException If the insertion index is out of bounds (i < 0 or i > size)
    public boolean addOption(final int index, final Option option)
    {
        if (this.hasOption(option.name))
        {
            return false;
        }
        
        this.options.add(index, option);
        this.setDefaultSelectedOption();
        this.sendOptionAddedNotification(option);
        
        return true;
    }
    
    /// Adds the given option with the given value and text to the end of the drop-down list.
    /// @param option The option to add
    /// @return `true` if the option was added, `false` if an option by that name already existed
    public boolean addOption(final Option option) { return this.addOption(this.getOptionCount(), option); }
    
    /// Adds the given options to the end of the drop-down list if options with the given names don't already exist.
    /// @param options The options to add
    /// @return The number of options that have been added
    public int addAllOptions(final Collection<Option> options)
    {
        final List<Option> new_options = options
            .stream()
            .filter(Predicate.not(this::hasOption))
            .toList();
        
        this.options.addAll(new_options);
        new_options.forEach(this::sendOptionAddedNotification);
        
        this.setDefaultSelectedOption();
        
        return new_options.size();
    }
    
    /// Removes the option with the given name from the drop-down list.
    /// @param optionName The unique name of the option
    /// @return `true` if the option was removed, `false` if no option by that name existed
    public boolean removeOption(final String optionName)
    {
        return Optional
            .ofNullable(this.getOptionFor(optionName, Option::getName))
            .map(p -> this.removeOption(p.getFirst()))
            .orElse(false);
    }
    
    /// Removes the option at the given index from the drop-down list.
    /// @param index The index of the option
    /// @return `true` if the option was removed, `false` if no option existed at that index
    public boolean removeOption(final int index)
    {
        if (index < 0 || index >= this.getOptionCount())
        {
            return false;
        }
        
        final Option option = this.options.remove(index);
        this.sendOptionRemovedNotification(option);
        
        this.setDefaultSelectedOption();
        
        if (this.selected > -1 && !this.hasAnyOptions())
        {
            this.selected = -1;
        }
        
        return true;
    }
    
    /// Removes the given option from the drop-down list if it existed.
    /// @param option The option to remove
    /// @return `true` if the option was removed, `false` if no option existed
    public boolean removeOption(final Option option) { return this.removeOption(option.name); }
    
    /// Removes all options that contain the given value.
    /// @param value The [Value] to check the options against
    /// @return The number of options that were removed
    public int removeAllOptions(final Value value)
    {
        int count = 0;
        
        for (int i = 0; i < this.getOptionCount(); ++i)
        {
            if (this.options.get(i).value.equals(value))
            {
                this.removeOption(i);
                ++count;
            }
        }
        
        return count;
    }
    
    /// Clears all options in the drop-down list.
    public void clearOptions()
    {
        final List<Option> temp_options = new ArrayList<>(this.options);
        
        this.options.clear();
        temp_options.forEach(this::sendOptionRemovedNotification);
        
        if (this.selected > -1)
        {
            this.selected = -1;
            
            this.updateText();
            this.sendChangeNotification();
        }
    }
    
    /// Clears the selected option in the drop-down if [#defaultOption] is not set, otherwise does nothing.
    public void clearSelectedOption()
    {
        if (this.defaultOption.isSet() || this.selected < 0)
        {
            return;
        }
        
        this.selected = -1;
        
        this.updateText();
        this.sendChangeNotification();
    }
    
    //==================================================================================================================
    private void toggleMenu(final @Nullable GuiComponent button, final GuiEventArgs args)
    {
        if (this.isOpen)
        {
            return;
        }
        
        (new Menu(this.options))
            .showModal(ModalArgs.popup(this))
            .onOpened(() -> this.arrowButton.setOpen(this.isOpen = true))
            .thenAccept(comp ->
            {
                this.arrowButton.setOpen(this.isOpen = false);
                this.setSelectedOption(Objects.requireNonNull((Menu) comp).getSelected());
            });
    }
    
    //==================================================================================================================
    @Override
    public boolean onKeyDown(final KeyEvent e)
    {
        if (this.isActive() && e.source == this.input && e.input == GLFW.GLFW_KEY_TAB)
        {
            final String suggestion = this.input.suggestion.get();
            
            if (suggestion != null)
            {
                this.input.setText(this.input.getText() + suggestion);
                return true;
            }
        }
        
        return false;
    }
    
    //==================================================================================================================
    public void onOptionAdded(final Option option) {}
    
    public void onOptionRemoved(final Option option) {}
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        final Rectangle input_bounds = this.getLocalBounds();
        this.arrowButton.setBounds(input_bounds.removeRight(10));
        this.input.setBounds(input_bounds.padRight(-3));
    }
    
    //==================================================================================================================
    @Override
    public void onChildFocusChanged(final GuiComponent child, final InputType type)
    {
        if (child == this.input && !child.isFocused())
        {
            this.updateText();
        }
    }
    
    //==================================================================================================================
    private void textChanged(final GuiComponent sender, final GuiEventArgs e)
    {
        final String text = this.input.getText();
        this.input.suggestion.set(null);
        
        if (text.isEmpty())
        {
            return;
        }
        
        final Pair<Integer, Option> opt = this.getOptionFor(text, (opt2 -> opt2.title));
        
        if (opt != null)
        {
            if (this.selected != opt.getFirst())
            {
                this.selected = opt.getFirst();
                this.sendChangeNotification();
            }
            
            return;
        }
        
        if (this.showSuggestion.get())
        {
            this.options
                .stream()
                .filter(opt2 -> opt2.name.startsWith(text))
                .findFirst()
                .map(opt2 -> opt2.name)
                .ifPresent(str -> this.input.suggestion.set(str.substring(text.length())));
        }
    }
    
    //==================================================================================================================
    private void sendOptionAddedNotification(final Option option)
    {
        this.onOptionAdded(option);
        this.optionAdded.post(this, new OptionEventArgs(option));
    }
    
    private void sendOptionRemovedNotification(final Option option)
    {
        this.onOptionRemoved(option);
        this.optionRemoved.post(this, new OptionEventArgs(option));
    }
    
    //==================================================================================================================
    private void updateSuggestionMode(final boolean enable)
    {
        if (!enable)
        {
            this.input.suggestion.set(null);
        }
    }
    
    private void updateText()
    {
        if (this.selected < 0 || !this.hasAnyOptions())
        {
            return;
        }
        
        this.input.mute();
        this.input.setText(this.options.get(this.selected).name);
        this.input.unmute();
        
        this.input.setCursorToStart(false);
        this.input.suggestion.set(null);
    }
}
