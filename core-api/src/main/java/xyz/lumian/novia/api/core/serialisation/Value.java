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
package xyz.lumian.novia.api.core.serialisation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullUnmarked;
import xyz.lumian.novia.api.core.serialisation.IVariant.MapVariant;
import xyz.lumian.novia.api.core.serialisation.IVariant.ListVariant;
import xyz.lumian.novia.api.core.serialisation.IVariant.StringVariant;
import xyz.lumian.novia.api.core.serialisation.IVariant.NumberVariant;
import xyz.lumian.novia.api.core.serialisation.IVariant.BooleanVariant;
import xyz.lumian.novia.api.core.serialisation.IVariant.VoidVariant;



//**********************************************************************************************************************
/// Represents an immutable limited variant type that can take a set of specified types only.
///
/// Supported types are lists, maps, numbers, booleans and strings. `null` is also supported as a distinct
/// type from others, where a Value object that contains `null` is considered an “empty” Value object.
///
/// Throughout this library there will be mentions of "this is a &lt;type&gt; qualified Value", which just means that
/// the API is expecting value objects of only a certain subset of types and might or might not throw an exception if
/// different type values are given. If no qualification is specified, it is qualified for any of the supported
/// value types.
public class Value
{
    //******************************************************************************************************************
    /// Type of object contained in a [Value] instance.
    public enum Type
        implements StringRepresentable
    {
        //**************************************************************************************************************
        /// Represents an [ImmutableMap] where both “key” and “value” are [Value] objects.
        MAP(new Value(new MapVariant(ImmutableMap.of()))),
        
        /// Represents an [ImmutableList] of [Value] objects.
        LIST(new Value(new ListVariant(ImmutableList.of()))),
        
        /// Represents a [Number] object.
        NUMBER(new Value(new NumberVariant(0))),
        
        /// Represents a [Boolean] object.
        BOOLEAN(new Value(new BooleanVariant(false))),
        
        /// Represents a [String] object.
        STRING(new Value(new StringVariant(""))),
        
        /// Represents an empty [Value] instance.
        VOID(new Value(VoidVariant.INSTANCE));
        
        //**************************************************************************************************************
        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = ByteBufCodecs
            .idMapper((i -> Type.values()[i]), Enum::ordinal);
        public static final Codec<Type>                CODEC        = StringRepresentable.fromEnum(Type::values);
        
        //**************************************************************************************************************
        private final Value defaultValue;
        
        //**************************************************************************************************************
        Type(final Value defaultValue) { this.defaultValue = defaultValue; }
        
        //==============================================================================================================
        private Value getDefault() { return this.defaultValue; }
        
        //==============================================================================================================
        @Override public String getSerializedName() { return (this == VOID ? "null" : name().toLowerCase()); }
    }

    //******************************************************************************************************************
    /// Describes the packet codec of a [Value] object.
    public static final StreamCodec<ByteBuf, Value> STREAM_CODEC;

    /// Describes the serialisation codec of a [Value] object.
    public static final Codec<Value> CODEC;
    
    /// A constant for [Value] objects representing a `true` boolean object.
    public static final Value TRUE = new Value(true);
    
    /// A constant for [Value] objects representing a `false` boolean object.
    public static final Value FALSE = new Value(false);
    
    /// A constant for [Value] objects representing a `null` object.
    public static final Value EMPTY = new Value();
    
