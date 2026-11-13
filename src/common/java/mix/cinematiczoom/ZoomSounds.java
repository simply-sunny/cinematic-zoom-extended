package mix.cinematiczoom;

/** Which wheel steps get a tick, and when zooming in or out plays a sound. The game calls live in ZoomAudio. */
public final class ZoomSounds {
    public static final float TICK_VOLUME = 0.25f;
    static final float TICK_IN_PITCH = 1.6f;
    static final float TICK_OUT_PITCH = 1.25f;
    // Trackpads send many tiny scroll events; one tick per 35 ms is about as fast as a wheel clicks.
    private static final long TICK_GAP_NANOS = 35_000_000L;

    private static long lastTick = Long.MIN_VALUE / 2;
    private static boolean zoomFromSpyglass;

    private ZoomSounds() {
    }

    /** The pitch of the tick for a wheel step from one magnification to another, or 0 for none. */
    public static float wheelTick(double before, double after, long nowNanos) {
        if (!ZoomConfig.INSTANCE.wheelTicks || before == after || nowNanos - lastTick < TICK_GAP_NANOS) return 0;
        lastTick = nowNanos;
        return after > before ? TICK_IN_PITCH : TICK_OUT_PITCH;
    }

    /**
     * 1 to play the zoom-in sound, -1 for zoom-out, 0 for none. Only starting or ending a zoom
     * plays one; switching between cinematic and regular doesn't. A zoom the spyglass started
     * already has the spyglass's own sounds, in and out, and so does a zoom the scope pauses or
     * gives back (quiet).
     */
    public static int zoomTransition(ZoomMode before, ZoomMode after, boolean spyglassScoping, boolean quiet) {
        if (!before.isActive() && after.isActive()) {
            zoomFromSpyglass = spyglassScoping;
            return ZoomConfig.INSTANCE.zoomSounds && !spyglassScoping && !quiet ? 1 : 0;
        }
        if (before.isActive() && !after.isActive()) {
            boolean spyglass = zoomFromSpyglass;
            zoomFromSpyglass = false;
            return ZoomConfig.INSTANCE.zoomSounds && !spyglass && !quiet ? -1 : 0;
        }
        return 0;
    }

    static void reset() {
        zoomFromSpyglass = false;
        lastTick = Long.MIN_VALUE / 2;
    }
}
