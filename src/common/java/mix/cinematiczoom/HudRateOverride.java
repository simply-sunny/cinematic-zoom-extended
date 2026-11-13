package mix.cinematiczoom;

/**
 * Whether HUD frame-rate limiters should stand down. Mods such as Gnetum redraw the HUD at a capped
 * rate and paste a cached copy in between, which can catch the bars and make them step. While a
 * zoom, its bars or the screenshot shutter are on screen, they're switched off so everything draws
 * every frame. See mixin.GnetumConfigMixin.
 */
public final class HudRateOverride {
    private HudRateOverride() {
    }

    public static boolean active() {
        return ZoomConfig.INSTANCE.overrideHudFrameLimiters
                && (ZoomManager.isZoomActive() || ScreenshotShutter.coverage(System.nanoTime()) > 0);
    }
}
