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
package xyz.lumian.novia.api.config.spec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import org.intellij.lang.annotations.Subst;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.Value;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.Map;
import java.util.stream.Collectors;



//**********************************************************************************************************************
public record GroupSpec<Container>(
    JsonPointer                         pointer,
    Map<String, IConfigSpec<Container>> subSpecs
) implements IConfigSpec<Container>
{
    //******************************************************************************************************************
    public Map<String, GroupSpec<Container>> getChildGroups()
    {
        return this.subSpecs.entrySet().stream()
            .filter(e -> (e.getValue() instanceof GroupSpec<Container>))
            .collect(Collectors.toMap(Map.Entry::getKey, (e -> (GroupSpec<Container>) e.getValue())));
    }

    public Map<String, PropertySpec<?, Container>> getChildProperties()
    {
        return this.subSpecs.entrySet().stream()
            .filter(e -> (e.getValue() instanceof PropertySpec))
            .collect(Collectors.toMap(Map.Entry::getKey, (e -> (PropertySpec<?, Container>) e.getValue())));
    }

    //==================================================================================================================
    @Override public JsonPointer getPointer() { return this.pointer; }
    
    //==================================================================================================================
    @Override
    public <T> DataResult<T> decode(final Container context, final DynamicOps<T> ops, final T input)
    {
        final DataResult<MapLike<T>> result = ops.getMap(input);

        if (result.isError())
        {
            return DataResult.error((() -> String.format("element '%s' is not a map", this.pointer)), input);
        }

        try
        {
            result.getOrThrow().entries().forEach(entry ->
            {
                @Subst("/pointer")
                final String key = Value.CODEC.decode(ops, entry.getFirst()).getOrThrow().getFirst().asString();
                final IConfigSpec<Container> sub_spec = this.subSpecs.get(key);

                if (sub_spec == null)
                {
                    Novia.LOGGER.warn("found redundant property '{}'", this.pointer.resolve(key));
                    return;
                }

                final DataResult<T> sub_result = sub_spec.decode(context, ops, entry.getSecond());
                sub_result.error().ifPresent(err -> { throw new IllegalStateException(err.message()); });
            });
        }
        catch (final IllegalStateException ex)
        {
            return DataResult.error(ex::getMessage, input);
        }
        
        return DataResult.success(input);
    }

    @Override
    public <T> DataResult<T> encode(final Container context, final DynamicOps<T> ops, final T prefix)
    {
        return DataResult.success(ops.createMap(this.subSpecs.entrySet().stream()
            .map(e -> Pair.of(
                ops.createString(e.getKey()),
                e.getValue().encode(context, ops, prefix).getOrThrow()))));
    }
}
