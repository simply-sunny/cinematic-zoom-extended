package mix.cinematiczoom;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class ZoomManager {
    private static final ZoomController ZOOM = new ZoomController();

    private static boolean smoothCameraForcedByUs;
    private static boolean wasPrevious, wasNext;
    private static boolean canRender;
    private static final ZoomInputState INPUT = new ZoomInputState();
    private static ZoomMode currentAppliedMode = ZoomMode.NONE;
    private static boolean scopeTookOver;

    private ZoomManager() {
    }

    public static void tick(MinecraftClient client, KeyBinding cinematicKey, KeyBinding regularKey,
                            KeyBinding increaseBarsKey, KeyBinding decreaseBarsKey) {
        tick(client, cinematicKey, regularKey, increaseBarsKey, decreaseBarsKey, null, null);
    }

    public static void tick(MinecraftClient client, KeyBinding cinematicKey, KeyBinding regularKey,
                            KeyBinding increaseBarsKey, KeyBinding decreaseBarsKey, KeyBinding previousAspect, KeyBinding nextAspect) {
        boolean inWorld = client.world != null && client.player != null;
        boolean canInteract = inWorld
                && client.currentScreen == null
                && client.isWindowFocused();

        boolean isCinematicDown = canInteract && isZoomKeyPressed(client, cinematicKey);
        boolean isRegularDown = canInteract && isZoomKeyPressed(client, regularKey);

        boolean isScoping = canInteract && client.player.isUsingItem() && client.player.getActiveItem().isOf(Items.SPYGLASS);

        ZoomMode desiredMode = INPUT.update(inWorld, isCinematicDown, isRegularDown, isScoping);
        boolean scopeTakesOver = SpyglassZoom.vanillaScopeTakesOver(isScoping && client.options.getPerspective().isFirstPerson());
        if (scopeTakesOver) desiredMode = ZoomMode.NONE;
        boolean scopeChanged = scopeTakesOver != scopeTookOver;
        scopeTookOver = scopeTakesOver;

        ZOOM.update(desiredMode);
        canRender = canInteract;
        boolean previous = isZoomKeyPressed(client, previousAspect);
        boolean next = isZoomKeyPressed(client, nextAspect);
        if (canInteract && previous && !wasPrevious && !next) ZOOM.cycleAspect(-1);
        if (canInteract && next && !wasNext && !previous) ZOOM.cycleAspect(1);
        wasPrevious = previous; wasNext = next;

        if (canInteract && ZOOM.isActive() && ZoomConfig.INSTANCE.arrowKeysAdjustBars) {
            boolean up = isZoomKeyPressed(client, increaseBarsKey);
            boolean down = isZoomKeyPressed(client, decreaseBarsKey);
            if (up && !down) {
                ZOOM.adjustBars(ZoomConfig.INSTANCE.barAdjustStep);
            } else if (down && !up) {
                ZOOM.adjustBars(-ZoomConfig.INSTANCE.barAdjustStep);
            }
        }

        if (desiredMode != currentAppliedMode) {
            int sound = ZoomSounds.zoomTransition(currentAppliedMode, desiredMode, isScoping && SpyglassZoom.enabled(), scopeChanged);
            if (sound > 0) ZoomAudio.zoomIn();
            else if (sound < 0) ZoomAudio.zoomOut();
            applyOverrides(client, desiredMode);
            currentAppliedMode = desiredMode;
        }
    }

    private static void applyOverrides(MinecraftClient client, ZoomMode mode) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        boolean wantSmoothCam = (mode == ZoomMode.CINEMATIC && cfg.cinematicCamera)
                || (mode == ZoomMode.REGULAR && cfg.regularCinematicCamera);

        if (wantSmoothCam) {
            if (!client.options.smoothCameraEnabled) {
                client.options.smoothCameraEnabled = true;
                smoothCameraForcedByUs = true;
            }
        } else if (smoothCameraForcedByUs) {
            client.options.smoothCameraEnabled = false;
            smoothCameraForcedByUs = false;
        }
    }

    private static boolean isZoomKeyPressed(MinecraftClient client, KeyBinding key) {
        if (key == null) {
            return false;
        }
        if (key.isPressed()) {
            return true;
        }
        if (key.isUnbound() || client.getWindow() == null) {
            return false;
        }
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(key);
        if (boundKey.getCode() == InputUtil.UNKNOWN_KEY.getCode()) {
            return false;
        }
        long handle = client.getWindow().getHandle();
        if (boundKey.getCategory() == InputUtil.Type.KEYSYM) {
            return GLFW.glfwGetKey(handle, boundKey.getCode()) == GLFW.GLFW_PRESS;
        }
        if (boundKey.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(handle, boundKey.getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }

    public static void reset(MinecraftClient client) {
        scopeTookOver = false;
        INPUT.reset();
        wasPrevious = wasNext = canRender = false;
        applyOverrides(client, ZoomMode.NONE);
        currentAppliedMode = ZoomMode.NONE;
        ZOOM.reset();
    }

    public static void frameUpdate() {
        ZOOM.updateFrame();
        if (ScreenshotShutter.reopenClickDue(System.nanoTime()) && ZoomConfig.INSTANCE.screenshotSound) {
            ZoomAudio.click(ScreenshotShutter.REOPEN_PITCH, ScreenshotShutter.REOPEN_VOLUME);
        }
    }

    public static double getCurrentFovMul() {
        return ZOOM.currentMultiplier();
    }

    public static boolean isZoomActive() {
        return ZOOM.isActive();
    }

    public static double getSensitivityMultiplier() {
        return ZOOM.getSensitivityMultiplier();
    }

    public static boolean shouldRemoveBobbing() {
        return ZOOM.shouldRemoveBobbing();
    }

    public static boolean adjustBars(float delta) {
        return ZOOM.adjustBars(delta);
    }

    public static boolean onWheel(double vertical) {
        double before = ZOOM.heldMagnification();
        boolean consumed = ZOOM.onWheel(vertical);
        float pitch = ZoomSounds.wheelTick(before, ZOOM.heldMagnification(), System.nanoTime());
        if (pitch > 0) ZoomAudio.click(pitch, ZoomSounds.TICK_VOLUME);
        return consumed;
    }

    public static void setBaseFov(double fov) {
        ZOOM.setBaseFov(fov);
    }

    public static double getBaseFov() {
        return ZOOM.getBaseFov();
    }

    public static float getCurrentBarsPercent() {
        return ZOOM.currentBarsPercent();
    }

    public static boolean shouldHideHud() {
        if (!currentAppliedMode.isActive()) return false;
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        return currentAppliedMode == ZoomMode.REGULAR ? cfg.regularHideHud : cfg.cinematicHideHud;
    }

    public static boolean shouldHideCrosshair() {
        if (!currentAppliedMode.isActive()) return false;
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        return currentAppliedMode == ZoomMode.REGULAR ? cfg.regularHideCrosshair : cfg.cinematicHideCrosshair;
    }

    public static boolean shouldHideHands() {
        if (!currentAppliedMode.isActive()) return false;
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        return currentAppliedMode == ZoomMode.REGULAR ? cfg.regularHideHands : cfg.cinematicHideHands;
    }

    /** Called when the game saves a screenshot. */
    public static void onScreenshot() {
        if (ScreenshotShutter.trigger(System.nanoTime()) && ZoomConfig.INSTANCE.screenshotSound) ZoomAudio.click(ScreenshotShutter.SHUT_PITCH, ScreenshotShutter.SHUT_VOLUME);
    }

    public static void renderBars(DrawContext context) {
        if (!canRender) return;
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        var window = MinecraftClient.getInstance().getWindow();
        int color = Math.round(ZoomConfig.INSTANCE.barOpacity * 255) << 24;
        ZOOM.barGeometry(window.getFramebufferWidth(), window.getFramebufferHeight()).draw(width, height, color, context::fill);
        int shutter = (int) Math.round(height * ScreenshotShutter.coverage(System.nanoTime()));
        if (shutter > 0) {
            context.fill(0, 0, width, shutter, 0xFF000000);
            context.fill(0, height - shutter, width, height, 0xFF000000);
        }
        int alpha = ZOOM.readoutAlpha();
        // Text drawn nearly transparent comes out opaque on older versions, so skip the faint end.
        if (alpha > 8) context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, ZOOM.readoutText(), width / 2, height - 68, (alpha << 24) | 0xFFFFFF);
    }
}
