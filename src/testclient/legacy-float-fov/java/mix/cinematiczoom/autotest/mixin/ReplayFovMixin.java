package mix.cinematiczoom.autotest.mixin;

import mix.cinematiczoom.autotest.AutoTestClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stands in for Flashback's replay camera: it cancels getFov at its head with its own value. */
@Mixin(GameRenderer.class)
public class ReplayFovMixin {
    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)F", at = @At("HEAD"), cancellable = true)
    private void cinematiczoomtest$replayFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (AutoTestClient.replayFov) cir.setReturnValue((float) AutoTestClient.REPLAY_FOV);
    }
}
