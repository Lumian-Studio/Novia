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

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.ConfigManager;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.config.client.document.ComponentStyle;
import xyz.lumian.novia.api.config.client.document.ScreenDocument;
import xyz.lumian.novia.api.config.client.document.ScreenEntry;
import xyz.lumian.novia.api.config.client.document.TabDefinition;
import xyz.lumian.novia.api.config.client.gui.schema.ISchemaFactory;
import xyz.lumian.novia.api.config.client.gui.schema.IComponentFactory;
import xyz.lumian.novia.api.config.client.gui.schema.component.NonSupportedSchemaButton;
import xyz.lumian.novia.api.config.property.Property;
import xyz.lumian.novia.api.config.provider.IConfigProvider;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.util.Colour;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class TabManager
{
    //******************************************************************************************************************
    public record PropertyEntry<Container>(
        PropertySpec<Container>    spec,
        PropertyId                 id,
        IConfigProvider<Container> provider,
        IComponentFactory<?>       factory,
        Component                  title,
        Optional<Component>        description
    )
    {
        //**************************************************************************************************************
        public static @Nullable PropertyEntry<?> create(final ScreenEntry.Property property)
        {
            final Identifier         provider_id = property.id().providerId();
            final IConfigProvider<?> provider    = ConfigManager.REGISTRY.get(provider_id);
            
            if (provider == null)
            {
                throw new RuntimeException("Could not find provider for id '" + provider_id + '\'');
            }
            
            return PropertyEntry.createImpl(
                provider,
                property.id(),
                property.title().orElse(Component.literal(Objects.requireNonNull(property.id().pointer().getName()))),
                property.description().orElse(null),
                property.style().orElse(ComponentStyle.EMPTY));
        }
        
        public static <Container> @Nullable PropertyEntry<Container> createImpl(
            final IConfigProvider<Container> provider,
            final PropertyId                 id,
            final Component                  title,
            
            @Nullable
            final Component description,
            
            final ComponentStyle definition
        )
        {
            final PropertySpec<Container> spec = provider.getSpec().flatPropertySpecs().get(id.pointer());
            
            if (spec == null)
            {
                Novia.LOGGER.debug("Could not find property specification for pointer '{}' in provider '{}'",
                     id.pointer(), id.providerId());
                return null;
            }
            
            IComponentFactory<?> comp_factory = null;
            
            if (spec.schema() instanceof ISchemaFactory schema_factory)
            {
                comp_factory = schema_factory.novia$getFactory(id, definition);
            }
            else
            {
                Novia.LOGGER.debug("Property '{}' for provider '{}' has no valid provided schema",
                    id.pointer(), id.providerId());
            }
            
            if (comp_factory == null)
            {
                comp_factory = (screen -> new NonSupportedSchemaButton(id.providerId()));
            }
            
            return new PropertyEntry<>(spec, id, provider, comp_factory, title, Optional.ofNullable(description));
        }
        
        //**************************************************************************************************************
        public Property getProperty() { return this.spec.get(this.provider.getManagedContainer()); }
    }
    
    public record FactoryGroup(List<PropertyEntry<?>> entries, Optional<Component> title)
    {
        //**************************************************************************************************************
        private static FactoryGroup create(final ScreenEntry.Group group)
        {
            return new FactoryGroup(
                group.properties()
                    .stream()
                    .flatMap(p -> Optional.ofNullable(PropertyEntry.create(p)).stream())
                    .collect(Collectors.toUnmodifiableList()),
                Optional.of(group.title()));
        }
    }
    
    public record TabEntry(
        List<FactoryGroup>  groups,
        Component           title,
        Optional<Colour>    colour,
        Optional<Component> description
    )
    {
        //**************************************************************************************************************
        private static TabEntry create(final TabDefinition definition)
        {
            final List<FactoryGroup>         groups               = new ArrayList<>();
            final List<ScreenEntry.Property> ungrouped_properties = new ArrayList<>();
            
            for (final var entry : definition.entry())
            {
                switch (entry.type())
                {
                    case GROUP:
                    {
                        final ScreenEntry.Group group = entry.object().right().orElseThrow();
                        
                        if (!group.properties().isEmpty())
                        {
                            groups.add(FactoryGroup.create(group));
                        }
                    } break;
                    
                    case PROPERTY: ungrouped_properties.add(entry.object().left().orElseThrow());
                }
            }
            
            if (!ungrouped_properties.isEmpty())
            {
                groups.addFirst(new FactoryGroup(
                    ungrouped_properties
                        .stream()
                        .flatMap(p -> Optional.ofNullable(PropertyEntry.create(p)).stream())
                        .collect(Collectors.toUnmodifiableList()),
                    Optional.empty()));
            }
            
            return new TabEntry(
                ImmutableList.copyOf(groups),
                definition.title(),
                definition.colour(),
                definition.description());
        }
    }
    
    //******************************************************************************************************************
    private final List<TabEntry> tabs;
    
    //******************************************************************************************************************
    public TabManager(final int initialCapacity) { this.tabs = new ArrayList<>(initialCapacity); }
    
    //==================================================================================================================
    public void reloadTabs(final ScreenDocument document)
    {
        Objects.requireNonNull(document, "provider list must not be null");
        
        this.tabs.clear();
        this.tabs.addAll(document.tabs().stream()
            .map(TabEntry::create)
            .toList());
    }
    
    //==================================================================================================================
    public Optional<TabEntry> getTab(final int index) { return Optional.ofNullable(this.tabs.get(index)); }
    
    public List<TabEntry> getEntries() { return this.tabs; }
    
    //==================================================================================================================
    public int size() { return this.tabs.size(); }
    
    public Stream<TabEntry> stream() { return this.tabs.stream(); }
    
    public Stream<FactoryGroup> streamGroups(final int index)
    {
        return this.getTab(index).stream().flatMap(e -> e.groups.stream());
    }
}
