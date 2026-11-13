package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ZoomRegressionTest {
    private static final ZoomConfig CFG = ZoomConfig.INSTANCE;
    private static Minecraft client;
    private static KeyMapping cinematic;
    private static KeyMapping regular;
    private static KeyMapping barsUp;
    private static KeyMapping barsDown;
    private static int failures;
    private static int checks;

    public static void main(String[] args) throws Exception {
        ZoomConfig.configureDirectory(Path.of(System.getProperty("cinematiczoom.testConfig")));
        run("disabling cinematic toggle releases zoom", () -> disabledToggle(false));
        run("disabling regular toggle releases zoom", () -> disabledToggle(true));
        run("regular sensitivity setting survives release", () -> exitProfile(false));
        run("regular sensitivity scaling survives release", () -> exitProfile(true));
        run("legacy default limit upgrades to 30x", () -> {
            load("{\"startingZoom\":3,\"minZoomMultiplier\":0.1}");
            CFG.cinematicStartingZoom = 30;
            CFG.save();
            near(CFG.cinematicStartingZoom, 30);
            CFG.load();
            near(CFG.cinematicStartingZoom, 30);
        });
        run("custom legacy limit stays at 5x", () -> {
            load("{\"startingZoom\":3,\"minZoomMultiplier\":0.2}");
            CFG.cinematicStartingZoom = 30;
            CFG.save();
            near(CFG.cinematicStartingZoom, 5);
        });
        run("new-profile custom limit is not migrated", () -> {
            load("{\"cinematicStartingZoom\":3,\"minZoomMultiplier\":0.1}");
            CFG.cinematicStartingZoom = 30;
            CFG.save();
            near(CFG.cinematicStartingZoom, 10);
        });
        run("legacy profile settings are preserved", () -> {
            load("{\"startingZoom\":4,\"smoothMs\":100,\"barsPercent\":20,"
                    + "\"hideHudDuringZoom\":false,\"enableCinematicCamera\":false,\"toggleMode\":true}");
            near(CFG.cinematicStartingZoom, 4);
            near(CFG.regularStartingZoom, 4);
            near(CFG.cinematicBarsPercent, 20);
            near(CFG.cinematicZoomInMs, 100);
            near(CFG.regularZoomOutMs, 100);
            require(!CFG.cinematicHideHud && !CFG.cinematicCamera && CFG.cinematicToggle,
                    "legacy flags were lost");
        });
        run("toggle profiles switch and disconnect resets state", () -> {
            CFG.cinematicToggle = true;
            CFG.regularToggle = true;
            CFG.regularHideHud = false;
            cinematic.down = true;
            tick();
            cinematic.down = false;
            regular.down = true;
            tick();
            require(!ZoomManager.shouldHideHud(), "regular mode should restore HUD");
            require(!client.options.smoothCamera, "regular mode should restore camera");
            client.level = null;
            tick();
            client.level = new Object();
            regular.down = false;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "disconnect left a toggle active");
        });
        run("switching profiles preserves the held zoom level", () -> {
            CFG.regularStartingZoom = 6;
            CFG.cinematicStartingZoom = 2;
            regular.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 6);
            regular.down = false;
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 6);
            require(ZoomManager.shouldHideHud(), "cinematic profile did not hide HUD");
        });
        run("hold zoom stops when a screen opens", () -> {
            cinematic.down = true;
            tick();
            client.gui.screen = client.screen = new Object();
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "hold zoom stayed active behind a screen");
        });
        run("spyglass zoom is off by default", () -> {
            client.player.useItem = new ItemStack(Items.SPYGLASS);
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "spyglass zoomed with the option off");
            cinematic.down = true;
            tick();
            require(ZoomManager.isZoomActive(), "zoom key stopped working with the option off");
        });
        run("spyglass drives the chosen profile and replaces the keys", () -> {
            CFG.spyglassZoom = SpyglassZoom.REGULAR;
            CFG.regularHideHud = false;
            cinematic.down = true;
            tick();
            require(!ZoomManager.isZoomActive(), "zoom key still works in spyglass mode");
            cinematic.down = false;
            client.player.useItem = new ItemStack(Items.SPYGLASS);
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            require(!ZoomManager.shouldHideHud(), "spyglass did not use the regular profile");
            CFG.spyglassZoom = SpyglassZoom.CINEMATIC;
            tick();
            require(ZoomManager.shouldHideHud(), "spyglass did not switch to the cinematic profile");
            client.player.useItem = new ItemStack(Items.BOW);
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "drawing a bow zoomed like a spyglass");
            client.player.useItem = ItemStack.EMPTY;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "zoom outlived the spyglass");
        });
        run("spyglass zoom ignores toggle mode", () -> {
            CFG.spyglassZoom = SpyglassZoom.CINEMATIC;
            CFG.cinematicToggle = true;
            client.player.useItem = new ItemStack(Items.SPYGLASS);
            tick();
            client.player.useItem = ItemStack.EMPTY;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "spyglass zoom latched like a toggle");
        });
        run("spyglass zoom stops when a screen opens", () -> {
            CFG.spyglassZoom = SpyglassZoom.CINEMATIC;
            client.player.useItem = new ItemStack(Items.SPYGLASS);
            client.gui.screen = client.screen = new Object();
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "spyglass zoom stayed active behind a screen");
        });
        run("HUD frame limiters stand down only while a zoom is on screen", () -> {
            require(!HudRateOverride.active(), "override without a zoom");
            cinematic.down = true;
            tick();
            require(HudRateOverride.active(), "no override while zoomed");
            CFG.overrideHudFrameLimiters = false;
            require(!HudRateOverride.active(), "override with the setting off");
            CFG.overrideHudFrameLimiters = true;
            cinematic.down = false;
            tick();
            settle();
            tick();
            require(!HudRateOverride.active(), "override kept after the zoom ended");
        });
        run("cinematic zoom keeps its arrow keys from other mods, only while zoomed", () -> {
            java.util.List<String> shared = java.util.List.of("key.gammautils.increase", "key.cinematiczoom.increase_bars");
            java.util.List<String> other = java.util.List.of("key.gammautils.increase");
            require(!ZoomKeys.claimedByZoom(false, shared), "claimed outside a cinematic zoom");
            require(ZoomKeys.claimedByZoom(true, shared), "not claimed during a cinematic zoom");
            require(!ZoomKeys.claimedByZoom(true, other), "claimed a key the zoom doesn't use");
            require(!ZoomKeys.claimedByZoom(true, java.util.List.of("key.cinematiczoom.zoom")), "claimed the zoom key itself");
            cinematic.down = true;
            tick();
            require(ZoomManager.isCinematicActive(), "cinematic zoom not reported");
            cinematic.down = false;
            regular.down = true;
            tick();
            require(!ZoomManager.isCinematicActive(), "regular zoom reported as cinematic");
        });
        run("wheel ticks going in and out", () -> {
            ZoomAudio.clear();
            regular.down = true;
            tick();
            settle();
            ZoomManager.onWheel(1);
            Thread.sleep(40);
            ZoomManager.onWheel(-1);
            require(ZoomAudio.ticks.equals(java.util.List.of(ZoomSounds.TICK_IN_PITCH, ZoomSounds.TICK_OUT_PITCH)),
                    "expected an in tick then an out tick, got " + ZoomAudio.ticks);
            ZoomManager.onWheel(-1);
            require(ZoomAudio.ticks.size() == 2, "a burst of scroll events ticked more than once");
        });
        run("wheel ticks stop at the zoom limit and when off", () -> {
            ZoomAudio.clear();
            CFG.regularStartingZoom = 30;
            regular.down = true;
            tick();
            settle();
            ZoomManager.onWheel(1);
            require(ZoomAudio.ticks.isEmpty(), "ticked at the zoom limit");
            CFG.wheelTicks = false;
            Thread.sleep(40);
            ZoomManager.onWheel(-1);
            require(ZoomAudio.ticks.isEmpty(), "ticked with ticks off");
        });
        run("zoom sounds play once in and once out, and not for the spyglass", () -> {
            ZoomAudio.clear();
            cinematic.down = true;
            tick();
            settle();
            require(ZoomAudio.zoomIns == 1 && ZoomAudio.zoomOuts == 0, "zoom-in sound did not play once");
            cinematic.down = false;
            tick();
            settle();
            require(ZoomAudio.zoomIns == 1 && ZoomAudio.zoomOuts == 1, "zoom-out sound did not play once");
            CFG.spyglassZoom = SpyglassZoom.CINEMATIC;
            client.player.useItem = new ItemStack(Items.SPYGLASS);
            tick();
            settle();
            client.player.useItem = ItemStack.EMPTY;
            tick();
            settle();
            require(ZoomAudio.zoomIns == 1 && ZoomAudio.zoomOuts == 1, "doubled the spyglass's own sounds");
            CFG.zoomSounds = false;
            regular.down = true;
            tick();
            regular.down = false;
            tick();
            settle();
            require(ZoomAudio.zoomIns == 1 && ZoomAudio.zoomOuts == 1, "played zoom sounds with the setting off");
        });
        run("zoom stacking off lets the spyglass take over, only in first person", () -> {
            ZoomAudio.clear();
            cinematic.down = true;
            client.player.useItem = new ItemStack(Items.SPYGLASS);
            tick();
            settle();
            require(ZoomManager.isZoomActive(), "zoom stopped under the spyglass with stacking on");
            CFG.zoomStacking = false;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "zoom stacked on the spyglass with stacking off");
            client.options.cameraType = Minecraft.CameraType.THIRD_PERSON_BACK;
            tick();
            settle();
            require(ZoomManager.isZoomActive(), "a third-person spyglass, which doesn't zoom, stopped the zoom");
            client.options.cameraType = Minecraft.CameraType.FIRST_PERSON;
            tick();
            settle();
            client.player.useItem = ItemStack.EMPTY;
            tick();
            settle();
            require(ZoomManager.isZoomActive(), "zoom didn't come back when the spyglass came down");
            require(ZoomAudio.zoomIns == 1 && ZoomAudio.zoomOuts == 0, "the spyglass taking over played zoom sounds");
        });
        run("vignette stays for cinematic zoom and not for regular by default", () -> {
            require(!ZoomManager.keepsVignette(), "kept the vignette with no zoom");
            cinematic.down = true;
            tick();
            settle();
            require(ZoomManager.keepsVignette(), "cinematic zoom dropped the vignette");
            CFG.cinematicVignette = false;
            require(!ZoomManager.keepsVignette(), "kept the vignette with the setting off");
            cinematic.down = false;
            regular.down = true;
            tick();
            settle();
            require(!ZoomManager.keepsVignette(), "regular zoom kept the vignette by default");
        });
        run("the kept vignette is drawn inside the bars", () -> {
            cinematic.down = true;
            tick();
            settle();
            var graphics = new net.minecraft.client.gui.GuiGraphicsExtractor();
            var pose = graphics.pose();
            float[] seen = new float[4];
            ZoomManager.drawInsideBars(graphics, () -> {
                seen[0] = pose.y;
                seen[1] = pose.scaleY;
                seen[2] = pose.x;
                seen[3] = pose.scaleX;
            });
            double bars = ZoomManager.getCurrentBarsPercent();
            require(bars > 0, "no bars to fit inside");
            near(seen[0], bars, 0.001);
            near(seen[1], 1 - 2 * bars / 100, 0.0001);
            near(seen[2], 0, 0.0001);
            near(seen[3], 1, 0.0001);
            require(pose.depth == 0, "left the pose pushed");
        });
        run("zoom percentage draws above the hotbar", () -> {
            regular.down = true;
            tick();
            var graphics = new net.minecraft.client.gui.GuiGraphicsExtractor();
            ZoomManager.renderBars(graphics);
            require(graphics.texts.equals(java.util.List.of("300%")), "expected 300%, drew " + graphics.texts);
            CFG.showZoomPercentage = false;
            graphics = new net.minecraft.client.gui.GuiGraphicsExtractor();
            ZoomManager.renderBars(graphics);
            require(graphics.texts.isEmpty(), "zoom percentage drew with the setting off");
        });
        run("screenshot shutter clicks once and draws its bars", () -> {
            ZoomAudio.clear();
            ScreenshotShutter.reset();
            tick();
            ZoomManager.onScreenshot();
            ZoomManager.onScreenshot();
            require(ZoomAudio.clicks.size() == 1, "shutter clicked " + ZoomAudio.clicks.size() + " times");
            Thread.sleep(100);
            var graphics = new net.minecraft.client.gui.GuiGraphicsExtractor();
            ZoomManager.renderBars(graphics);
            require(graphics.fills.size() == 2 && graphics.fills.get(0)[3] > 0, "shutter bars were not drawn");
            ScreenshotShutter.reset();
        });
        run("spyglass zoom setting is saved", () -> {
            freshConfigDir();
            CFG.spyglassZoom = SpyglassZoom.REGULAR;
            CFG.save();
            CFG.spyglassZoom = SpyglassZoom.OFF;
            CFG.load();
            require(CFG.spyglassZoom == SpyglassZoom.REGULAR, "spyglass setting was not saved");
            load("{\"spyglassZoom\":\"BOGUS\"}");
            require(CFG.spyglassZoom == SpyglassZoom.OFF, "bad spyglass value was not reset");
        });
        run("discrete wheel steps forward and back", () -> {
            CFG.discreteScroll = true;
            regular.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            require(ZoomManager.onWheel(1), "active wheel event was not consumed");
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);
            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
        });
        run("discrete wheel snaps off-grid zoom and respects limits", () -> {
            CFG.discreteScroll = true;
            CFG.regularStartingZoom = 3.4f;
            regular.down = true;
            tick();
            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);
            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            ZoomManager.onWheel(100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 30);
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), CFG.maxZoomMultiplier);
        });
        run("zero and disabled wheel events are not consumed", () -> {
            regular.down = true;
            tick();
            require(!ZoomManager.onWheel(0), "zero wheel event was consumed");
            CFG.mouseWheelEnabled = false;
            require(!ZoomManager.onWheel(1), "disabled wheel event was consumed");
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
        });
        run("wheel respects both zoom boundaries", () -> {
            regular.down = true;
            tick();
            ZoomManager.onWheel(100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 30);
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), CFG.maxZoomMultiplier);
            regular.down = false;
            tick();
            require(!ZoomManager.onWheel(1), "inactive wheel event was consumed");
        });
        run("wheel allows ultrawide backward zoom past 1x", () -> {
            CFG.allowUltrawide = true;
            CFG.clamp();
            regular.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);
            require(ZoomManager.isZoomActive(), "ultrawide zoom should remain active");
            near(ZoomManager.getSensitivityMultiplier(), 1.0);
            CFG.allowUltrawide = false;
            CFG.clamp();
        });
        run("wheel does not go past 1x when ultrawide is disabled", () -> {
            CFG.allowUltrawide = false;
            CFG.clamp();
            regular.down = true;
            tick();
            settle();
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0);
        });
        run("hands and hud reappear instantly on release without waiting for animation", () -> {
            CFG.cinematicHideHud = true;
            CFG.cinematicHideHands = true;
            CFG.cinematicHideCrosshair = true;
            CFG.cinematicZoomOutMs = 500;
            cinematic.down = true;
            tick();
            require(ZoomManager.shouldHideHud(), "hud should be hidden while holding");
            require(ZoomManager.shouldHideHands(), "hands should be hidden while holding");
            require(ZoomManager.shouldHideCrosshair(), "crosshair should be hidden while holding");
            cinematic.down = false;
            tick();
            require(!ZoomManager.shouldHideHud(), "hud should reappear instantly on release");
            require(!ZoomManager.shouldHideHands(), "hands should reappear instantly on release");
            require(!ZoomManager.shouldHideCrosshair(), "crosshair should reappear instantly on release");
            CFG.cinematicZoomOutMs = 0;
            settle();
        });
        run("magnetize toggle steps strictly between defined values", () -> {
            CFG.cinematicMagnetize = true;
            CFG.cinematicSnapPoints = "2, 4, 8";
            CFG.cinematicStartingZoom = 2.0f;
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.125);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5);

            CFG.cinematicMagnetize = false;
            CFG.cinematicSnapPoints = "";
        });
        run("disabled magnetize uses normal smooth wheel scroll even with targets set", () -> {
            CFG.cinematicMagnetize = false;
            CFG.cinematicSnapPoints = "2, 4, 8";
            CFG.cinematicStartingZoom = 2.0f;
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5 - CFG.wheelStep);

            CFG.cinematicSnapPoints = "";
        });
        run("snap points parse zoom levels and FOVs automatically without separate mode", () -> {
            CFG.regularMagnetize = true;
            CFG.regularSnapPoints = "35, 14fov, 2x";
            ZoomManager.setBaseFov(70.0);
            CFG.regularStartingZoom = 1.0f;
            regular.down = true;
            tick();
            settle();

            // 35 is >= 20 -> 35 FOV (0.5 mul, 2x)
            // 2x -> 2x zoom (0.5 mul) - deduplicated with 35 FOV
            // 14fov -> 14 FOV (0.2 mul, 5x)
            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5); // 35 FOV

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.2); // 14 FOV

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5); // back to 35 FOV

            CFG.regularMagnetize = false;
            CFG.regularSnapPoints = "";
            require(ZoomSnapPoints.stepBetweenSnaps(0.5f, java.util.List.of(0.2f, 0.5f), 0.0) == 0.5f,
                    "zero vertical should not step snaps");
            require(ZoomSnapPoints.stepBetweenSnaps(0.5f, java.util.List.of(0.2f, 0.5f), Double.NaN) == 0.5f,
                    "NaN vertical should not step snaps");
        });
        run("default settings have hide hands on and regular hide hud and hands on", () -> {
            ZoomConfig fresh = new ZoomConfig();
            require(fresh.cinematicHideHud, "cinematic hide hud should be true");
            require(fresh.cinematicHideHands, "cinematic hide hands should default to true");
            require(fresh.regularHideHud, "regular hide hud should default to true");
            require(fresh.regularHideHands, "regular hide hands should default to true");
            require(!fresh.allowUltrawide, "allow ultrawide should default to false");
            require(!fresh.preserveBarAdjustment, "preserve bar adjustment should default to false");
            require(!fresh.cinematicMagnetize, "cinematic magnetize should default to false");
            require(!fresh.regularMagnetize, "regular magnetize should default to false");
            near(fresh.maxZoomMultiplier, 1.0);
        });
        run("easing curves stay bounded and monotonic", () -> {
            for (ZoomCurve curve : ZoomCurve.values()) {
                near(curve.apply(0), 0);
                near(curve.apply(1), 1);
                double previous = 0;
                for (int i = 1; i <= 100; i++) {
                    double value = curve.apply(i / 100.0);
                    require(value >= previous && value <= 1, curve + " overshot or reversed");
                    previous = value;
                }
            }
        });
        run("arrow keys adjust bar size when zoomed in", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.preserveBarAdjustment = true;
            cinematic.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 16);
            barsUp.down = false;
            barsDown.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
        });
        run("arrow keys step by custom barAdjustStep", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.preserveBarAdjustment = true;
            CFG.barAdjustStep = 2.5f;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 17.5);
        });
        run("preserveBarAdjustment toggles config persistence", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.preserveBarAdjustment = true;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 16);
            cinematic.down = false;
            tick();
            settle();
            CFG.preserveBarAdjustment = false;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 16);
        });
        run("smoothBarChange animates while discrete snaps immediately", () -> {
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentBarsPercent(), 15);

            CFG.smoothBarChange = false;
            require(ZoomManager.adjustBars(5.0f), "adjustBars failed");
            near(ZoomManager.getCurrentBarsPercent(), 20);

            CFG.smoothBarChange = true;
            require(ZoomManager.adjustBars(10.0f), "adjustBars failed");
            // The animation starts at once, so allow for the time since adjustBars.
            require(ZoomManager.getCurrentBarsPercent() < 21.0f, "smooth change should not jump");

            // Sample often so a slow machine can't sleep past the whole animation between frames.
            boolean sawIntermediate = false;
            for (int i = 0; i < 2000 && Math.abs(ZoomManager.getCurrentBarsPercent() - 30.0f) > 1e-3f; i++) {
                Thread.sleep(1);
                ZoomManager.frameUpdate();
                float current = ZoomManager.getCurrentBarsPercent();
                sawIntermediate |= current > 20.0f && current < 30.0f;
            }
            require(sawIntermediate, "bars should animate smoothly between frames");
            // Smooth easing creeps into its end, so let the whole animation run out first.
            Thread.sleep(CFG.barSwitchMs + 50L);
            ZoomManager.frameUpdate();
            near(ZoomManager.getCurrentBarsPercent(), 30);
        });
        run("arrow keys do nothing when zoom is inactive or in regular zoom", () -> {
            CFG.cinematicBarsPercent = 15;
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
            regular.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
        });
        run("granular hiding controls hands, crosshair, and hud independently", () -> {
            CFG.cinematicHideHud = false;
            CFG.cinematicHideCrosshair = false;
            CFG.cinematicHideHands = true;
            cinematic.down = true;
            tick();
            settle();
            require(!ZoomManager.shouldHideHud(), "hud should not be hidden");
            require(!ZoomManager.shouldHideCrosshair(), "crosshair should not be hidden");
            require(ZoomManager.shouldHideHands(), "hands should be hidden");

            CFG.cinematicHideHands = false;
            CFG.cinematicHideCrosshair = true;
            require(!ZoomManager.shouldHideHands(), "hands should be visible");
            require(ZoomManager.shouldHideCrosshair(), "crosshair should be hidden");

            cinematic.down = false;
            tick();
            settle();
            require(!ZoomManager.shouldHideHands(), "inactive zoom should not hide hands");
            require(!ZoomManager.shouldHideCrosshair(), "inactive zoom should not hide crosshair");
            require(!ZoomManager.shouldHideHud(), "inactive zoom should not hide hud");
        });
        run("disabling arrow keys config ignores bar adjustment", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.arrowKeysAdjustBars = false;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentBarsPercent(), 15);
        });
        run("bar size adjustment stays clamped between 0 and 50", () -> {
            CFG.cinematicBarsPercent = 49.8f;
            CFG.preserveBarAdjustment = true;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 50);
            barsUp.down = false;
            cinematic.down = false;
            tick();
            settle();
            CFG.cinematicBarsPercent = 0.3f;
            cinematic.down = true;
            tick();
            settle();
            barsDown.down = true;
            tick();
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 0);
        });
        run("debug screen hiding contract tracks shouldHideHud across activate, toggle, and release", () -> {
            CFG.cinematicHideHud = true;
            CFG.regularHideHud = false;
            require(!ZoomManager.shouldHideHud(), "inactive zoom must not hide debug screen");

            cinematic.down = true;
            tick();
            require(ZoomManager.shouldHideHud(), "cinematic hold must hide debug screen");

            cinematic.down = false;
            tick();
            require(!ZoomManager.shouldHideHud(), "release cinematic hold must restore debug screen");

            regular.down = true;
            tick();
            require(!ZoomManager.shouldHideHud(), "regular zoom with regularHideHud false must not hide debug screen");
            regular.down = false;
            tick();

            CFG.cinematicToggle = true;
            cinematic.down = true;
            tick();
            cinematic.down = false;
            tick();
            require(ZoomManager.shouldHideHud(), "toggled cinematic zoom must keep debug screen hidden");

            cinematic.down = true;
            tick();
            cinematic.down = false;
            tick();
            require(!ZoomManager.shouldHideHud(), "toggling off must restore debug screen");
        });
        run("ultrawide starting zoom under 1x activates and clamps properly", () -> {
            CFG.allowUltrawide = true;
            CFG.clamp();
            CFG.cinematicStartingZoom = 0.5f;
            CFG.clamp();
            near(CFG.cinematicStartingZoom, 0.5);

            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);
            require(ZoomManager.isZoomActive(), "ultrawide zoom should be active");

            cinematic.down = false;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "zoom should release cleanly");
        });
        run("discrete scrolling steps across 1x ultrawide boundary", () -> {
            CFG.allowUltrawide = true;
            CFG.clamp();
            CFG.discreteScroll = true;
            CFG.discreteScrollStep = 0.5f;
            CFG.cinematicStartingZoom = 0.5f;
            CFG.clamp();

            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 1.5);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);
        });
        run("legacy baseZoomMultiplier config migration", () -> {
            load("{\"baseZoomMultiplier\":0.33}");
            near(CFG.cinematicStartingZoom, 3.0);
            load("{\"baseZoomMultiplier\":0.2}");
            near(CFG.cinematicStartingZoom, 5.0);
        });
        run("first run imports Zoomify settings", () -> {
            Path dir = freshConfigDir();
            Files.writeString(dir.resolve("zoomify.json"),
                    "{\"initialZoom\":5,\"zoomInTime\":0.5,\"zoomOutTime\":0.25,"
                            + "\"zoomInTransition\":\"LINEAR\",\"zoomOutTransition\":\"SINE\","
                            + "\"zoomKeyBehaviour\":\"TOGGLE\",\"relativeSensitivity\":0,"
                            + "\"relativeViewBobbing\":true,\"cinematicCamera\":0,"
                            + "\"scrollZoom\":false,\"secondaryZoomAmount\":6}");
            CFG.load();
            near(CFG.cinematicStartingZoom, 5);
            near(CFG.regularStartingZoom, 6);
            if (!Files.exists(dir.resolve("zoomify.json"))) throw new AssertionError("the other mod's config stays");
            near(CFG.cinematicZoomInMs, 500);
            near(CFG.cinematicZoomOutMs, 250);
            require(CFG.cinematicZoomInCurve == ZoomCurve.LINEAR, "in curve not imported");
            require(CFG.cinematicZoomOutCurve == ZoomCurve.SINE, "out curve not imported");
            require(CFG.cinematicToggle && CFG.regularToggle, "toggle mode not imported");
            require(CFG.cinematicRemoveBobbing, "bobbing not imported");
            require(!CFG.cinematicScaleSensitivity, "sensitivity not imported");
            require(!CFG.cinematicCamera && !CFG.regularCinematicCamera, "camera not imported");
            require(!CFG.mouseWheelEnabled, "scroll zoom not imported");
            clearExternalConfigs();
        });
        run("first run imports Ok Zoomer TOML settings", () -> {
            Path dir = freshConfigDir();
            Path okDir = Files.createDirectories(dir.resolve("ok_zoomer"));
            Files.writeString(okDir.resolve("config.toml"),
                    "[zoom_transition]\n"
                            + "start_transition = \"LINEAR\"\n"
                            + "start_transition_ticks = 6\n"
                            + "end_transition = \"SINE\"\n"
                            + "end_transition_ticks = 10\n"
                            + "\n[appearance]\n"
                            + "hide_crosshair = false\n"
                            + "reduce_view_bobbing = true\n"
                            + "persistent_interface = true\n"
                            + "\n[controls]\n"
                            + "reduce_sensitivity = false\n"
                            + "cinematic_camera = true\n"
                            + "zoom_mode = \"TOGGLE\"\n"
                            + "\n[zoom_scrolling]\n"
                            + "zoom_scrolling = false\n");
            CFG.load();
            near(CFG.cinematicZoomInMs, 300);
            near(CFG.cinematicZoomOutMs, 500);
            require(CFG.cinematicZoomInCurve == ZoomCurve.LINEAR, "in curve not imported");
            require(CFG.cinematicZoomOutCurve == ZoomCurve.SINE, "out curve not imported");
            require(!CFG.cinematicHideCrosshair && !CFG.regularHideCrosshair, "crosshair not imported");
            require(!CFG.cinematicHideHud && !CFG.regularHideHud, "HUD not imported");
            require(!CFG.cinematicScaleSensitivity, "sensitivity not imported");
            require(CFG.cinematicCamera && CFG.regularCinematicCamera, "camera not imported");
            require(CFG.cinematicToggle && CFG.regularToggle, "toggle not imported");
            require(!CFG.mouseWheelEnabled, "wheel not imported");
            clearExternalConfigs();
        });
        run("first run imports Just Zoom settings", () -> {
            Path dir = freshConfigDir();
            Path justZoom = Files.createDirectories(dir.resolve("justzoom"));
            Files.writeString(justZoom.resolve("config.json"),
                    "{\"zoom\":{\"base_zoom_factor\":75,"
                            + "\"start_zooming_animation_speed\":0.3,"
                            + "\"stop_zooming_animation_speed\":0.2,"
                            + "\"smooth_camera_movement_on_zoom\":true,"
                            + "\"normalize_mouse_sensitivity_on_zoom\":true,"
                            + "\"hide_arms_when_zooming\":true,"
                            + "\"show_hud\":\"ALWAYS\"}}");
            CFG.load();
            require(CFG.cinematicStartingZoom > 4.4f && CFG.cinematicStartingZoom < 4.7f,
                    "base zoom not imported, got " + CFG.cinematicStartingZoom);
            near(CFG.cinematicZoomInMs, 300);
            near(CFG.cinematicZoomOutMs, 200);
            require(CFG.cinematicCamera && CFG.regularCinematicCamera, "camera not imported");
            require(CFG.cinematicScaleSensitivity, "sensitivity not imported");
            require(CFG.cinematicHideHands && CFG.regularHideHands, "hands not imported");
            require(!CFG.cinematicHideHud && !CFG.regularHideHud, "HUD not imported");
            clearExternalConfigs();
        });
        run("first run imports any zoom-named config", () -> {
            Path dir = freshConfigDir();
            Files.writeString(dir.resolve("logical_zoom.json"),
                    "{\"zoom\":7,\"hideCrosshair\":false,\"scrollZoom\":false}");
            CFG.load();
            near(CFG.cinematicStartingZoom, 7);
            require(!CFG.cinematicHideCrosshair, "crosshair not imported");
            require(!CFG.mouseWheelEnabled, "wheel not imported");
            clearExternalConfigs();
        });
        run("existing config is not overwritten by imports", () -> {
            Path dir = freshConfigDir();
            Files.createDirectories(ownConfig(dir).getParent());
            Files.writeString(ownConfig(dir), "{\"cinematicStartingZoom\":2}");
            Files.writeString(dir.resolve("zoomify.json"), "{\"initialZoom\":9}");
            CFG.load();
            near(CFG.cinematicStartingZoom, 2);
            clearExternalConfigs();
        });
        if (failures != 0) {
            throw new AssertionError(failures + " of " + checks + " regression checks failed");
        }
        System.out.println(checks + " regression checks passed");
        FeatureRegressionTest.main(args);
    }

    private static void disabledToggle(boolean regularMode) {
        CFG.cinematicToggle = !regularMode;
        CFG.regularToggle = regularMode;
        KeyMapping key = regularMode ? regular : cinematic;
        key.down = true;
        tick();
        key.down = false;
        tick();
        require(ZoomManager.isZoomActive(), "toggle was not activated");
        CFG.cinematicToggle = false;
        CFG.regularToggle = false;
        tick();
        settle();
        require(!ZoomManager.isZoomActive(), "disabled toggle left zoom active");
    }

    private static void exitProfile(boolean scale) {
        CFG.regularScaleSensitivity = scale;
        CFG.cinematicScaleSensitivity = !scale;
        CFG.regularRemoveBobbing = scale;
        CFG.cinematicRemoveBobbing = !scale;
        regular.down = true;
        tick();
        settle();
        CFG.regularZoomOutMs = 240;
        regular.down = false;
        tick();
        near(ZoomManager.getSensitivityMultiplier(), scale ? 1.0 / 3 : 1);
        require(ZoomManager.shouldRemoveBobbing() == scale, "exit used cinematic bobbing setting");
        CFG.regularZoomOutMs = 0;
    }

    private static void run(String name, Check check) throws Exception {
        ZoomManager.reset(client == null ? new Minecraft() : client);
        ZoomConfig defaults = new ZoomConfig();
        for (var field : ZoomConfig.class.getFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.set(CFG, field.get(defaults));
            }
        }
        // This suite freezes the legacy settings contract; new defaults have separate tests.
        CFG.aspectRatio = AspectRatios.LEGACY;
        CFG.aspectProfiles.add(AspectRatios.LEGACY);
        CFG.fovMode = FovMode.LEGACY;
        CFG.logarithmicScroll = false;
        CFG.minZoomMultiplier = 1f/30f;
        CFG.barEnterMs = CFG.barExitMs = 0;
        CFG.cinematicZoomInMs = CFG.cinematicZoomOutMs = 0;
        CFG.regularZoomInMs = CFG.regularZoomOutMs = 0;
        client = new Minecraft();
        cinematic = new KeyMapping();
        regular = new KeyMapping();
        barsUp = new KeyMapping();
        barsDown = new KeyMapping();
        checks++;
        try {
            check.run();
            System.out.println("PASS: " + name);
        } catch (Throwable error) {
            failures++;
            System.err.println("FAIL: " + name + " — " + error.getMessage());
        }
    }

    private static void load(String json) throws Exception {
        Path dir = Path.of(System.getProperty("cinematiczoom.testConfig"));
        Files.createDirectories(dir);
        Files.createDirectories(ownConfig(dir).getParent());
        Files.writeString(ownConfig(dir), json);
        CFG.load();
    }

    private static Path ownConfig(Path dir) {
        return dir.resolve("cinematiczoom").resolve("config.json");
    }

    private static Path freshConfigDir() throws Exception {
        Path dir = Path.of(System.getProperty("cinematiczoom.testConfig"));
        Files.createDirectories(dir);
        clearExternalConfigs();
        return dir;
    }

    private static void clearExternalConfigs() throws Exception {
        Path dir = Path.of(System.getProperty("cinematiczoom.testConfig"));
        Files.deleteIfExists(ownConfig(dir));
        Files.deleteIfExists(dir.resolve("zoomify.json"));
        Files.deleteIfExists(dir.resolve("okzoomer.json"));
        Files.deleteIfExists(dir.resolve("ok_zoomer.json"));
        Files.deleteIfExists(dir.resolve("logical_zoom.json"));
        Path okDir = dir.resolve("ok_zoomer");
        Files.deleteIfExists(okDir.resolve("config.toml"));
        Files.deleteIfExists(okDir);
        Path justZoom = dir.resolve("justzoom");
        Files.deleteIfExists(justZoom.resolve("config.json"));
        Files.deleteIfExists(justZoom);
    }

    private static void tick() {
        ZoomManager.tick(client, cinematic, regular, barsUp, barsDown);
    }

    private static void settle() {
        ZoomManager.frameUpdate();
        ZoomManager.frameUpdate();
    }

    private static void near(double actual, double expected, double tolerance) {
        require(Math.abs(actual - expected) < tolerance, "expected " + expected + ", got " + actual);
    }

    private static void near(double actual, double expected) {
        require(Math.abs(actual - expected) < 0.00001, "expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface Check {
        void run() throws Exception;
    }
}
