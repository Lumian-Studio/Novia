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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import xyz.lumian.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;
import java.util.*;



//**********************************************************************************************************************
/// The main entrypoint to the Novia library.
/// This class is mainly used to access side-agnostic utility functions, such as directories or threads that differ
/// between the physical client and server.
public final class Novia
    extends BaseEnvironmentHandler
    implements ModInitializer
{
    //******************************************************************************************************************
    @ApiStatus.Internal
    public static final Logger LOGGER = LogManager.getLogger("novia-api");
    
    //------------------------------------------------------------------------------------------------------------------
    @Nullable
    private static Novia INSTANCE = null;

    //******************************************************************************************************************
    /// Gets the instance of the Novia class once it has been initialised.
    /// Make sure NOT to touch this before the mod's core initialisation.
    /// @return An instance of the [Novia] class
    public static Novia getInstance()
    {
        if (Novia.INSTANCE == null)
        {
            throw new InternalModException("Novia's environment handler has not yet been initialised");
        }

        return Novia.INSTANCE;
    }

    //******************************************************************************************************************
    private final Path                   dirMods;
    private final BaseEnvironmentHandler handler;
    
    //******************************************************************************************************************
    @ApiStatus.Internal
    public Novia()
    {
        if (Novia.INSTANCE != null)
        {
            throw new UnsupportedOperationException("Novia's mutual handler had already been initialised");
        }
        
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        {
            this.handler = this.getHandler(ClientModInitializer.class);
        }
        else
        {
            this.handler = this.getHandler(DedicatedServerModInitializer.class);
        }

        Novia.INSTANCE = this;
        this.dirMods   = FabricLoader.getInstance().getGameDir().resolve("mods");
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private <T> BaseEnvironmentHandler getHandler(final Class<T> initializerClass)
    {
        final String                       environment_name = FabricLoader.getInstance().getEnvironmentType().toString()
                                                                          .toLowerCase();
        final List<EntrypointContainer<T>> initializers     = FabricLoader.getInstance().getEntrypointContainers(
            environment_name,
            initializerClass);
        
        return (BaseEnvironmentHandler) initializers.stream()
            .filter(init -> init.getProvider().getMetadata().getId().equals(CoreDefine.ID))
            .findFirst()
            .orElseThrow()
            .getEntrypoint();
    }
    
    //==================================================================================================================
    @Override
    public void onInitialize()
    {
        ServerLifecycleEvents.SERVER_STARTING.register(this::initServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::shutdownServer);
        CoreApiBootstrap.init();
    }
    
    //==================================================================================================================
    /// Gets the path to the mods folder of the current instance.
    /// @return The mods directory
    public Path getModsFolder() { return this.dirMods; }
    
    @Override public @Nullable MinecraftServer getServer()     { return this.handler.getServer(); }
    @Override public           Path            getDataFolder() { return this.handler.getDataFolder(); }
    
    //==================================================================================================================
    @Override public void initServer(final @Nullable MinecraftServer server) { this.handler.initServer(server); }

    @Override
    public void shutdownServer(final @Nullable MinecraftServer server)
    {
        this.handler.shutdownServer(server);
    }

    //==================================================================================================================
    @Override
    public boolean isRenderThread() { return this.handler.isRenderThread(); }

    @Override
    public boolean isServerThread() { return this.handler.isServerThread(); }

    //==================================================================================================================
    @Override
    public @Nullable LogicalSide guessLogicalSide() { return this.handler.guessLogicalSide(); }
}
