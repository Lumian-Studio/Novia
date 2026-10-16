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
package xyz.lumian.novia.api.core.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.lumian.novia.api.core.serialisation.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;



//**********************************************************************************************************************
public class ValueTest
{
    //******************************************************************************************************************
    private static Value encodeAndDecodePacket(@NotNull final Value value)
    {
        final ByteBuf string_buf = Unpooled.buffer();
        Value.STREAM_CODEC.encode(string_buf, value);
        return Value.STREAM_CODEC.decode(string_buf);
    }

    private <T> Value encodeAndDecodeDfu(@NotNull final Value value, @NotNull final DynamicOps<T> ops)
    {
        final DataResult<T> encode_result = Value.CODEC.encodeStart(ops, value);
        Assertions.assertTrue(encode_result.isSuccess(), (() -> String.format(
            "could not encode value: %s",
            encode_result.error().orElseThrow().message())));

        final DataResult<Pair<Value, T>> decode_result = Value.CODEC.decode(ops, encode_result.getOrThrow());
        Assertions.assertTrue(decode_result.isSuccess(), (() -> String.format(
            "could not decode value: %s",
            decode_result.error().orElseThrow().message())));

        return decode_result.getOrThrow().getFirst();
    }

    //******************************************************************************************************************
    private final Value intValue    = new Value(0);
    private final Value floatValue  = new Value(0.00001f);
    private final Value stringValue = new Value("420");
    private final Value boolValue   = new Value(true);

