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
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.lumian.novia.api.gui.font.FontMetrics;
import xyz.lumian.novia.api.gui.font.GuiFont;
import xyz.lumian.novia.api.gui.impl.FontMetricsExtension;
import xyz.lumian.novia.api.gui.impl.FontSetAccessor;

import java.util.List;
import java.util.Objects;
import java.util.Set;



//**********************************************************************************************************************
@Mixin(FontSet.class)
public abstract class FontSetMixin
    implements FontSetAccessor
{
    //******************************************************************************************************************
    @Shadow private List<GlyphProvider.Conditional> allProviders;
    
    //==================================================================================================================
    @Unique private @Nullable FontMetrics metrics = null;
    
    //******************************************************************************************************************
    @Inject(method = "reload(Ljava/util/Set;)V", at = @At("TAIL"))
    public void createMetrics(final Set<FontOption> options, final CallbackInfo ci)
    {
        float max_ascent  = -Float.MAX_VALUE;
        float max_descent = -Float.MAX_VALUE;
        
        for (final var font : this.allProviders)
        {
            final FontMetrics metrics = ((FontMetricsExtension) font.provider()).novia$getMetrics();
            max_ascent  = Math.max(max_ascent,  metrics.logicalAscent());
            max_descent = Math.max(max_descent, metrics.logicalDescent());
        }
        
        final float height = (max_ascent + max_descent);
        this.metrics = new FontMetrics(height, max_ascent, (GuiFont.getDefaultFontHeight() / height));
    }
    
    //==================================================================================================================
    @Override public FontMetrics novia$getMetrics() { return Objects.requireNonNull(this.metrics); }
}
