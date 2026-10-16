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
package xyz.lumialights.novia.api.config;

import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lumialights.novia.api.config.registry.ProviderOverride;
import xyz.lumialights.novia.api.config.registry.ConfigRegistry;
import xyz.lumialights.novia.api.core.Novia;
import xyz.lumialights.novia.api.config.provider.IConfigProvider;
import xyz.lumialights.novia.api.core.serialisation.dynops.DynamicOpsSerialisers;
import xyz.lumialights.novia.api.core.serialisation.Value;

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
    
    private static ConfigManager INSTANCE = null;
    
    //==================================================================================================================
    static
    {
        REGISTRY = new ConfigRegistry(ConfigApiIds.Registry.CONFIG_OVERRIDE);
    }
    
    //******************************************************************************************************************
    public static @NotNull ConfigManager getInstance()
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
    public ConfigManager(final @NotNull Path configDir, final @NotNull IConfigHandler handler)
    {
        if (ConfigManager.INSTANCE != null)
        {
            throw new IllegalStateException("Config manager had already been initialized");
        }
        
        ConfigManager.INSTANCE = this;
        
        this.configDir = configDir;
        this.handler   = handler;
        
        ConfigManager.REGISTRY
            .freeze()
            .parallelStream()
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
    public @NotNull Path getConfigDir() { return this.configDir; }
    
    /**
     * Gets the file path for the given config ID.
     * @param id The ID of the configuration
     * @return The file path
     */
    public @NotNull Path getFileForId(final @NotNull Identifier id)
    {
        final IConfigProvider<?> provider = ConfigManager.REGISTRY.get(id);
        return this.getFileForId(id, provider);
    }
    
    public @NotNull Path getFileForProvider(final @NotNull IConfigProvider<?> provider)
    {
        return this.getFileForId(provider.getId(), provider);
    }
    
    //------------------------------------------------------------------------------------------------------------------
    private @NotNull Path getFileForId(final @NotNull Identifier id, final @Nullable IConfigProvider<?> provider)
    {
        return this.configDir.resolve(id.getNamespace()).resolve(id.getPath() + '.' + this.getFileExtension(provider));
    }
    
    private @NotNull String getFileExtension(final @Nullable IConfigProvider<?> provider)
    {
        String ext = "txt";
        
        if (provider != null)
        {
            final IDynamicOpsSerialiser<?> serialiser = DynamicOpsSerialisers.get(provider.getOps());
            
            if (serialiser != null)
            {
                ext = serialiser.getFileExtension();
            }
        }
        
        return ext;
    }
    
    //==================================================================================================================
    public @Nullable ProviderOverride getProviderOverride(final @NotNull Identifier providerId)
    {
        return this.handler.getRegistryOverride(providerId);
    }
    
    //==================================================================================================================
    private <Container> void initialiseProvider(final @NotNull Identifier                 id,
                                                final @NotNull IConfigProvider<Container> provider)
    {
        final Path file = this.getFileForId(id, provider);
        
        if (!Files.exists(file))
        {
            final String template = (
                id.toTranslationKey().replace('/', '.')
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
    void sendUpdates(final @NotNull Identifier id, final @NotNull List<Pair<JsonPointer, Value>> updates)
    {
        if (!Novia.getInstance().isServerThread())
        {
            Novia.LOGGER.debug("Tried updating server provider '{}' on logical client", id);
            return;
        }
        
        this.handler.sendUpdates(id, updates);
    }
}
