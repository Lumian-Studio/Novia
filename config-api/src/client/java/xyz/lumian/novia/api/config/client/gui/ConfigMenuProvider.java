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
package xyz.lumian.novia.api.config.client.gui;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;
import xyz.lumian.novia.api.config.ConfigManager;
import xyz.lumian.novia.api.config.client.document.ScreenDocument;
import xyz.lumian.novia.api.config.registry.ConfigRegistry;
import xyz.lumian.novia.api.config.event.ConfigRegistryEvent;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;
import xyz.lumian.novia.api.core.serialisation.dynops.IDynamicOpsSerialiser;
import xyz.lumian.novia.api.core.util.ReflectionException;
import xyz.lumian.novia.api.core.util.ReflectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;



//**********************************************************************************************************************
@ApiStatus.Internal
public final class ConfigMenuProvider
    implements ModMenuApi
{
    //******************************************************************************************************************
    private static Optional<Path> getScreenResource(final String modId)
    {
        final Optional<ModContainer> opt_mod_container = FabricLoader.getInstance().getModContainer(modId);
        
        if (opt_mod_container.isEmpty())
        {
            return Optional.empty();
        }
        
        final ModContainer mod_container = opt_mod_container.orElseThrow();
        final String       resource      = ScreenDocument.DOCUMENT_FILE_PATTERN.formatted(modId);
        
        return mod_container.findPath(resource);
    }

    //******************************************************************************************************************
    public static ConfigScreenFactory<?> createFactory(
        final String                                                 modId,
        final Codec<Either<ScreenDocument, Class<? extends Screen>>> codec,
        final Path                                                   resource
    )
    {
        try (final InputStream input = resource.toUri().toURL().openStream())
        {
            if (input != null)
            {
                final IDynamicOpsSerialiser<?> serialiser = DynamicOpsSerialisers.get(ScreenDocument.DOCUMENT_OPS)
                    .orElseThrow();

                final DataResult<Either<ScreenDocument, Class<? extends Screen>>> doc_result = serialiser.decode(
                    input,
                    codec);

                if (doc_result.isSuccess())
                {
                    final Either<ScreenDocument, Class<? extends Screen>> either = doc_result.getOrThrow();

                    if (either.left().isPresent())
                    {
                        final ScreenDocument document = either.left().orElseThrow();
                        return (parent -> (new ConfigScreen(parent, modId, document,
                                                            new DefaultConfigScreenLookAndFeel()))
                            .getMcScreen());
                    }

                    final Class<? extends Screen> screen_class = either.right().orElseThrow();
                    return (parent ->
                    {
                        try
                        {
                            return ReflectionUtil.instantiateClass(screen_class, parent);
                        }
                        catch (final ReflectionException ex)
                        {
                            throw new RuntimeException(
                                ("Could not load custom screen from given class-path "
                                    + "specified in config screen document '%s': %s")
                                    .formatted(resource, ex)
                            );
                        }
                    });
                }

                return (parent ->
                {
                    throw new RuntimeException("Error in config screen document '%s': %s"
                        .formatted(resource, doc_result.error().orElseThrow().message()));
                });
            }
            else
            {
                Novia.LOGGER.debug("No config screen document found for namespace '{}'", resource);
            }
        }
        catch (final IOException ex)
        {
            return (parent ->
            {
                throw new RuntimeException("Could not load screen from config screen document '%s': %s"
                    .formatted(resource, ex));
            });
        }

        return (parent -> new ConfigScreen(parent, modId, new ScreenDocument(List.of(), Optional.empty()),
                                           new DefaultConfigScreenLookAndFeel())
            .getMcScreen()
        );
    }

    //******************************************************************************************************************
    private final Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
    
    //******************************************************************************************************************
    public ConfigMenuProvider() { ConfigRegistryEvent.FREEZING.register(this::initialiseFactories); }
    
    //==================================================================================================================
    @Override public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() { return this.factories; }

    //==================================================================================================================
    private void initialiseFactories(final ConfigRegistry registry)
    {
        if (registry != ConfigManager.REGISTRY)
        {
            return;
        }
        
        if (!this.factories.isEmpty())
        {
            throw new IllegalStateException("Config screen factories were already setup");
        }
        
        final Codec<Either<ScreenDocument, Class<? extends Screen>>> screen_codec = ScreenDocument.createCodec();
        
        this.factories.putAll(registry.namespaced().entrySet().stream()
            .flatMap(e -> ConfigMenuProvider.getScreenResource(e.getKey())
                .map(path -> Pair.of(e.getKey(), ConfigMenuProvider.createFactory(e.getKey(), screen_codec, path)))
                .stream())
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }
}
