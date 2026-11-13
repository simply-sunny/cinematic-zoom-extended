package mix.cinematiczoom.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mix.cinematiczoom.ZoomConfig;
import mix.cinematiczoom.ZoomManager;
import mix.cinematiczoom.ZoomMath;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {

    // Hooks the call in update() rather than calculateFov's return, so the zoom still applies
    // when another mod replaces calculateFov, such as Flashback setting the replay camera's FOV.
    @ModifyExpressionValue(method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;calculateFov(F)F"))
    private float cinematiczoom$applyZoom(float fov) {
        ZoomManager.frameUpdate();

        ZoomManager.setBaseFov(fov);
        double mul = ZoomManager.getCurrentFovMul();
        if (mul == 1.0) return fov;
        float zoomed = (float) ZoomMath.fov(fov, mul, ZoomConfig.INSTANCE.fovMode);
        return Math.min(zoomed, 175.0f);
    }
}
