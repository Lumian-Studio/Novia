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
package xyz.lumian.novia.api.core.block;

import net.minecraft.core.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

import java.util.Objects;



//**********************************************************************************************************************
/// The BlockContext class is a utility class around a block in the world, providing the position, the world instance
/// it is in and a cached [BlockState].
///
/// Since the [BlockState] instance is locally cached only, it might become stale if the given block changes state
/// (such as redstone updates or its replacement). Due to this fact, it might be advisable to use [#update()]
/// if necessary. If this class is used to update a block's state, however, the cached state will be updated as well.
public class BlockContext
{
    //******************************************************************************************************************
    /// Creates a new [BlockContext] object for the given world and block position and automatically determines the
    /// block state.
    /// @param level The [Level] instance the block is placed in
    /// @param pos   The [BlockPos] of the block in the world
    public static BlockContext forLevel(final Level level, final BlockPos pos)
    {
        return new BlockContext(level, level.getBlockState(pos), pos);
    }
    
    /// Convenience version of [#forLevel(Level, BlockPos)], by getting the world object implicitly from the player.
    /// @param player The [Player] to get the world object from
    /// @param pos    The [BlockPos] of the block in the world
    public static BlockContext forPlayer(final Player player, final BlockPos pos)
    {
        return BlockContext.forLevel(player.level(), pos);
    }
    
    //******************************************************************************************************************
    private final Level    level;
    private final BlockPos pos;
    
    private BlockState state;
    
    //******************************************************************************************************************
    /// Creates a new [BlockContext].
    /// @param level The [Level] instance the block is placed in
    /// @param state The [BlockState] of the block
    /// @param pos   The [BlockPos] of the block in the world
    /// @see #forLevel(Level, BlockPos)
    /// @see #forPlayer(Player, BlockPos)
    public BlockContext(final Level level, final BlockState state, final BlockPos pos)
    {
        this.level = Objects.requireNonNull(level, "world must not be null");
        this.pos   = pos.immutable();
        this.state = Objects.requireNonNull(state, "state must not be null");
    }
    
    //==================================================================================================================
    /// Gets the block state currently cached for this block.
    /// Do note that the returned block state might have become stale, which may require an explicit update.
    /// @return The [BlockState]
    public BlockState state() { return this.state; }
    
    /// Gets the block position for this block.
    /// @return The [BlockPos]
    public BlockPos pos() { return this.pos; }
    
    /// Gets the world this block is placed in.
    /// @return The [Level]
    public Level level() { return this.level; }
    
    /// Gets the associated block entity for this block.
    /// @return The [BlockEntity], or `null` if this block has no block entity
    @Nullable
    public BlockEntity getBlockEntity() { return this.level.getBlockEntity(this.pos); }
    
    /// {@return the destroy speed of this block}
    public float getDestroySpeed() { return this.state.getDestroySpeed(this.level, this.pos); }
    
    /// Gets the chunk this block is contained in.
    /// @return The [ChunkAccess], or `null` if the chunk doesn't exist
    @Nullable
    public ChunkAccess getChunk()
    {
        final int x_section = SectionPos.blockToSectionCoord(this.pos.getX());
        final int z_section = SectionPos.blockToSectionCoord(this.pos.getZ());
        return this.level.getChunk(x_section, z_section, ChunkStatus.FULL, false);
    }
    
    //==================================================================================================================
    /// {@return whether the chunk this block sits in actually exists}
    public boolean hasChunk() { return (this.getChunk() != null); }
    
    /// {@return whether the block state associated with this block context has already changed}
    public boolean isStale() { return !this.is(this.level.getBlockState(this.pos)); }
    
    //==================================================================================================================
    /// Gets a new block context with the given distance from this one, containing the block at that distance.
    /// @param direction The [Direction] in which to offset from this one
    /// @param distance  The distance in blocks from this one
    /// @return The new offset [BlockContext]
    public BlockContext neighbour(final Direction direction, final int distance)
    {
        if (distance == 0)
        {
            return this;
        }
        
        return BlockContext.forLevel(this.level, this.pos.relative(direction, distance));
    }
    
    /// Gets a directly neighbouring block of this one.
    /// @param direction The [Direction] of the neíghbouring block
    /// @return The [BlockContext] for the neighbouring block
    public BlockContext neighbour(final Direction direction) { return this.neighbour(direction, 1); }
    
    /// Gets a new block context from a given offset location of this one.
    /// @param offset The directional [Vec3i] offset from this block
    /// @return The new offset [BlockContext]
    public BlockContext relative(final Vec3i offset)
    {
        if (offset.equals(Vec3i.ZERO))
        {
            return this;
        }
        
        return BlockContext.forLevel(this.level, this.pos.offset(offset));
    }
    
    /// Gets a new block context at the given exact location in the same world
    /// @param newPos The new [BlockPos]
    /// @return The new [BlockContext]
    public BlockContext withPos(final BlockPos newPos)
    {
        if (newPos.equals(this.pos))
        {
            return this;
        }
        
        return BlockContext.forLevel(this.level, newPos);
    }
    
    //==================================================================================================================
    /// Sets the current block state to the new given state. This function will also update the context's cached state.
    /// @param state       The new [BlockState]
    /// @param flags       The update flags to apply to this operation
    ///                    (for a list of flags, look [here](https://docs.neoforged.net/docs/blocks/states#levelsetblock))
    /// @param updateLimit The maximum number of times block neighbours can update each other before being stopped
    ///                    explicitly
    /// @return `true` if the block state has been set successfully, `false` otherwise
    public boolean set(final BlockState state, final @Block.UpdateFlags int flags, final int updateLimit)
    {
        if (this.level.setBlock(this.pos, state, flags, updateLimit))
        {
            this.updateState(state);
            return true;
        }
        
        return false;
    }
    
