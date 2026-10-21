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
package xyz.lumian.novia.api.config;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import xyz.lumian.novia.api.config.registry.ProviderOverride;
import xyz.lumian.novia.api.config.registry.ConfigRegistry;
import xyz.lumian.novia.api.config.provider.IConfigProvider;
import xyz.lumian.novia.api.core.Novia;
import xyz.lumian.novia.api.core.serialisation.dynops.ConfigSerialisationException;
import xyz.lumian.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;
import xyz.lumian.novia.api.core.util.JsonPointer;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;



//**********************************************************************************************************************
public final class ConfigManager
{
    //******************************************************************************************************************
    public static final ConfigRegistry REGISTRY;

    //------------------------------------------------------------------------------------------------------------------
    private static final String DEFAULTS_FILE_NAME = ".config.";
    
    @Nullable
    private static ConfigManager INSTANCE = null;
    
    //==================================================================================================================
    static
    {
        REGISTRY = new ConfigRegistry(ConfigApiIds.Registry.CONFIG_OVERRIDE);
    }
    
    //******************************************************************************************************************
    public static ConfigManager getInstance()
    {
        if (ConfigManager.INSTANCE == null)
        {
            throw new IllegalStateException("Config manager has not yet been initialized");
        }
        
        return ConfigManager.INSTANCE;
    }
    
    //******************************************************************************************************************
    private final Path            configDir;
    private final IConfigHandler  handler;
    private final Set<Identifier> success = new HashSet<>();
    
    //******************************************************************************************************************
    public ConfigManager(final Path configDir, final IConfigHandler handler)
    {
        if (ConfigManager.INSTANCE != null)
        {
            throw new IllegalStateException("Config manager had already been initialized");
        }
        
        ConfigManager.INSTANCE = this;
        
        this.configDir = configDir;
        this.handler   = handler;
        
        ConfigManager.REGISTRY.freeze().parallelStream()
            .forEach(p -> this.initialiseProvider(p.getKey(), p.getValue()));
        
        Novia.LOGGER.info("Successfully loaded {} configurations: {}", this.success.size(), this.success);
        this.success.clear();
        
        System.gc();
        
        this.handler.onRegistryFrozen(ConfigManager.REGISTRY);
    }
    
    //==================================================================================================================
    /**
     * Gets the configurations base dir.
     * @return The config directory path
     */
    public Path getConfigDir() { return this.configDir; }
    
    /**
     * Gets the file path for the given config ID.
     * @param id The ID of the configuration
     * @return The file path
     */
    public Path getFileForId(final Identifier id)
    {
        final IConfigProvider<?> provider = ConfigManager.REGISTRY.get(id);
        return this.getFileForId(id, provider);
    }
    
    public Path getFileForProvider(final IConfigProvider<?> provider)
    {
        return this.getFileForId(provider.getId(), provider);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private Path getFileForId(final Identifier id, final @Nullable IConfigProvider<?> provider)
    {
        return this.configDir.resolve(id.getNamespace()).resolve(id.getPath() + '.' + this.getFileExtension(provider));
    }
    
    private String getFileExtension(final @Nullable IConfigProvider<?> provider)
    {
        return Optional.ofNullable(provider)
            .flatMap(prov -> DynamicOpsSerialisers.get(prov.getOps())
                .map(ser -> ser.getFileExtensions()[0]))
            .orElse("txt");
    }
    
    //==================================================================================================================
    public Optional<ProviderOverride> getProviderOverride(final Identifier providerId)
    {
        return this.handler.getRegistryOverride(providerId);
    }
    
    //==================================================================================================================
    private <Container> void initialiseProvider(final Identifier id, final IConfigProvider<Container> provider)
    {
        final Path file = this.getFileForId(id, provider);
        
        if (!Files.exists(file))
        {
            final String template = (
                id.toLanguageKey().replace('/', '.')
                + ConfigManager.DEFAULTS_FILE_NAME
                + this.getFileExtension(provider)
            );
            
            try
            {
                Files.createDirectories(file.getParent());
                
                final URL template_url = this.getClass().getClassLoader().getResource(template);
                
                if (template_url != null)
                {
                    IOUtils.copy(template_url, file.toFile());
                    
                    try
                    {
                        provider.getSpec().read(provider.getOps(), file, provider.getManagedContainer());
                        this.success.add(id);
                    }
                    catch (final ConfigSerialisationException ex)
                    {
                        Novia.LOGGER.error("Error loading the saved template", ex);
                    }
                    
                    return;
                }
                
                Novia.LOGGER.debug(
                    "Could not find configuration template '{}' for '{}', generating default configuration file",
                    template, id);
            }
            catch (final IOException ex)
            {
                Novia.LOGGER.error(
                    "Could not copy configuration template '{}' for '{}'",
                    template, id, ex);
            }
            
            try
            {
                provider.getSpec().write(provider.getOps(), file, provider.getManagedContainer());
            }
            catch (final ConfigSerialisationException ex)
            {
                Novia.LOGGER.error("Error saving default configuration '{}'", id, ex);
            }
            
            return;
        }
        
        try
        {
            provider.getSpec().read(provider.getOps(), file, provider.getManagedContainer());
            this.success.add(id);
        }
        catch (final ConfigSerialisationException ex)
        {
            Novia.LOGGER.error("Error loading configuration '{}' from configuration file '{}'", id, file, ex);
        }
    }
    
    //==================================================================================================================
    void sendUpdates(final Identifier id, final List<Pair<JsonPointer, Optional<?>>> updates)
    {
        if (!Novia.getInstance().isServerThread())
        {
            Novia.LOGGER.debug("Tried updating server provider '{}' on logical client", id);
            return;
        }
        
        this.handler.sendUpdates(id, updates);
    }
}
