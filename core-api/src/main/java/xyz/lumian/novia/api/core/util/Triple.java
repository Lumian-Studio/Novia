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
package xyz.lumian.novia.api.core.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;



//**********************************************************************************************************************
public record Triple<T, U, V>(T first, U second, V third)
{
    //******************************************************************************************************************
    public static <T, U, V> Triple<T, U, V> of(final T first, final U second, final V third)
    {
        return new Triple<>(first, second, third);
    }
    
    //==================================================================================================================
    /// Utility function providing a converter for the first element of a [Triple].
    /// @param converter The function converting the first object of a [Triple]
    /// @return The conversion function
    /// @param <T> Type of first object in [Triple]
    /// @param <U> Type of second object in [Triple]
    /// @param <V> Type of third object in [Triple]
    /// @param <R> Resulting type of the conversion of the first object
    public static <T, U, V, R> Function<Triple<T, U, V>, R> forFirst(final Function<T, R> converter)
    {
        return (triple -> converter.apply(triple.first()));
    }

    /// Utility function providing a converter for the second element of a [Triple].
    /// @param converter The function converting the second object of a [Triple]
    /// @return The conversion function
    /// @param <T> Type of first object in [Triple]
    /// @param <U> Type of second object in [Triple]
    /// @param <V> Type of third object in [Triple]
    /// @param <R> Resulting type of the conversion of the second object
    public static <T, U, V, R> Function<Triple<T, U, V>, R> forSecond(final Function<U, R> converter)
    {
        return (triple -> converter.apply(triple.second()));
    }
    
    /// Utility function providing a converter for the third element of a [Triple].
    /// @param converter The function converting the third object of a [Triple]
    /// @return The conversion function
    /// @param <T> Type of first object in [Triple]
    /// @param <U> Type of second object in [Triple]
    /// @param <V> Type of third object in [Triple]
    /// @param <R> Resulting type of the conversion of the third object
    public static <T, U, V, R> Function<Triple<T, U, V>, R> forThird(final Function<V, R> converter)
    {
        return (triple -> converter.apply(triple.third()));
    }
    
    //==================================================================================================================
    /// Creates a [StreamCodec] for a given [Triple].
    /// @param codecFirst  The [StreamCodec] of the first element
    /// @param codecSecond The [StreamCodec] of the second element
    /// @param codecThird  The [StreamCodec] of the third element
    /// @return The newly combined [StreamCodec]
    /// @param <T> Type of first object in [Triple]
    /// @param <U> Type of second object in [Triple]
    /// @param <V> Type of third object in [Triple]
    /// @param <B> The [ByteBuf] type
    public static <T, U, V, B extends ByteBuf> StreamCodec<B, Triple<T, U, V>> createPacketCodec(
        final StreamCodec<B, T> codecFirst,
        final StreamCodec<B, U> codecSecond,
        final StreamCodec<B, V> codecThird
    )
    {
        return StreamCodec.composite(
            codecFirst,  Triple::first,
            codecSecond, Triple::second,
            codecThird,  Triple::third,
            Triple::new);
    }
    
