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
package xyz.lumian.novia.api.config.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.ConfigApiIds;
import xyz.lumian.novia.api.core.network.codec.NoviaStreamCodecs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;



//**********************************************************************************************************************
public record PlayC2SConfigSerialisePayload(
    Action         action,
    Identifier     providerId,
    Optional<Path> file
) implements CustomPacketPayload
{
    //******************************************************************************************************************
    public enum Action { SAVE, LOAD }
    
    //******************************************************************************************************************
    public static final Type<PlayC2SConfigSerialisePayload>                         ID;
    public static final StreamCodec<FriendlyByteBuf, PlayC2SConfigSerialisePayload> CODEC;

    //==================================================================================================================
    static
    {
        ID = new Type<>(ConfigApiIds.Payload.CONFIG_C2S_SERIALISE);
        
        final StreamCodec<ByteBuf, Path> path_codec = ByteBufCodecs.STRING_UTF8.map(Paths::get, Path::toString);
        CODEC = StreamCodec.composite(
            NoviaStreamCodecs.enumeration(Action.class), PlayC2SConfigSerialisePayload::action,
            Identifier.STREAM_CODEC,                     PlayC2SConfigSerialisePayload::providerId,
            ByteBufCodecs     .optional   (path_codec),  PlayC2SConfigSerialisePayload::file,
            PlayC2SConfigSerialisePayload::new);
    }
    
    //******************************************************************************************************************
    public static PlayC2SConfigSerialisePayload saving(final Identifier providerId, final @Nullable Path file)
    {
        return new PlayC2SConfigSerialisePayload(Action.SAVE, providerId, Optional.ofNullable(file));
    }
    
    public static PlayC2SConfigSerialisePayload saving(final Identifier providerId)
    {
        return PlayC2SConfigSerialisePayload.saving(providerId, null);
    }
    
    public static PlayC2SConfigSerialisePayload loading(final Identifier providerId, final @Nullable Path file)
    {
        return new PlayC2SConfigSerialisePayload(Action.LOAD, providerId, Optional.ofNullable(file));
    }
    
    public static PlayC2SConfigSerialisePayload loading(final Identifier providerId)
    {
        return PlayC2SConfigSerialisePayload.loading(providerId, null);
    }
    
    //******************************************************************************************************************
    @Override public Type<PlayC2SConfigSerialisePayload> type() { return PlayC2SConfigSerialisePayload.ID; }
}
