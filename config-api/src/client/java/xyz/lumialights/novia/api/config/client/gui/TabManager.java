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
package xyz.lumialights.novia.api.config.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.ConfigManager;
import xyz.lumialights.novia.api.config.PropertyId;
import xyz.lumialights.novia.api.config.client.gui.schema.ISchemaFactory;
import xyz.lumialights.novia.api.config.client.gui.schema.IComponentFactory;
import xyz.lumialights.novia.api.config.client.gui.schema.component.NonSupportedSchemaButton;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.provider.IConfigProvider;
import xyz.lumialights.novia.api.config.spec.PropertySpec;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.core.util.Colour;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class TabManager
{
    //******************************************************************************************************************
    public record PropertyEntry<Container>(@NotNull  PropertySpec<Container>    spec,
                                           @NotNull  PropertyId                 id,
                                           @NotNull  IConfigProvider<Container> provider,
                                           @NotNull  IComponentFactory<?>       factory,
                                           @NotNull  Text                       title,
                                           @Nullable Text                       description)
    {
        //**************************************************************************************************************
        public static @Nullable PropertyEntry<?> create(@NotNull final ScreenEntry.Property property)
        {
            final Identifier         provider_id = property.id().providerId();
            final IConfigProvider<?> provider    = ConfigManager.REGISTRY.get(provider_id);
            
            if (provider == null)
            {
                throw new RuntimeException("Could not find provider for id '" + provider_id + '\'');
            }
            
            return createImpl(
                provider,
                property.id(),
                property.title().orElse(Text.literal(Objects.requireNonNull(property.id().pointer().getName()))),
                property.description().orElse(null),
                property.style().orElse(ComponentStyle.EMPTY));
        }
        
        public static <Container> @Nullable PropertyEntry<Container> createImpl(
            @NotNull  final IConfigProvider<Container> provider,
            @NotNull  final PropertyId                 id,
            @NotNull  final Text                       title,
            @Nullable final Text                       description,
            @NotNull  final ComponentStyle             definition
        )
        {
            final PropertySpec<Container> spec = provider.getSpec().flatPropertySpecs().get(id.pointer());
            
            if (spec == null)
            {
                Novia.LOGGER.debug(
                    "Could not find property specification for pointer '{}' in provider '{}'",
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
                Novia.LOGGER.debug(
                    "Property '{}' for provider '{}' has no valid provided schema",
                    id.pointer(), id.providerId());
            }
            
            if (comp_factory == null)
            {
                comp_factory = (screen -> new NonSupportedSchemaButton(id.providerId()));
            }
            
            return new PropertyEntry<>(spec, id, provider, comp_factory, title, description);
        }
        
        //**************************************************************************************************************
        public @NotNull Property getProperty() { return this.spec.get(this.provider.getManagedContainer()); }
    }
    
    public record FactoryGroup(@NotNull List<PropertyEntry<?>> entries, @NotNull Optional<Text> title)
    {
        //**************************************************************************************************************
        private static @NotNull FactoryGroup create(@NotNull final ScreenEntry.Group group)
        {
            return new FactoryGroup(
                group.properties()
                    .stream()
                    .flatMap(p -> Optional.ofNullable(PropertyEntry.create(p)).stream())
                    .collect(Collectors.toUnmodifiableList()),
                Optional.of(group.title()));
        }
    }
    
    public record TabEntry(@NotNull List<FactoryGroup> groups,
                           @NotNull Text               title,
                           @NotNull Optional<Colour>   colour,
                           @NotNull Optional<Text>     description)
    {
        //**************************************************************************************************************
        private static @NotNull TabEntry create(@NotNull final TabDefinition definition)
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
    public void reloadTabs(@NotNull final ScreenDocument document)
    {
        Objects.requireNonNull(document, "provider list must not be null");
        
        this.tabs.clear();
        this.tabs.addAll(document
            .tabs()
            .stream()
            .map(TabEntry::create)
            .toList());
    }
    
    //==================================================================================================================
    public @NotNull Optional<TabEntry> getTab(final int index) { return Optional.ofNullable(this.tabs.get(index)); }
    
    public @NotNull List<TabEntry> getEntries() { return this.tabs; }
    
    //==================================================================================================================
    public int size() { return this.tabs.size(); }
    
    public @NotNull Stream<TabEntry> stream() { return this.tabs.stream(); }
    
    public @NotNull Stream<FactoryGroup> streamGroups(final int index)
    {
        return this.getTab(index).stream().flatMap(e -> e.groups.stream());
    }
}
