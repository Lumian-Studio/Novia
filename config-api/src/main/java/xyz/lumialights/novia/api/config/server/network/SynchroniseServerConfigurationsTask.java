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
package xyz.lumialights.novia.api.config.server.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xyz.lumialights.novia.api.config.ApiDefine;
import xyz.lumialights.novia.api.config.network.payload.ConfigS2CConfigSyncPayload;
import xyz.lumialights.novia.api.config.network.payload.PlayS2CConfigSyncPayload;
import xyz.lumialights.novia.api.config.provider.BaseNetworkProvider;
import xyz.lumialights.novia.api.config.registry.SnapshotCache;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;



//**********************************************************************************************************************
public record SynchroniseServerConfigurationsTask(
    @NotNull SnapshotCache                           cache,
    @NotNull Map<Identifier, BaseNetworkProvider<?>> providers
)
    implements ServerPlayerConfigurationTask
{
    //******************************************************************************************************************
    public static final Key KEY = new Key(ApiDefine.API_ID + ":synchronise_configs");

    //------------------------------------------------------------------------------------------------------------------
    private static final int MAX_PAYLOAD_SIZE = 1048576;

    //******************************************************************************************************************
    @Override public Key getKey() { return SynchroniseServerConfigurationsTask.KEY; }

    //==================================================================================================================
    @Override
    public void sendPacket(final Consumer<Packet<?>> sender)
    {
        final PacketByteBuf buf = PacketByteBufs.create();
        
        buf.writeVarInt(this.cache.namespaces().size());
        
        for (final var namespace_entry : this.cache.namespaces().entrySet())
        {
            buf.writeVarInt(namespace_entry.getValue().size());
            buf.writeString(namespace_entry.getKey());
            
            for (final var config_entry : namespace_entry.getValue())
            {
                buf.writeVarInt (config_entry.snapshots().size());
                buf.writeString (config_entry.path());
                
                final BaseNetworkProvider<?> provider = this.providers
                    .get(Identifier.of(namespace_entry.getKey(), config_entry.path()));
                buf.writeBoolean(provider.isOptional());
                
                for (final var snapshot : config_entry.snapshots())
                {
                    SnapshotCache.Snapshot.PACKET_CODEC.encode(buf, snapshot);
                }
            }
        }
        
        sendPackets(buf, sender, ServerConfigurationNetworking::createS2CPacket, ConfigS2CConfigSyncPayload::new);
    }
    
    public void sendPlayPacket(final Consumer<Packet<?>> sender)
    {
        final PacketByteBuf buf = PacketByteBufs.create();
        SnapshotCache.PACKET_CODEC.encode(buf, this.cache);
        sendPackets(buf, sender, ServerPlayNetworking::createS2CPacket, PlayS2CConfigSyncPayload::new);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private void sendPackets(@NotNull final PacketByteBuf                          buf,
                             @NotNull final Consumer<Packet<?>>                    sender,
                             @NotNull final Function<CustomPayload, Packet<?>>     packetGenerator,
                             @NotNull final Function<PacketByteBuf, CustomPayload> payloadGenerator)
    {
        final Consumer<PacketByteBuf> make_send = (b ->
            sender.accept(packetGenerator.apply(payloadGenerator.apply(b))));
        
        final int total_bytes = buf.readableBytes();
		int       byte_start  = 0;

		while (byte_start < total_bytes)
        {
			final int           payload_size = Math.min(
                (total_bytes - byte_start),
                SynchroniseServerConfigurationsTask.MAX_PAYLOAD_SIZE);
			final PacketByteBuf payload_buf  = PacketByteBufs.slice(buf, byte_start, payload_size);
            
            make_send.accept(payload_buf);
            byte_start += payload_size;
		}
        
        // Mark the end
        make_send.accept(PacketByteBufs.empty());
    }
}
