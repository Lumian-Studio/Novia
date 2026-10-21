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
package xyz.lumian.novia.api.config.server.network;

import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import xyz.lumian.novia.api.config.ConfigDefine;
import xyz.lumian.novia.api.config.network.payload.ConfigS2CConfigSyncPayload;
import xyz.lumian.novia.api.config.network.payload.PlayS2CConfigSyncPayload;
import xyz.lumian.novia.api.config.provider.BaseNetworkProvider;
import xyz.lumian.novia.api.config.registry.SnapshotCache;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;



//**********************************************************************************************************************
public record SynchroniseServerConfigurationsTask(
    SnapshotCache                           cache,
    Map<Identifier, BaseNetworkProvider<?>> providers
)
    implements ConfigurationTask
{
    //******************************************************************************************************************
    public static final Type TYPE = new Type(ConfigDefine.ID + ":synchronise_configs");

    //------------------------------------------------------------------------------------------------------------------
    private static final int MAX_PAYLOAD_SIZE = 1048576;

    //******************************************************************************************************************
    @Override public Type type() { return SynchroniseServerConfigurationsTask.TYPE; }

    //==================================================================================================================
    @Override
    public void start(final Consumer<Packet<?>> sender)
    {
        final FriendlyByteBuf buf = FriendlyByteBufs.create();
        buf.writeVarInt(this.cache.namespaces().size());
        
        for (final var namespace_entry : this.cache.namespaces().entrySet())
        {
            buf.writeVarInt(namespace_entry.getValue().size());
            buf.writeUtf(namespace_entry.getKey());
            
            for (final var config_entry : namespace_entry.getValue())
            {
                buf.writeVarInt(config_entry.snapshots().size());
                buf.writeUtf   (config_entry.path());
                
                final BaseNetworkProvider<?> provider = this.providers
                    .get(Identifier.fromNamespaceAndPath(namespace_entry.getKey(), config_entry.path()));
                buf.writeBoolean(provider.isOptional());
                
                for (final var snapshot : config_entry.snapshots())
                {
                    SnapshotCache.Snapshot.STREAM_CODEC.encode(buf, snapshot);
                }
            }
        }
        
        this.sendPackets(buf, sender, ServerConfigurationNetworking::createClientboundPacket,
                         ConfigS2CConfigSyncPayload::new);
    }
    
    public void sendPlayPacket(final Consumer<Packet<?>> sender)
    {
        final FriendlyByteBuf buf = FriendlyByteBufs.create();
        SnapshotCache.STREAM_CODEC.encode(buf, this.cache);
        this.sendPackets(buf, sender, ServerPlayNetworking::createClientboundPacket, PlayS2CConfigSyncPayload::new);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void sendPackets(final FriendlyByteBuf buf, final Consumer<Packet<?>> sender,
                             final Function<CustomPacketPayload, Packet<?>> packetGenerator,
                             final Function<FriendlyByteBuf, CustomPacketPayload> payloadGenerator)
    {
        final Consumer<FriendlyByteBuf> make_send = (b ->
            sender.accept(packetGenerator.apply(payloadGenerator.apply(b))));
        
        final int total_bytes = buf.readableBytes();
		int       byte_start  = 0;

		while (byte_start < total_bytes)
        {
			final int           payload_size = Math.min(
                (total_bytes - byte_start),
                SynchroniseServerConfigurationsTask.MAX_PAYLOAD_SIZE);
			final FriendlyByteBuf payload_buf  = FriendlyByteBufs.slice(buf, byte_start, payload_size);
            
            make_send.accept(payload_buf);
            byte_start += payload_size;
		}
        
        // Mark the end
        make_send.accept(FriendlyByteBufs.empty());
    }
}
