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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.ConfigApiIds;
import xyz.lumian.novia.api.config.ConfigManager;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.config.client.ConfigApiLangClient;
import xyz.lumian.novia.api.config.client.document.ScreenDocument;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.provider.BaseNetworkProvider;
import xyz.lumian.novia.api.config.provider.IConfigProvider;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.core.util.JsonPointer;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.component.GuiScreen;
import xyz.lumian.novia.api.gui.component.input.KeyEvent;
import xyz.lumian.novia.api.gui.component.integration.IStatefulComponent;
import xyz.lumian.novia.api.gui.component.provided.NVLabel;
import xyz.lumian.novia.api.gui.component.provided.NVSimpleButton;
import xyz.lumian.novia.api.gui.geometry.Alignment;
import xyz.lumian.novia.api.gui.geometry.Rectangle;
import xyz.lumian.novia.api.gui.util.KeyboardUtil;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public class ConfigScreen
    extends GuiScreen
{
    //******************************************************************************************************************
    private static final Identifier TEXTURE_UNCATEGORISED = ConfigApiIds.of("category/uncategorised");
    private static final Identifier TEXTURE_CATEGORISED   = ConfigApiIds.of("category/categorised");

    //******************************************************************************************************************
    private final Map<Identifier, IConfigProvider<?>> providers;
    private final ConfigScreenLookAndFeel             lookAndFeel;
    private final TabManager                          tabManager;
    private final TabList                             tabList;
    private final OptionList                          optList;
    private final NVSimpleButton                      viewButton;
    private final NVLabel                             title;
    private final TreeMap<PropertyId, Value>          cache;
    private final EditPanel                           editPanel;
    
    private boolean isCategorised = true;

    //******************************************************************************************************************
    ConfigScreen(final Screen parent, final String modId, final ScreenDocument document,
                 final ConfigScreenLookAndFeel lookAndFeel)
    {
        super(document.title().orElseGet(() -> Component.literal(modId)), parent);
        
        Objects.requireNonNull(modId,       "mod id must not be null");
        Objects.requireNonNull(document,    "document must not be null");
        Objects.requireNonNull(lookAndFeel, "lookandfeel must not be null");
        
        this.lookAndFeel = lookAndFeel;
        this.cache       = new TreeMap<>(Comparator.comparing(e -> e.providerId().toString()));
        
        this.title = this.addChild(this.lookAndFeel.createTitleWidget(this.getMessage()));
        
        this.tabList = this.addChild(new TabList());
        this.tabList.selectionChanged.subscribe((sender, e) -> this.onTabChanged(e.itemIndex()));
        
        this.optList = this.addChild(new OptionList());
        
        this.tabManager = new TabManager(1);
        this.tabManager.reloadTabs(document);
        this.tabManager.getEntries().forEach(tab -> this.tabList.addTab(
            tab.title(),
            tab.colour().orElse(new Colour(0xFF3C8527))));
        this.tabList.refreshList();
        
        this.providers = this.tabManager.getEntries().stream()
                .flatMap(entry -> entry.groups().stream()
                    .flatMap(group -> group.entries().stream().map(TabManager.PropertyEntry::provider)))
                        .distinct()
                        .collect(Collectors.toMap(IConfigProvider::getId, (provider -> provider)));
        
        this.viewButton = this.addChild(new NVSimpleButton());
        this.viewButton.setIcon(this.isCategorised
            ? ConfigScreen.TEXTURE_CATEGORISED
            : ConfigScreen.TEXTURE_UNCATEGORISED);
        this.viewButton.setTooltip(Tooltip.create(Component.nullToEmpty(this.isCategorised
            ? "Categorised"
            : "Uncategorised")));
        this.viewButton.clicked.subscribe((sender, args) ->
        {
            this.isCategorised = !this.isCategorised;
            this.optList.setDisplayMode(new OptionList.DisplayMode(
                Optional.empty(),
                Optional.of(Comparator.comparing(Component::getString)),
                this.isCategorised));

            final int selected = this.tabList.getIndexOfFirstSelectedItem();
            this.onTabChanged(selected);

            sender.setTooltip(Tooltip.create(Component.nullToEmpty(this.isCategorised
                ? "Categorised"
                : "Uncategorised")));
            ((NVSimpleButton) sender).setIcon(this.isCategorised
                ? ConfigScreen.TEXTURE_CATEGORISED
                : ConfigScreen.TEXTURE_UNCATEGORISED);
        });
        
        this.optList.setDisplayMode(new OptionList.DisplayMode(
            Optional.empty(),
            Optional.of(Comparator.comparing(Component::getString)),
            this.isCategorised));

        this.editPanel = this.addChild(new EditPanel(lookAndFeel));
        this.editPanel.setVisible(false);
        
        this.setFont(lookAndFeel.getDefaultFont());
    }
    
    //==================================================================================================================
    public void showEditScreen(final PropertyId propertyId, final Component title, final IStatefulComponent content)
    {
        this.editPanel.setProperty(propertyId, title, content);
    }
    
    //==================================================================================================================
    @Override
    public void resized()
    {
        this.lookAndFeel.resize(this.getLocalBounds());

        final Rectangle title_bounds = this.lookAndFeel.getTitleBounds(
            this.title.getMessage(),
            this.lookAndFeel.getConfigPanelClientBounds());

        this.title  .setBounds(title_bounds);
        this.tabList.setBounds(this.lookAndFeel.getTabListBounds());
        this.optList.setBounds(this.lookAndFeel.getOptionListBounds());

        final Rectangle button_area = title_bounds.withRightCut(100).pad(10, 0);
        this.viewButton.setBounds(button_area
            .withSize(16, 16)
            .align(Alignment.BOTTOM_RIGHT, button_area)
            .translateY(-5));
        
        this.editPanel.setBounds(this.getBounds());
    }
    
    //==================================================================================================================
    @Override
    public void draw(final Canvas canvas)
    {
        super.draw(canvas);
        this.lookAndFeel.drawBackground(canvas);
    }
    
    //==================================================================================================================
    private void onTabChanged(final int index)
    {
        this.optList.clearOptions();
        this.tabManager.streamGroups(index).forEach(this::addGroup);
        this.optList.refreshList();
    }
    
    private void addGroup(final TabManager.FactoryGroup group)
    {
        final OptionList.Group option = this.optList.addOrGetGroup(group.title()
            .orElse(ConfigApiLangClient.CONFIG_GROUP_UNGROUPED_TITLE));
        
        group.entries().forEach(property ->
        {
            final IStatefulComponent component = property.factory().create(this);
            component.setValue(property.getProperty().getValue());
            component.getChangeEvent().subscribe((sender, e)
                -> this.valueChanged(property.id(), ((IStatefulComponent) sender).getValue()));
            option.addProperty(property.id(), property.title(), property.description().orElse(null), component);
        });
    }
    
    //==================================================================================================================
    @Override
    public boolean onKeyDown(final KeyEvent e)
    {
        final int number = KeyboardUtil.getKeyNumber(e.input);

        if (number > -1 && number < this.tabList.getItemCount())
        {
            this.tabList.selectItem(number);
            return true;
        }

        return super.onKeyDown(e);
    }

    //==================================================================================================================
    private void valueChanged(final PropertyId id, final @Nullable Value value)
    {
        if (value == null)
        {
            this.cache.remove(id);
            return;
        }
        
        final IConfigProvider<?> provider = Objects.requireNonNull(ConfigManager.REGISTRY.get(id.providerId()));
        final Property           property = this.getProperty(id.pointer(), provider);
        
        if (property.getValue().equals(value))
        {
            this.cache.remove(id);
        }
        else
        {
            this.cache.put(id, value);
        }
    }
    
    private <Container> Property getProperty(final JsonPointer pointer, final IConfigProvider<Container> provider)
    {
        final Container container = (provider instanceof BaseNetworkProvider<Container> bnp
            ? bnp.getClientContainer()
            : provider.getManagedContainer());
        return provider.getSpec().flatPropertySpecs().get(pointer).get(Objects.requireNonNull(container));
    }
}
