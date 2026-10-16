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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;



//**********************************************************************************************************************
/// An extension to Minecraft [Identifier] objects. Every ID ends in an additional section starting with a "#", which
/// is the fragment of the ID.
///
/// This system is inspired by [URI Fragments](https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Fragment) and
/// can be used to point to a specific part of an identifiable object.
///
/// @param id       The Minecraft [Identifier]
/// @param fragment The ID's fragment
public record FragmentId(
    Identifier id,
    String     fragment
)
{
    //******************************************************************************************************************
    /// The [Codec] for the [FragmentId] class.
    public static final Codec<FragmentId> CODEC = Codec.withAlternative(
        Codec.STRING.comapFlatMap(
            (pid ->
            {
                try
                {
                    return DataResult.success(FragmentId.parse(pid));
                }
                catch (final IdentifierException ex)
                {
                    return DataResult.error(ex::getMessage);
                }
            }),
            FragmentId::toString),
        RecordCodecBuilder.create(instance -> instance
            .group(
                Identifier.CODEC
                    .fieldOf("id")
                    .forGetter(FragmentId::id),
                Codec.STRING
                    .fieldOf("fragment")
                    .forGetter(FragmentId::fragment))
            .apply(instance, FragmentId::new)));
    
    //******************************************************************************************************************
    /// Creates a [FragmentId] with a custom namespace.
    /// @param namespace The [Identifier] namespace
    /// @param path      The [Identifier] path
    /// @param fragment  The [FragmentId] fragment
    /// @return A new [FragmentId]
    public static FragmentId modded(final String namespace, final String path, final String fragment)
    {
        return new FragmentId(Identifier.fromNamespaceAndPath(namespace, path), fragment);
    }
    
    /// Creates a [FragmentId] with the vanilla namespace.
    /// @param path     The [Identifier] path
    /// @param fragment The [FragmentId] fragment
    /// @return A new [FragmentId]
    public static FragmentId vanilla(final String path, final String fragment)
    {
        return new FragmentId(Identifier.withDefaultNamespace(path), fragment);
    }
    
    /// Creates a [FragmentId] with the id given as string parsed as [Identifier].
    /// @param id       The ID string
    /// @param fragment The [FragmentId] fragment
    /// @return A new [FragmentId]
    public static FragmentId parse(final String id, final String fragment)
    {
        return new FragmentId(Identifier.parse(id), fragment);
    }
    
    /// Creates a [FragmentId] with the id given as string parsed as [Identifier] and an empty fragment.
    /// @param id The ID string
    /// @return A new [FragmentId]
    public static FragmentId parse(final String id)
    {
        if (!id.contains("#"))
        {
            return new FragmentId(Identifier.parse(id), "");
        }
        
        final int hash_index = id.indexOf('#');
        return FragmentId.parse(id.substring(0, hash_index), id.substring(hash_index + 1));
    }

    //******************************************************************************************************************
    /// Translates the [FragmentId] to its string representation.
    /// @return The string version of the [FragmentId]
    @Override public String toString() { return (this.id + "#" + this.fragment); }
}
