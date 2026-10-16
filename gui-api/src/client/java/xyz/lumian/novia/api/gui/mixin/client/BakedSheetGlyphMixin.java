package xyz.lumian.novia.api.gui.mixin.client;


import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xyz.lumian.novia.api.gui.impl.BakedSheetGlyphAccessor;



//**********************************************************************************************************************
@Mixin(BakedSheetGlyph.class)
public abstract class BakedSheetGlyphMixin
    implements BakedSheetGlyphAccessor
{
    //******************************************************************************************************************
    @Accessor(value = "renderTypes")
    @Override public abstract GlyphRenderTypes novia$renderTypes();
    
    @Accessor(value = "textureView")
    @Override public abstract GpuTextureView novia$texture();
    
    @Accessor(value = "u0")
    @Override public abstract float novia$u0();
    
    @Accessor(value = "u1")
    @Override public abstract float novia$u1();
    
    @Accessor(value = "v0")
    @Override public abstract float novia$v0();
    
    @Accessor(value = "v1")
    @Override public abstract float novia$v1();
    
    @Accessor(value = "left")
    @Override public abstract float novia$left();
    
    @Accessor(value = "right")
    @Override public abstract float novia$right();
    
    @Accessor(value = "up")
    @Override public abstract float novia$up();
    
    @Accessor(value = "down")
    @Override public abstract float novia$down();
}
