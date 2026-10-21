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
package xyz.lumian.novia.api.config.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.ConfigManager;
import xyz.lumian.novia.api.config.registry.ProviderOverride;
import xyz.lumian.novia.api.config.registry.ConfigRegistry;
import xyz.lumian.novia.api.config.IConfigHandler;
import xyz.lumian.novia.api.config.network.payload.ConfigC2SConfigAcknowledgePayload;
import xyz.lumian.novia.api.config.network.payload.ConfigS2CConfigSyncPayload;
import xyz.lumian.novia.api.config.network.payload.IPlayS2CUpdateConfigPayload;
import xyz.lumian.novia.api.config.provider.BaseNetworkProvider;
import xyz.lumian.novia.api.config.registry.SnapshotCache;
import xyz.lumian.novia.api.config.server.network.SynchroniseServerConfigurationsTask;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.*;



//**********************************************************************************************************************
@ApiStatus.Internal
public class ServerConfigManagerHandler
    implements IConfigHandler
{
    //******************************************************************************************************************
    protected final SnapshotCache rollingCache = new SnapshotCache(new HashMap<>());
    
    protected Map<Identifier, BaseNetworkProvider<?>> providers = new LinkedHashMap<>();
    
    @Nullable
    protected SnapshotCache originCache = null;

    //******************************************************************************************************************
    public ServerConfigManagerHandler()
    {
        ServerConfigurationConnectionEvents.CONFIGURE       .register(this::processConfigTask);
        ServerPlayConnectionEvents         .JOIN            .register(this::processRollingUpdates);
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register((handler, server) ->
            ServerConfigurationNetworking.registerReceiver(
                handler,
                ConfigC2SConfigAcknowledgePayload.ID,
                ((ignored, ctx) -> ctx.packetListener().completeTask(SynchroniseServerConfigurationsTask.TYPE))));
    }
    
    //==================================================================================================================
    @Override
    public void onRegistryFrozen(final ConfigRegistry registry)
    {
        if (this.originCache != null)
        {
            throw new IllegalStateException("Providers had already been initialised");
        }
        
        this.providers = registry.entries().stream()
            .filter(e -> e.getValue().isSynced())
            .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (e -> (BaseNetworkProvider<?>) e.getValue())));
        
        if (this.providers.isEmpty())
        {
            this.originCache = new SnapshotCache(ImmutableMap.of());
            return;
        }
        
        this.originCache = SnapshotCache.from(this.providers);
    }

    @Override
    public void sendUpdates(final Identifier configId, final List<Pair<JsonPointer, Optional<Value>>> updates)
    {
        if (this.originCache == null)
        {
            throw new IllegalStateException("cache has not yet been initialised");
        }
        
        if (updates.isEmpty())
        {
            return;
        }
        
        this.updateRollingCache(configId, updates);
        
        final MinecraftServer server = Objects.requireNonNull(Novia.getInstance().getServer());
        this.sendPacket(configId, PlayerLookup.all(server), updates);
    }
    
    @Override
    public Optional<ProviderOverride> getRegistryOverride(final Identifier providerId)
    {
        final MinecraftServer server = Novia.getInstance().getServer();
        
        if (server != null)
        {
            final Level level = server.overworld();
            return ConfigManager.REGISTRY.getOverride(level, providerId);
        }
        
        return Optional.empty();
    }
    
    //------------------------------------------------------------------------------------------------------------------
    protected void updateRollingCache(final Identifier configId, final List<Pair<JsonPointer, Optional<Value>>> updates)
    {
        final SnapshotCache.SnapshotGroup orig_config_cache
            = Objects.requireNonNull(this.originCache).getGroup(configId);
        Objects.requireNonNull(orig_config_cache, "Origin cache was null for id '" + configId + "'");
        
        final SnapshotCache.SnapshotGroup prev_config_cache = this.rollingCache.getGroup(configId);
        final SnapshotCache.SnapshotGroup roll_config_cache = new SnapshotCache.SnapshotGroup(
            configId.getPath(),
            new ArrayList<>());
        
        if (prev_config_cache != null)
        {
            roll_config_cache.snapshots().addAll(prev_config_cache.snapshots());
        }
        
        for (final var update : updates)
        {
            final Value origin_value = Objects.requireNonNull(orig_config_cache.findSnapshot(update.getFirst()))
                .value();
            
            final Optional<Value> new_value_opt = update.getSecond();
            final JsonPointer     pointer       = update.getFirst();
            
            if (new_value_opt.isEmpty() || new_value_opt.orElseThrow().equals(origin_value))
            {
                for (int i = 0; i < roll_config_cache.snapshots().size(); ++i)
                {
                    final SnapshotCache.Snapshot snapshot = roll_config_cache.snapshots().get(i);
                    
                    if (snapshot.pointer().equals(pointer))
                    {
                        roll_config_cache.snapshots().remove(i);
                        break;
                    }
                }
            }
            else
            {
                final Value new_value = new_value_opt.orElseThrow();
                boolean     found     = false;
                
                for (int i = 0; i < roll_config_cache.snapshots().size(); ++i)
                {
                    final SnapshotCache.Snapshot snapshot = roll_config_cache.snapshots().get(i);
                    
                    if (snapshot.pointer().equals(pointer))
                    {
                        roll_config_cache.snapshots().set(i, snapshot.withValue(new_value));
                        found = true;
                        break;
                    }
                }
                
                if (!found)
                {
                    roll_config_cache.snapshots().add(new SnapshotCache.Snapshot(pointer, new_value));
                }
            }
        }
        
        if (roll_config_cache.snapshots().isEmpty() && prev_config_cache != null)
        {
            final List<SnapshotCache.SnapshotGroup> groups = this.rollingCache
                .namespaces()
                .get(configId.getNamespace());
            
            if (groups.size() < 2)
            {
                synchronized (this.rollingCache)
                {
                    this.rollingCache.namespaces().remove(configId.getNamespace());
                }
            }
            else
            {
                for (int i = 0; i < groups.size(); ++i)
                {
                    if (groups.get(i).path().equals(configId.getPath()))
                    {
                        synchronized (this.rollingCache)
                        {
                            groups.remove(i);
                        }
                        
                        break;
                    }
                }
            }
        }
        else if (!roll_config_cache.snapshots().isEmpty())
        {
            if (prev_config_cache != null)
            {
                final List<SnapshotCache.SnapshotGroup> groups = this.rollingCache
                    .namespaces()
                    .get(configId.getNamespace());
                
                for (int i = 0; i < groups.size(); ++i)
                {
                    if (groups.get(i).path().equals(configId.getPath()))
                    {
                        synchronized (this.rollingCache)
                        {
                            groups.set(i, roll_config_cache);
                        }
                        
                        break;
                    }
                }
            }
            else
            {
                synchronized (this.rollingCache)
                {
                    this.rollingCache
                        .namespaces()
                        .computeIfAbsent(configId.getNamespace(), (s -> new ArrayList<>()))
                        .add(roll_config_cache);
                }
            }
        }
    }
    
    protected void sendPacket(final Identifier configId, final Collection<ServerPlayer> players,
                              final List<Pair<JsonPointer, Optional<Value>>> updates)
    {
        CustomPacketPayload payload;
        
        if (updates.size() == 1)
        {
            payload = new IPlayS2CUpdateConfigPayload.Single(configId, updates.getFirst());
        }
        else
        {
            payload = new IPlayS2CUpdateConfigPayload.Bulk(configId, updates);
        }
        
        players.forEach(player ->
        {
            if (ServerPlayNetworking.canSend(player, payload.type()))
            {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }
    
    //==================================================================================================================
    protected void processConfigTask(final ServerConfigurationPacketListenerImpl handler, final MinecraftServer server)
    {
        if (ServerConfigurationNetworking.canSend(handler, ConfigS2CConfigSyncPayload.ID))
        {
            handler.addTask(new SynchroniseServerConfigurationsTask(
                Objects.requireNonNull(this.originCache),
                this.providers));
        }
        else
        {
            handler.disconnect(Component.literal("Configuration providers out of sync"));
        }
    }
    
    protected void processRollingUpdates(final ServerGamePacketListenerImpl handler, final PacketSender sender,
                                         final MinecraftServer ignored)
    {
        synchronized (this.rollingCache)
        {
            final SynchroniseServerConfigurationsTask task = new SynchroniseServerConfigurationsTask(this.rollingCache,
                                                                                                     this.providers);
            task.sendPlayPacket(sender::sendPacket);
        }
    }
}
