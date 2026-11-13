package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Flashback's replay editor reads the scroll wheel from its own UI, not from the game's scroll
 * handler, and turns it into fly speed. While the wheel is zooming, that read sees no scroll, so
 * scrolling changes only the zoom. Does nothing when Flashback isn't installed.
 */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.editor.ui.ReplayUI", remap = false)
public class FlashbackFlySpeedMixin {
    @Redirect(method = "drawOverlayInternal", at = @At(value = "INVOKE", target = "Ljava/lang/Math;signum(F)F"), require = 0, remap = false)
    private static float cinematiczoom$wheelZoomsInstead(float wheel) {
        return ZoomManager.wheelZooms() ? 0 : Math.signum(wheel);
    }
}
