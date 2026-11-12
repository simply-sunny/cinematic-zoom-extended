package mix.cinematiczoom.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$removeBobView(CallbackInfo ci) {
        if (ZoomManager.isZoomActive() && ZoomManager.shouldRemoveBobbing()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideHands(CallbackInfo ci) {
        if (ZoomManager.shouldHideHands()) {
            ci.cancel();
        }
    }

    // Wraps the whole method so the zoom still applies when another mod cancels it early,
    // such as Flashback setting the replay camera's FOV.
    @WrapMethod(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D")
    private double cinematiczoom$applyZoom(Camera camera, float tickDelta, boolean changingFov,
                                         Operation<Double> original) {
        ZoomManager.frameUpdate();

        double fov = original.call(camera, tickDelta, changingFov);
        ZoomManager.setBaseFov(fov);
        double mul = ZoomManager.getCurrentFovMul();
        if (mul == 1.0) return fov;
        double zoomed = mix.cinematiczoom.ZoomMath.fov(fov, mul, mix.cinematiczoom.ZoomConfig.INSTANCE.fovMode);
        return Math.min(zoomed, 175.0);
    }
}
