package mix.cinematiczoom.mixin;
import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
    private void cinematiczoom$crosshair(CallbackInfo ci) {if(ZoomManager.shouldHideCrosshair()) ci.cancel();}
    @Inject(method={"renderHotbarAndDecorations","renderEffects","renderDebugOverlay","renderBossOverlay","renderScoreboardSidebar"},at=@At("HEAD"),cancellable=true)
    private void cinematiczoom$hud(CallbackInfo ci) {if(ZoomManager.shouldHideHud()) ci.cancel();}
}
