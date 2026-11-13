package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Shadow
    private boolean isHidden;

    @Shadow
    private Minecraft minecraft;

    @Shadow
    public abstract void extractVignette(GuiGraphicsExtractor graphics, Entity camera);

    @Unique
    private static final StackWalker cinematiczoom$STACK = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    @Unique
    private boolean cinematiczoom$drawingVignette;

    // A zoom that keeps the vignette draws it here, inside the bars, whether or not it hides the HUD.
    // The game's own call is skipped meanwhile so it isn't drawn twice.
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void cinematiczoom$vignetteInsideBars(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (isHidden || !ZoomManager.keepsVignette() || !minecraft.options.vignette().get()
                || minecraft.level == null || minecraft.gui.screen() instanceof LevelLoadingScreen) return;
        cinematiczoom$drawingVignette = true;
        try {
            ZoomManager.drawInsideBars(extractor, () -> extractVignette(extractor, minecraft.getCameraEntity()));
        } finally {
            cinematiczoom$drawingVignette = false;
        }
    }

    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$skipFullScreenVignette(GuiGraphicsExtractor extractor, Entity camera, CallbackInfo ci) {
        if (!cinematiczoom$drawingVignette && ZoomManager.keepsVignette()) ci.cancel();
    }

    // A zoom that hides the HUD hides all of it, the way F1 does: vanilla's own elements, chat,
    // the scoreboard, and HUDs other mods add inside this method. The bars are drawn outside Hud.
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;isHidden:Z"))
    private boolean cinematiczoom$hideWholeHud(boolean hidden) {
        return hidden || ZoomManager.shouldHideHud();
    }

    // The hidden flag is copied to the render state, where it also hides the hand. Put the real
    // value back so the zoom's own hand setting decides that.
    @Inject(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;isHudHidden:Z", shift = At.Shift.AFTER))
    private void cinematiczoom$keepHandFlag(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden = isHidden;
    }

    // Mods that draw their own HUDs follow F1 through this (OneConfig's VanillaHUD and
    // EvergreenHUD among them), so report hidden while the zoom hides the HUD. Flashback is the
    // exception: it hides its whole replay editor on F1, which would leave the zoom filling the screen.
    @Inject(method = "isHidden", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$reportHidden(CallbackInfoReturnable<Boolean> cir) {
        if (ZoomManager.shouldHideHud() && !cinematiczoom$askedByFlashback()) cir.setReturnValue(true);
    }

    @Unique
    private static boolean cinematiczoom$askedByFlashback() {
        return cinematiczoom$STACK.walk(frames -> frames.map(StackWalker.StackFrame::getDeclaringClass)
                        .filter(type -> type != Hud.class).findFirst())
                .map(type -> type.getName().startsWith("com.moulberry.flashback."))
                .orElse(false);
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideCrosshair(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (ZoomManager.shouldHideCrosshair()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideHotbar(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (ZoomManager.shouldHideHud()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideEffects(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (ZoomManager.shouldHideHud()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideBossOverlay(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (ZoomManager.shouldHideHud()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractDebugOverlay", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideDebugOverlay(GuiGraphicsExtractor extractor, CallbackInfo ci) {
        if (ZoomManager.shouldHideHud()) {
            ci.cancel();
        }
    }
}
