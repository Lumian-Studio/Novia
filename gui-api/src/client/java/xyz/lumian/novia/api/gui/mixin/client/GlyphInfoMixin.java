package xyz.lumian.novia.api.gui.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import org.spongepowered.asm.mixin.Mixin;
import xyz.lumian.novia.api.gui.impl.GlyphMetricsExtension;



//**********************************************************************************************************************
@Mixin(GlyphInfo.class)
public interface GlyphInfoMixin
    extends GlyphMetricsExtension
{}
