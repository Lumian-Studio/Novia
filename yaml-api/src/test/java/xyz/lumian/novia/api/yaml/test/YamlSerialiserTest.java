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
package xyz.lumian.novia.api.yaml.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.nodes.Node;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.yaml.YamlOps;
import xyz.lumian.novia.api.yaml.YamlOpsSerialiser;

import java.nio.ByteBuffer;
import java.util.*;



//**********************************************************************************************************************
public class YamlSerialiserTest
{
    //******************************************************************************************************************
    private record TestDocument(
        String  app_name,
        String  version,
        String  environment,
        int     port,
        float   pi_approximation,
        boolean is_enabled,
        
        @Nullable
        Integer maintenance_window,
        
        String description_folded,
        String description_literal,
        
        ServerConfig production_server,
        ServerConfig staging_server,
        
        TypedValues typed_values,
        
        List<String> block_list,
        List<String> flow_list,
        
        Map<String, Database> block_map,
        Map<String, String>   flow_map
    )
    {
        //**************************************************************************************************************
        public record ServerConfig(
            String  host,
            String  protocol,
            int     timeout_ms,
            int     retries,
            boolean debug
        )
        {
            //**********************************************************************************************************
            public static final Codec<ServerConfig> CODEC = RecordCodecBuilder.create(i -> i
                .group(
                    Codec.STRING
                        .fieldOf("host")
                        .forGetter(ServerConfig::host),
                    Codec.STRING
                        .fieldOf("protocol")
                        .forGetter(ServerConfig::protocol),
                    Codec.INT
                        .fieldOf("timeout_ms")
                        .forGetter(ServerConfig::timeout_ms),
                    Codec.INT
                        .fieldOf("retries")
                        .forGetter(ServerConfig::retries),
                    Codec.BOOL
                        .fieldOf("debug")
                        .forGetter(ServerConfig::debug))
                .apply(i, ServerConfig::new));
        }
        
        public record TypedValues(
            String  explicit_string,
            int     explicit_integer,
            float   explicit_float,
            boolean explicit_boolean,
            
            Optional<String> explicit_null,
            
            ByteBuffer explicit_binary
        )
        {
            //**********************************************************************************************************
            public static final Codec<TypedValues> CODEC = RecordCodecBuilder.create(i -> i
                .group(
                    Codec.STRING
                        .fieldOf("explicit_string")
                        .forGetter(TypedValues::explicit_string),
                    Codec.INT
                        .fieldOf("explicit_integer")
                        .forGetter(TypedValues::explicit_integer),
                    Codec.FLOAT
                        .fieldOf("explicit_float")
                        .forGetter(TypedValues::explicit_float),
                    Codec.BOOL
                        .fieldOf("explicit_boolean")
                        .forGetter(TypedValues::explicit_boolean),
                    Codec.STRING
                        .optionalFieldOf("explicit_null")
                        .forGetter(TypedValues::explicit_null),
                    Codec.BYTE_BUFFER
                        .fieldOf("explicit_binary")
                        .forGetter(TypedValues::explicit_binary))
                .apply(i, TypedValues::new));
        }
        
        public record Database(
            String host,
            int    port
        )
        {
            //**********************************************************************************************************
            public static final Codec<Database> CODEC = RecordCodecBuilder.create(i -> i
                .group(
                    Codec.STRING
                        .fieldOf("host")
                        .forGetter(Database::host),
                    Codec.INT
                        .fieldOf("port")
                        .forGetter(Database::port))
                .apply(i, Database::new));
        }
        
        //**************************************************************************************************************
        public static final Integer DEFAULT_MAINTENANCE_WINDOW = 12;
        
        public static final Codec<TestDocument> CODEC = RecordCodecBuilder.create(i -> i
            .group(
                Codec.STRING
                    .fieldOf("app_name")
                    .forGetter(TestDocument::app_name),
                Codec.STRING
                    .fieldOf("version")
                    .forGetter(TestDocument::version),
                Codec.STRING
                    .fieldOf("environment")
                    .forGetter(TestDocument::environment),
                Codec.INT
                    .fieldOf("port")
                    .forGetter(TestDocument::port),
                Codec.FLOAT
                    .fieldOf("pi_approximation")
                    .forGetter(TestDocument::pi_approximation),
                Codec.BOOL
                    .fieldOf("is_enabled")
                    .forGetter(TestDocument::is_enabled),
                Codec.INT
                    .optionalFieldOf("maintenance_window", DEFAULT_MAINTENANCE_WINDOW)
                    .forGetter(TestDocument::maintenance_window),
                
                Codec.STRING
                    .fieldOf("description_folded")
                    .forGetter(TestDocument::description_folded),
                Codec.STRING
                    .fieldOf("description_literal")
                    .forGetter(TestDocument::description_literal),
                
                ServerConfig.CODEC
                    .fieldOf("production_server")
                    .forGetter(TestDocument::production_server),
                ServerConfig.CODEC
                    .fieldOf("staging_server")
                    .forGetter(TestDocument::staging_server),
                
                TypedValues.CODEC
                    .fieldOf("typed_values")
                    .forGetter(TestDocument::typed_values),
                
                Codec.list(Codec.STRING)
                    .fieldOf("block_list")
                    .forGetter(TestDocument::block_list),
                Codec.list(Codec.STRING)
                    .fieldOf("flow_list")
                    .forGetter(TestDocument::flow_list),
                
                Codec.unboundedMap(Codec.STRING, Database.CODEC)
                    .fieldOf("block_map")
                    .forGetter(TestDocument::block_map),
                
                Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .fieldOf("flow_map")
                    .forGetter(TestDocument::flow_map))
            .apply(i, TestDocument::new));
    }
    
