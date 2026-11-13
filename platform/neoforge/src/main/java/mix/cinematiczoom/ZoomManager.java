package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;

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

    public static void tick(Minecraft client, KeyMapping cinematicKey, KeyMapping regularKey,
                            KeyMapping increaseBarsKey, KeyMapping decreaseBarsKey) {
        tick(client, cinematicKey, regularKey, increaseBarsKey, decreaseBarsKey, null, null);
    }

    public static void tick(Minecraft client, KeyMapping cinematicKey, KeyMapping regularKey,
                            KeyMapping increaseBarsKey, KeyMapping decreaseBarsKey, KeyMapping previousAspect, KeyMapping nextAspect) {
        boolean inWorld = client.level != null && client.player != null;
        boolean canInteract = inWorld
                && client.screen == null
                && client.isWindowActive();

        boolean isCinematicDown = canInteract && isZoomKeyDown(client, cinematicKey);
        boolean isRegularDown = canInteract && isZoomKeyDown(client, regularKey);

        boolean isScoping = canInteract && client.player.isUsingItem() && client.player.getUseItem().is(Items.SPYGLASS);

        ZoomMode desiredMode = INPUT.update(inWorld, isCinematicDown, isRegularDown, isScoping);
        boolean scopeTakesOver = SpyglassZoom.vanillaScopeTakesOver(isScoping && client.options.getCameraType().isFirstPerson());
        if (scopeTakesOver) desiredMode = ZoomMode.NONE;
        boolean scopeChanged = scopeTakesOver != scopeTookOver;
        scopeTookOver = scopeTakesOver;

        ZOOM.update(desiredMode);
        canRender = canInteract;
        boolean previous = isZoomKeyDown(client, previousAspect);
        boolean next = isZoomKeyDown(client, nextAspect);
        if (canInteract && previous && !wasPrevious && !next) ZOOM.cycleAspect(-1);
        if (canInteract && next && !wasNext && !previous) ZOOM.cycleAspect(1);
        wasPrevious = previous; wasNext = next;

        if (canInteract && ZOOM.isActive() && ZoomConfig.INSTANCE.arrowKeysAdjustBars) {
            boolean up = isZoomKeyDown(client, increaseBarsKey);
            boolean down = isZoomKeyDown(client, decreaseBarsKey);
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

    private static void applyOverrides(Minecraft client, ZoomMode mode) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        boolean wantSmoothCam = (mode == ZoomMode.CINEMATIC && cfg.cinematicCamera)
                || (mode == ZoomMode.REGULAR && cfg.regularCinematicCamera);

        if (wantSmoothCam) {
            if (!client.options.smoothCamera) {
                client.options.smoothCamera = true;
                smoothCameraForcedByUs = true;
            }
        } else if (smoothCameraForcedByUs) {
            client.options.smoothCamera = false;
            smoothCameraForcedByUs = false;
        }
    }

    private static boolean isZoomKeyDown(Minecraft client, KeyMapping key) {
        return key != null && !key.isUnbound() && key.isDown();
    }

    public static void reset(Minecraft client) {
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

    public static boolean wheelZooms() {

        return ZOOM.wheelZooms();

    }


    public static boolean onWheel(double vertical) {
        double before = ZOOM.heldMagnification();
        boolean consumed = ZOOM.onWheel(vertical);
        float pitch = ZoomSounds.wheelTick(before, ZOOM.heldMagnification(), System.nanoTime());
        if (pitch > 0) ZoomAudio.click(pitch, ZoomSounds.TICK_VOLUME);
        return consumed;
    }

    /** Called when the game saves a screenshot. */
    public static void onScreenshot() {
        if (ScreenshotShutter.trigger(System.nanoTime()) && ZoomConfig.INSTANCE.screenshotSound) ZoomAudio.click(ScreenshotShutter.SHUT_PITCH, ScreenshotShutter.SHUT_VOLUME);
    }

    public static void renderBars(GuiGraphics context) {
        if (!canRender) return;
        int width = context.guiWidth();
        int height = context.guiHeight();
        var window = Minecraft.getInstance().getWindow();
        int color = Math.round(ZoomConfig.INSTANCE.barOpacity * 255) << 24;
        ZOOM.barGeometry(window.getWidth(), window.getHeight()).draw(width, height, color, context::fill);
        int shutter = (int) Math.round(height * ScreenshotShutter.coverage(System.nanoTime()));
        if (shutter > 0) {
            context.fill(0, 0, width, shutter, 0xFF000000);
            context.fill(0, height - shutter, width, height, 0xFF000000);
        }
        int alpha = ZOOM.readoutAlpha();
        // Text drawn nearly transparent comes out opaque on older versions, so skip the faint end.
        if (alpha > 8) context.drawCenteredString(Minecraft.getInstance().font, ZOOM.readoutText(), width / 2, height - 68, (alpha << 24) | 0xFFFFFF);
    }
}
