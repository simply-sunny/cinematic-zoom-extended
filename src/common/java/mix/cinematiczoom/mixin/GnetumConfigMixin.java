package mix.cinematiczoom.mixin;

import mix.cinematiczoom.HudRateOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gnetum draws the HUD at a capped rate and checks this method before each of its hooks. Reporting
 * it as off while a zoom is on screen makes the HUD, and the bars drawn with it, update every frame.
 * Does nothing when Gnetum isn't installed.
 */
@Pseudo
@Mixin(targets = "me.decce.gnetum.GnetumConfig", remap = false)
public class GnetumConfigMixin {
    @Unique
    private static boolean cinematiczoom$overriding;

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void cinematiczoom$standDownWhileZoomed(CallbackInfoReturnable<Boolean> cir) {
        boolean override = HudRateOverride.active();
        if (cinematiczoom$overriding && !override) cinematiczoom$refreshCache();
        cinematiczoom$overriding = override;
        if (override) cir.setReturnValue(false);
    }

    /** Gnetum's cached HUD is from before the zoom; have it redraw before showing that again. */
    @Unique
    private static void cinematiczoom$refreshCache() {
        try {
            Object framebuffers = Class.forName("me.decce.gnetum.Gnetum").getMethod("framebuffers").invoke(null);
            if (framebuffers != null) framebuffers.getClass().getMethod("markForCatchUp").invoke(framebuffers);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // An older or newer Gnetum without this method catches up on its own within a few frames.
        }
    }
}
