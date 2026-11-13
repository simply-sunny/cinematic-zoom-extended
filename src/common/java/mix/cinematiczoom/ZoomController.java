package mix.cinematiczoom;

import java.util.List;
import java.util.function.LongSupplier;

/** Loader-neutral zoom state. Only magnification is interpolated; bars have their own clock. */
public final class ZoomController {
    private final LongSupplier clock;
    private final BarAnimator bars;
    private ZoomMode activeMode = ZoomMode.NONE, lastActiveMode = ZoomMode.CINEMATIC;
    private double current = 1, target = 1, held = 3, start = 1;
    private long started;
    private int duration;
    private ZoomCurve curve = ZoomCurve.LINEAR;
    private float heldBarsPercent = 15;
    private BarGeometry geometry = BarGeometry.NONE;
    private int width = 1920, height = 1080;
    private double baseFov = 70;
    private String snapKey = "";
    private List<Float> cachedSnaps = List.of();
    private String geometryProfile;
    private int geometryWidth, geometryHeight;
    private float geometryLegacyPercent;
    private BarGeometry cachedGeometry = BarGeometry.NONE;
    // The magnification readout shows when a zoom starts or the wheel changes it, then fades.
    private static final long READOUT_SHOW_NANOS = 1_500_000_000L, READOUT_FADE_NANOS = 500_000_000L;
    private long readoutShownAt = Long.MIN_VALUE / 2;

    public ZoomController() { this(System::nanoTime); }
    public ZoomController(LongSupplier clock) { this.clock = clock; this.bars = new BarAnimator(clock); }

    public void update(ZoomMode mode) {
        updateFrame();
        boolean entering = mode.isActive() && !activeMode.isActive();
        ZoomMode previous = activeMode;
        if (entering) {
            ZoomConfig cfg = ZoomConfig.INSTANCE;
            held = bounded(mode == ZoomMode.REGULAR ? cfg.regularStartingZoom : cfg.cinematicStartingZoom);
            heldBarsPercent = cfg.cinematicBarsPercent;
            readoutShownAt = clock.getAsLong();
        }
        activeMode = mode;
        if (mode.isActive()) lastActiveMode = mode;
        transition(mode.isActive() ? held : 1);
        if (mode == ZoomMode.REGULAR) {
            bars.reset(); // Regular zoom has no cinematic overlay, including mode switches.
        } else if (previous != mode) {
            ZoomConfig cfg = ZoomConfig.INSTANCE;
            bars.target(barTarget(), mode == ZoomMode.CINEMATIC ? cfg.barEnterMs : cfg.barExitMs,
                    mode == ZoomMode.CINEMATIC ? cfg.barEnterMode : cfg.barExitMode);
        }
        geometry = bars.value();
    }

