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
package xyz.lumian.novia.api.config.client.gui;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.core.serialisation.IValueConvertible;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.IComponentNavigator;
import xyz.lumian.novia.api.gui.component.NaturalNavigator;
import xyz.lumian.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumian.novia.api.gui.component.provided.INVItemModel;
import xyz.lumian.novia.api.gui.component.provided.NVLabel;
import xyz.lumian.novia.api.gui.component.provided.NVListBox;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class OptionList
    extends NVListBox<OptionList.BaseItem>
{
    //******************************************************************************************************************
    public sealed interface BaseItem
        extends INVItemModel
        permits
            GroupItem,
            OptionItem
    {}
    
    public record GroupItem(Component text)
        implements BaseItem
    {
        //**************************************************************************************************************
        @Override
        public void draw(final Canvas canvas, final Rectangle bounds, final int index, final boolean selected,
                         final boolean hovered, final boolean focused)
        {
            canvas.drawTextAligned(this.text, bounds.withLocalPos(), Alignment.BOTTOM_LEFT);
        }
    }
    
    public final class OptionItem
        implements
            BaseItem,
            IValueConvertible
    {
        //**************************************************************************************************************
        public final PropertyId         id;
        public final NVLabel            label;
        public final IStatefulComponent component;
        
        //**************************************************************************************************************
        public OptionItem(final PropertyId id, final Component title, final @Nullable Component description,
                          final IStatefulComponent component)
        {
            this.id        = id;
            this.component = component;
            
            this.label = new NVLabel(title);
            this.label.textAlign.set(Alignment.MIDDLE_LEFT);
            this.label.setColour(NVLabel.COLOUR_TEXT, 0xFFAAAAAA);
            
            if (description != null)
            {
                this.label.setTooltip(Tooltip.create(description));
            }
        }
        
        //==============================================================================================================
        @Override public List<GuiComponent> getChildren() { return List.of(this.label, this.getComponent()); }
        
        public GuiComponent getComponent() { return (GuiComponent) this.component; }
        
        //==============================================================================================================
        @Override public Value getValue() { return this.component.getValue(); }
        
        //==============================================================================================================
        @Override public void setValue(final Value value) { this.component.setValue(value); }
        
        //==============================================================================================================
        @Override
        public void resized(final Rectangle bounds)
        {
            final Rectangle local_bounds = bounds.withLocalPos();
            final int       indent       = (OptionList.this.displayMode.categorised ? 8 : 0);
            
            this.label.setBounds(local_bounds
                .withRightPadding(OPTION_COMPONENT_WIDTH + indent)
                .withTranslationX(indent));

            final GuiComponent component = this.getComponent();
            component.setBounds(local_bounds
                .withWidth(OPTION_COMPONENT_WIDTH)
                .withX(this.label.getRight())
                .withPadding(0, 1, 4, 0));
            component.setPosition(
                component.getX(),
                (int) (local_bounds.y() + ((local_bounds.height() - component.getHeight()) * 0.5)));
        }
    }
    
    //==================================================================================================================
    public record DisplayMode(
        Optional<Comparator<Component>> groupComparator,
        Optional<Comparator<Component>> optionComparator,
        boolean                         categorised
    )
    {
        //**************************************************************************************************************
        public List<BaseItem> apply(final List<Group> options)
        {
            return (this.categorised ? this.applyCategorised(options) : this.applyUncategorised(options));
        }
        
        //--------------------------------------------------------------------------------------------------------------
        private List<BaseItem> applyCategorised(final List<Group> options)
        {
            return this.groupComparator
                .map(comp -> options.stream().sorted(Comparator.comparing((g -> g.groupItem.text), comp)))
                .orElseGet(options::stream)
                .flatMap(e -> Stream.concat(
                    Stream.of(e.groupItem),
                    this.optionComparator
                        .map(comp -> e.optionItems.stream()
                            .sorted(Comparator.comparing((o -> o.label.getMessage()), comp)))
                        .orElseGet(e.optionItems::stream)))
                .toList();
        }
        
        private List<BaseItem> applyUncategorised(final List<Group> options)
        {
            final Stream<OptionItem> stream = options.stream().flatMap(e -> e.optionItems.stream());
            return this.optionComparator
                .map(comp -> stream.sorted(Comparator.comparing((o -> o.label.getMessage()), comp)))
                .orElse(stream)
                .collect(Collectors.toUnmodifiableList());
        }
    }
    
    public class Group
    {
        //**************************************************************************************************************
        private final GroupItem groupItem;
        
        private final List<OptionItem> optionItems;
        
        //**************************************************************************************************************
        public Group(final Component title, final List<OptionItem> optionItems)
        {
            this.groupItem   = new GroupItem(title);
            this.optionItems = optionItems;
        }
        
        //==============================================================================================================
        public void addProperty(final PropertyId propertyId, final Component title,
                                final @Nullable Component description, final IStatefulComponent component)
        {
            this.optionItems.add(new OptionItem(propertyId, title, description, component));
        }
    }
    
    //******************************************************************************************************************
    private static final int OPTION_COMPONENT_WIDTH = 120;
    
    //******************************************************************************************************************
    private final List<Group> groups = new ArrayList<>();
    
    private DisplayMode displayMode = new DisplayMode(Optional.empty(), Optional.empty(), true);
    
    //******************************************************************************************************************
    public OptionList()
    {
        this.itemSize     .set(18);
        this.selectionMode.set(SelectionMode.NONE);
    }
    
    //==================================================================================================================
    public DisplayMode getDisplayMode() { return this.displayMode; }
    
    @Override
    public @Nullable IComponentNavigator getNavigator()
    {
        return NaturalNavigator.withComponents(OptionList.this.getItems().stream()
            .flatMap(item -> item.getChildren().stream())
            .filter(GuiComponent::wantsFocus)
            .toList());
    }
    
    //==================================================================================================================
    public void setDisplayMode(final DisplayMode displayMode)
    {
        if (!this.displayMode.equals(displayMode))
        {
            this.displayMode = displayMode;
            this.refreshList();
        }
    }
    
    //==================================================================================================================
    public Group addOrGetGroup(final Component title)
    {
        final Optional<Group> group = this.groups.stream()
            .filter(g -> g.groupItem.text.getString().equals(title.getString()))
            .findFirst();
        
        if (group.isPresent())
        {
            return group.orElseThrow();
        }
        
        final Group option = new Group(title, new ArrayList<>());
        this.groups.add(option);
        
        return option;
    }
    
    public void clearOptions()
    {
        this.groups.clear();
        this.refreshList();
    }
    
    @Override
    public void refreshList()
    {
        this.clearItems();
        this.addAllItems(this.displayMode.apply(this.groups));
        super.refreshList();
    }
}
