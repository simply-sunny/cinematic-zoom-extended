package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Starts the shutter when the screenshot key is pressed. Screenshot mods such as Screencapper wrap
 * this call and save through their own code, so ScreenshotMixin's hook inside vanilla's grab never
 * runs for them. This runs just before the call either way; the shutter ignores a second start.
 */
@Mixin(Minecraft.class)
public abstract class ScreenshotKeyMixin {
    @Inject(method = "handleGlobalKeyPress",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Lnet/minecraft/client/Minecraft;Z)V"))
    private void cinematiczoom$shutterOnScreenshotKey(CallbackInfoReturnable<Boolean> cir) {
        ZoomManager.onScreenshot();
    }
}
