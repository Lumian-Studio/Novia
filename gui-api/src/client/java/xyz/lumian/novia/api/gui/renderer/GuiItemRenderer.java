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
package xyz.lumian.novia.api.gui.renderer;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import xyz.lumian.novia.api.core.util.Colour;
import xyz.lumian.novia.api.gui.canvas.Canvas;

import java.util.*;



//**********************************************************************************************************************
public class GuiItemRenderer
{
    //******************************************************************************************************************
    private final Minecraft client = Minecraft.getInstance();
    
    private TrackingItemStackRenderState keyedItemRenderState = null;
    private ItemStack                    stack                = null;
    private int                          seed                 = 0;
    private boolean                      withEntity           = true;
    
    //******************************************************************************************************************
    public GuiItemRenderer(final @Nullable ItemStack stack)
    {
        this.stack = Objects.requireNonNull(stack, "stack must not be null");
        this.updateStack();
    }
    
    public GuiItemRenderer() {}
    
    //==================================================================================================================
    public ItemStack getStack() { return this.stack; }
    
    public int getSeed() { return this.seed; }
    
    public boolean shouldDrawWithEntity() { return this.withEntity; }
    
    //==================================================================================================================
    public final void setItem(final ItemStack stack)
    {
        if (!this.stack.equals(stack))
        {
            this.stack = Objects.requireNonNull(stack, "stack must not be null");
            this.updateStack();
        }
    }
    
    public final void setSeed(final int seed)
    {
        if (this.seed != seed)
        {
            this.seed = seed;
            this.updateStack();
        }
    }
    
    public final void setDrawWithEntity(final boolean withEntity)
    {
        if (this.withEntity != withEntity)
        {
            this.withEntity = withEntity;
            this.updateStack();
        }
    }
    
    //==================================================================================================================
    public void drawItem(final Canvas canvas, final int x, final int y)
    {
        if (this.keyedItemRenderState == null)
        {
            return;
        }
        
        try
        {
            canvas.drawItem(new GuiItemRenderState(
                (Matrix3x2f) canvas.getTransform().getMatrix(),
                this.keyedItemRenderState,
                x, y,
                canvas.getClippingRegion().toScreenRect()));
        }
        catch (final Throwable throwable)
        {
            final CrashReport         report  = CrashReport.forThrowable(throwable, "Rendering item");
            final CrashReportCategory section = report.addCategory("Item being rendered");
            
            section.setDetail("Item Type",       (() -> String.valueOf(this.stack.getItem())));
            section.setDetail("Item Components", (() -> String.valueOf(this.stack.getComponents())));
            section.setDetail("Item Foil",       (() -> String.valueOf(this.stack.hasFoil())));
            
            throw new ReportedException(report);
        }
    }
    
    public void drawStackOverlay(final Canvas canvas, final int x, final int y, final @Nullable String stackCountText)
    {
        if (this.stack != null && !this.stack.isEmpty())
        {
            canvas.runWithState(() ->
            {
                this.drawItemBar(canvas, x, y);
                this.drawCooldownProgress(canvas, x, y);
                this.drawStackCount(canvas, x, y, stackCountText);
            });
		}
    }
    
    public void drawItemBar(final Canvas canvas, int x, int y)
    {
        if (this.stack.isBarVisible())
        {
            x += 2;
            y += 13;
            
            canvas.setColour(-16777216);
			canvas.fill(x, y, (x + 13), (y + 2));
            
            canvas.setColour(ARGB.opaque(this.stack.getBarColor()));
			canvas.fill(x, y, (x + this.stack.getBarWidth()), (y + 1));
		}
    }
    
    public void drawCooldownProgress(final Canvas canvas, final int x, final int y)
    {
        final LocalPlayer player   = this.client.player;
		final float       cooldown = (player != null
			? player
                .getCooldowns()
                .getCooldownPercent(this.stack, this.client.getDeltaTracker().getGameTimeDeltaPartialTick(true))
			: 0f);
   
		if (cooldown > 0f)
        {
			final int draw_y = (y + Mth.floor(16f * (1f - cooldown)));
			
            canvas.setColour(Integer.MAX_VALUE);
            canvas.fill(x, draw_y, (x + 16), (draw_y + Mth.ceil(16f * cooldown)));
		}
    }
    
    public void drawStackCount(final Canvas canvas, int x, int y, final @Nullable String stackCountText)
    {
        if (this.stack.getCount() != 1 || stackCountText != null)
        {
			final String text = (stackCountText == null ? String.valueOf(this.stack.getCount()) : stackCountText);
            
            x += (17 - canvas.getFont().getTextWidthFitted(text));
            y += 9;
            
            canvas.setColour(Colour.WHITE);
            canvas.drawText(text, x, y);
		}
    }
    
    //==================================================================================================================
    private void updateStack()
    {
        if (this.stack == null || this.stack.isEmpty())
        {
            this.keyedItemRenderState = null;
            return;
        }
        
        this.keyedItemRenderState = new TrackingItemStackRenderState();
        this.client.getItemModelResolver().updateForTopItem(
            this.keyedItemRenderState,
            this.stack,
            ItemDisplayContext.GUI,
            this.client.level,
            (this.withEntity ? this.client.player : null),
            this.seed);
    }
}