    private final Value mapValue = new Value(Maps.newLinkedHashMap(ImmutableMap.of(
        new Value("boolean"), new Value(true),

        new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("string"), new Value("hello"),
            new Value("null"), new Value(),

            new Value("list"), new Value(List.of(
                new Value("number"),
                new Value(1)
            ))
        )))
    )));

    private final Value listValue = new Value(List.of(
        new Value(true),
        new Value("hello"),
        new Value("map"),
        new Value(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("null"), new Value(),

            new Value("list"), new Value(List.of(
                new Value("number"),
                new Value(1)
            ))
        )))
    ));

    private final Value nullValue = new Value();

    //******************************************************************************************************************
    @Test
    public void conversion()
    {
        //................................
        Assertions.assertEquals("0",                                  intValue.asString());
        Assertions.assertEquals(0,                                    intValue.asNumber());
        Assertions.assertEquals(false,                                intValue.asBoolean());
        Assertions.assertEquals(List.of(intValue),                    intValue.asList());
        Assertions.assertEquals(Map.of(new Value("value"), intValue), intValue.asMap());

        //................................
        Assertions.assertEquals(Float.valueOf(0.00001f).toString(),     floatValue.asString());
        Assertions.assertEquals(0.00001f,                               floatValue.asNumber());
        Assertions.assertEquals(true,                                   floatValue.asBoolean());
        Assertions.assertEquals(List.of(floatValue),                    floatValue.asList());
        Assertions.assertEquals(Map.of(new Value("value"), floatValue), floatValue.asMap());

        //................................
        Assertions.assertEquals("420",                                   stringValue.asString());
        Assertions.assertEquals(420,                                     stringValue.asNumber());
        Assertions.assertEquals(false,                                   stringValue.asBoolean());
        Assertions.assertEquals(List.of(stringValue),                    stringValue.asList());
        Assertions.assertEquals(Map.of(new Value("value"), stringValue), stringValue.asMap());

        // Extra tests
        Assertions.assertTrue  (new Value("true").asBoolean());
        Assertions.assertEquals(0, new Value("true").asNumber());
        Assertions.assertEquals(0, new Value("true").asNumber());

        //................................
        Assertions.assertEquals("true",                                boolValue.asString());
        Assertions.assertEquals(1,                                     boolValue.asNumber());
        Assertions.assertEquals(true,                                  boolValue.asBoolean());
        Assertions.assertEquals(List.of(boolValue),                    boolValue.asList());
        Assertions.assertEquals(Map.of(new Value("value"), boolValue), boolValue.asMap());

        //................................
        Assertions.assertEquals(
            "{\"boolean\":true,\"map\":{\"string\":\"hello\",\"null\":null,\"list\":[\"number\",1]}}",
            mapValue.asString());
        Assertions.assertEquals(2,    mapValue.asNumber());
        Assertions.assertEquals(true, mapValue.asBoolean());
        Assertions.assertEquals(List.of(
            new Value(true),
            new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                new Value("string"), new Value("hello"),
                new Value("null"), new Value(),

                new Value("list"), new Value(List.of(
                    new Value("number"),
                    new Value(1)
                ))
            )))
        ), mapValue.asList());
        Assertions.assertEquals(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("boolean"), new Value(true),

            new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                new Value("string"), new Value("hello"),
                new Value("null"), new Value(),

                new Value("list"), new Value(List.of(
                    new Value("number"),
                    new Value(1)
                ))
            )))
        )), mapValue.asMap());

        // Extra tests
        Assertions.assertEquals(false, new Value(Lists.newArrayList()).asBoolean());

        //................................
        Assertions.assertEquals(
            "[true,\"hello\",\"map\",{\"null\":null,\"list\":[\"number\",1]}]",
            listValue.asString());
        Assertions.assertEquals(4,    listValue.asNumber());
        Assertions.assertEquals(true, listValue.asBoolean());
        Assertions.assertEquals(List.of(
            new Value(true),
            new Value("hello"),
            new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                new Value("null"), new Value(),

                new Value("list"), new Value(List.of(
                    new Value("number"),
                    new Value(1)
                ))
            )))
        ), listValue.asList());
        Assertions.assertEquals(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value(0), new Value(true),
            new Value(1), new Value("hello"),
            new Value(2), new Value("map"),
            new Value(3), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                new Value("null"), new Value(),

                new Value("list"), new Value(List.of(
                    new Value("number"),
                    new Value(1)
                ))
            )))
        )), listValue.asMap());

        // Extra tests
        Assertions.assertEquals(false, new Value(Maps.newHashMap()).asBoolean());

        //................................
        Assertions.assertEquals("null",    nullValue.asString());
        Assertions.assertEquals(0,         nullValue.asNumber());
        Assertions.assertEquals(false,     nullValue.asBoolean());
        Assertions.assertEquals(List.of(), nullValue.asList());
        Assertions.assertEquals(Map.of(),  nullValue.asMap());
    }

    @Test
    public void casting()
    {
        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, intValue::getBoolean);
        Assertions.assertThrowsExactly(ClassCastException.class, intValue::getString);
        Assertions.assertThrowsExactly(ClassCastException.class, intValue::getList);
        Assertions.assertThrowsExactly(ClassCastException.class, intValue::getMap);
        Assertions.assertDoesNotThrow(intValue::getNumber);

        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, floatValue::getBoolean);
        Assertions.assertThrowsExactly(ClassCastException.class, floatValue::getString);
        Assertions.assertThrowsExactly(ClassCastException.class, floatValue::getList);
        Assertions.assertThrowsExactly(ClassCastException.class, floatValue::getMap);
        Assertions.assertDoesNotThrow(floatValue::getNumber);

        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, stringValue::getBoolean);
        Assertions.assertThrowsExactly(ClassCastException.class, stringValue::getNumber);
        Assertions.assertThrowsExactly(ClassCastException.class, stringValue::getList);
        Assertions.assertThrowsExactly(ClassCastException.class, stringValue::getMap);
        Assertions.assertDoesNotThrow(stringValue::getString);

        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, boolValue::getString);
        Assertions.assertThrowsExactly(ClassCastException.class, boolValue::getNumber);
        Assertions.assertThrowsExactly(ClassCastException.class, boolValue::getList);
        Assertions.assertThrowsExactly(ClassCastException.class, boolValue::getMap);
        Assertions.assertDoesNotThrow(boolValue::getBoolean);

        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, mapValue::getString);
        Assertions.assertThrowsExactly(ClassCastException.class, mapValue::getNumber);
        Assertions.assertThrowsExactly(ClassCastException.class, mapValue::getList);
        Assertions.assertThrowsExactly(ClassCastException.class, mapValue::getBoolean);
        Assertions.assertDoesNotThrow(mapValue::getMap);

        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, listValue::getString);
        Assertions.assertThrowsExactly(ClassCastException.class, listValue::getNumber);
        Assertions.assertThrowsExactly(ClassCastException.class, listValue::getMap);
        Assertions.assertThrowsExactly(ClassCastException.class, listValue::getBoolean);
        Assertions.assertDoesNotThrow(listValue::getList);

        //................................
        Assertions.assertThrowsExactly(ClassCastException.class, nullValue::getString);
        Assertions.assertThrowsExactly(ClassCastException.class, nullValue::getNumber);
        Assertions.assertThrowsExactly(ClassCastException.class, nullValue::getMap);
        Assertions.assertThrowsExactly(ClassCastException.class, nullValue::getBoolean);
        Assertions.assertThrowsExactly(ClassCastException.class, nullValue::getList);
    }

    @Test
    public void parsing()
    {
        //................................
        Assertions.assertEquals(intValue, Value.parse("0"));
        Assertions.assertEquals(intValue, Value.parse("0.0"));
        Assertions.assertEquals(intValue, Value.fromObject(0));
        Assertions.assertEquals(intValue, Value.fromObject(0.0));

        //................................
        Assertions.assertEquals(floatValue, Value.parse("0.00001"));
        Assertions.assertEquals(floatValue, Value.parse("1e-5"));
        Assertions.assertEquals(floatValue, Value.fromObject(0.00001));
        Assertions.assertEquals(floatValue, Value.fromObject(1e-5));

        //................................
        Assertions.assertEquals(stringValue, Value.parse("\"420\""));
        Assertions.assertEquals(stringValue, Value.fromObject("420"));
        Assertions.assertNotEquals(stringValue, Value.parse("420"));

        //................................
        Assertions.assertEquals(boolValue, Value.parse("true"));
        Assertions.assertEquals(boolValue, Value.fromObject(true));
        Assertions.assertNotEquals(boolValue, Value.parse("false"));
        Assertions.assertNotEquals(boolValue, Value.fromObject(false));

        //................................
        Assertions.assertEquals(
            mapValue,
            Value.parse("{\"boolean\":true,\"map\":{\"string\":\"hello\",\"null\":null,\"list\":[\"number\",1]}}"));
        Assertions.assertEquals(
            mapValue,
            Value.fromObject(Maps.newLinkedHashMap(ImmutableMap.of(
                new Value("boolean"), new Value(true),

                new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                    new Value("string"), new Value("hello"),
                    new Value("null"), new Value(),

                    new Value("list"), new Value(List.of(
                        new Value("number"),
                        new Value(1)
                    ))
                )))
            ))));

        //................................
        Assertions.assertEquals(
            listValue,
            Value.parse("[true,\"hello\",\"map\",{\"null\":null,\"list\":[\"number\",1]}]"));
        Assertions.assertEquals(
            listValue,
            Value.fromObject(List.of(
                new Value(true),
                new Value("hello"),
                new Value("map"),
                new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                    new Value("null"), new Value(),

                    new Value("list"), new Value(List.of(
                        new Value("number"),
                        new Value(1)
                    ))
                )))
            )));

        //................................
        Assertions.assertEquals(nullValue, Value.parse("null"));
        Assertions.assertEquals(nullValue, Value.fromObject(null));
    }

    @Test
    public void types()
    {
        //................................
        Assertions.assertTrue(intValue.is(Value.Type.NUMBER));
        Assertions.assertTrue(intValue.isNumber());

        Assertions.assertFalse(intValue.isBoolean());
        Assertions.assertFalse(intValue.isString());
        Assertions.assertFalse(intValue.isList());
        Assertions.assertFalse(intValue.isMap());
        Assertions.assertFalse(intValue.isEmpty());

        //................................
        Assertions.assertTrue(floatValue.is(Value.Type.NUMBER));
        Assertions.assertTrue(floatValue.isNumber());

        Assertions.assertFalse(floatValue.isBoolean());
        Assertions.assertFalse(floatValue.isString());
        Assertions.assertFalse(floatValue.isList());
        Assertions.assertFalse(floatValue.isMap());
        Assertions.assertFalse(floatValue.isEmpty());

        //................................
        Assertions.assertTrue(stringValue.is(Value.Type.STRING));
        Assertions.assertTrue(stringValue.isString());

        Assertions.assertFalse(stringValue.isBoolean());
        Assertions.assertFalse(stringValue.isNumber());
        Assertions.assertFalse(stringValue.isList());
        Assertions.assertFalse(stringValue.isMap());
        Assertions.assertFalse(stringValue.isEmpty());

        //................................
        Assertions.assertTrue(boolValue.is(Value.Type.BOOLEAN));
        Assertions.assertTrue(boolValue.isBoolean());

        Assertions.assertFalse(boolValue.isString());
        Assertions.assertFalse(boolValue.isNumber());
        Assertions.assertFalse(boolValue.isList());
        Assertions.assertFalse(boolValue.isMap());
        Assertions.assertFalse(boolValue.isEmpty());

        //................................
        Assertions.assertTrue(mapValue.is(Value.Type.MAP));
        Assertions.assertTrue(mapValue.isMap());

        Assertions.assertFalse(mapValue.isString());
        Assertions.assertFalse(mapValue.isNumber());
        Assertions.assertFalse(mapValue.isList());
        Assertions.assertFalse(mapValue.isBoolean());
        Assertions.assertFalse(mapValue.isEmpty());

        //................................
        Assertions.assertTrue(listValue.is(Value.Type.LIST));
        Assertions.assertTrue(listValue.isList());

        Assertions.assertFalse(listValue.isString());
        Assertions.assertFalse(listValue.isNumber());
        Assertions.assertFalse(listValue.isMap());
        Assertions.assertFalse(listValue.isBoolean());
        Assertions.assertFalse(listValue.isEmpty());

        //................................
        Assertions.assertTrue(nullValue.is(Value.Type.VOID));
        Assertions.assertTrue(nullValue.isEmpty());

        Assertions.assertFalse(nullValue.isString());
        Assertions.assertFalse(nullValue.isNumber());
        Assertions.assertFalse(nullValue.isMap());
        Assertions.assertFalse(nullValue.isBoolean());
        Assertions.assertFalse(nullValue.isList());
    }

    @Test
    public void equality()
    {
        //................................
        Assertions.assertEquals(new Value(42), new Value(42.0f));
        Assertions.assertEquals(new Value(42), new Value(42.0d));
        Assertions.assertEquals(new Value(42), new Value(42.0));
        Assertions.assertEquals(new Value(42), new Value(42L));
        Assertions.assertEquals(new Value(42), new Value(42));
        Assertions.assertEquals(new Value(42), new Value(42));
        Assertions.assertEquals(new Value(42), new Value(BigInteger.valueOf(42)));
        Assertions.assertEquals(new Value(42), new Value(BigDecimal.valueOf(42)));
        Assertions.assertEquals(new Value(42), new Value(new AtomicLong(42)));
        Assertions.assertEquals(new Value(42), new Value(new AtomicInteger(42)));
        Assertions.assertEquals(new Value(42.0f), new Value(BigInteger.valueOf(42)));
        Assertions.assertEquals(new Value(42.0), new Value(BigDecimal.valueOf(42)));
        Assertions.assertEquals(new Value(42.0f), new Value(new AtomicLong(42)));
        Assertions.assertEquals(new Value(42.0), new Value(new AtomicInteger(42)));
        Assertions.assertNotEquals(new Value(42), new Value(43));
        Assertions.assertNotEquals(new Value(42), new Value(42.1f));
        Assertions.assertNotEquals(new Value(42), new Value(42.1d));
        Assertions.assertNotEquals(new Value(42), new Value(42.1));

        //................................
        final List<Value> l1 = List.of(new Value(42), new Value(43));
        final List<Value> l2 = List.of(new Value(43), new Value(42));
        final List<Value> l3 = List.of(new Value(43), new Value(42));
        Assertions.assertNotEquals(new Value(l1), new Value(l2));
        Assertions.assertEquals(new Value(l2), new Value(l3));

        //................................
        final Map<Value, Value> m1 = Map.of(new Value("a"), new Value(42), new Value("b"), new Value(43));
        final Map<Value, Value> m2 = Map.of(new Value("b"), new Value(43), new Value("a"), new Value(42));
        final Map<Value, Value> m3 = Map.of(new Value("b"), new Value(43), new Value("c"), new Value(42));
        Assertions.assertEquals(new Value(m1), new Value(m2));
        Assertions.assertNotEquals(new Value(m2), new Value(m3));
    }

    @Test
    public void packetCodec()
    {
        //................................
        final Value int_value = encodeAndDecodePacket(intValue);
        Assertions.assertEquals(intValue, int_value);

        //................................
        final Value float_value = encodeAndDecodePacket(floatValue);
        Assertions.assertEquals(floatValue, float_value);

        //................................
        final Value string_value = encodeAndDecodePacket(stringValue);
        Assertions.assertEquals(stringValue, string_value);

        //................................
        final Value boolean_value = encodeAndDecodePacket(boolValue);
        Assertions.assertEquals(boolValue, boolean_value);

        //................................
        final Value map_value = encodeAndDecodePacket(mapValue);
        Assertions.assertEquals(mapValue, map_value);

        //................................
        final Value list_value = encodeAndDecodePacket(listValue);
        Assertions.assertEquals(listValue, list_value);

        //................................
        final Value null_value = encodeAndDecodePacket(nullValue);
        Assertions.assertEquals(nullValue, null_value);

        //................................
        final ByteBuf test_buf = Unpooled.buffer();
        Value.STREAM_CODEC.encode(test_buf, floatValue);
        Value.STREAM_CODEC.encode(test_buf, boolValue);
        Value.STREAM_CODEC.encode(test_buf, nullValue);
        Value.STREAM_CODEC.encode(test_buf, intValue);
        Value.STREAM_CODEC.encode(test_buf, mapValue);
        Value.STREAM_CODEC.encode(test_buf, stringValue);
        Value.STREAM_CODEC.encode(test_buf, listValue);

        Assertions.assertEquals(floatValue,  Value.STREAM_CODEC.decode(test_buf));
        Assertions.assertEquals(boolValue,   Value.STREAM_CODEC.decode(test_buf));
        Assertions.assertEquals(nullValue,   Value.STREAM_CODEC.decode(test_buf));
        Assertions.assertEquals(intValue,    Value.STREAM_CODEC.decode(test_buf));
        Assertions.assertEquals(mapValue,    Value.STREAM_CODEC.decode(test_buf));
        Assertions.assertEquals(stringValue, Value.STREAM_CODEC.decode(test_buf));
        Assertions.assertEquals(listValue,   Value.STREAM_CODEC.decode(test_buf));
    }

    @Test
    void codec()
    {
        {
            //................................
            final Value int_value = encodeAndDecodeDfu(intValue, JsonOps.INSTANCE);
            Assertions.assertEquals(intValue, int_value);

            //................................
            final Value float_value = encodeAndDecodeDfu(floatValue, JsonOps.INSTANCE);
            Assertions.assertEquals(floatValue, float_value);

            //................................
            final Value string_value = encodeAndDecodeDfu(stringValue, JsonOps.INSTANCE);
            Assertions.assertEquals(stringValue, string_value);

            //................................
            final Value boolean_value = encodeAndDecodeDfu(boolValue, JsonOps.INSTANCE);
            Assertions.assertEquals(boolValue, boolean_value);

            //................................
            final Value map_value = encodeAndDecodeDfu(mapValue, JsonOps.INSTANCE);
            Assertions.assertEquals(mapValue, map_value);

            //................................
            final Value list_value = encodeAndDecodeDfu(listValue, JsonOps.INSTANCE);
            Assertions.assertEquals(listValue, list_value);

            //................................
            final Value null_value = encodeAndDecodeDfu(nullValue, JsonOps.INSTANCE);
            Assertions.assertEquals(nullValue, null_value);
        }

        {
            //................................
            final Value int_value = encodeAndDecodeDfu(intValue, NbtOps.INSTANCE);
            Assertions.assertEquals(intValue, int_value);

            //................................
            final Value float_value = encodeAndDecodeDfu(floatValue, NbtOps.INSTANCE);
            Assertions.assertEquals(floatValue, float_value);

            //................................
            final Value string_value = encodeAndDecodeDfu(stringValue, NbtOps.INSTANCE);
            Assertions.assertEquals(stringValue, string_value);

            //................................
            final Value boolean_value = encodeAndDecodeDfu(boolValue, NbtOps.INSTANCE);

            // Here we will have a problem decoding, as NBT won't be able to distinguish between bool and byte
            Assertions.assertEquals(boolValue.asNumber().byteValue(), boolean_value.asNumber().byteValue());

            //................................
            final Value map_value = encodeAndDecodeDfu(mapValue, NbtOps.INSTANCE);
            Assertions.assertEquals(new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                new Value("boolean"), new Value(1), // boolean in NBT becomes byte

                new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                    new Value("string"), new Value("hello"),
                    new Value("null"), new Value(),

                    new Value("list"), new Value(List.of(
                        new Value("number"),
                        new Value(1)
                    ))
                )))
            ))), map_value);

            //................................
            final Value list_value = encodeAndDecodeDfu(listValue, NbtOps.INSTANCE);
            Assertions.assertEquals(new Value(List.of(
                new Value(1), // boolean in NBT becomes byte
                new Value("hello"),
                new Value("map"),
                new Value(Maps.newLinkedHashMap(ImmutableMap.of(
                    new Value("null"), new Value(),

                    new Value("list"), new Value(List.of(
                        new Value("number"),
                        new Value(1)
                    ))
                )))
            )), list_value);

            //................................
            final Value null_value = encodeAndDecodeDfu(nullValue, NbtOps.INSTANCE);
            Assertions.assertEquals(nullValue, null_value);
        }
    }
}
