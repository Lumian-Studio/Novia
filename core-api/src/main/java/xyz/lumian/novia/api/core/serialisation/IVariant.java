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
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.serialisation.codec.NoviaCodecs;
import xyz.lumian.novia.api.core.network.codec.NoviaStreamCodecs;
import xyz.lumian.novia.api.core.util.NumberType;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
sealed interface IVariant<T>
    permits
        IVariant.MapVariant,
        IVariant.ListVariant,
        IVariant.StringVariant,
        IVariant.NumberVariant,
        IVariant.BooleanVariant,
        IVariant.VoidVariant
{
    //******************************************************************************************************************
    record MapVariant(Map<Value, Value> value)
        implements IVariant<Map<Value, Value>>
    {
        //**************************************************************************************************************
        public static final StreamCodec<ByteBuf, MapVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                HashMap::new, Value.STREAM_CODEC,
                Value.STREAM_CODEC),
            MapVariant::value,
            (m -> new MapVariant(ImmutableMap.copyOf(m))));
        public static final Codec<MapVariant>                CODEC        = Codec
            .unboundedMap(Value.CODEC, Value.CODEC)
            .xmap((map -> new MapVariant(ImmutableMap.copyOf(map))), MapVariant::value);

        //**************************************************************************************************************
        private static String toKeyValuePair(final Map.Entry<Value, Value> entry)
        {
            final String key = entry.getKey().asString();
            final Value  val = entry.getValue();
            return (getQuoted(key) + ":" + (val.isString() ? getQuoted(val.getString()) : val.asString()));
        }

        //**************************************************************************************************************
        @Override public Map<Value, Value> getValue() { return this.value; }
        @Override public Value.Type        getType()  { return Value.Type.MAP; }
        
        //==============================================================================================================
        @Override public Map<Value, Value> asMap()     { return this.value; }
        @Override public List<Value>       asList()    { return this.value.values().stream().toList(); }
        @Override public boolean           asBoolean() { return !this.value.isEmpty(); }
        @Override public Number            asNumber()  { return this.value.size(); }

        @Override
        public String asString()
        {
            return this.value.entrySet()
                .stream()
                .map(MapVariant::toKeyValuePair)
                .collect(Collectors.joining(",", "{", "}"));
        }
    }

    record ListVariant(List<Value> value)
        implements IVariant<List<Value>>
    {
        //**************************************************************************************************************
        public static final StreamCodec<ByteBuf, ListVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Value.STREAM_CODEC), ListVariant::value,
            (l -> new ListVariant(ImmutableList.copyOf(l))));
        public static final Codec<ListVariant>                CODEC        = Value.CODEC
            .listOf()
            .xmap((list -> new ListVariant(ImmutableList.copyOf(list))), ListVariant::value);

        //**************************************************************************************************************
        @Override public List<Value> getValue() { return this.value; }
        @Override public Value.Type  getType()  { return Value.Type.LIST; }
        
        //==============================================================================================================
        @Override public List<Value> asList()    { return this.value; }
        @Override public boolean     asBoolean() { return !this.value.isEmpty(); }
        @Override public Number      asNumber()  { return this.value.size(); }

        @Override public String asString()
        {
            return this.value
                .stream()
                .map(val -> (val.isString() ? IVariant.getQuoted(val.asString()) : val.asString()))
                .collect(Collectors.joining(",", "[", "]"));
        }

        @Override
        public Map<Value, Value> asMap()
        {
            return Streams
                .mapWithIndex(this.value.stream(), (v, i) -> Pair.of(i, v))
                .collect(Collectors.toMap((p -> new Value(p.getFirst())), Pair::getSecond));
        }
    }

    record StringVariant(String value)
        implements IVariant<String>
    {
        //**************************************************************************************************************
        public static final StreamCodec<ByteBuf, StringVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StringVariant::value,
            StringVariant::new);
        public static final Codec<StringVariant>                CODEC        = Codec.STRING
            .xmap(StringVariant::new, StringVariant::value);

        //**************************************************************************************************************
        @Override public String               getValue() { return this.value; }
        @Override public Value.Type           getType()  { return Value.Type.STRING; }

        //==============================================================================================================
        @Override public String            asString()  { return this.value; }
        @Override public boolean           asBoolean() { return this.value.equalsIgnoreCase("true"); }
        @Override public List<Value>       asList() { return createSingletonList(new Value(this.value)); }
        @Override public Map<Value, Value> asMap()     { return createSingletonMap(new Value(this.value)); }

        @Override
        public Number asNumber()
        {
            try
            {
                return NumberUtils.createNumber(this.value);
            }
            catch (final NumberFormatException ignored) {}

            return 0;
        }
    }

    record NumberVariant(Number value)
        implements IVariant<Number>
    {
        //**************************************************************************************************************
        public static final StreamCodec<ByteBuf, NumberVariant> STREAM_CODEC = StreamCodec.composite(
            NoviaStreamCodecs.NUMBER, NumberVariant::getValue,
            NumberVariant::new);
        public static final Codec<NumberVariant>                CODEC        = NoviaCodecs.NUMBER
            .xmap(NumberVariant::new, NumberVariant::getValue);

        //**************************************************************************************************************
        private static final double EPS = 1.0e-6;
        
        //**************************************************************************************************************
        public NumberVariant(final Number value)
        {
            Objects.requireNonNull(value, "value must not be null");
            
            if (NumberType.isScalar(value))
            {
                this.value = value;
            }
            else if (NumberType.isNonScalarIntegral(value))
            {
                this.value = value.longValue();
            }
            else
            {
                this.value = value.doubleValue();
            }
        }
        
        //==============================================================================================================
        @Override public Number     getValue() { return this.value; }
        @Override public Value.Type getType()  { return Value.Type.NUMBER; }
        
        //==============================================================================================================
        @Override public Number            asNumber()  { return this.value; }
        @Override public String            asString()  { return this.value.toString(); }
        @Override public          boolean           asBoolean() { return (Math.abs(this.value.doubleValue()) > EPS); }
        @Override public List<Value>       asList()    { return createSingletonList(new Value(this.value)); }
        @Override public Map<Value, Value> asMap()     { return createSingletonMap(new Value(this.value)); }

        //==============================================================================================================
        @Override
        public boolean equals(final Object obj)
        {
            if (obj == this)                                   return true;
            if (!(obj instanceof NumberVariant(Number other))) return false;
            
            if (NumberType.isFloatingPointScalar(this.value) || NumberType.isFloatingPointScalar(other))
            {
                final BigDecimal bd1 = new BigDecimal(this.value.toString());
                final BigDecimal bd2 = new BigDecimal(other.toString());
                return (bd1.compareTo(bd2) == 0);
            }

            return (this.value.longValue() == other.longValue());
        }
    }

    record BooleanVariant(Boolean value)
        implements IVariant<Boolean>
    {
        //**************************************************************************************************************
        public static final StreamCodec<ByteBuf, BooleanVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, BooleanVariant::value,
            BooleanVariant::new);
        public static final Codec<BooleanVariant>                CODEC        = Codec.BOOL
            .xmap(BooleanVariant::new, BooleanVariant::value);

        //**************************************************************************************************************
        @Override public Boolean    getValue() { return this.value; }
        @Override public Value.Type getType()  { return Value.Type.BOOLEAN; }
        
        //==============================================================================================================
        @Override public boolean           asBoolean() { return this.value; }
        @Override public Number            asNumber()  { return (this.value ? 1 : 0); }
        @Override public String            asString()  { return String.valueOf(this.value); }
        @Override public List<Value>       asList()    { return createSingletonList(new Value(this.value)); }
        @Override public Map<Value, Value> asMap()     { return createSingletonMap(new Value(this.value)); }
    }

    final class VoidVariant
        implements IVariant<Void>
    {
        //**************************************************************************************************************
        public static final VoidVariant                       INSTANCE     = new VoidVariant();
        public static final StreamCodec<ByteBuf, VoidVariant> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final Codec<VoidVariant>                CODEC        = NoviaCodecs.unit(INSTANCE);
        
        //**************************************************************************************************************
        private VoidVariant() {}
        
        //==============================================================================================================
        @Override public @Nullable Void       getValue() { return null; }
        @Override public Value.Type getType()  { return Value.Type.VOID; }

        //==============================================================================================================
        @Override public boolean           asBoolean() { return false; }
        @Override public Number            asNumber()  { return 0; }
        @Override public String            asString()  { return "null"; }
        @Override public List<Value>       asList()    { return Lists.newArrayList(); }
        @Override public Map<Value, Value> asMap()     { return Maps.newHashMap(); }
    }

    //******************************************************************************************************************
    private static Map<Value, Value> createSingletonMap(final Value value)
    {
        return Maps.newHashMap(Collections.singletonMap(new Value("value"), value));
    }

    private static List<Value> createSingletonList(final Value value)
    {
        return Lists.newArrayList(Collections.singletonList(value).iterator());
    }

    //==================================================================================================================
    private static String getQuoted(final String str) { return ('"' + str + '"'); }

    //******************************************************************************************************************
    @Nullable T getValue();
    Value.Type  getType();

    //==================================================================================================================
    Map<Value, Value> asMap();
    List<Value>       asList();
    String            asString();
    boolean           asBoolean();
    Number            asNumber();
}
