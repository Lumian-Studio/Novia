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

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;



//**********************************************************************************************************************
public abstract class RefUtils
{
    //******************************************************************************************************************
    /// {@return a {@link Predicate} that equality compares given object `other` {@link Objects#equals(Object, Object)}}
    public static <T> Predicate<T> equal(final @Nullable T other) { return (t -> Objects.equals(t, other)); }
    
    /// {@return a {@link Predicate} that equality compares given object `other` with the equality operator `==`}
    public static <T> Predicate<T> same(final @Nullable T other) { return (t -> t == other); }
    
    /// {@return a {@link Predicate} that is true if `other` is greater}
    public static <T extends Comparable<T>> Predicate<T> greaterThan(final T other)
    {
        return (t -> t.compareTo(other) > 0);
    }
    
    /// {@return a {@link Predicate} that is true if `other` is greater or equal}
    public static <T extends Comparable<T>> Predicate<T> greaterThanOrEqual(final T other)
    {
        return (t -> t.compareTo(other) >= 0);
    }
    
    /// {@return a {@link Predicate} that is true if `other` is less}
    public static <T extends Comparable<T>> Predicate<T> lessThan(final T other)
    {
        return (t -> t.compareTo(other) < 0);
    }
    
    /// {@return a {@link Predicate} that is true if `other` is less or equal}
    public static <T extends Comparable<T>> Predicate<T> lessThanOrEqual(final T other)
    {
        return (t -> t.compareTo(other) <= 0);
    }
    
    /// {@return a {@link Predicate} that is always `true`}
    public static <T> Predicate<T> alwaysTrue() { return (t -> true); }
    
    /// {@return a {@link Predicate} that is always `false`}
    public static <T> Predicate<T> alwaysFalse() { return (t -> false); }
    
    /// {@return a {@link Consumer} that does nothing with its argument}
    public static <T> Consumer<T> emptyConsumer() { return (t -> {}); }
    
    /// {@return a {@link Runnable} that does nothing}
    public static Runnable emptyRunnable() { return (() -> {}); }
    
    /// {@return a {@link Supplier} that always returns `null`}
    public static <T> Supplier<@Nullable T> nullSupplier() { return (() -> null); }
}
