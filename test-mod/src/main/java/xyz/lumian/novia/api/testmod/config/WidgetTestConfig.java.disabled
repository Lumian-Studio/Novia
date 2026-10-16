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
package xyz.lumialights.novia.test.mod.config;

import net.minecraft.util.Identifier;
import xyz.lumialights.novia.api.config.property.Property;
import xyz.lumialights.novia.api.config.schema.factory.SchemaFactory;
import xyz.lumialights.novia.api.config.spec.ConfigSpec;
import xyz.lumialights.novia.api.config.spec.builder.ConfigSpecBuilder;
import xyz.lumialights.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public record WidgetTestConfig(
    Property string,
    Property number,
    Property bool,
    Property bool2,
    Property bool3,
    Property nil,
    Property list,
    Property map,
    
    Property intRange,
    Property constant
)
{
    //******************************************************************************************************************
    public static final ConfigSpec<WidgetTestConfig> SPEC
        = (new ConfigSpecBuilder<>(Identifier.of("novia-test-mod:widget-test-config"), WidgetTestConfig.class))
            .withGroup("primitives", bg -> bg
                .withProperty("string",   WidgetTestConfig::string)
                .withProperty("number",   WidgetTestConfig::number)
                .withProperty("bool",     WidgetTestConfig::bool)
                .withProperty("bool2",    WidgetTestConfig::bool2)
                .withProperty("bool3",    WidgetTestConfig::bool3)
                .withProperty("nil",      WidgetTestConfig::nil)
                .withProperty("list",     WidgetTestConfig::list)
                .withProperty("map",      WidgetTestConfig::map))
            .withGroup("advanced", bg -> bg
                .withProperty("intRange", WidgetTestConfig::intRange, bp -> bp
                    .withSchema(SchemaFactory.rangeSchema(32, 99)))
                .withProperty("constant", WidgetTestConfig::constant))
            .build(WidgetTestConfig::new);
    
    //******************************************************************************************************************
    public WidgetTestConfig()
    {
        this(
            Property.create("Hello world!"),
            Property.create(400),
            Property.create(true),
            Property.create(false),
            Property.create(true),
            Property.create(),
            Property.create(List.of()),
            Property.create(Map.of()),
            
            Property.create(42),
            Property.create(Map.of(
                new Value("hello_prop"), new Value("some_property"),
                new Value("and_another"), new Value(List.of(
                    new Value("with_strings_and"),
                    new Value(420),
                    Value.TRUE))
            )));
    }
}
