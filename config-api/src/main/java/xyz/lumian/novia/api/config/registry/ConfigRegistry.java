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
package xyz.lumian.novia.api.config.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.config.PropertyValidationException;
import xyz.lumian.novia.api.config.event.ConfigRegistryEvent;
import xyz.lumian.novia.api.config.provider.IConfigProvider;
import xyz.lumian.novia.api.config.spec.ConfigSpec;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.*;



//**********************************************************************************************************************
/// The ConfigRegistry is the central registry for the Novia configuration API.
public final class ConfigRegistry
{
    //******************************************************************************************************************
    /// Looks up a [ConfigSpec] by its ID and returns it.
    /// @param specId The ID of the config specification
    /// @return The config specification or null if not found
    public static Optional<ConfigSpec<?>> getSpec(final Identifier specId) { return ConfigSpec.findSpec(specId); }
    
    //******************************************************************************************************************
    @Nullable
    public final ResourceKey<Registry<ProviderOverride>> overrideRegistryKey;
    
    //------------------------------------------------------------------------------------------------------------------
    private final Map<Identifier, IConfigProvider<?>>   id2provider;
    private final Map<String, List<IConfigProvider<?>>> namespace2providers = new HashMap<>();
    
    private volatile boolean frozen = false;
    
    //******************************************************************************************************************
    public ConfigRegistry(final @Nullable Identifier overrideRegistryId)
    {
        if (overrideRegistryId != null)
        {
            this.overrideRegistryKey = ResourceKey.createRegistryKey(overrideRegistryId);
            DynamicRegistries.registerSynced(this.overrideRegistryKey, ProviderOverride.CODEC);
            DynamicRegistrySetupCallback.EVENT.register(view -> view.registerEntryAdded(
                this.overrideRegistryKey,
                this::validateOverride));
        }
        else
        {
            this.overrideRegistryKey = null;
        }
        
        this.id2provider = new TreeMap<>(Comparator.comparing(Identifier::toString));
    }
    
    //==================================================================================================================
    public Codec<IConfigProvider<?>> getCodec()
    {
        return Identifier.CODEC
            .comapFlatMap((id ->
            {
                final IConfigProvider<?> provider = this.id2provider.get(id);
                
                if (provider == null)
                {
                    return DataResult.error(() -> "No provider for id '" + id + "' found");
                }
                
                return DataResult.success(provider);
            }),
            IConfigProvider::getId);
    }
    
    //==================================================================================================================
    private void validateOverride(final int rawId, final Identifier providerId, final ProviderOverride override)
    {
        final IConfigProvider<?> provider = this.id2provider.get(providerId);
        
        if (provider == null)
        {
            return;
        }
        
        var it = override.overrides().entrySet().iterator();
        
        while (it.hasNext())
        {
            final Map.Entry<JsonPointer, Value> entry = it.next();
            final PropertySpec<?>               spec  = provider.getSpec().flatPropertySpecs().get(entry.getKey());
            
            if (spec == null)
            {
                continue;
            }
            
            try
            {
                spec.validate(entry.getValue());
            }
            catch (final PropertyValidationException ex)
            {
                Novia.LOGGER.error("invalid override '{}' for provider '{}'", entry.getKey(), providerId, ex);
                it.remove();
            }
        }
    }
    
    //==================================================================================================================
    /// Registers the given config provider.
    /// @param id          The ID of the provider
    /// @param builder     The provider builder
    /// @param <Container> The config container type
    /// @param <Provider>  The config provider type
    /// @return The registered {@link IConfigProvider}
    /// @throws ConfigRegistryException If a config with that same ID had already been registered before,
    ///                                 or when the registry had already been frozen
    public <Container, Provider extends IConfigProvider<Container>> Provider register(
        final Identifier                                      id,
        final IConfigProvider.Builder<?, Container, Provider> builder
    )
    {
        Objects.requireNonNull(id,      "id must not be null");
        Objects.requireNonNull(builder, "builder must not be null");
        
        if (this.frozen)
        {
            throw new ConfigRegistryException("The configuration registry had already been frozen");
        }
        
        if (this.id2provider.containsKey(id))
        {
            throw new ConfigRegistryException("Config provider with ID '" + id + "' was already registered");
        }
        
        final Provider provider = builder.build(id);
        this.id2provider.put(id, provider);
        
        this.namespace2providers
            .computeIfAbsent(id.getNamespace(), (k -> new ArrayList<>()))
            .add(provider);
        
        ConfigRegistryEvent.REGISTER.invoker().handle(this, id, provider);
        
        return provider;
    }
    
    //==================================================================================================================
    /// Gets a configuration provider by its ID.
    /// @param id The configuration ID
    /// @return The provider or null if none was registered for that ID
    public @Nullable IConfigProvider<?> get(final Identifier id)
    {
        Objects.requireNonNull(id, "id must not be null");
        return this.id2provider.get(id);
    }
    
    /// Tries to fetch a provider override for the given level if one was available, otherwise `null`
    /// @param level      The level providing the overrides
    /// @param providerId The ID of the provider
    /// @return The provider override or null
    public Optional<ProviderOverride> getOverride(final Level level, final Identifier providerId)
    {
        if (this.overrideRegistryKey == null)
        {
            return Optional.empty();
        }
        
        final HolderLookup.Provider                        manager   = level.registryAccess();
        final Holder.Reference<Registry<ProviderOverride>> overrides = manager.getOrThrow(this.overrideRegistryKey);
        
        return overrides.value().get(providerId).map(Holder.Reference::value);
    }
    
    //==================================================================================================================
    public @Nullable PropertySpec<?> findProperty(final PropertyId id)
    {
        final IConfigProvider<?> provider = this.id2provider.get(id.providerId());
        
        if (provider == null)
        {
            return null;
        }
        
        return provider.getSpec().flatPropertySpecs().get(id.pointer());
    }
    
    //==================================================================================================================
    /// Determines whether this registry contains any providers.
    /// @return True if there is at least one provider
    public boolean isEmpty() { return this.id2provider.isEmpty(); }
    
    /// Gets whether this registry had already been frozen and does not allow new entries to be made.
    /// @return `true` if the registry had already been frozen
    public boolean isFrozen() { return this.frozen; }
    
    /// Returns the number of providers in this registry.
    /// @return The number of providers
    public int size() { return this.id2provider.size(); }
    
    //==================================================================================================================
    /**
     * Determines whether this registry contains a provider with that ID.
     * @param id The ID to search
     * @return True if there is a provider by that ID
     */
    public boolean contains(final Identifier id)
    {
        Objects.requireNonNull(id, "id must not be null");
        return this.id2provider.containsKey(id);
    }
    
    //==================================================================================================================
    public Set<Identifier> identifiers() { return new HashSet<>(this.id2provider.keySet()); }
    
    /// Gets a map with every element being a list of providers that share the same namespace mapped to that namespace.
    /// @return The namespaced map
    public Map<String, List<IConfigProvider<?>>> namespaced() { return this.namespace2providers; }
    
    public Set<IConfigProvider<?>> providers() { return new HashSet<>(this.id2provider.values()); }
    
    public Set<Map.Entry<Identifier, IConfigProvider<?>>> entries() { return this.id2provider.entrySet(); }
    
    //==================================================================================================================
    public Set<Map.Entry<Identifier, IConfigProvider<?>>> freeze()
    {
        if (this.frozen)
        {
            throw new ConfigRegistryException("The configuration registry had already been frozen");
        }
        
        this.frozen = true;
        ConfigRegistryEvent.FREEZING.invoker().handle(this);
        
        return this.entries();
    }
}
