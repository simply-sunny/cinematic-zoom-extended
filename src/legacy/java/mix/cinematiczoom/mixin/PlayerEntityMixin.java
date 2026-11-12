package mix.cinematiczoom.mixin;

import mix.cinematiczoom.SpyglassZoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    // With spyglass zoom on, the local player never "scopes": no overlay, no 0.1x FOV and no slow mouse.
    @Inject(method = "isUsingSpyglass", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$replaceSpyglassScope(CallbackInfoReturnable<Boolean> cir) {
        if (SpyglassZoom.enabled() && (Object) this == MinecraftClient.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
