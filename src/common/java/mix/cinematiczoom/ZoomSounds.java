package mix.cinematiczoom;

/** When the music setting is turned down and which wheel steps get a tick. The game calls live in ZoomAudio. */
public final class ZoomSounds {
    public static final float TICK_VOLUME = 0.25f;
    static final float TICK_IN_PITCH = 1.6f;
    static final float TICK_OUT_PITCH = 1.25f;
    // Trackpads send many tiny scroll events; one tick per 35 ms is about as fast as a wheel clicks.
    private static final long TICK_GAP_NANOS = 35_000_000L;

    private static final double NOT_MUTED = -1;
    private static double savedMusicVolume = NOT_MUTED;
    private static long lastTick = Long.MIN_VALUE / 2;

    private ZoomSounds() {
    }

    public static boolean isMusicMuted() {
        return savedMusicVolume != NOT_MUTED;
    }

    /**
     * The Music volume setting to apply now, or NaN to leave it. Zooming sets it to 0 and remembers
     * the old value, and the zoom ending puts that back unless the player has changed it since.
     */
    public static double musicVolume(boolean zoomActive, double current) {
        boolean mute = zoomActive && ZoomConfig.INSTANCE.muteMusicWhileZooming;
        if (mute && !isMusicMuted()) {
            savedMusicVolume = current;
            return 0;
        }
        if (!mute && isMusicMuted()) {
            double saved = savedMusicVolume;
            savedMusicVolume = NOT_MUTED;
            return current == 0 ? saved : Double.NaN;
        }
        return Double.NaN;
    }

    /** The pitch of the tick for a wheel step from one magnification to another, or 0 for none. */
    public static float wheelTick(double before, double after, long nowNanos) {
        if (!ZoomConfig.INSTANCE.wheelTicks || before == after || nowNanos - lastTick < TICK_GAP_NANOS) return 0;
        lastTick = nowNanos;
        return after > before ? TICK_IN_PITCH : TICK_OUT_PITCH;
    }

    static void reset() {
        savedMusicVolume = NOT_MUTED;
        lastTick = Long.MIN_VALUE / 2;
    }
}
