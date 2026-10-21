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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.*;



//**********************************************************************************************************************
public record ScreenDocument(List<TabDefinition> tabs, Optional<Component> title)
{
    //******************************************************************************************************************
    public static final String        DOCUMENT_FILE_PATTERN = "%s.config.screen.json";
    public static final DynamicOps<?> DOCUMENT_OPS          = JsonOps.INSTANCE;
    
    //******************************************************************************************************************
    @SuppressWarnings("unchecked")
    public static Codec<Either<ScreenDocument, Class<? extends Screen>>> createCodec()
    {
        return Codec.either(
            RecordCodecBuilder.create(instance -> instance
                .group(
                    TabDefinition.CODEC
                        .listOf()
                        .fieldOf("tabs")
                        .forGetter(ScreenDocument::tabs),
                    ComponentSerialization.CODEC
                        .optionalFieldOf("title")
                        .forGetter(ScreenDocument::title))
                .apply(instance, ScreenDocument::new)),
            Codec.STRING.xmap(
                (str ->
                {
                    try
                    {
                        final Class<?> clazz = Class.forName(str);

                        if (!Screen.class.isAssignableFrom(clazz))
                        {
                            throw new RuntimeException(
                                "the given config class string %s is not a valid screen class".formatted(str));
                        }

                        return (Class<? extends Screen>) clazz;
                    }
                    catch (final ClassNotFoundException ex)
                    {
                        throw new RuntimeException(
                            "the given config class string %s is not a valid screen class: %s".formatted(str, ex));
                    }
                }),
                Class::getName
            ));
    }
}
