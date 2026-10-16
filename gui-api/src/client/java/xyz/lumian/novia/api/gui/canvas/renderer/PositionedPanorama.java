package xyz.lumian.novia.api.gui.canvas.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.gui.PanoramaRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import xyz.lumian.novia.api.gui.canvas.Canvas;
import xyz.lumian.novia.api.gui.geometry.UvMapping;



//**********************************************************************************************************************
public class PositionedPanorama
{
    //******************************************************************************************************************
    public static final Identifier PANORAMA_OVERLAY = Identifier
        .withDefaultNamespace("textures/gui/title/background/panorama_overlay.png");
    public static final PositionedPanorama INSTANCE = new PositionedPanorama();
    
    //******************************************************************************************************************
    private float spin;
    
    //******************************************************************************************************************
    public void render(final Canvas canvas, final int x, final int y, final int width, final int height,
                       final boolean shouldSpin)
    {
        final Minecraft mc = Minecraft.getInstance();
        
        if (shouldSpin)
        {
            final float rt_delta = mc.getDeltaTracker().getRealtimeDeltaTicks();
            final float delta    = (float)(
                (double) rt_delta
                * mc.gameRenderer.getGameRenderState().optionsRenderState.panoramaSpeed
            );
            this.spin = Mth.wrapDegrees(this.spin + delta * 0.1F);
        }

        mc.gameRenderer.getGameRenderState().guiRenderState.panoramaRenderState = new PanoramaRenderState(-this.spin);
        canvas.drawTexture(PositionedPanorama.PANORAMA_OVERLAY, x, y, width, height, UvMapping.mapped(0, 0, 16, 128),
                           false);
    }
}