    /// Sets the current block state to the new given state. This function will also update the context's cached state.
    /// @param state The new [BlockState]
    /// @param flags The update flags to apply to this operation
    ///              (for a list of flags, look [here](https://docs.neoforged.net/docs/blocks/states#levelsetblock))
    /// @return `true` if the block state has been set successfully, `false` otherwise
    public boolean set(final BlockState state, final @Block.UpdateFlags int flags)
    {
        if (this.level.setBlock(this.pos, state, flags))
        {
            this.updateState(state);
            return true;
        }
        
        return false;
    }
    
    /// Sets the current block state to the new given state and sends updates to all its neighbours. This function
    /// will also update the context's cached state.
    /// @param state The new [BlockState]
    /// @return `true` if the block state has been set successfully, `false` otherwise
    public boolean setAndUpdate(final BlockState state)
    {
        if (this.level.setBlockAndUpdate(this.pos, state))
        {
            this.updateState(state);
            return true;
        }
        
        return false;
    }
    
    /// Destroys the block represented by this block context. This function will also update the context's cached state.
    /// @param shouldDrop  Whether this block, upon destruction, should drop items
    /// @param breaker     The [Entity] that destroyed the block
    /// @param updateLimit The maximum number of times block neighbours can update each other before being stopped
    ///                    explicitly
    /// @return `true` if the block has been destroyed successfully, `false` otherwise
    public boolean destroy(final boolean shouldDrop, final @Nullable Entity breaker, final int updateLimit)
    {
        if (this.level.destroyBlock(this.pos, shouldDrop, breaker, updateLimit))
        {
            return this.update();
        }
        
        return false;
    }
    
    /// Destroys the block represented by this block context. This function will also update the context's cached state.
    /// @param shouldDrop  Whether this block, upon destruction, should drop items
    /// @param breaker     The [Entity] that destroyed the block
    /// @return `true` if the block has been destroyed successfully, `false` otherwise
    public boolean destroy(final boolean shouldDrop, final @Nullable Entity breaker)
    {
        if (this.level.destroyBlock(this.pos, shouldDrop, breaker))
        {
            return this.update();
        }
        
        return false;
    }
    
    /// Destroys the block represented by this block context. This function will also update the context's cached state.
    /// @param shouldDrop Whether this block, upon destruction, should drop items
    /// @return `true` if the block has been destroyed successfully, `false` otherwise
    public boolean destroy(final boolean shouldDrop) { return this.destroy(shouldDrop, null); }
    
    /// Instructs this block context to explicitly request a world update for the state associated with it.
    /// @return `true` if the update could be successfully requested
    public boolean update()
    {
        if (!this.hasChunk())
        {
            return false;
        }
        
        final BlockState old_state = this.state;
        this.updateState(this.level.getBlockState(this.pos));
        
        return (old_state != this.state);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    protected void updateState(final BlockState state) { this.state = state; }
    
    //==================================================================================================================
    /// Gets whether this context's associated block state (might be stale) matches the given tag.
    /// @param tag The [TagKey] to match the currently cached [BlockState] with
    /// @return `true` if the [TagKey] matches the block state
    public boolean is(final TagKey<Block> tag) { return this.state.is(tag); }
    
    /// Gets whether this context's associated block state (might be stale) matches the given block instance.
    /// @param block The [Block] to match the currently cached [BlockState] with
    ///              (see [net.minecraft.world.level.block.Blocks])
    /// @return `true` if the [TagKey] matches the [BlockState]
    public boolean is(final Block block) { return this.state.is(block); }
    
    /// Gets whether this context's associated block state (might be stale) matches the given block holder instance.
    /// @param block The [Block] holder to match the currently cached [BlockState] with
    /// @return `true` if the [Block] matches the [BlockState]
    public boolean is(final Holder<Block> block) { return this.state.is(block); }
    
    /// Gets whether this context's associated block state (might be stale) matches any of the given block instances.
    /// @param blocks The [Block] of which to match any against the currently cached [BlockState] with
    /// @return `true` if any of the [Block] in the set matches the [BlockState]
    public boolean is(final HolderSet<Block> blocks) { return this.state.is(blocks); }
    
    /// Gets whether this context's associated block state (might be stale) matches the given block state.
    /// @param state The [BlockState] to match with
    /// @return `true` if the given state matches this context's [BlockState]
    public boolean is(final BlockState state) { return (this.state == state); }
    
    /// Gets whether this context's associated block state (might be stale) is air.
    /// @return `true` if this block state is an air block
    public boolean isAir() { return this.state.isAir(); }
    
    //==================================================================================================================
    /// {@return `true` if the block is considered solid (can conduct redstone) }
    public boolean isSolid() { return this.state.isRedstoneConductor(this.level, this.pos); }
    
    //==================================================================================================================
    @Override
    public boolean equals(final @Nullable Object obj)
    {
        if (obj == this)                          return true;
        if (!(obj instanceof BlockContext other)) return false;
        return (this.pos.equals(other.pos) && this.level == other.level);
    }
    
    @Override public int hashCode() { return Objects.hash(this.pos, this.level); }
}
