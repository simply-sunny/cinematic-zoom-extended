package mix.cinematiczoom.autotest.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mix.cinematiczoom.autotest.AutoTestClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;

/** Stands in for Flashback's replay camera: it replaces calculateFov without calling the original. */
@Mixin(Camera.class)
public class ReplayFovMixin {
    @WrapMethod(method = "calculateFov")
    private float cinematiczoomtest$replayFov(float partialTicks, Operation<Float> original) {
        return AutoTestClient.replayFov ? AutoTestClient.REPLAY_FOV : original.call(partialTicks);
    }
}
