package xyz.lumian.novia.api.gui.mixin.client;

import com.mojang.blaze3d.platform.Window;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.lumian.novia.api.gui.component.GuiScreen;



//**********************************************************************************************************************
@Mixin(Window.class)
public class WindowMixin
{
    //******************************************************************************************************************
    @Inject(
        method = "onFocus",
        at     = @At(
            value  = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lcom/mojang/blaze3d/platform/Window;focused:Z",
            shift  = At.Shift.AFTER))
    public void focusChange(long handle, boolean focused, CallbackInfo ci)
    {
        if (GuiScreen.getCurrentScreen() != null)
        {
            GuiScreen.getCurrentScreen().windowFocusChanged(focused);
        }
    }
}
