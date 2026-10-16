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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.lumian.novia.api.gui.impl.GlyphMetricsExtension;
import xyz.lumian.novia.api.gui.impl.TrueTypeFontAccessor;



//**********************************************************************************************************************
@Mixin(UnbakedGlyph.class)
public interface UnbakedGlyphMixin
    extends GlyphMetricsExtension
{
    //******************************************************************************************************************
    @Mixin(BitmapProvider.Glyph.class)
    abstract class BitmapGlyphMixin
    {
        //**************************************************************************************************************
        @Shadow @Final private int   width;
        @Shadow @Final private int   height;
        @Shadow @Final private int   ascent;
        @Shadow @Final private int   advance;
        @Shadow @Final private float scale;
        
        //**************************************************************************************************************
        @Inject(
            method      = "info",
            at          = @At("RETURN"),
            cancellable = true)
		public void info(final CallbackInfoReturnable<GlyphInfo> cir)
        {
			cir.setReturnValue(new GlyphMetricsExtension.Defaulted()
            {
                //******************************************************************************************************
                public float getOversample() { return (1f / BitmapGlyphMixin.this.scale); }
                
                @Override public float getAdvance()      { return BitmapGlyphMixin.this.advance; }
                @Override public float novia$getHeight() { return BitmapGlyphMixin.this.height; }
                @Override public float novia$getAscent() { return BitmapGlyphMixin.this.ascent; }
                @Override public float novia$getLeft()   { return 0f; }
                @Override public float novia$getTop()    { return (7.0f - BitmapGlyphMixin.this.ascent); }
                
                @Override public float novia$getRight() { return (BitmapGlyphMixin.this.width / this.getOversample()); }
                
                @Override
                public float novia$getBottom()
                {
                    return (
                        this.novia$getTop()
                        + (BitmapGlyphMixin.this.height / this.getOversample())
                    );
                }
            });
		}
    }
    
    @Mixin(TrueTypeGlyphProvider.Glyph.class)
    abstract class TrueTypeGlyphMixin
    {
        //**************************************************************************************************************
        @Shadow @Final TrueTypeGlyphProvider this$0;

        @Shadow @Final private int   width;
        @Shadow @Final private int   height;
        @Shadow @Final private float bearingX;
        @Shadow @Final private float bearingY;
        
        //**************************************************************************************************************
        @ModifyExpressionValue(
            method = "<init>",
            at     = @At(
                value  = "INVOKE",
                target = "Lcom/mojang/blaze3d/font/GlyphInfo;simple(F)Lcom/mojang/blaze3d/font/GlyphInfo;"))
        public GlyphInfo changeInfo(GlyphInfo original)
        {
            return new GlyphMetricsExtension.Defaulted()
            {
                public float getOversample()
                {
                    return ((TrueTypeFontAccessor) TrueTypeGlyphMixin.this.this$0).novia$getOversample();
                }
                
                @Override public float getAdvance()      { return original.getAdvance(); }
                @Override public float novia$getHeight() { return TrueTypeGlyphMixin.this.height; }
                @Override public float novia$getAscent() { return TrueTypeGlyphMixin.this.bearingY; }
                @Override public float novia$getLeft()   { return TrueTypeGlyphMixin.this.bearingX; }
                @Override public float novia$getTop()    { return (7.0f - TrueTypeGlyphMixin.this.bearingY); }
                
                @Override
                public float novia$getRight()
                {
                    return (this.novia$getLeft() + (TrueTypeGlyphMixin.this.width / this.getOversample()));
                }
                
                @Override
                public float novia$getBottom()
                {
                    return (this.novia$getTop() + (TrueTypeGlyphMixin.this.height / this.getOversample()));
                }
            };
        }
    }
    
    @Mixin(UnihexProvider.Glyph.class)
    abstract class UnihexGlyphMixin
    {
        //**************************************************************************************************************
        @Unique private static final float OVERSAMPLE         = 0.5f;
        @Unique private static final int   UNIHEX_HEIGHT      = 16;
        @Unique private static final float OVERSAMPLED_HEIGHT = (UNIHEX_HEIGHT * OVERSAMPLE);
        
        //**************************************************************************************************************
        @Shadow public abstract int width();
        
        //==============================================================================================================
        @Inject(
            method      = "info",
            at          = @At("RETURN"),
            cancellable = true)
		public void info(final CallbackInfoReturnable<GlyphInfo> cir)
        {
			cir.setReturnValue(new GlyphMetricsExtension.Defaulted()
            {
                @Override public float getAdvance()      { return (UnihexGlyphMixin.this.width() / 2f + 1f); }
                @Override public float getShadowOffset() { return 0.5F; }
                @Override public float getBoldOffset()   { return 0.5F; }
                
                @Override public float novia$getHeight() { return UnihexGlyphMixin.OVERSAMPLED_HEIGHT; }
                @Override public float novia$getAscent() { return 7f; }
                @Override public float novia$getLeft()   { return 0f; }
                @Override public float novia$getTop()    { return 0f; }
                
                @Override
                public float novia$getRight()
                {
                    return (UnihexGlyphMixin.this.width() * UnihexGlyphMixin.OVERSAMPLE);
                }
                
                @Override public float novia$getBottom() { return UnihexGlyphMixin.OVERSAMPLED_HEIGHT; }
            });
		}
    }
    
    @Mixin(EmptyGlyph.class)
    abstract class EmptyGlyphMixin
    {
        //**************************************************************************************************************
        @ModifyExpressionValue(
            method = "<init>",
            at     = @At(
                value  = "INVOKE",
                target = "Lcom/mojang/blaze3d/font/GlyphInfo;simple(F)Lcom/mojang/blaze3d/font/GlyphInfo;"))
        public GlyphInfo changeInfo(GlyphInfo original)
        {
            return new GlyphMetricsExtension.Defaulted()
            {
                @Override public float getAdvance()      { return original.getAdvance(); }
                @Override public float getShadowOffset() { return 0.5F; }
                @Override public float getBoldOffset()   { return 0.5F; }
                
                @Override public float novia$getHeight() { return 8f; }
                @Override public float novia$getAscent() { return 7f; }
                @Override public float novia$getLeft()   { return 0f; }
                @Override public float novia$getTop()    { return 0f; }
                @Override public float novia$getRight()  { return 0f; }
                @Override public float novia$getBottom() { return 0f; }
            };
        }
    }
    
    @Mixin(SpecialGlyphs.class)
    abstract class SpecialGlyphsMixin
        implements Defaulted
    {
        //**************************************************************************************************************
        @Override public float novia$getAscent() { return 7f; }
        @Override public float novia$getHeight() { return 8f; }
        @Override public float novia$getLeft()   { return 0f; }
        @Override public float novia$getTop()    { return 0f; }
        @Override public float novia$getRight()  { return 5f; }
        @Override public float novia$getBottom() { return 8f; }
    }
}
