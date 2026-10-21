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

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;



//**********************************************************************************************************************
public class ConfigApiPayloads
{
    //******************************************************************************************************************
    public static void initialise()
    {
        ConfigApiPayloads.registerS2CConfig(PayloadTypeRegistry.clientboundConfiguration());
        ConfigApiPayloads.registerS2CPlay  (PayloadTypeRegistry.clientboundPlay());
        ConfigApiPayloads.registerC2SConfig(PayloadTypeRegistry.serverboundConfiguration());
        ConfigApiPayloads.registerC2SPlay  (PayloadTypeRegistry.serverboundPlay());
    }

    //==================================================================================================================
    // Client-bound : Config-Phase
    private static void registerS2CConfig(final PayloadTypeRegistry<? extends FriendlyByteBuf> registry)
    {
        registry.register(ConfigS2CConfigSyncPayload.ID, ConfigS2CConfigSyncPayload.CODEC);
    }

    // Client-bound : Play-Phase
    private static void registerS2CPlay(final PayloadTypeRegistry<? extends FriendlyByteBuf> registry)
    {
        registry.register(IPlayS2CUpdateConfigPayload.Single.ID, IPlayS2CUpdateConfigPayload.Single.CODEC);
        registry.register(IPlayS2CUpdateConfigPayload.Bulk.ID,   IPlayS2CUpdateConfigPayload.Bulk.CODEC);
        registry.register(PlayS2CConfigSyncPayload.ID,           PlayS2CConfigSyncPayload.CODEC);
    }
    
    //==================================================================================================================
    // Server-bound : Config-Phase
    private static void registerC2SConfig(final PayloadTypeRegistry<? extends FriendlyByteBuf> registry)
    {
        registry.register(ConfigC2SConfigAcknowledgePayload.ID, ConfigC2SConfigAcknowledgePayload.CODEC);
    }
    
    // Server-bound : Play-Phase
    private static void registerC2SPlay(final PayloadTypeRegistry<? extends FriendlyByteBuf> registry)
    {
        registry.register(PlayC2SConfigSerialisePayload.ID, PlayC2SConfigSerialisePayload.CODEC);
    }
}
