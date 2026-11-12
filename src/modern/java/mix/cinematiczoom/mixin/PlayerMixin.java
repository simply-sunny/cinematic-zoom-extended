package mix.cinematiczoom.mixin;

import mix.cinematiczoom.SpyglassZoom;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    // With spyglass zoom on, the local player never "scopes": no overlay, no 0.1x FOV and no slow mouse.
    @Inject(method = "isScoping", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$replaceSpyglassScope(CallbackInfoReturnable<Boolean> cir) {
        if (SpyglassZoom.enabled() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
