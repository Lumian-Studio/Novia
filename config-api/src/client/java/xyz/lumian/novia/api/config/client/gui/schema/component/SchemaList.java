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
package xyz.lumian.novia.api.config.client.gui.schema.component;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.client.ConfigApiLangClient;
import xyz.lumian.novia.api.config.client.gui.ConfigScreen;
import xyz.lumian.novia.api.config.client.gui.schema.IComponentFactory;
import xyz.lumian.novia.api.core.serialisation.IValueConvertible;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.gui.component.GuiComponent;
import xyz.lumian.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumian.novia.api.gui.component.provided.INVItemModel;
import xyz.lumian.novia.api.gui.component.provided.NVListBox;
import xyz.lumian.novia.api.gui.component.provided.NVSimpleButton;
import xyz.lumian.novia.api.gui.event.GuiEvent;
import xyz.lumian.novia.api.gui.geometry.Rectangle;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



//**********************************************************************************************************************
public class SchemaList
    extends NVListBox<SchemaList.Entry>
    implements IStatefulComponent
{
    //******************************************************************************************************************
    public class Entry
        implements
            INVItemModel,
            IValueConvertible
    {
        //**************************************************************************************************************
        private static final int REMOVE_BUTTON_WIDTH = 20;
        
        //**************************************************************************************************************
        public final IStatefulComponent component;
        public final NVSimpleButton     removeButton;
        
        //**************************************************************************************************************
        public Entry(final IStatefulComponent component)
        {
            this.component = Objects.requireNonNull(component, "component must not be null");
            
            this.removeButton = new NVSimpleButton(Component.literal("X"));
            this.removeButton.setTooltip(Tooltip.create(ConfigApiLangClient.CONFIG_SCHEMA_LIST_REMOVE_ITEM));
            this.removeButton.setDimensions((SchemaList.this.tuple ? 0 : REMOVE_BUTTON_WIDTH), 0);
            this.removeButton.clicked.subscribe((sender, e) -> SchemaList.this.removeItem(this));
        }
        
        //==============================================================================================================
        @Override public Value getValue() { return this.component.getValue(); }
        
        @Override
        public List<GuiComponent> getChildren()
        {
            if (SchemaList.this.tuple)
            {
                return List.of(((GuiComponent) this.component));
            }
            
            return List.of(this.removeButton, ((GuiComponent) this.component));
        }
        
        //==============================================================================================================
        @Override public void setValue(final Value value) { this.component.setValue(value); }
        
        //==============================================================================================================
        @Override
        public void resized(final Rectangle bounds)
        {
            this.removeButton.setHeight(bounds.height());
            ((GuiComponent) this.component).setBounds(bounds.withLeftPadding(this.removeButton.getWidth()));
        }
    }
    
    //******************************************************************************************************************
    private static final int ADD_BUTTON_WIDTH = 20;
    private static final int ITEM_HEIGHT      = 20;
    
    //******************************************************************************************************************
    public final GuiEvent.Simple saved = new GuiEvent.Simple();
    
    //==================================================================================================================
    private final List<Optional<IComponentFactory<?>>> factories;
    
    @Nullable
    private final NVSimpleButton addEntryButton;
    
    private final boolean      tuple;
    private final ConfigScreen screen;
    private final Identifier   providerId;
    
    //******************************************************************************************************************
    public SchemaList(final ConfigScreen screen, final Identifier providerId, final IComponentFactory<?> factory)
    {
        this.itemSize.set(SchemaList.ITEM_HEIGHT);
        this.selectionMode.set(SelectionMode.NONE);
        
        this.screen     = screen;
        this.factories  = Collections.singletonList(Optional.of(factory));
        this.tuple      = false;
        this.providerId = providerId;
        
        this.addEntryButton = this.addChild(new NVSimpleButton(Component.literal("+")));
        this.addEntryButton.clicked.subscribe((sender, e) -> this.addItem(new Entry(factory.create(screen))));
        this.addEntryButton.setPinned(true);
    }
    
    public SchemaList(final ConfigScreen screen, final Identifier providerId,
                      final List<Optional<IComponentFactory<?>>> factories)
    {
        this.itemSize     .set(SchemaList.ITEM_HEIGHT);
        this.selectionMode.set(SelectionMode.NONE);
        
        this.screen         = screen;
        this.factories      = factories;
        this.tuple          = true;
        this.addEntryButton = null;
        this.providerId     = providerId;
        
        this.initTuple();
    }
    
    //==================================================================================================================
    private void initTuple()
    {
        if (!this.tuple)
        {
            return;
        }
        
        this.addAllItems(this.factories.stream()
            .map(factory -> factory
                .map      (f -> new Entry(f.create(this.screen)))
                .orElseGet(() -> new Entry(new NonSupportedSchemaButton(this.providerId))))
            .toList());
    }
    
    //==================================================================================================================
    @Override
    public Value getValue()
    {
        return new Value(this
            .streamItems()
            .map(Entry::getValue)
            .collect(Collectors.toList()));
    }
    
    @Override public GuiEvent.Simple getChangeEvent() { return this.saved; }
    
    //==================================================================================================================
    @Override
    public void setValue(final Value value)
    {
        if (!value.isList())
        {
            return;
        }
        
        final List<Value> list = value.getList();
        
        if (this.tuple)
        {
            IntStream.range(0, Math.min(list.size(), this.getItemCount()))
                .forEach(i -> this.getItemAt(i).orElseThrow().setValue(list.get(i)));
            return;
        }
        
        final IComponentFactory<?> factory = this.factories.getFirst().orElseThrow();
        
        this.clearItems();
        this.addAllItems(list.stream()
            .map(val ->
            {
                final IStatefulComponent component = factory.create(this.screen);
                component.setValue(val);
                return new Entry(component);
            })
            .toList());
    }
    
    //==================================================================================================================
    @Override public void mute()   {}
    @Override public void unmute() {}
}
