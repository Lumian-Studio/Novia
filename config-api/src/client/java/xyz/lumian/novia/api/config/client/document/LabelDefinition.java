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
package xyz.lumian.novia.api.config.client.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import xyz.lumian.novia.api.core.serialisation.Value;

import java.util.*;



//**********************************************************************************************************************
public record LabelDefinition(Value value, Component label)
{
    //******************************************************************************************************************
    public static final Codec<Map<Value, Component>> CODEC;
    
    //------------------------------------------------------------------------------------------------------------------
    private static final Codec<LabelDefinition> STRUCT_CODEC;
    
    //==================================================================================================================
    static
    {
        STRUCT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                Value.CODEC
                    .fieldOf("value")
                    .forGetter(LabelDefinition::value),
                ComponentSerialization.CODEC
                    .fieldOf("label")
                    .forGetter(LabelDefinition::label))
            .apply(instance, LabelDefinition::new));
        
        CODEC = STRUCT_CODEC
            .listOf()
            .xmap(
                (l -> l
                    .stream()
                    .collect(ImmutableMap.toImmutableMap(LabelDefinition::value, LabelDefinition::label))),
                (m -> m.entrySet()
                    .stream()
                    .map(e -> new LabelDefinition(e.getKey(), e.getValue()))
                    .collect(ImmutableList.toImmutableList())));
    }
}