    //==================================================================================================================
    static
    {
        STREAM_CODEC = StreamCodec.recursive(ignored -> Type.STREAM_CODEC
            .dispatch(
                IVariant::getType,
                (type -> switch (type)
                {
                    case VOID    -> VoidVariant   .STREAM_CODEC;
                    case MAP     -> MapVariant    .STREAM_CODEC;
                    case LIST    -> ListVariant   .STREAM_CODEC;
                    case STRING  -> StringVariant .STREAM_CODEC;
                    case NUMBER  -> NumberVariant .STREAM_CODEC;
                    case BOOLEAN -> BooleanVariant.STREAM_CODEC;
                }))
            .map(Value::new, (var -> var.variant)));

        CODEC = Codec.recursive("NoviaValue", ignored -> new Codec<>()
        {
            //**********************************************************************************************************
            @SuppressWarnings("unchecked")
            private static <T> T getVar(final Value value) { return (T) value.variant; }

            //**********************************************************************************************************
            @Override
            public <T> DataResult<Pair<Value, T>> decode(final DynamicOps<T> ops, final T input)
            {
                final DataResult<Pair<MapVariant, T>> m_res = MapVariant.CODEC.decode(ops, input);
                if (m_res.isSuccess()) return m_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<ListVariant, T>> l_res = ListVariant.CODEC.decode(ops, input);
                if (l_res.isSuccess()) return l_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<NumberVariant, T>> n_res = NumberVariant.CODEC.decode(ops, input);
                if (n_res.isSuccess()) return n_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<BooleanVariant, T>> b_res = BooleanVariant.CODEC.decode(ops, input);
                if (b_res.isSuccess()) return b_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                final DataResult<Pair<StringVariant, T>> s_res = StringVariant.CODEC.decode(ops, input);
                if (s_res.isSuccess()) return s_res.map(p -> Pair.of(new Value(p.getFirst()), p.getSecond()));

                return DataResult.success(Pair.of(new Value(), input));
            }
            
            @Override
            public <T> DataResult<T> encode(final Value input, final DynamicOps<T> ops, final T prefix)
            {
                return switch (input.getType())
                {
                    case MAP     -> MapVariant    .CODEC.encode(getVar(input), ops, prefix);
                    case LIST    -> ListVariant   .CODEC.encode(getVar(input), ops, prefix);
                    case STRING  -> StringVariant .CODEC.encode(getVar(input), ops, prefix);
                    case NUMBER  -> NumberVariant .CODEC.encode(getVar(input), ops, prefix);
                    case BOOLEAN -> BooleanVariant.CODEC.encode(getVar(input), ops, prefix);
                    case VOID    -> VoidVariant   .CODEC.encode(getVar(input), ops, prefix);
                };
            }
        });
    }

