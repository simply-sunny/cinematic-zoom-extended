package mix.cinematiczoom;

/** Which profile using a spyglass zooms with; OFF leaves the spyglass and zoom keys as they are. */
public enum SpyglassZoom {
    OFF(ZoomMode.NONE),
    CINEMATIC(ZoomMode.CINEMATIC),
    REGULAR(ZoomMode.REGULAR);

    private final ZoomMode mode;

    SpyglassZoom(ZoomMode mode) {
        this.mode = mode;
    }

    public ZoomMode mode() {
        return mode;
    }

    /**
     * With zoom stacking off, the game's own spyglass zoom replaces a zoom from the keys while the
     * scope is up, instead of multiplying with it. The key zoom comes back when the scope comes down.
     */
    public static boolean vanillaScopeTakesOver(boolean scopingInFirstPerson) {
        return scopingInFirstPerson && !enabled() && !ZoomConfig.INSTANCE.zoomStacking;
    }

    /** True when the local player's spyglass should drive the zoom instead of the vanilla scope. */
    public static boolean enabled() {
        SpyglassZoom setting = ZoomConfig.INSTANCE.spyglassZoom;
        return setting != null && setting != OFF;
    }
}