    /// Creates a [MapCodec] for a given [Triple].
    /// @param codecFirst  The [Codec] of the first element
    /// @param codecSecond The [Codec] of the second element
    /// @param codecThird  The [Codec] of the third element
    /// @return The newly combined [MapCodec]
    /// @param <T> Type of first object in [Triple]
    /// @param <U> Type of second object in [Triple]
    /// @param <V> Type of third object in [Triple]
    public static <T, U, V> MapCodec<Triple<T, U, V>> createMapCodec(
        final Codec<T> codecFirst,
        final Codec<U> codecSecond,
        final Codec<V> codecThird
    )
    {
        return RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                codecFirst
                    .fieldOf("first")
                    .forGetter(Triple::first),
                codecSecond
                    .fieldOf("second")
                    .forGetter(Triple::second),
                codecThird
                    .fieldOf("third")
                    .forGetter(Triple::third))
            .apply(instance, Triple::new));
    }
    
    /// Creates a [Codec] for a given [Triple].
    /// @param codecFirst  The [Codec] of the first element
    /// @param codecSecond The [Codec] of the second element
    /// @param codecThird  The [Codec] of the third element
    /// @return The newly combined [Codec]
    /// @param <T> Type of first object in [Triple]
    /// @param <U> Type of second object in [Triple]
    /// @param <V> Type of third object in [Triple]
    public static <T, U, V> Codec<Triple<T, U, V>> createCodec(
        final Codec<T> codecFirst,
        final Codec<U> codecSecond,
        final Codec<V> codecThird
    )
    {
        return Triple.createMapCodec(codecFirst, codecSecond, codecThird).codec();
    }
    
    //******************************************************************************************************************
    /// Gets a new [Triple] with the first object replaced.
    /// @param first The new first object
    /// @return The new [Triple]
    public <F> Triple<F, U, V> withFirst(final F first) { return Triple.of(first, this.second, this.third); }
    
    /// Gets a new [Triple] with the second object replaced.
    /// @param second The new second object
    /// @return The new [Triple]
    public <S> Triple<T, S, V> withSecond(final S second) { return Triple.of(this.first, second, this.third); }
    
    /// Gets a new [Triple] with the third object replaced.
    /// @param third The new third object
    /// @return The new [Triple]
    public <D> Triple<T, U, D> withThird(final D third) { return Triple.of(this.first, this.second, third); }
    
    //==================================================================================================================
    /// Gets a new [Triple] with all objects mapped by the given mappers.
    /// @param firstMapper  The [Function] used to map the first object
    /// @param secondMapper The [Function] used to map the second object
    /// @param thirdMapper  The [Function] used to map the third object
    /// @return The new [Triple]
    public <F, S, D> Triple<F, S, D> map(final Function<T, F> firstMapper,
                                         final Function<U, S> secondMapper,
                                         final Function<V, D> thirdMapper)
    {
        return Triple.of(firstMapper.apply(this.first), secondMapper.apply(this.second), thirdMapper.apply(this.third));
    }
    
    /// Gets a new [Triple] with the first object mapped by the given mapper.
    /// @param mapper The [Function] used to map the first object
    /// @return The new [Triple]
    public <F> Triple<F, U, V> mapFirst(final Function<T, F> mapper)
    {
        return Triple.of(mapper.apply(this.first), this.second, this.third);
    }
    
    /// Gets a new [Triple] with the second object mapped by the given mapper.
    /// @param mapper The [Function] used to map the second object
    /// @return The new [Triple]
    public <S> Triple<T, S, V> mapSecond(final Function<U, S> mapper)
    {
        return Triple.of(this.first, mapper.apply(this.second), this.third);
    }
    
    /// Gets a new [Triple] with the third object mapped by the given mapper.
    /// @param mapper The [Function] used to map the third object
    /// @return The new [Triple]
    public <D> Triple<T, U, D> mapThird(final Function<V, D> mapper)
    {
        return Triple.of(this.first, this.second, mapper.apply(this.third));
    }
    
    public <R> R flatMapFirst(final Function<T, R> mapper) { return mapper.apply(this.first); }
    
    public <R> R flatMapSecond(final Function<U, R> mapper) { return mapper.apply(this.second); }
    
    public <R> R flatMapThird(final Function<V, R> mapper) { return mapper.apply(this.third); }
    
    public void apply(final Consumer<T> first, final Consumer<U> second, final Consumer<V> third)
    {
        first .accept(this.first);
        second.accept(this.second);
        third .accept(this.third);
    }
    
    public void applyFirst(final Consumer<T> consumer) { consumer.accept(this.first); }
    
    public void applySecond(final Consumer<U> consumer) { consumer.accept(this.second); }
    
    public void applyThird(final Consumer<V> consumer) { consumer.accept(this.third); }
}
