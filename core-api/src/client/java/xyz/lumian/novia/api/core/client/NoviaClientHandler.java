package xyz.lumian.novia.api.core.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.BaseEnvironmentHandler;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;



//**********************************************************************************************************************
/// The client-only side handler version of the [Novia] class.
/// This class should rarely be touched, as the main side-agnostic handler is the [Novia] class
/// (unless for client only functions that are not available in a side agnostic context).
public final class NoviaClientHandler
    extends BaseEnvironmentHandler
    implements ClientModInitializer
{
    //******************************************************************************************************************
    private static @Nullable NoviaClientHandler INSTANCE = null;
    
    //******************************************************************************************************************
    @Nullable
    public static NoviaClientHandler getInstance() { return INSTANCE; }
    
    //*******************************s***********************************************************************************
    private final Path serverDataFolder;
    
    private @Nullable MinecraftServer server = null;

    //******************************************************************************************************************
    public NoviaClientHandler()
    {
        if (NoviaClientHandler.INSTANCE != null)
        {
            throw new IllegalStateException("Handler had already been initialised");
        }

        NoviaClientHandler.INSTANCE = this;
        this.serverDataFolder       = FabricLoader.getInstance().getConfigDir().resolve("novia");
    }

    //==================================================================================================================
    @Override public void onInitializeClient() { CoreApiClientBootstrap.init(); }

    //==================================================================================================================
    @Override public void initServer    (final @Nullable MinecraftServer server) { this.server = server; }
    @Override public void shutdownServer(final @Nullable MinecraftServer server) { this.server = null; }

    //==================================================================================================================
    @Override public @Nullable MinecraftServer getServer()       { return this.server; }
    @Override public Path                      getDataFolder()   { return this.serverDataFolder; }
    @Override public EnvType                   getPhysicalSide() { return EnvType.CLIENT; }

    //==================================================================================================================
    @Override public boolean isRenderThread() { return RenderSystem.isOnRenderThread(); }

    //==================================================================================================================
    @Override
    public @Nullable LogicalSide guessLogicalSide()
    {
        return (this.isServerThread() ? LogicalSide.SERVER : (this.isRenderThread() ? LogicalSide.CLIENT : null));
    }
}
