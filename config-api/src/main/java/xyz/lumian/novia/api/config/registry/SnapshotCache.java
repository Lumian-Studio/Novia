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
package xyz.lumian.novia.api.config.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.PropertyId;
import xyz.lumian.novia.api.config.provider.IConfigProvider;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.*;



//**********************************************************************************************************************
@ApiStatus.Internal
public record SnapshotCache(Map<String, List<SnapshotGroup>> namespaces)
{
    //******************************************************************************************************************
    public record Snapshot(JsonPointer pointer, Value value)
    {
        //**************************************************************************************************************
        public static final StreamCodec<FriendlyByteBuf, Snapshot> STREAM_CODEC = StreamCodec.composite(
            JsonPointer.STREAM_CODEC, Snapshot::pointer,
            Value      .STREAM_CODEC, Snapshot::value,
            Snapshot::new);

        //**************************************************************************************************************
        @SuppressWarnings("unchecked")
        static <Container> Snapshot from(final Object container, final PropertySpec<Container> spec)
        {
            return new Snapshot(spec.pointer(), spec.get((Container) container).getValue());
        }

        //==============================================================================================================
        public Snapshot withValue(final Value value) { return new Snapshot(this.pointer, value); }
    }

    public record SnapshotGroup(String path, List<Snapshot> snapshots)
    {
        //**************************************************************************************************************
        public static final StreamCodec<FriendlyByteBuf, SnapshotGroup> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SnapshotGroup::path,
            Snapshot.STREAM_CODEC.apply(ByteBufCodecs.list()), SnapshotGroup::snapshots,
            SnapshotGroup::new);

        //**************************************************************************************************************
        static <T extends IConfigProvider<?>> SnapshotGroup from(final Identifier id, final T provider)
        {
            return new SnapshotGroup(
                id.getPath(),
                provider.getSpec().flatPropertySpecs().values()
                    .stream()
                    .map(spec -> Snapshot.from(provider.getManagedContainer(), spec))
                    .toList());
        }
        
        //==============================================================================================================
        public @Nullable Snapshot findSnapshot(final JsonPointer pointer)
        {
            return this.snapshots
                .stream()
                .filter(snap -> snap.pointer.equals(pointer))
                .findFirst()
                .orElse(null);
        }
        
        public void setSnapshot(final JsonPointer pointer, final Value newValue)
        {
            for (int i = 0; i < this.snapshots.size(); ++i)
            {
                final Snapshot snapshot = this.snapshots.get(i);
                
                if (snapshot.pointer.equals(pointer))
                {
                    this.snapshots.set(i, snapshot.withValue(newValue));
                }
            }
        }
    }

    //******************************************************************************************************************
    public static final StreamCodec<FriendlyByteBuf, SnapshotCache> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(
            LinkedHashMap::new, ByteBufCodecs.STRING_UTF8,
            SnapshotGroup.STREAM_CODEC.apply(ByteBufCodecs.list())),
        SnapshotCache::namespaces,
        SnapshotCache::new);

    //******************************************************************************************************************
    public static <T extends IConfigProvider<?>> SnapshotCache from(final Map<Identifier, T> registry)
    {
        final Map<String, List<SnapshotGroup>> result  = new LinkedHashMap<>();
        final Set<Map.Entry<Identifier, T>>    entries = registry.entrySet();
        
        registry.keySet().stream()
            .map(Identifier::getNamespace)
            .distinct()
            .forEach(namespace ->
                result.put(
                    namespace,
                    entries
                        .stream()
                        .filter(e -> e.getKey().getNamespace().equals(namespace))
                        .map(e -> SnapshotGroup.from(e.getKey(), e.getValue()))
                        .collect(ImmutableList.toImmutableList())));
        
        return new SnapshotCache(ImmutableMap.copyOf(result));
    }

    //******************************************************************************************************************
    public @Nullable SnapshotGroup getGroup(final Identifier id)
    {
        final List<SnapshotGroup> groups = this.namespaces.get(id.getNamespace());
        
        if (groups == null)
        {
            return null;
        }
        
        for (final var group : groups)
        {
            if (group.path.equals(id.getPath()))
            {
                return group;
            }
        }

        return null;
    }
    
    public @Nullable Snapshot getSnapshot(final Identifier id, final JsonPointer pointer)
    {
        final SnapshotGroup group = this.getGroup(id);
        
        if (group == null)
        {
            return null;
        }
        
        for (final var snapshot : group.snapshots)
        {
            if (snapshot.pointer.equals(pointer))
            {
                return snapshot;
            }
        }

        return null;
    }
    
    public @Nullable Snapshot getSnapshot(final PropertyId propertyId)
    {
        return this.getSnapshot(propertyId.providerId(), propertyId.pointer());
    }
    
    //==================================================================================================================
    public boolean setSnapshot(final Identifier id, final JsonPointer pointer, final Value value)
    {
        final SnapshotGroup group = this.getGroup(id);
        
        if (group == null)
        {
            return false;
        }
        
        for (int i = 0; i < group.snapshots.size(); ++i)
        {
            final Snapshot snapshot = group.snapshots.get(i);
            
            if (snapshot.pointer.equals(pointer))
            {
                group.snapshots.set(i, snapshot.withValue(value));
                return true;
            }
        }

        return false;
    }
    
    public boolean setSnapshot(final PropertyId propertyId, final Value value)
    {
        return this.setSnapshot(propertyId.providerId(), propertyId.pointer(), value);
    }
}
