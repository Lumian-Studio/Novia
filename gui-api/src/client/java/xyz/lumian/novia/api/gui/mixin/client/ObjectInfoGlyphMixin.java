package xyz.lumian.novia.api.gui.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.AtlasGlyphProvider;
import net.minecraft.client.gui.font.PlayerGlyphProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.lumian.novia.api.gui.impl.GlyphMetricsExtension;



//**********************************************************************************************************************
@Mixin({ AtlasGlyphProvider.class, PlayerGlyphProvider.class })
public class ObjectInfoGlyphMixin
{
    //******************************************************************************************************************
    @Redirect(
        method = "<clinit>",
        at     = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/font/GlyphInfo;simple(F)Lcom/mojang/blaze3d/font/GlyphInfo;"))
    private static GlyphInfo modifyInfo(float advance)
    {
        return new GlyphMetricsExtension.Defaulted()
        {
            @Override public float getAdvance()      { return advance; }
            @Override public float novia$getAscent() { return 8.0f; }
            @Override public float novia$getHeight() { return 8.0f; }
            @Override public float novia$getLeft()   { return 0; }
            @Override public float novia$getTop()    { return 0; }
            @Override public float novia$getRight()  { return (this.novia$getLeft() + this.getAdvance()); }
            @Override public float novia$getBottom() { return (this.novia$getTop() + this.novia$getHeight()); }
        };
    }
}