    private double bounded(double value) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        return ZoomMath.finiteClamp(value, 1.0 / cfg.maxZoomMultiplier,
                Math.min(500, 1.0 / cfg.minZoomMultiplier), 1);
    }

    private void transition(double next) {
        if (next == target) return;
        updateFrame();
        start = current; target = next; started = clock.getAsLong();
        boolean in = next > current;
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        if (lastActiveMode == ZoomMode.REGULAR) {
            duration = in ? cfg.regularZoomInMs : cfg.regularZoomOutMs;
            curve = in ? cfg.regularZoomInCurve : cfg.regularZoomOutCurve;
        } else {
            duration = in ? cfg.cinematicZoomInMs : cfg.cinematicZoomOutMs;
            curve = in ? cfg.cinematicZoomInCurve : cfg.cinematicZoomOutCurve;
        }
        if (curve == null) curve = ZoomCurve.LINEAR;
        updateFrame();
    }

    public void updateFrame() {
        double progress = duration <= 0 ? 1 : Math.max(0, Math.min(1, (clock.getAsLong()-started)/(duration*1_000_000.0)));
        current = progress >= 1 ? target : start + (target-start)*curve.apply(progress);
        geometry = bars.value();
    }

    private BarGeometry barTarget() {
        if (activeMode != ZoomMode.CINEMATIC) return BarGeometry.NONE;
        String profile = ZoomConfig.INSTANCE.aspectRatio;
        if (width != geometryWidth || height != geometryHeight || heldBarsPercent != geometryLegacyPercent
                || !java.util.Objects.equals(profile, geometryProfile)) {
            geometryWidth = width; geometryHeight = height;
            geometryProfile = profile; geometryLegacyPercent = heldBarsPercent;
            cachedGeometry = AspectRatios.geometry(width, height, profile, heldBarsPercent);
        }
        return cachedGeometry;
    }

    public BarGeometry barGeometry(int framebufferWidth, int framebufferHeight) {
        boolean resized = width != framebufferWidth || height != framebufferHeight;
        width = framebufferWidth; height = framebufferHeight;
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        if (activeMode == ZoomMode.CINEMATIC) {
            bars.target(barTarget(), resized ? 0 : cfg.barSwitchMs,
                    resized ? BarAnimationMode.INSTANT : cfg.barSwitchMode);
        }
        updateFrame();
        return geometry;
    }

    public boolean cycleAspect(int direction) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        if (activeMode != ZoomMode.CINEMATIC && !cfg.globalProfileCycling) return false;
        cfg.aspectRatio = AspectRatios.cycle(cfg.aspectProfiles, cfg.aspectRatio, direction);
        bars.target(barTarget(), cfg.animateProfileCycle ? cfg.barSwitchMs : 0, cfg.barSwitchMode);
        cfg.save();
        return true;
    }

    public boolean adjustBars(float delta) {
        if (activeMode != ZoomMode.CINEMATIC || !Float.isFinite(delta)) return false;
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        heldBarsPercent = (float)ZoomMath.finiteClamp(heldBarsPercent + delta, 0, 50, 15);
        cfg.aspectRatio = AspectRatios.LEGACY;
        if (!cfg.aspectProfiles.contains(AspectRatios.LEGACY)) cfg.aspectProfiles.add(AspectRatios.LEGACY);
        if (cfg.preserveBarAdjustment) cfg.cinematicBarsPercent = heldBarsPercent;
        bars.target(barTarget(), cfg.smoothBarChange ? cfg.barSwitchMs : 0,
                cfg.smoothBarChange ? cfg.barSwitchMode : BarAnimationMode.INSTANT);
        geometry = bars.value();
        return true;
    }

    public boolean onWheel(double vertical) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        if (!activeMode.isActive() || !cfg.mouseWheelEnabled || !Double.isFinite(vertical) || vertical == 0) return false;
        double previousHeld = held;
        String raw = activeMode == ZoomMode.REGULAR ? cfg.regularSnapPoints : cfg.cinematicSnapPoints;
        String key = raw + "/" + baseFov + "/" + cfg.minZoomMultiplier + "/" + cfg.maxZoomMultiplier + "/" + cfg.fovMode;
        if (!key.equals(snapKey)) {
            cachedSnaps = ZoomSnapPoints.parse(raw,baseFov,cfg.minZoomMultiplier,cfg.maxZoomMultiplier,cfg.fovMode);
            snapKey = key;
        }
        boolean magnetize = activeMode == ZoomMode.REGULAR ? cfg.regularMagnetize : cfg.cinematicMagnetize;
        if (magnetize && !cachedSnaps.isEmpty()) {
            held = 1.0 / ZoomSnapPoints.stepBetweenSnaps((float)(1/held),cachedSnaps,vertical);
        } else if (cfg.discreteScroll) {
            double step = Math.max(.1,cfg.discreteScrollStep), notches = Math.max(1,Math.min(10000,Math.round(Math.abs(vertical))));
            held = (vertical > 0 ? Math.floor(held/step+1e-4)+notches : Math.ceil(held/step-1e-4)-notches)*step;
        } else if (cfg.logarithmicScroll) {
            held *= Math.exp(Math.max(-20,Math.min(20,vertical*cfg.wheelStep*3)));
        } else {
            held = 1 / ZoomMath.finiteClamp(1/held-vertical*cfg.wheelStep,cfg.minZoomMultiplier,cfg.maxZoomMultiplier,1);
        }
        held = bounded(held);
        if (held != previousHeld) readoutShownAt = clock.getAsLong();
        transition(held);
        return true;
    }

    /** The magnification as a percentage, such as 300% at 3x. */
    public String readoutText() { return Math.round(held * 100) + "%"; }

    /** Opacity of the readout from 0 to 255: shown for 1.5 s after a change, then faded over 0.5 s. */
    public int readoutAlpha() {
        if (!ZoomConfig.INSTANCE.showZoomPercentage || !activeMode.isActive()) return 0;
        long elapsed = clock.getAsLong() - readoutShownAt;
        if (elapsed < READOUT_SHOW_NANOS) return 255;
        if (elapsed >= READOUT_SHOW_NANOS + READOUT_FADE_NANOS) return 0;
        return (int) Math.round(255 * (1 - (double) (elapsed - READOUT_SHOW_NANOS) / READOUT_FADE_NANOS));
    }

    public void reset() {
        readoutShownAt = Long.MIN_VALUE / 2;
        activeMode = ZoomMode.NONE; lastActiveMode = ZoomMode.CINEMATIC;
        current = target = start = 1; duration = 0; started = clock.getAsLong();
        bars.reset(); geometry = BarGeometry.NONE; baseFov = 70; snapKey = "";
    }
    /** The magnification the zoom is heading for. */
    public double heldMagnification() { return held; }
    public double currentMultiplier() { return 1/current; }
    public double magnification() { return current; }
    /** The profile of the zoom on screen, or of the last one while it eases out. */
    public ZoomMode lastActiveMode() { return lastActiveMode; }
    public void setBaseFov(double fov) { if (Double.isFinite(fov) && fov>0 && fov<180) baseFov=fov; }
    public double getBaseFov() { return baseFov; }
    public float currentBarsPercent() { return (float)(geometry.top()*100); }
    public boolean isActive() { return activeMode.isActive() || current != 1 || geometry.visible(); }
    public double getSensitivityMultiplier() {
        ZoomConfig cfg=ZoomConfig.INSTANCE;
        boolean scale=lastActiveMode==ZoomMode.REGULAR ? cfg.regularScaleSensitivity : cfg.cinematicScaleSensitivity;
        return isActive() && scale ? Math.min(1,1/current) : 1;
    }
    public boolean shouldRemoveBobbing() {
        ZoomConfig cfg=ZoomConfig.INSTANCE;
        return isActive() && (lastActiveMode==ZoomMode.REGULAR ? cfg.regularRemoveBobbing : cfg.cinematicRemoveBobbing);
    }
}
