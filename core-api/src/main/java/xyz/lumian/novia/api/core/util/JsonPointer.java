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
package xyz.lumian.novia.api.core.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.ArrayUtils;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.core.lang.ITranslationHolder;

import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
/// JSON Pointers allow the user to target specific properties inside a JSON Document. They are much like a URI but
/// instead point to JSON values.
/// For more information read [JSON Pointer](https://www.rfc-editor.org/info/rfc6901/#section-5).
public class JsonPointer
    implements ITranslationHolder
{
    //******************************************************************************************************************
    /// The RegEx pattern to parse JSON pointer segments (where capture group 1 is the current segment's content).
    @Language("RegExp")
    public static final String POINTER_PATTERN = "/([^/]*)";
    
    /// The RegEx pattern to evaluate a full non-capturing JSON pointer expression.
    @Language("RegExp")
    public static final String FULL_PATTERN = "^(?:/[^/]*)*$";
    
    /// Represents a pointer to the JSON root value. (by that means, a JSON pointer with a value of "/")
    public static final JsonPointer ROOT = new JsonPointer(new String[0]);
    
    /// Provides a streaming codec for a JsonPointer object.
    public static final StreamCodec<ByteBuf, JsonPointer> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, JsonPointer::toString,
        JsonPointer::tryCompile);
    
    /// Provides a codec for a JsonPointer object.
    public static final Codec<JsonPointer> CODEC = Codec.STRING
        .xmap(JsonPointer::tryCompile, JsonPointer::toString);

    //******************************************************************************************************************
    /// Tries to compile a JSON pointer string to a new instance of the [JsonPointer] class.
    ///
    /// If the pointer string was invalid, which really only happens if the string length is over 0, and it doesn't
    /// start with a '/', this returns `null`.
    ///
    /// @param pointer The pointer string
    /// @return The new [JsonPointer] or null if the pointer string was invalid.
    @Nullable
    public static JsonPointer compile(@Pattern(JsonPointer.FULL_PATTERN) final String pointer)
    {
        if (pointer.isEmpty())                          return JsonPointer.ROOT;
        if (!pointer.matches(JsonPointer.FULL_PATTERN)) return null;
        
        return new JsonPointer(Arrays.stream(pointer.split("/", -1))
            .skip(1)
            .map(JsonPointer::unescape)
            .toArray(String[]::new));
    }
    
    /// This method is equivalent to [#compile(String)], but instead of returning `null` on error, will throw an
    /// exception.
    ///
    /// @param pointer The pointer string
    /// @return The new [JsonPointer]
    /// @throws IllegalArgumentException If the given `pointer` is not a valid JSON Pointer string
    public static JsonPointer tryCompile(@Pattern(JsonPointer.FULL_PATTERN) final String pointer)
    {
        @SuppressWarnings("PatternValidation")
        final JsonPointer json_pointer = JsonPointer.compile(pointer);
        
        if (json_pointer == null)
        {
            throw new IllegalArgumentException("'%s' is not a valid JSON Pointer".formatted(pointer));
        }
        
        return json_pointer;
    }
    
    /// Escapes all characters in a string so that it can be represented as a JSON Pointer.
    ///
    /// In the JSON Pointer specification this will only affect the characters: `~` which will get replaced by `~0`
    /// and `/` which will be replaced by `~1`.
    ///
    /// @param string The unescaped string
    /// @return The escaped string
    public static String escape(final String string)
    {
        Objects.requireNonNull(string, "string must not be null");
        return string.replace("~", "~0").replace("/", "~1");
    }
    
    /// Unescapes all previously escaped JSON Pointer characters (see [#escape(String)]).
    /// @param string The escaped string
    /// @return The unescaped string
    public static String unescape(final String string)
    {
        Objects.requireNonNull(string, "string must not be null");
        return string.replace("~1", "/").replace("~0", "~");
    }

    //******************************************************************************************************************
    private final String[] paths;
    private final String   pointer;

    //******************************************************************************************************************
    private JsonPointer(final String[] paths)
    {
        this.paths   = paths;
        this.pointer = (paths.length > 0
            ? ('/' + Arrays.stream(paths).map(JsonPointer::escape).collect(Collectors.joining("/")))
            : "");
    }
    
    private JsonPointer(final String[] paths, final String pointer)
    {
        this.paths   = paths;
        this.pointer = pointer;
    }

    //==================================================================================================================
    /// Gets a copy of the segment array this pointer comprises. (starting from root and ending at deepest key)
    /// @return The segments array
    public String[] getPaths() { return this.paths.clone(); }
    
    /// Gets the number of segments this pointer comprises.
    /// @return The number of segments
    public int getDepth() { return this.paths.length; }
    
    /// Gets the value of the last (current) segment. If the current pointer is the root pointer,
    /// this will return `null`.
    /// @return The current segment name
    @Nullable
    public String getName()
    {
        return (!isRoot()
            ? this.paths[this.paths.length - 1]
            : null);
    }
    
    /// Gets the parent JSON pointer of the last (current) segment. If the current pointer is the root pointer,
    /// this will return `null`.
    /// @return The parent {@link JsonPointer} object
    @Nullable
    public JsonPointer getParent()
    {
        if (isRoot())               return null;
        if (this.paths.length == 1) return ROOT;

        return new JsonPointer(Arrays.copyOfRange(this.paths, 0, (this.paths.length - 1)));
    }
    
    /// Gets a pointer to a neighbouring key. If this is the root pointer, returns null.
    /// @param name Name of the sibling
    /// @return The sibling pointer
    @Nullable
    public JsonPointer getSibling(final String name)
    {
        if (name.equals(getName())) return this;
        
        final JsonPointer parent = getParent();
        return (parent != null ? parent.getChild(name) : null);
    }
    
    /// Gets a pointer to a direct child of the current pointer.
    /// @param name The name of the child
    /// @return The child pointer
    public JsonPointer getChild(final String name)
    {
        return new JsonPointer(ArrayUtils.add(this.paths, name), (this.pointer + '/' + escape(name)));
    }

    //==================================================================================================================
    /// Determines whether the current JSON pointer points to the root value of a JSON document.
    /// @return `true` if the pointer points to the root value
    public boolean isRoot() { return (this == JsonPointer.ROOT); }

    //==================================================================================================================
    /// Parses the given pointer string, combines it with the current pointer and returns a new pointer. If the given
    /// pointer string was invalid, `null` will be returned.
    /// @param pointer The pointer to resolve
    /// @return The new JSON pointer or `null`
    @Nullable
    public JsonPointer resolve(@Pattern(JsonPointer.FULL_PATTERN) final String pointer)
    {
        if (pointer.isEmpty()) return this;
        
        @Subst("")
        final String      subst = pointer;
        final JsonPointer temp  = JsonPointer.compile(subst);
        
        return (temp != null
            ? new JsonPointer(ArrayUtils.addAll(this.paths, temp.paths), (this.pointer + temp.pointer))
            : null);
    }
    
    //==================================================================================================================
    /// Converts the JSON Pointer to a Minecraft translation key (e.g. `/this/is/a/path` => `this.is.a.path`)
    /// @return The translation key
    public String toTranslationKey() { return String.join(".", this.paths); }
    
    //==================================================================================================================
    /// {@return the JSON Pointer's string representation}
    @Override public String toString() { return this.pointer; }
    
    //==================================================================================================================
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof JsonPointer other)) return false;
        return this.pointer.equals(other.pointer);
    }

    @Override public int hashCode() { return this.pointer.hashCode(); }
}
