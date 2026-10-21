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
package xyz.lumian.novia.api.config.document;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.io.FilenameUtils;
import xyz.lumian.novia.api.config.spec.ConfigSpec;
import xyz.lumian.novia.api.config.spec.PropertySpec;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;



//**********************************************************************************************************************
public record PointerReference(PointerReference.Method method, String pointer)
{
    //******************************************************************************************************************
    public enum Method
        implements StringRepresentable
    {
        POINTER ("pointer",  ((subj, ptr) -> ptr.toString().equals(subj))),
        WILDCARD("wildcard", ((subj, ptr) -> FilenameUtils.wildcardMatch(ptr.toString(), subj))),
        PATTERN ("pattern",  ((subj, ptr) -> ptr.toString().matches(subj)));
        
        //**************************************************************************************************************
        public static final Codec<Method> CODEC = StringRepresentable.fromEnum(Method::values);
        
        //**************************************************************************************************************
        private final String                           name;
        private final BiPredicate<String, JsonPointer> predicate;
        
        //**************************************************************************************************************
        Method(final String name, final BiPredicate<String, JsonPointer> predicate)
        {
            this.name      = name;
            this.predicate = predicate;
        }
        
        //==============================================================================================================
        public boolean matches(final String subject, final JsonPointer pointer)
        {
            return this.predicate.test(subject, pointer);
        }
        
        //==============================================================================================================
        @Override public String getSerializedName() { return this.name; }
    }
    
    //******************************************************************************************************************
    public static final Codec<PointerReference> CODEC;
    
    //==================================================================================================================
    static
    {
        CODEC = Codec
            .withAlternative(
                Codec.STRING
                    .flatComapMap(
                        (ptr -> new PointerReference(Method.POINTER, ptr)),
                        (ref -> ((ref.method != Method.POINTER)
                            ? DataResult.error(() -> "")
                            : DataResult.success(ref.pointer)))),
                RecordCodecBuilder.create(instance -> instance
                    .group(
                        Method.CODEC
                            .fieldOf("method")
                            .forGetter(PointerReference::method),
                        Codec.STRING
                            .fieldOf("matches")
                            .forGetter(PointerReference::pointer))
                    .apply(instance, PointerReference::new)));
    }
    
    //******************************************************************************************************************
    public <Container> Stream<PropertySpec<Container>> stream(final ConfigSpec<Container> spec)
    {
        if (this.method == Method.POINTER)
        {
            final PropertySpec<Container> prop_spec = spec.flatPropertySpecs().get(JsonPointer.compile(this.pointer));
            return (prop_spec != null ? Stream.of(prop_spec) : Stream.empty());
        }
        
        return spec.flatPropertySpecs().entrySet().stream()
            .filter(e -> this.method.matches(this.pointer, e.getKey()))
            .map(Map.Entry::getValue);
    }
    
    public <Container> List<PropertySpec<Container>> resolve(final ConfigSpec<Container> spec)
    {
        return this.stream(spec).collect(Collectors.toList());
    }
    
    //==================================================================================================================
    public boolean matches(final JsonPointer pointer) { return this.method.matches(this.pointer, pointer); }
}
