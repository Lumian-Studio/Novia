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
package xyz.lumian.novia.api.yaml;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;
import xyz.lumian.novia.api.core.util.NumberType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public class YamlOps
    implements DynamicOps<Node>
{
    //******************************************************************************************************************
    public static final YamlOps INSTANCE = new YamlOps();
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Node EMPTY = new ScalarNode(
        Tag.NULL,
        false,
        "null",
        ScalarStyle.PLAIN,
        Optional.empty(),
        Optional.empty());
    
    //******************************************************************************************************************
    @Override public Node empty() { return EMPTY; }

    //==================================================================================================================
    @Override
    public <U> U convertTo(final DynamicOps<U> outOps, final Node input)
    {
        return switch (input)
        {
            case MappingNode  node -> convertMap (outOps, node);
            case SequenceNode node -> convertList(outOps, node);

            case ScalarNode node ->
            {
                final Tag tag = node.getTag();

                if      (tag.equals(Tag.NULL))  yield outOps.empty();
                else if (tag.equals(Tag.STR))   yield outOps.createString(node.getValue());
                else if (tag.equals(Tag.BOOL))  yield outOps.createBoolean(node.getValue().equalsIgnoreCase("true"));
                else if (tag.equals(Tag.FLOAT)) yield outOps.createDouble(Double.parseDouble(node.getValue()));
                else if (tag.equals(Tag.INT))
                {
                    final BigInteger value = new BigInteger(node.getValue());
                    final long       l     = value.longValue();
                    
                    yield ((((int) l) == l) ? outOps.createInt((int) l) : outOps.createLong(l));
                }
                else if (tag.equals(Tag.BINARY))
                {
                    final ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(node.getValue()));
                    yield outOps.createByteList(buffer);
                }
                else
                {
                    throw new UnsupportedOperationException("Unsupported scalar tag: " + tag.getValue());
                }
            }

            default -> throw new UnsupportedOperationException("Unsupported node type: " + input.getNodeType().name());
        };
    }

    //==================================================================================================================
    @Override
    public DataResult<Number> getNumberValue(final Node input)
    {
        if (input instanceof ScalarNode node)
        {
            if (input.getTag().equals(Tag.INT))
            {
                try
                {
                    final long l = Long.parseLong(node.getValue());
                    return DataResult.success((((int) l) == l) ? (int) l : l);
                }
                catch (final NumberFormatException ex)
                {
                    return DataResult.success(new BigInteger(node.getValue()).longValue());
                }
            }
            else if (input.getTag().equals(Tag.FLOAT))
            {
                try
                {
                    return DataResult.success(Double.parseDouble(node.getValue()));
                }
                catch (final NumberFormatException ex)
                {
                    return DataResult.success(new BigDecimal(node.getValue()).doubleValue());
                }
            }
        }

        return DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public DataResult<String> getStringValue(final Node input)
    {
        return (input.getTag().equals(Tag.STR)
                ? DataResult.success(((ScalarNode) input).getValue())
                : DataResult.error(() -> "Not a number: " + input));
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final Node input)
    {
        if (input.getTag().equals(Tag.BOOL))
        {
            return DataResult.success(((ScalarNode) input).getValue().equalsIgnoreCase("true"));
        }

        return DataResult.error(() -> "Not a boolean: " + input);
    }

    @Override
    public DataResult<Stream<Pair<Node, Node>>> getMapValues(final Node input)
    {
        if (input instanceof MappingNode node)
        {
            return DataResult.success(node.getValue()
                .stream()
                .map(e -> Pair.of(e.getKeyNode(), e.getValueNode())));
        }

        return DataResult.error(() -> "Not a YAML dictionary: " + input);
    }

    @Override
    public DataResult<Consumer<BiConsumer<Node, Node>>> getMapEntries(final Node input)
    {
        if (!(input instanceof MappingNode node))
        {
            return DataResult.error(() -> "Not a YAML dictionary: " + input);
        }

        return DataResult.success(c -> node.getValue()
            .forEach(entry -> c.accept(entry.getKeyNode(), entry.getValueNode())));
    }

    @Override
    public DataResult<MapLike<Node>> getMap(final Node input)
    {
        if (!(input instanceof MappingNode node))
        {
            return DataResult.error(() -> "Not a YAML dictionary: " + input);
        }

        final Map<Node, Node> node_map = node.getValue()
            .stream()
            .collect(Collectors.toMap(NodeTuple::getKeyNode, NodeTuple::getValueNode));
        return DataResult.success(new MapLike<>()
        {
            //**********************************************************************************************************
            @Override
            public @Nullable Node get(final @Nullable Node key)
            {
                return YamlOps.this.getNodeOrNull(node_map.get(key));
            }

            @Override
            public @Nullable Node get(final @Nullable String key)
            {
                for (final var entry : node_map.entrySet())
                {
                    final Node key_node = entry.getKey();

                    if ((key_node instanceof ScalarNode scalar) && scalar.getValue().equals(key))
                    {
                        return YamlOps.this.getNodeOrNull(entry.getValue());
                    }
                }

                return null;
            }

            //==========================================================================================================
            @Override
            public Stream<Pair<Node, Node>> entries()
            {
                return node_map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
            }

            //==========================================================================================================
            @Override
            public String toString() { return "MapLike[" + node + "]"; }
        });
    }

    @Override
    public DataResult<Stream<Node>> getStream(final Node input)
    {
        if (input instanceof SequenceNode node)
        {
            return DataResult.success(node.getValue().stream());
        }

        return DataResult.error(() -> "Not a YAML sequence: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<Node>>> getList(Node input)
    {
        if (!(input instanceof SequenceNode node))
        {
            return DataResult.error(() -> "Not a YAML sequence: " + input);
        }

        return DataResult.success(node.getValue()::forEach);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(final Node input)
    {
        if (input.getTag().equals(Tag.BINARY))
        {
            return DataResult.success(ByteBuffer.wrap(Base64.getMimeDecoder().decode(((ScalarNode) input).getValue())));
        }

        return DataResult.error(() -> "Not a YAML binary: " + input);
    }

    //------------------------------------------------------------------------------------------------------------------
    private @Nullable Node getNodeOrNull(final @Nullable Node node)
    {
        return ((node == null || node.getTag().equals(Tag.NULL)) ? null : node);
    }

    //==================================================================================================================
    @Override
    public Node createNumeric(final Number i)
    {
        if (NumberType.isFloatingPoint(i))
        {
            return new ScalarNode(Tag.FLOAT, String.format("%f", i.doubleValue()), ScalarStyle.PLAIN);
        }
        
        return new ScalarNode(Tag.INT, i.toString(), ScalarStyle.PLAIN);
    }

    @Override public Node createString(final String value) { return new ScalarNode(Tag.STR, value, ScalarStyle.PLAIN); }

    @Override
    public Node createMap(final Stream<Pair<Node, Node>> map)
    {
        final List<NodeTuple> nodes = map.map(e -> new NodeTuple(e.getFirst(), e.getSecond())).toList();
        return new MappingNode(Tag.MAP, nodes, FlowStyle.BLOCK);
    }

    @Override
    public Node createList(final Stream<Node> input)
    {
        return new SequenceNode(Tag.SEQ, input.toList(), FlowStyle.BLOCK);
    }

    @Override
    public Node createBoolean(final boolean value)
    {
        return new ScalarNode(Tag.BOOL, Boolean.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public Node createByteList(final ByteBuffer input)
    {
        return new ScalarNode(Tag.BINARY, Base64.getEncoder().encodeToString(input.array()), ScalarStyle.PLAIN);
    }

    //==================================================================================================================
    @Override
    public DataResult<Node> mergeToList(final Node list, final Node value)
    {
        if (list.getTag().equals(Tag.NULL))
        {
            return DataResult.success(createList(Stream.of(value)));
        }

        if (!(list instanceof SequenceNode seq_node))
        {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        return DataResult.success(createList(Stream.concat(seq_node.getValue().stream(), Stream.of(value))));
    }

    @Override
    public DataResult<Node> mergeToList(final Node list, final List<Node> values)
    {
        if (list.getTag().equals(Tag.NULL))
        {
            return DataResult.success(createList(values.stream()));
        }

        if (!(list instanceof SequenceNode seq_node))
        {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        return DataResult.success(createList(Stream.concat(seq_node.getValue().stream(), values.stream())));
    }

    @Override
    public DataResult<Node> mergeToMap(final Node map, final Node key, final Node value)
    {
        if (map.getTag().equals(Tag.NULL))
        {
            return DataResult.success(createMap(Stream.of(new Pair<>(key, value))));
        }

        if (!(map instanceof MappingNode map_node))
        {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        return DataResult.success(createMap(Stream
            .concat(
                map_node.getValue().stream().map(e -> Pair.of(e.getKeyNode(), e.getValueNode())),
                Stream.of(new Pair<>(key, value)))));
    }

    @Override
    public DataResult<Node> mergeToMap(final Node map, final MapLike<Node> values)
    {
        if (map.getTag().equals(Tag.NULL))
        {
            return DataResult.success(createMap(values.entries()));
        }

        if (!(map instanceof MappingNode map_node))
        {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        return DataResult.success(this.createMap(Stream
            .concat(
                map_node.getValue().stream().map(e -> Pair.of(e.getKeyNode(), e.getValueNode())),
                values.entries())));
    }

    //==================================================================================================================
    @Override
    public Node remove(final Node input, final String key)
    {
        if (input instanceof MappingNode map_node)
        {
            return this.createMap(map_node.getValue()
                .stream()
                .map(e -> Pair.of(e.getKeyNode(), e.getValueNode()))
                .filter(p -> (!(p.getFirst() instanceof ScalarNode scalar) || !scalar.getValue().equals(key))));
        }

        return input;
    }

    //==================================================================================================================
    @Override
    public ListBuilder<Node> listBuilder()
    {
        return new SequenceBuilder();
    }

    private static final class SequenceBuilder
        implements ListBuilder<Node>
    {
        //**************************************************************************************************************
        private DataResult<SequenceNode> builder = DataResult.success(
            new SequenceNode(Tag.SEQ, Lists.newArrayList(), FlowStyle.BLOCK),
            Lifecycle.stable());

        //**************************************************************************************************************
        @Override
        public DynamicOps<Node> ops() { return INSTANCE; }

        //==============================================================================================================
        @Override
        public ListBuilder<Node> add(final Node value)
        {
            builder = builder.map(b -> { b.getValue().add(value); return b; });
            return this;
        }

        @Override
        public ListBuilder<Node> add(final DataResult<Node> value)
        {
            builder = builder.apply2stable((b, element) -> { b.getValue().add(element); return b; }, value);
            return this;
        }

        //==============================================================================================================
        @Override
        public ListBuilder<Node> withErrorsFrom(final DataResult<?> result)
        {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<Node> mapError(final UnaryOperator<String> onError)
        {
            builder = builder.mapError(onError);
            return this;
        }

        //==============================================================================================================
        @Override
        public DataResult<Node> build(final Node prefix)
        {
            final DataResult<Node> result = builder.flatMap(b ->
            {
                if (prefix.getTag().equals(Tag.NULL))
                {
                    return DataResult.success(b, Lifecycle.stable());
                }

                if (!(prefix instanceof SequenceNode seq_node))
                {
                    return DataResult.error((() -> "Cannot append a list to not a list: " + prefix), prefix);
                }

                final List<Node> seq = Stream.concat(seq_node.getValue().stream(), b.getValue().stream()).toList();
                return DataResult.success(
                    new SequenceNode(Tag.SEQ, seq, FlowStyle.BLOCK),
                    Lifecycle.stable());
            });

            builder = DataResult.success(
                new SequenceNode(Tag.SEQ, Lists.newArrayList(), FlowStyle.BLOCK),
                Lifecycle.stable());

            return result;
        }
    }

    @Override
    public RecordBuilder<Node> mapBuilder()
    {
        return new YamlRecordBuilder();
    }

    private class YamlRecordBuilder
        extends RecordBuilder.AbstractStringBuilder<Node, MappingNode>
    {
        //**************************************************************************************************************
        protected YamlRecordBuilder()
        {
            super(YamlOps.this);
        }

        //==============================================================================================================
        @Override
        protected MappingNode initBuilder()
        {
            return new MappingNode(Tag.MAP, Lists.newArrayList(), FlowStyle.BLOCK);
        }

        //==============================================================================================================
        @Override
        protected MappingNode append(final String key, final Node value, final MappingNode builder)
        {
            builder.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, key, ScalarStyle.PLAIN), value));
            return builder;
        }

        //==============================================================================================================
        @Override
        protected DataResult<Node> build(final MappingNode builder, final Node prefix)
        {
            if (prefix.getTag().equals(Tag.NULL))
            {
                return DataResult.success(builder);
            }

            if (prefix instanceof MappingNode map_node)
            {
                return DataResult.success(new MappingNode(
                    Tag.MAP,
                    Stream.concat(map_node.getValue().stream(), builder.getValue().stream()).toList(),
                    FlowStyle.BLOCK));
            }

            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }

    //==================================================================================================================
    @Override public String toString() { return "YAML"; }
}
