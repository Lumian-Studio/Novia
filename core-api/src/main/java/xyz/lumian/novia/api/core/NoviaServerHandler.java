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
package xyz.lumian.novia.api.core;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import org.jspecify.annotations.Nullable;
import xyz.lumian.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;



//**********************************************************************************************************************
/// The server-only side handler version of the [Novia] class.
/// This class should rarely be touched, as the main side-agnostic handler is the [Novia] class
/// (unless for server only functions that are not available in a side agnostic context).
public final class NoviaServerHandler
    extends BaseEnvironmentHandler
    implements DedicatedServerModInitializer
{
    //******************************************************************************************************************
    @Nullable
    private static NoviaServerHandler INSTANCE = null;

    //******************************************************************************************************************
    public static NoviaServerHandler getInstance()
    {
        if (NoviaServerHandler.INSTANCE == null)
        {
            throw new InternalModException("Novia's server handler has not yet been initialised");
        }
        
        return INSTANCE;
    }

    //******************************************************************************************************************
    private final Path serverDataFolder;

    @Nullable
    private MinecraftServer server = null;

    //******************************************************************************************************************
    public NoviaServerHandler()
    {
        if (NoviaServerHandler.INSTANCE != null)
        {
            throw new UnsupportedOperationException("server handler already initialised");
        }

        NoviaServerHandler.INSTANCE = this;
        this.serverDataFolder       = FabricLoader.getInstance().getConfigDir().resolve("novia");
    }

    //==================================================================================================================
    @Override public @Nullable MinecraftServer getServer()       { return this.server; }
    @Override public           Path            getDataFolder()   { return this.serverDataFolder; }
    @Override public           EnvType         getPhysicalSide() { return EnvType.SERVER; }
    
    //==================================================================================================================
    @Override public void initServer    (final @Nullable MinecraftServer server) { this.server = server; }
    @Override public void shutdownServer(final @Nullable MinecraftServer server) { this.server = null; }

    //==================================================================================================================
    @Override public void onInitializeServer() {}

    //==================================================================================================================
    @Override public boolean isRenderThread() { return false; }

    //==================================================================================================================
    @Override
    public @Nullable LogicalSide guessLogicalSide()
    {
        return (this.isServerThread() ? LogicalSide.SERVER : null);
    }
}
