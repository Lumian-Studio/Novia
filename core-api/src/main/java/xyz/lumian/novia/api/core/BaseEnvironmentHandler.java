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

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.Nullable;
import xyz.lumian.novia.api.core.mutual.LogicalSide;

import java.nio.file.Path;



//**********************************************************************************************************************
/// The environment handler base class for the client and server.
public abstract class BaseEnvironmentHandler
{
    //******************************************************************************************************************
    protected abstract void initServer(@Nullable MinecraftServer server);
    protected abstract void shutdownServer(@Nullable MinecraftServer server);

    //==================================================================================================================
    /// Gets the Minecraft server instance, or `null` if there currently is no server
    /// (such as in the main menu or in a dedicated multiplayer session).
    /// @return The running server instance
    public abstract @Nullable MinecraftServer getServer();

    /// Gets the folder where Novia will store its instance data (`<Minecraft>/config/Novia`).
    ///
    /// Instance data is data that depends on the physical side of the mod, such as the server cache on the client side
    /// or config cache on the server side.
    ///
    /// @return The Novia data folder
    public abstract Path getDataFolder();

    /// Gets the physical environment type the mod is currently running on.
    /// @return The [EnvType]
    public EnvType getPhysicalSide() { return FabricLoaderImpl.INSTANCE.getEnvironmentType(); }
    
    //==================================================================================================================
    /// Determines whether the current thread is the client render thread (not main thread).
    /// This will always be false on a dedicated server.
    /// @return `true` if the current thread is the render thread
    public abstract boolean isRenderThread();
    
    /// Determines whether the current thread is the server thread.
    /// This will always be false when not in-game.
    /// @return `true` if the current thread is the server thread
    public boolean isServerThread() { return (this.getServer() != null && this.getServer().isSameThread()); }

    //==================================================================================================================
    /// Tries to guess the logical side based on the current thread being operated on.
    /// This might not be accurate enough, thus should be avoided when possible and only used as a last resort.
    ///
    /// The result will be [LogicalSide#SERVER] if this is the server thread, [LogicalSide#CLIENT] if it
    /// is the render thread and `null` if no proper guess could be made.
    ///
    /// On a physical server this will always return [LogicalSide#SERVER] or `null` if the thread is not the server
    /// thread.
    ///
    /// On the client this will always be `null` when not in-game or when not on the render thread or server thread.
    /// @return The estimated logical side
    public abstract @Nullable LogicalSide guessLogicalSide();
}
