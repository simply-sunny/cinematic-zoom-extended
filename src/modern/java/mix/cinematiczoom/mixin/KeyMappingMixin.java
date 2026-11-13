package mix.cinematiczoom.mixin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import mix.cinematiczoom.ZoomKeys;
import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Key presses and clicks reach every keybind on a key through forAllKeyMappings. During a cinematic
 * zoom, a key bound to the zoom's bar or aspect controls goes to those controls only (see ZoomKeys).
 * Releases always reach every keybind, so nothing is left held down.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
    @Shadow
    @Final
    private static Map<InputConstants.Key, List<KeyMapping>> MAP;

    @Unique
    private static boolean cinematiczoom$releasing;

    @Inject(method = "set", at = @At("HEAD"))
    private static void cinematiczoom$noteRelease(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
        cinematiczoom$releasing = !pressed;
    }

    @Inject(method = "set", at = @At("RETURN"))
    private static void cinematiczoom$endRelease(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
        cinematiczoom$releasing = false;
    }

    @Inject(method = "forAllKeyMappings", at = @At("HEAD"), cancellable = true)
    private static void cinematiczoom$zoomControlsOnly(InputConstants.Key key, Consumer<KeyMapping> action, CallbackInfo ci) {
        if (cinematiczoom$releasing) return;
        List<KeyMapping> mappings = MAP.get(key);
        if (mappings == null) return;
        if (!ZoomKeys.claimedByZoom(ZoomManager.isCinematicActive(), mappings.stream().map(KeyMapping::getName).toList())) return;
        for (KeyMapping mapping : List.copyOf(mappings)) {
            if (ZoomKeys.isCinematicControl(mapping.getName())) action.accept(mapping);
        }
        ci.cancel();
    }
}
