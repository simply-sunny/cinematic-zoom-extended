package mix.cinematiczoom.mixin;
import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    @Inject(method="turnPlayer(D)V",at=@At("HEAD"))
    private void cinematiczoom$sensitivity(double delta, CallbackInfo ci) {
        double multiplier=ZoomManager.getSensitivityMultiplier();
        accumulatedDX*=multiplier; accumulatedDY*=multiplier;
    }
}