    //******************************************************************************************************************
    /// Tries to construct a [Value] from a given object if possible.
    /// @param object The object to construct the [Value] object from
    /// @return The new [Value] object, or `null` on failure
    public static @Nullable Value fromObject(final @Nullable Object object)
    {
        if (object instanceof Value value)
        {
            return value;
        }

        try
        {
            return switch (object)
            {
                case String  value -> new Value(value);
                case Boolean value -> new Value(value);
                case Number  value ->
                {
                    try
                    {
                        yield new Value(value);
                    }
                    catch (UnsupportedOperationException ignored)
                    {
                        yield null;
                    }
                }
                case null          -> Value.EMPTY;
                case List<?> value -> new Value(value
                    .stream()
                    .map(Value::tryFromObject).toList());

                case Map<?, ?> value -> new Value(value
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        e -> tryFromObject(e.getKey()),
                        e -> tryFromObject(e.getValue()))));

                default -> null;
            };
        }
        catch (final ValueConversionException ignored) {}

        return null;
    }

    /// Tries to construct a [Value] from a given object if possible.
    /// @param object The object to construct the [Value] object from
    /// @return The new [Value] object
    /// @throws ValueConversionException If the given object could not be converted to [Value]
    public static Value tryFromObject(final @Nullable Object object)
    {
        final Value result = Value.fromObject(object);

        if (result == null)
        {
            throw new ValueConversionException(String.format(
                "invalid object of type '%s', is not a valid value type",
                Objects.requireNonNull(object).getClass().getName()));
        }

        return result;
    }

     /// Tries to parse a [Value] object from a given string.
     /// For the parsing, [JsonOps] will be used to check if the value is a valid JSON string and return a
     /// successful result, otherwise the input will be treated as a string value.
     ///
     /// Any output from [Value#asString()] can be parsed back to the original value with this factory.
     ///
     /// @param string The string to parse from
     /// @return The new [Value] object
    public static Value parse(final String string)
    {
        try
        {
            return tryParse(string);
        }
        catch (final ValueConversionException ignored) {}

        return new Value(string);
    }

    /// Tries to parse a [Value] object from a given string.
    /// For the parsing, [JsonOps] will be used to check if the value is a valid JSON string and return a
    /// successful result, otherwise a [ValueConversionException] is thrown.
    ///
    /// Any output from [Value#asString()] can be parsed back to the original value with this factory.
    ///
    /// @param string The string to parse from
    /// @return The new [Value] object
    /// @throws ValueConversionException If the input string could not be parsed
    public static Value tryParse(final String string)
    {
        final JsonElement       json   = JsonParser.parseString(string);
        final DataResult<Value> result = Value.CODEC.parse(JsonOps.INSTANCE, json);

        if (result.isSuccess())
        {
            return result.result().orElseThrow();
        }

        throw new ValueConversionException(String.format("could not parse value '%s'", string));
    }

    //******************************************************************************************************************
    private final IVariant<?> variant;

    //******************************************************************************************************************
    /// Constructs a new empty Value.
    public Value() { this(VoidVariant.INSTANCE); }

    /// Constructs a Value with a specified default depending on the given type.
    /// <ul>
    ///     <li>[Type#MAP] An empty map</li>
    ///     <li>[Type#LIST] An empty list</li>
    ///     <li>[Type#STRING] An empty string (`""`)</li>
    ///     <li>[Type#NUMBER] `0`</li>
    ///     <li>[Type#BOOLEAN] `false`</li>
    ///     <li>[Type#VOID] `null`</li>
    /// </ul>
    ///
    /// @param type The [Type]
    public Value(final Type type) { this(type.getDefault().variant); }

    /// Constructs a new map value.
    /// @param value The map object
    public Value(final Map<Value, Value> value) { this(new MapVariant(ImmutableMap.copyOf(value))); }

    /// Constructs a new list variant.
    /// @param value The list object
    public Value(final Collection<Value> value) { this(new ListVariant(ImmutableList.copyOf(value))); }
    
    /// Constructs a new string variant.
    /// @param value The string
    public Value(final String value) { this(new StringVariant(value)); }

    /// Constructs a new boolean variant.
    /// @param value The boolean value
    public Value(final boolean value) { this(new BooleanVariant(value)); }

    /// Constructs a new number variant.
    ///
    /// This constructor takes a [Number] object and casts it to any one of the primitive types.
    /// For the BigX, AtomicX, XAccumulator and XAdder variants, it will gather the appropriate scalar variant type.
    /// For any remaining non hard-coded types, it will just gather the double value.
    ///
    /// Do note that the BigX variants may lose precision as they are converted to their scalar variants.
    ///
    /// @param value The number value
    /// @throws UnsupportedOperationException If the number object is not supported
    public Value(final Number value) { this(new NumberVariant(value)); }
    
    /// Copies the internal reference to the given [Value] object.
    ///
    /// Since [Value] objects are immutable, this does not create a new object but rather just acts as a view to
    /// the internal object of the given object.
    ///
    /// @param value The value to copy
    public Value(final Value value) { this(Objects.requireNonNull(value, "value must not be null").variant); }
    
    /// Constructs a new [Value] from a [IValueConvertible].
    /// If the convertible's [IValueConvertible#getValue()] returns null, this will throw an exception.
    ///
    /// @param convertible The convertible
    public Value(final IValueConvertible convertible)
    {
        this(Objects.requireNonNull(convertible, "convertible must not be null").getValue());
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private Value(final IVariant<?> variant) { this.variant = variant; }

    //==================================================================================================================
    /// If the object represented by this [Value] object is a map, will return the value as a map object.
    /// @return The underlying map object
    /// @throws ClassCastException If the underlying value object was not a map
    public Map<Value, Value> getMap() { return castOrThrow(Type.MAP); }

    /// If the object represented by this [Value] object is a list, will return the value as a list object.
    /// @return The underlying list object
    /// @throws ClassCastException If the underlying value object was not a list
    public List<Value> getList() { return castOrThrow(Type.LIST); }

    /// If the object represented by this [Value] object is a {@link Number}, will return the value as a {@link Number}
    /// object.
    ///
    /// @return The underlying {@link Number} object
    /// @throws ClassCastException If the underlying value object was not a {@link Number}
    public Number getNumber() { return castOrThrow(Type.NUMBER); }

    /// If the object represented by this [Value] object is a boolean, will return the value as a boolean object.
    /// @return The underlying boolean object
    /// @throws ClassCastException If the underlying value object was not a boolean object
    public Boolean getBoolean() { return castOrThrow(Type.BOOLEAN); }

    /// If the object represented by this [Value] object is a string, will return the value as a string.
    ///
    /// Other than [Value#asString()] and [Value#toString()] this will only return a string if the underlying object
    /// actually is a string object and throw otherwise.
    ///
    /// @return The underlying string
    /// @throws ClassCastException If the underlying value object was not a string
    public String getString() { return castOrThrow(Type.STRING); }

    /// Gets the underlying value object.
    /// @return The underlying object
    @NullUnmarked
    public Object getValue() { return this.variant.getValue(); }

    /// Gets the type this Value has been overloaded with.
    /// @return The current value type
    public Type getType() { return this.variant.getType(); }

    //------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private <T> T castOrThrow(final Type type)
    {
        if (!is(type))
        {
            throw new ClassCastException("Cannot convert " + getType().name() + " to " + type.name());
        }

        return (T) getValue();
    }

    //==================================================================================================================
    /// Gets the current value as map and transforms the value if it isn't already one.
    /// <ul>
    ///     <li>[Type#MAP] As-is</li>
    ///     <li>[Type#LIST] A map with each value of the list mapped to its respective index</li>
    ///     <li>[Type#VOID] An empty map</li>
    ///     <li><b>Others</b> A singleton map with the value mapped to a `value` key</li>
    /// </ul>
    ///
    /// @return The transformed map object
    public Map<Value, Value> asMap() { return this.variant.asMap(); }

    /// Gets the current value as map and transforms the value if it isn't already one.
    /// <ul>
    ///     <li>[Type#LIST] As-is</li>
    ///     <li>[Type#MAP] A list with all values of the map and keys dropped (order is unspecified) </li>
    ///     <li>[Type#VOID] An empty list</li>
    ///     <li><b>Others</b> A singleton list with the value inserted at the beginning</li>
    /// </ul>
    ///
    /// @return The transformed list object
    public List<Value> asList() { return this.variant.asList(); }

    /// Gets the current value as map and transforms the value if it isn't already one.
    /// <ul>
    ///     <li>[Type#NUMBER] As-is</li>
    ///     <li>[Type#STRING] The parsed number if successful, otherwise `0`</li>
    ///     <li>[Type#BOOLEAN] If the boolean value is true `1`, otherwise `0`</li>
    ///     <li>[Type#LIST] The amount of elements in the list</li>
    ///     <li>[Type#MAP] The amount of entries in the list</li>
    ///     <li>[Type#VOID] `0`</li>
    /// </ul>
    ///
    /// @return The transformed {@link Number}> object
    public Number asNumber() { return this.variant.asNumber(); }

    /// Gets the current value as map and transforms the value if it isn't already one.
    /// <ul>
    ///     <li>[Type#BOOLEAN] As-is</li>
    ///     <li>[Type#STRING] The parsed boolean if successful, otherwise `false`</li>
    ///     <li>[Type#NUMBER] If 0 then `false`, otherwise `true`</li>
    ///     <li>[Type#LIST] `true` if there's at least one element in the list</li>
    ///     <li>[Type#MAP] `true` if there's at least one entry in the map</li>
    ///     <li>[Type#VOID] `false`</li>
    /// </ul>
    ///
    /// @return The transformed boolean object
    public Boolean asBoolean() { return this.variant.asBoolean(); }

    /// Gets the current value as map and transforms the value if it isn't already one.
    /// <ul>
    ///     <li>[Type#STRING] As-is</li>
    ///     <li>[Type#MAP] A textually mapped list of all entries (e.g. `{key="val",key2=2}`)</li>
    ///     <li>[Type#LIST] A textually enumerated list of all elements (e.g. `["val", 2]`)</li>
    ///     <li>[Type#VOID] `"null"`</li>
    ///     <li><b>Others</b> The `toString()` value (e.g. `"true"`, `"0"`)</li>
    /// </ul>
    /// Other than [Value#toString()] and [Value#getString()] this will transform the object's textual
    /// representation in a way that represents the whole contents of the object.
    ///
    /// @return The transformed string object
    public String asString() { return this.variant.asString(); }

    //==================================================================================================================
    /// Determines whether the current held object is a map.
    /// @return `true` if it is a map
    public boolean isMap() { return (getType() == Type.MAP); }

    /// Determines whether the current held object is a list.
    /// @return `true` if it is a list
    public boolean isList() { return (getType() == Type.LIST); }

    /// Determines whether the current held object is a string.
    /// @return `true` if it is a string
    public boolean isString() { return (getType() == Type.STRING); }

    /// Determines whether the current held object is a [Number].
    /// @return `true` if it is a number value
    public boolean isNumber() { return (getType() == Type.NUMBER); }

    /// Determines whether the current held object is a boolean value.
    /// @return `true` if it is a boolean value
    public boolean isBoolean() { return (getType() == Type.BOOLEAN); }

    /// Determines whether the current held object is `null`.
    /// @return `true` if this value is empty
    public boolean isEmpty() { return (getType() == Type.VOID); }

    /// Determines whether current held object is of type `type`.
    /// @param type The [Type] to check
    /// @return `true` if the types match
    public boolean is(final Type type) { return (getType() == type); }

    //==================================================================================================================
    /// Returns the string representation of the currently held value.
    ///
    /// Other than [Value#getString()] and [Value#asString()],
    /// this will use the object's `toString()` method to gather the textual representation,
    /// which may or may not reflect the actual contents of the object.
    ///
    /// @return The string representation
    @Override
    public String toString()
    {
        return (super.toString() + "{type=" + this.getType() + ",value=" + this.getValue() + '}');
    }

    /// Returns true if this and `obj` are references to the same object or if the underlying object of both
    /// contain the same value.
    ///
    /// If `obj` is not a [Value], then `false` is returned.
    ///
    /// @return `true` if obj and the current [Value] instance are equal
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)                   return true;
        if (!(obj instanceof Value other)) return false;
        
        return this.variant.equals(other.variant);
    }

    /// Gets the hash code of the underlying value object or null if this [Value] object is empty.
    /// @return The hash code
    @Override public int hashCode() { return (this.variant.hashCode() + getType().ordinal()); }
}
