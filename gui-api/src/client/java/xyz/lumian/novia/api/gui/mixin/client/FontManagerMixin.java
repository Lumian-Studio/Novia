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
package xyz.lumian.novia.api.gui.mixin.client;

import net.minecraft.client.gui.font.AtlasGlyphProvider;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.PlayerGlyphProvider;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.lumian.novia.api.gui.font.FontMetrics;
import xyz.lumian.novia.api.gui.impl.FontManagerAccessor;
import xyz.lumian.novia.api.gui.impl.FontSetAccessor;

import java.util.Map;
import java.util.Optional;



//**********************************************************************************************************************
@Mixin(FontManager.class)
public abstract class FontManagerMixin
    implements FontManagerAccessor
{
    //******************************************************************************************************************
    @Unique
    private static final FontMetrics OBJECT_INFO_METRICS = new FontMetrics(8f, 8f, 1f);
    
    //******************************************************************************************************************
    @Shadow @Final private FontManager.CachedFontProvider      anyGlyphs;
    @Shadow @Final private Map<Identifier, AtlasGlyphProvider> atlasProviders;
    @Shadow @Final private PlayerGlyphProvider                 playerProvider;
    @Shadow @Final private FontSet                             missingFontSet;
    
    //******************************************************************************************************************
    @Shadow
    protected abstract FontSet getFontSetRaw(Identifier id);
    
    //==================================================================================================================
    @Override public EffectGlyph novia$getWhiteGlyph() { return this.anyGlyphs.effect(); }
    
    @Override
    public Optional<FontInstance> novia$getFontInstance(final FontDescription descriptor)
    {
        return Optional.ofNullable(switch (descriptor)
        {
            case FontDescription.Resource resource ->
            {
                final FontSet set = this.getFontSetRaw(resource.id());
                
                if (set == this.missingFontSet)
                {
                    yield null;
                }
                
                yield new FontInstance(set.source(true), ((FontSetAccessor) set).novia$getMetrics());
            }
            
            case FontDescription.AtlasSprite sprite -> Optional
                .ofNullable(this.atlasProviders.get(sprite.atlasId()))
                .map(prov -> new FontInstance(
                    prov.sourceForSprite(sprite.spriteId()),
                    FontManagerMixin.OBJECT_INFO_METRICS))
                .orElseThrow();
            
            case FontDescription.PlayerSprite player   -> new FontInstance(
                this.playerProvider.sourceForPlayer(player),
                FontManagerMixin.OBJECT_INFO_METRICS);
            
            default -> null;
        });
    }
}
