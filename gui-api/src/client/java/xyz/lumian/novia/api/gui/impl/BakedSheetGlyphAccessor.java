package xyz.lumian.novia.api.gui.impl;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.font.GlyphRenderTypes;



//**********************************************************************************************************************
public interface BakedSheetGlyphAccessor
{
    //******************************************************************************************************************
    GlyphRenderTypes novia$renderTypes();
    GpuTextureView   novia$texture();
    
    //==================================================================================================================
    float novia$u0();
    float novia$u1();
    float novia$v0();
    float novia$v1();
    float novia$left();
    float novia$right();
    float novia$up();
    float novia$down();
}
