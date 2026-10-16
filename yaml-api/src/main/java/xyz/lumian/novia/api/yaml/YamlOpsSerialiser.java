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

import com.mojang.serialization.DynamicOps;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.Node;
import xyz.lumian.novia.api.core.serialisation.dynops.ConfigSerialisationException;
import xyz.lumian.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;



//**********************************************************************************************************************
public class YamlOpsSerialiser
    extends DynamicOpsSerialisers.TextSerialiser<Node>
{
    //******************************************************************************************************************
    public static final YamlOpsSerialiser INSTANCE;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final LoadSettings LOADER;
    private static final DumpSettings DUMPER;
    
    //==================================================================================================================
    static
    {
        INSTANCE = new YamlOpsSerialiser();
        LOADER   = LoadSettings
            .builder()
            .build();
        DUMPER   = DumpSettings
            .builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDereferenceAliases(true)
            .build();
        
        DynamicOpsSerialisers.register(YamlOps.class, INSTANCE);
    }
    
    //******************************************************************************************************************
    public static void initialize() {}
    
    //******************************************************************************************************************
    @Override public String[]         getFileExtensions() { return new String[] { "yaml", "yml" }; }
    @Override public DynamicOps<Node> getOps()            { return YamlOps.INSTANCE; }
    
    //==================================================================================================================
    @Override
    public Node deserialiseString(final String input)
        throws ConfigSerialisationException
    {
        final Node node = YamlHelper.parse(input, LOADER);
        
        if (node == null)
        {
            throw new ConfigSerialisationException("Could not parse YAML document");
        }
        
        return node;
    }
    
    @Override public String serialiseString(final Node input) { return YamlHelper.serialiseToString(input, DUMPER); }
}
