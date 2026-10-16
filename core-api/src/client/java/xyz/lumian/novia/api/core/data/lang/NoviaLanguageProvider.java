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
package xyz.lumian.novia.api.core.data.lang;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import xyz.lumian.novia.api.core.lang.LanguageMap;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;



//**********************************************************************************************************************
public class NoviaLanguageProvider
    implements DataProvider
{
    //******************************************************************************************************************
    public static FabricDataGenerator.Pack.RegistryDependentFactory<NoviaLanguageProvider> simple(
        final String         modId,
        final LanguageMap<?> languageMap
    )
    {
        return ((output, registry) -> new NoviaLanguageProvider(output, modId, languageMap, registry));
    }
    
    //******************************************************************************************************************
    protected final PackOutput dataOutput;
    
    //------------------------------------------------------------------------------------------------------------------
    private final LanguageMap<?>                           languageMap;
    private final CompletableFuture<HolderLookup.Provider> registryLookup;
    private final String                                   modId;
    
    //==================================================================================================================
    public NoviaLanguageProvider(final PackOutput                               packOutput,
                                 final String                                   modId,
                                 final LanguageMap<?>                           languageMap,
                                 final CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        this.dataOutput     = Objects.requireNonNull(packOutput);
        this.languageMap    = Objects.requireNonNull(languageMap);
        this.registryLookup = Objects.requireNonNull(registryLookup);
        this.modId          = modId;
    }

    //==================================================================================================================
    public String getName() { return "Languages [%s]".formatted(String.join(", ", this.languageMap.keySet())); }
    
    //==================================================================================================================
    public CompletableFuture<?> run(final CachedOutput output)
    {
        if (this.languageMap.isEmpty())
        {
            return CompletableFuture.completedFuture(null);
        }
        
        return this.registryLookup.thenCompose(_ ->
        {
            final List<CompletableFuture<?>> futures = new ArrayList<>();
            
            for (final var lang_code : this.languageMap.keySet())
            {
                final JsonObject lang_entry_obj = new JsonObject();
                this.languageMap.streamLanguage(lang_code)
                    .forEach(entry -> lang_entry_obj.addProperty(entry.getFirst(), entry.getSecond()));
                
                futures.add(DataProvider.saveStable(
                    output,
                    lang_entry_obj,
                    this.getLangFilePath(lang_code)));
            }
            
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    //------------------------------------------------------------------------------------------------------------------
    private Path getLangFilePath(final String code)
    {
        return this.dataOutput
            .createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang")
            .json(Identifier.fromNamespaceAndPath(this.modId, code));
    }
}
