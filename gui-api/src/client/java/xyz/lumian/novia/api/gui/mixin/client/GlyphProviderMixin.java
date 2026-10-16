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

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SpaceProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.AllMissingGlyphProvider;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Size_Metrics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.lumian.novia.api.gui.font.FontMetrics;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.impl.FontMetricsExtension;
import xyz.lumian.novia.api.gui.impl.TrueTypeFontAccessor;

import java.nio.ByteBuffer;
import java.util.Objects;



//**********************************************************************************************************************
@Mixin(GlyphProvider.class)
public interface GlyphProviderMixin
    extends FontMetricsExtension
{
    //******************************************************************************************************************
    @Mixin(BitmapProvider.class)
    abstract class BitmapProviderMixin
        implements FontMetricsExtension
    {
        //**************************************************************************************************************
        @Unique private @Nullable FontMetrics metrics;
        
        //**************************************************************************************************************
        @Inject(method = "<init>", at = @At("TAIL"))
        public void initMetrics(final NativeImage image, final CodepointMap<BitmapProvider.Glyph> glyphs,
                                final CallbackInfo ci)
        {
            final var it = glyphs.keySet().intIterator();
            
            if (it.hasNext())
            {
                final BitmapProvider.Glyph glyph      = Objects.requireNonNull(glyphs.get(it.nextInt()));
                final float                oversample = (1f / glyph.scale());
                final float                height     = (glyph.height() / oversample);
                
                this.metrics = new FontMetrics(
                    height,
                    (glyph.ascent() / oversample),
                    (GuiFont.getDefaultFontHeight() / height));
            }
            else
            {
                this.metrics = new FontMetrics(8f, 7f, 1f);
            }
        }
        
        //==============================================================================================================
        @Override public FontMetrics novia$getMetrics() { return Objects.requireNonNull(this.metrics); }
    }
    
    @Mixin(TrueTypeGlyphProvider.class)
    abstract class TrueTypeGlyphProviderMixin
        implements
            FontMetricsExtension,
            TrueTypeFontAccessor
    {
        //**************************************************************************************************************
        @Unique private @Nullable FontMetrics metrics;
        
        //==============================================================================================================
        @Shadow @Final private float oversample;
        
        //**************************************************************************************************************
        @Inject(method = "<init>", at = @At("TAIL"))
        public void initMetrics(final ByteBuffer fontMemory, final FT_Face face, final float size,
                                final float oversample, final float shiftX, final float shiftY, final String skip,
                                final CallbackInfo ci)
        {
            final FT_Size_Metrics metrics = Objects.requireNonNull(face.size()).metrics();
            final float           ascent  = ((int) (face.ascender()          * (metrics.y_scale() / 65536.0)) >> 6);
            final float           descent = ((int) Math.abs(face.descender() * (metrics.y_scale() / 65536.0)) >> 6);
            final float           height  = ((ascent + descent) / this.oversample);
            this.metrics = new FontMetrics(height, (ascent / this.oversample), (GuiFont.getDefaultFontHeight() / height));
        }
        
        //==============================================================================================================
        @Override public FontMetrics novia$getMetrics()    { return Objects.requireNonNull(this.metrics); }
        @Override public float       novia$getOversample() { return this.oversample; }
    }
    
    @Mixin(UnihexProvider.class)
    abstract class UnihexProviderMixin
        implements FontMetricsExtension
    {
        //**************************************************************************************************************
        @Unique private static final FontMetrics METRICS = new FontMetrics(8f, 7f, 1f);
        
        //==============================================================================================================
        @Override public FontMetrics novia$getMetrics() { return UnihexProviderMixin.METRICS; }
    }
    
    @Mixin(SpaceProvider.class)
    abstract class SpaceProviderMixin
        implements FontMetricsExtension
    {
        //**************************************************************************************************************
        @Unique private static final FontMetrics METRICS = new FontMetrics(8f, 7f, 1f);
        
        //==============================================================================================================
        @Override public FontMetrics novia$getMetrics() { return SpaceProviderMixin.METRICS; }
    }
    
    @Mixin(AllMissingGlyphProvider.class)
    abstract class AllMissingGlyphProviderMixin
        implements FontMetricsExtension
    {
        //**************************************************************************************************************
        @Unique private static final FontMetrics METRICS = new FontMetrics(8f, 7f, 1f);
        
        //==============================================================================================================
        @Override public FontMetrics novia$getMetrics() { return AllMissingGlyphProviderMixin.METRICS; }
    }
}
