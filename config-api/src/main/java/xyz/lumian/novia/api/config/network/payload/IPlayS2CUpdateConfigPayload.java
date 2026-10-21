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

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.config.ConfigApiIds;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.List;
import java.util.Optional;



//**********************************************************************************************************************
public sealed interface IPlayS2CUpdateConfigPayload
    extends CustomPacketPayload
    permits
        IPlayS2CUpdateConfigPayload.Single,
        IPlayS2CUpdateConfigPayload.Bulk
{
    //******************************************************************************************************************
    StreamCodec<ByteBuf, Pair<JsonPointer, Optional<Value>>> UPDATE_CODEC = StreamCodec.composite(
        JsonPointer.STREAM_CODEC,                   Pair::getFirst,
        ByteBufCodecs.optional(Value.STREAM_CODEC), Pair::getSecond,
        Pair::new);
    
    //******************************************************************************************************************
    record Single(Identifier id, Pair<JsonPointer, Optional<Value>> update)
        implements IPlayS2CUpdateConfigPayload
    {
        //**************************************************************************************************************
        public static final Type<Single>                           ID;
        public static final StreamCodec<FriendlyByteBuf, Single> CODEC;
        
        //==============================================================================================================
        static
        {
            ID    = new Type<>(ConfigApiIds.Payload.PLAY_S2C_CONFIG_SINGLE);
            CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,                  Single::id,
                IPlayS2CUpdateConfigPayload.UPDATE_CODEC, Single::update,
                Single::new);
        }
        
        //**************************************************************************************************************
        @Override public List<Pair<JsonPointer, Optional<Value>>> getUpdates() { return List.of(update); }
        @Override public Identifier getProviderId() { return this.id; }
        @Override public Type<Single> type() { return ID; }
    }

    record Bulk(Identifier id, List<Pair<JsonPointer, Optional<Value>>> updates)
        implements IPlayS2CUpdateConfigPayload
    {
        //**************************************************************************************************************
        public static final Type<Bulk>                         ID;
        public static final StreamCodec<FriendlyByteBuf, Bulk> CODEC;

        //==============================================================================================================
        static
        {
            ID    = new Type<>(ConfigApiIds.Payload.PLAY_S2C_CONFIG_BULK);
            CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Bulk::id,
                IPlayS2CUpdateConfigPayload.UPDATE_CODEC.apply(ByteBufCodecs.list()), Bulk::updates,
                Bulk::new);
        }

        //**************************************************************************************************************
        @Override public List<Pair<JsonPointer, Optional<Value>>> getUpdates() { return this.updates; }
        @Override public Identifier getProviderId() { return this.id; }
        @Override public Type<Bulk> type() { return ID; }
    }
    
    //******************************************************************************************************************
    List<Pair<JsonPointer, Optional<Value>>> getUpdates();
    Identifier                               getProviderId();
}
