package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract boolean isMouseGrabbed();
    @Shadow @Final private SmoothDouble smoothTurnX;
    @Shadow @Final private SmoothDouble smoothTurnY;
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Unique private double cinematiczoom$lastInertiaTime;

    @Inject(method = "turnPlayer()V", at = @At("HEAD"))
    private void cinematiczoom$scaleSensitivity(CallbackInfo ci) {
        double sensMul = ZoomManager.getSensitivityMultiplier();
        if (sensMul != 1.0) {
            this.accumulatedDX *= sensMul;
            this.accumulatedDY *= sensMul;
        }
    }

    @Inject(method = "turnPlayer()V", at = @At("RETURN"))
    private void cinematiczoom$continueInertia(CallbackInfo ci) {
        if (!isMouseGrabbed() && minecraft.player != null && (ZoomManager.isZoomActive() || minecraft.options.smoothCamera)) {
            double now = GLFW.glfwGetTime();
            double dt = now - cinematiczoom$lastInertiaTime;
            cinematiczoom$lastInertiaTime = now;
            if (dt > 0.0 && dt < 0.2) {
                double sens = this.minecraft.options.sensitivity().get() * 0.6 + 0.2;
                double mult = sens * sens * sens * 8.0;
                double dx = this.smoothTurnX.getNewDeltaValue(0.0, dt * mult);
                double dy = this.smoothTurnY.getNewDeltaValue(0.0, dt * mult);
                int ySign = this.minecraft.options.invertYMouse().get() ? -1 : 1;
                this.minecraft.player.turn(dx, dy * ySign);
            }
        } else {
            cinematiczoom$lastInertiaTime = GLFW.glfwGetTime();
        }
    }
}
