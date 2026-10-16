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
package xyz.lumialights.novia.api.config.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.ConfigApiIds;
import xyz.lumialights.novia.api.core.network.codec.NoviaPacketCodecs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;



//**********************************************************************************************************************
public record PlayC2SConfigSerialisePayload(
    @NotNull Action         action,
    @NotNull Identifier     providerId,
    @NotNull Optional<Path> file
)
    implements CustomPayload
{
    //******************************************************************************************************************
    public enum Action { SAVE, LOAD }
    
    //******************************************************************************************************************
    public static final Id<PlayC2SConfigSerialisePayload>                         ID;
    public static final PacketCodec<PacketByteBuf, PlayC2SConfigSerialisePayload> CODEC;

    //==================================================================================================================
    static
    {
        ID = new Id<>(ConfigApiIds.Payload.CONFIG_C2S_SERIALISE);
        
        final PacketCodec<ByteBuf, Path> path_codec = PacketCodecs.STRING.xmap(Paths::get, Path::toString);
        CODEC = PacketCodec.tuple(
            NoviaPacketCodecs.enumeration(Action.class), PlayC2SConfigSerialisePayload::action,
            Identifier.PACKET_CODEC,                     PlayC2SConfigSerialisePayload::providerId,
            PacketCodecs     .optional   (path_codec),   PlayC2SConfigSerialisePayload::file,
            PlayC2SConfigSerialisePayload::new);
    }
    
    //******************************************************************************************************************
    public static @NotNull PlayC2SConfigSerialisePayload saving(final @NotNull  Identifier providerId,
                                                                final @Nullable Path       file)
    {
        return new PlayC2SConfigSerialisePayload(Action.SAVE, providerId, Optional.ofNullable(file));
    }
    
    public static @NotNull PlayC2SConfigSerialisePayload saving(final @NotNull Identifier providerId)
    {
        return PlayC2SConfigSerialisePayload.saving(providerId, null);
    }
    
    public static @NotNull PlayC2SConfigSerialisePayload loading(final @NotNull  Identifier providerId,
                                                                 final @Nullable Path       file)
    {
        return new PlayC2SConfigSerialisePayload(Action.LOAD, providerId, Optional.ofNullable(file));
    }
    
    public static @NotNull PlayC2SConfigSerialisePayload loading(final @NotNull Identifier providerId)
    {
        return PlayC2SConfigSerialisePayload.loading(providerId, null);
    }
    
    //******************************************************************************************************************
    @Override public @NotNull Id<PlayC2SConfigSerialisePayload> getId() { return PlayC2SConfigSerialisePayload.ID; }
}
