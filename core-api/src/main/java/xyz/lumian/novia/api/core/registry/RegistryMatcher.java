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
package xyz.lumian.novia.api.core.registry;

import net.minecraft.IdentifierException;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.io.FilenameUtils;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Subst;
import xyz.lumian.novia.api.core.block.BlockContext;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;



//**********************************************************************************************************************
/// Provides a set of matching tools that can be applied to blocks, items, entities or anything else that has a provided
/// registry.
///
/// A match expression is a string that resembles a certain aspect about a matchable object, like the ID of such an
/// object.
///
/// Match expressions can be cascaded with the pipe character '|' where the given object must match only one of the
/// subexpressions.
/// Additionally, subexpressions may begin with an exclamation mark '!' to indicate that the result is inverted and
/// will only evaluate true if the subexpression did not match.
///
/// There is 4 types of matchers provided by this library:
/// <table>
///     <tr>
///         <th style="white-space:nowrap">Matcher Type</th>
///         <th>Description</th>
///     </tr>
///     <tr>
///         <td>Tag</td>
///         <td>Matches a tag ID to the given object's registered tags. (Tag match expressions start with a '#')</td>
///     </tr>
///     <tr>
///         <td>RegEx</td>
///         <td>
///             Matches a given RegEx pattern to the object's registry ID.
///             (Starts and ends with '/', end can be omitted if it is the last subexpression)
///         </td>
///     </tr>
///     <tr>
///         <td>Wildcard</td>
///         <td>
///             Matches a given wildcard pattern to the object's registry ID.
///             A wildcard pattern is a normal string where the character '?' indicates to match any single character
///             and '*' to match any one to infinite characters.
///             (see {@link FilenameUtils#wildcardMatch(String, String)})
///         </td>
///     </tr>
///     <tr>
///         <td>Exact</td>
///         <td>Matches an object only if the test and the object's ID are exactly the same.</td>
///     </tr>
/// </table>
///
/// Be aware that RegEx matchers can only stand by themselves, that means they cannot be cascaded and only be one
/// expression.
///
/// This is heavily inspired by Denizen's
/// [Advanced Object Matching](https://meta.denizenscript.com/Docs/Languages/Advanced%20Object%20Matching) system.
public record RegistryMatcher(
    List<IRegistryObjectMatcher> matchers,
    boolean                      negated
)
{
    //******************************************************************************************************************
    public interface IMethodMatcher
        extends IRegistryObjectMatcher
    {
        //**************************************************************************************************************
        @Override default <E> boolean match(final Holder<E> entry) { return this.matchesId(this.getId(entry)); }
        boolean matchesId(final Identifier id);
    }
    
    //==================================================================================================================
    public record ExactMatcher(String id)
        implements IMethodMatcher
    {
        //**************************************************************************************************************
        @Language("RegExp")
        public static final String  ID_PATTERN_STR = "^([_\\-a-z0-9.]+:)?[_\\-a-z0-9/.]+$";
        public static final Pattern ID_PATTERN     = Pattern.compile(ID_PATTERN_STR);
        
        //**************************************************************************************************************
        @Override public boolean matchesId(final Identifier id) { return id.toString().equals(this.id); }
    }
    
    public record RegexMatcher(Pattern pattern)
        implements IMethodMatcher
    {
        //**************************************************************************************************************
        @Override
        public boolean matchesId(final Identifier id) { return this.pattern.matcher(id.toString()).matches(); }
    }
    
    public record WildcardMatcher(String wildcardPattern)
        implements IMethodMatcher
    {
        //**************************************************************************************************************
        @Language("RegExp")
        public static final String  WILDCARD_PATTERN_STR = "^([_\\-a-z0-9.?*]+:)?[_\\-a-z0-9/.?*]+$";
        public static final Pattern WILDCARD_PATTERN     = Pattern.compile(WILDCARD_PATTERN_STR);
        
        //**************************************************************************************************************
        @Override
        public boolean matchesId(final Identifier id)
        {
            return FilenameUtils.wildcardMatch(id.toString(), this.wildcardPattern);
        }
    }
    
    public record TagMatcher(IMethodMatcher method)
        implements IRegistryObjectMatcher
    {
        //**************************************************************************************************************
        @Override
        public <E> boolean match(final Holder<E> entry)
        {
            return entry.tags().anyMatch(tag -> this.method.matchesId(tag.location()));
        }
    }
    
    //******************************************************************************************************************
    /// Parses a match expression from a list of sub-expressions and constructs a new [RegistryMatcher].
    /// @param subExpressions A list of match sub-expressions
    /// @param negated        Whether the matcher is negated (see [#negated()])
    /// @return The new {@link RegistryMatcher} object
    /// @throws MatcherSyntaxException If the expression contained a syntax error
    public static RegistryMatcher compile(final List<String> subExpressions, final boolean negated)
    {
        final List<IRegistryObjectMatcher> result = new ArrayList<>(8);

        for (int i = 0; i < subExpressions.size(); i++)
        {
            final String expression = subExpressions.get(i);
            
            if (expression.isEmpty())
            {
                throw new IdentifierException("Empty sub-expression at index " + i);
            }
            
            final CharacterIterator it = new StringCharacterIterator(expression);
            RegistryMatcher.skipWhitespace(it);
            
            if (it.current() == '#')
            {
                it.next();
                result.add(new TagMatcher(parseMethod(it, (_ -> false))));
            }
            else
            {
                result.add(parseMethod(it, (_ -> false)));
            }
        }
        
        return new RegistryMatcher(result, negated);
    }
    
    /// Parses a match expression and constructs a new [RegistryMatcher].
    /// @param expression The match expression
    /// @return The new [RegistryMatcher]object
    /// @throws MatcherSyntaxException If the expression contained a syntax error
    public static RegistryMatcher compile(final String expression)
    {
        if (expression.isEmpty())
        {
            throw new MatcherSyntaxException("Empty match expression");
        }
        
        if (expression.charAt(0) == '|')
        {
            throw new MatcherSyntaxException("Empty match expression at 1");
        }
        
        if (expression.endsWith("|"))
        {
            throw new MatcherSyntaxException("Empty match expression at " + expression.length());
        }
        
        final List<IRegistryObjectMatcher> result = new ArrayList<>(8);
        final CharacterIterator            it     = new StringCharacterIterator(expression);
        final boolean                      negated;
        
        // Match expression is negated
        if (it.first() == '!')
        {
            negated = true;
            
            if (it.next() == CharacterIterator.DONE)
            {
                throw new MatcherSyntaxException("Empty match expression at " + (it.getIndex() + 1));
            }
        }
        else
        {
            negated = false;
        }
        
        do
        {
            final char c = RegistryMatcher.skipWhitespace(it);
            result.add(switch (c)
            {
                // Starting expression with expression terminators indicates an empty expression
                case '|', CharacterIterator.DONE ->
                    throw new MatcherSyntaxException("Empty sub-expression at " + (it.getIndex() + 1));
                
                // We are dealing with a tag expression
                case '#' ->
                {
                    it.next();
                    yield new TagMatcher(RegistryMatcher.parseMethod(it, RegistryMatcher.charEquals('|')));
                }
                
                // The remaining types are registry ID expressions
                default -> RegistryMatcher.parseMethod(it, RegistryMatcher.charEquals('|'));
            });
        }
        while (it.next() != CharacterIterator.DONE);
        
        return new RegistryMatcher(result, negated);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private static IMethodMatcher parseMethod(final CharacterIterator it, final Function<Character, Boolean> terminator)
    {
        char c = it.current();
        
        // Method cannot start with an expression terminator
        if (c == CharacterIterator.DONE || terminator.apply(c))
        {
            throw new MatcherSyntaxException("Empty match sub-expression at " + (it.getIndex() + 1));
        }
        
        final IMethodMatcher method = switch (c)
        {
            // '/' indicates that we are dealing with a RegEx expression
            case '/':
            {
                final char n = it.next();
                
                if (n == CharacterIterator.DONE || terminator.apply(n))
                {
                    throw new MatcherSyntaxException("Empty RegEx pattern at " + (it.getIndex() + 1));
                }
                
                @Language("RegExp")
                final String content = readRegexExpression(it);
                
                try
                {
                    yield new RegexMatcher(Pattern.compile(content));
                }
                catch (final PatternSyntaxException ex)
                {
                    throw new MatcherSyntaxException("Invalid RegEx pattern", ex);
                }
            }
            
            // We are dealing with a name expression,
            // optionally indicated by a '@' (necessary if the expression starts with a '/')
            case '@':
            {
                c = it.next();
                
                if (c == CharacterIterator.DONE || terminator.apply(c))
                {
                    throw new MatcherSyntaxException("Empty name pattern at " + (it.getIndex() + 1));
                }
            } // FALLTHROUGH
            
            default:
            {
                final StringBuilder result = new StringBuilder(64);
                
                do
                {
                    result.append(c);
                    c = it.next();
                }
                while (!(c == CharacterIterator.DONE || terminator.apply(c) || Character.isWhitespace(c)));
                
                final String  content             = result.toString();
                final boolean is_wildcard_pattern = (content.indexOf('*') > -1 || content.indexOf('?') > -1);
                
                final Matcher matcher =
                    (is_wildcard_pattern
                        ? WildcardMatcher.WILDCARD_PATTERN
                        : ExactMatcher   .ID_PATTERN)
                    .matcher(content);
                
                if (!matcher.matches())
                {
                    throw new MatcherSyntaxException(String.format(
                        "Invalid name '%s' pattern at %s, not a valid Minecraft (wildcard) ID expression",
                        content, (it.getIndex() + 1)));
                }
                
                @Subst("minecraft:test")
                final String id_pattern = (matcher.group(1) != null ? content : "minecraft:" + content);
                yield (is_wildcard_pattern ? new WildcardMatcher(id_pattern) : new ExactMatcher(id_pattern));
            }
        };
        
        final char end = RegistryMatcher.skipWhitespace(it);
        
        if (end != CharacterIterator.DONE && !terminator.apply(end))
        {
            throw new MatcherSyntaxException(String.format(
                "Unexpected '%c' token in method expression at %d, expected match expression terminator",
                end, (it.getIndex() + 1)));
        }
        
        return method;
    }
    
    private static char skipWhitespace(final CharacterIterator it)
    {
        for (char c = it.current(); c != CharacterIterator.DONE; c = it.next())
        {
            if (!Character.isWhitespace(c))
            {
                return c;
            }
        }
        
        return CharacterIterator.DONE;
    }
    
    private static String readRegexExpression(final CharacterIterator it)
    {
        final StringBuilder result = new StringBuilder(64);
        
        for (char c = it.current();; c = it.next())
        {
            if (c == CharacterIterator.DONE)
            {
                throw new MatcherSyntaxException("Unterminated RegEx escape sequence at " + (it.getIndex() + 1));
            }
            
            if (c == '\\')
            {
                c = it.next();
                
                if (c == CharacterIterator.DONE)
                {
                    throw new MatcherSyntaxException("Unterminated RegEx escape sequence at " + (it.getIndex() + 1));
                }
                
                if (c != '/')
                {
                    result.append('\\');
                }
            }
            else if (c == '/')
            {
                it.next();
                break;
            }
            
            result.append(c);
        }
        
        return result.toString();
    }
    
    private static Function<Character, Boolean> charEquals(final char c) { return (oc -> (c == oc)); }
    
    //******************************************************************************************************************
    /// Matches against a block state.
    /// @param state The [BlockState] to match
    /// @return True if the matcher matched
    public boolean matches(final BlockState state) { return this.matches(state.typeHolder()); }
    
    /// Matches against an item stack.
    /// @param stack The [ItemStack] to match
    /// @return True if the matcher matched
    public boolean matches(final ItemStack stack) { return this.matches(stack.typeHolder()); }
    
    /// Matches against a block.
    /// @param block The [Block] to match
    /// @return True if the matcher matched
    public boolean matches(final Block block) { return this.matches(BuiltInRegistries.BLOCK.wrapAsHolder(block)); }
    
    /// Matches against a block context.
    /// @param block The [BlockContext] to match
    /// @return True if the matcher matched
    public boolean matches(final BlockContext block) { return this.matches(block.state().typeHolder()); }
    
    /// Matches against an item.
    /// @param item The [Item] to match
    /// @return True if the matcher matched
    public boolean matches(final Item item) { return this.matches(BuiltInRegistries.ITEM.wrapAsHolder(item)); }
    
    /// Matches against a static registry [ResourceKey].
    ///
    /// Please note that this will only match against registry keys from static registries. Dynamic registry contents,
    /// such as for enchantments, will fail; for these cases it is advisable to use
    /// [RegistryMatcher#matches(Level, ResourceKey)] instead.
    ///
    /// If no registry for the given [ResourceKey] could be found, or the registry has no object with the key,
    /// this will return `false`.
    ///
    /// @param key The registry key to lookup
    /// @return True if the matcher matched
    /// @param <E> The object type the registry holds
    @SuppressWarnings("unchecked")
    public <E> boolean matches(final ResourceKey<E> key)
    {
        return BuiltInRegistries.REGISTRY.get(key.registry())
            .map(reg -> this.matches((Registry<E>) reg, key))
            .orElse(false);
    }
    
    /// Matches against a [ResourceKey] in the given registry.
    ///
    /// If the given registry does not contain such a [ResourceKey], this will return `false`.
    ///
    /// @param key The [ResourceKey] to look up
    /// @return `true` if the registry contains the given [ResourceKey] and the matcher matched
    /// @param <E> The object type the registry holds
    public <E> boolean matches(final Registry<E> registry, final ResourceKey<E> key)
    {
        final Optional<Holder.Reference<E>> entry = registry.get(key.identifier());
        return (entry.isPresent() && matches(entry.get()));
    }
    
    /// Matches against a holder.
    /// @param entry The [Holder] to match against
    /// @return `true` if the [Holder] matched the matcher
    /// @param <E> The object type the registry holds
    public <E> boolean matches(final Holder<E> entry)
    {
        return (!this.negated
            ? this.matchers.stream().anyMatch (m -> m.match(entry))
            : this.matchers.stream().noneMatch(m -> m.match(entry)));
    }
    
    /// Matches against a dynamic registry [ResourceKey].
    /// 
    /// Please note that this will only match against registry keys from dynamic registries. Static registry contents,
    /// such as for blocks, items etc., will fail; for these cases it is advisable to use
    /// [#matches(ResourceKey)] instead.
    /// 
    /// If no registry for the given [ResourceKey] could be found, or the registry has no object with the key,
    /// this will return `false`.
    ///
    /// @param level The current [Level] holding the dynamic registries
    /// @param key   The registry key to lookup
    /// @return `true` if the matcher matched
    /// @param <E> The object type the registry holds
    public <E> boolean matches(final Level level, final ResourceKey<E> key)
    {
        final Optional<Holder.Reference<Registry<E>>> registry = level.registryAccess().get(key.registryKey());
        return (registry.isPresent() && matches(registry.orElseThrow().value(), key));
    }
    
    //==================================================================================================================
    /// Gets a Java stream with all [Holder] objects that matched the match expression for a specific static registry.
    /// @param registry The static [Registry] containing the elements to match against
    /// @return A Java [Stream] with all the matched [Holder] objects
    public <E> Stream<Holder.Reference<E>> streamRegistryMatches(final Registry<E> registry)
    {
        return registry.listElements().filter(this::matches);
    }
    
    /// Gets a Java stream with all [Holder] objects that matched the match expression for a specific dynamic registry.
    /// @param lookup The registry lookup [HolderGetter.Provider]
    /// @param key    The [ResourceKey] describing the dynamic [Registry] to lookup
    /// @return A Java [Stream] with all the matched [Holder] objects
    public <E> Stream<Holder.Reference<E>> streamDynamicRegistryMatches(final HolderGetter.Provider    lookup,
                                                                        final ResourceKey<Registry<E>> key)
    {
        final Optional<Holder.Reference<Registry<E>>> registry = lookup.get(key);
        return (registry.isPresent() ? this.streamRegistryMatches(registry.orElseThrow().value()) : Stream.empty());
    }
}