    //******************************************************************************************************************
    private final Value intValue    = new Value(0);
    private final Value floatValue  = new Value(0.00001f);
    private final Value stringValue = new Value("420");
    private final Value boolValue   = Value.TRUE;

    private final Value mapValue = new Value(Maps.newLinkedHashMap(ImmutableMap.of(
        new Value("boolean"), new Value(true),

        new Value("map"), new Value(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("string"), new Value("hello"),
            new Value("null"), new Value(),

            new Value("list"), new Value(List.of(
                new Value("number"),
                new Value(1)))))))));

    private final Value listValue = new Value(List.of(
        new Value(true),
        new Value("hello"),
        new Value("map"),
        new Value(Maps.newLinkedHashMap(ImmutableMap.of(
            new Value("null"), new Value(),

            new Value("list"), new Value(List.of(
                new Value("number"),
                new Value(1))))))));
    
    private final Value all = new Value(ImmutableMap.of(
        new Value("int"),    intValue,
        new Value("float"),  floatValue,
        new Value("string"), stringValue,
        new Value("bool"),   boolValue,
        new Value("map"),    mapValue,
        new Value("list"),   listValue,
        new Value("null"),   Value.EMPTY));
    
    //******************************************************************************************************************
    @Test
    void serialise()
    {
        final DataResult<Node> node = Value.CODEC.encodeStart(YamlOps.INSTANCE, this.all);
        Assertions.assertTrue(node.isSuccess(), () -> node.error().orElseThrow().message());
        Assertions.assertDoesNotThrow(() -> YamlOpsSerialiser.INSTANCE.serialiseString(node.getOrThrow()));
    }
    
    @Test
    void deserialise()
    {
        @Language("YAML")
        final String document = """
            # 1. Comments & Basic Scalar Data Types
            app_name: "YAML Feature Explorer"     # String (double quoted)
            version: '1.2.0'                      # String (single quoted)
            environment: production               # Plain string
            port: 8080                            # Integer
            pi_approximation: 3.14159             # Floating-point number
            is_enabled: true                      # Boolean (true/false/yes/no/on/off)
            maintenance_window: null              # Null / Empty value
            
            # 2. Multi-line Strings
            description_folded: >
              This is a folded multi-line string.
              Newlines are converted to spaces
              unless blank lines separate them.
            
            description_literal: |
              Line 1: Exact formatting
              Line 2: Preserves newlines
                Indentation is kept intact.
            
            # 3. Anchors (&), Aliases (*), and Merge Keys (<<)
            default_server_config:
              protocol: &proto https
              timeout_ms: &timeout 5000
              retries: &retries 3
              debug: &debug false
            
            production_server:
              host: "://example.com"
              protocol: *proto
              timeout_ms: *timeout
              retries: *retries
              debug: false
            
            staging_server:
              host: "://example.com"
              protocol: *proto
              timeout_ms: *timeout
              retries: 7
              debug: true
            
            # 4. Explicit Type Tags (!!)
            typed_values:
              explicit_string: !!str 12345
              explicit_integer: !!int "42"
              explicit_float: !!float "3.14"
              explicit_boolean: !!bool "true"
              explicit_null: !!null ""
              explicit_binary: !!binary |
                R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7
            
            # 5. Lists / Sequences (Block vs. Flow Style)
            block_list:
              - Alpha
              - Beta
              - Gamma
            
            flow_list: [red, green, blue]
            
            # 6. Maps / Dictionaries (Block vs. Flow Style)
            block_map:
              database:
                host: localhost
                port: 5432
              cache:
                host: redis.local
                port: 6379
            
            flow_map: { cpu: "8-core", ram: "16GB", storage: "1TB NVMe" }
            """;
        
        final DataResult<TestDocument> res = YamlOpsSerialiser.INSTANCE.decodeString(document, TestDocument.CODEC);
        
        if (res.isError())
        {
            System.out.println(res.error().orElseThrow().message());
        }
        
        Assertions.assertTrue(res.isSuccess());
        
        final TestDocument doc = res.getOrThrow();
        
        Assertions.assertEquals("YAML Feature Explorer", doc.app_name);
        Assertions.assertEquals("1.2.0",                 doc.version);
        Assertions.assertEquals("production",            doc.environment);
        Assertions.assertEquals(8080,                    doc.port);
        Assertions.assertEquals(3.14159,                 doc.pi_approximation, Math.ulp(1.0f));
        Assertions.assertTrue  (doc.is_enabled);
        Assertions.assertEquals(TestDocument.DEFAULT_MAINTENANCE_WINDOW, doc.maintenance_window);
        
        Assertions.assertEquals("This is a folded multi-line string. Newlines are converted to spaces unless blank lines separate them.\n", doc.description_folded);
        Assertions.assertEquals("Line 1: Exact formatting\nLine 2: Preserves newlines\n  Indentation is kept intact.\n", doc.description_literal);
        
        Assertions.assertEquals("://example.com", doc.production_server.host);
        Assertions.assertEquals("https",          doc.production_server.protocol);
        Assertions.assertEquals(5000,             doc.production_server.timeout_ms);
        Assertions.assertEquals(3,                doc.production_server.retries);
        Assertions.assertFalse(doc.production_server.debug);
        Assertions.assertEquals("://example.com", doc.staging_server.host);
        Assertions.assertEquals("https",          doc.production_server.protocol);
        Assertions.assertEquals(5000,             doc.production_server.timeout_ms);
        Assertions.assertEquals(3,                doc.production_server.retries);
        Assertions.assertTrue(doc.staging_server.debug);
        
        Assertions.assertEquals("12345", doc.typed_values.explicit_string);
        Assertions.assertEquals(42,      doc.typed_values.explicit_integer);
        Assertions.assertEquals(3.14f,   doc.typed_values.explicit_float, Math.ulp(1.0f));
        Assertions.assertTrue(doc.typed_values.explicit_boolean);
        Assertions.assertTrue(doc.typed_values.explicit_null.isEmpty());
        
        final ByteBuffer test_bytes = ByteBuffer.wrap(new byte[] {
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00,
            0x01, 0x00, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x21, (byte) 0xf9, 0x04, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x01, 0x44,
            0x00, 0x3b
        });
        
        Assertions.assertEquals(test_bytes, doc.typed_values.explicit_binary);
        
        Assertions.assertIterableEquals(List.of("Alpha", "Beta", "Gamma"), doc.block_list);
        Assertions.assertIterableEquals(List.of("red", "green", "blue"), doc.flow_list);
        
        final TestDocument.Database database = new TestDocument.Database("localhost",   5432);
        final TestDocument.Database cache    = new TestDocument.Database("redis.local", 6379);
        
        Assertions.assertTrue(Maps
            .difference(
                Map.of("database", database, "cache", cache),
                doc.block_map)
            .areEqual());
        Assertions.assertTrue(Maps
            .difference(
                Map.of("cpu", "8-core", "ram", "16GB", "storage", "1TB NVMe"),
                doc.flow_map)
            .areEqual());
    }
}
