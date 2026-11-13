package mix.cinematiczoom;

/**
 * Black bars that snap shut and open again after a screenshot, like a camera shutter. Its sound is
 * two button clicks: a bright one as the bars shut and a softer, lower one as they start to open.
 */
public final class ScreenshotShutter {
    static final long CLOSE_NANOS = 90_000_000L, HOLD_NANOS = 40_000_000L, OPEN_NANOS = 170_000_000L;
    static final long TOTAL_NANOS = CLOSE_NANOS + HOLD_NANOS + OPEN_NANOS;
    public static final float SHUT_PITCH = 1.15f, SHUT_VOLUME = 0.6f;
    public static final float REOPEN_PITCH = 0.75f, REOPEN_VOLUME = 0.45f;
    static final long REOPEN_CLICK_NANOS = CLOSE_NANOS + HOLD_NANOS;

    private static long triggeredAt = Long.MIN_VALUE / 2;
    private static boolean reopenClickPending;

    private ScreenshotShutter() {
    }

    /** Starts the shutter. Returns true when it started, so the caller plays its sound once. */
    public static boolean trigger(long nowNanos) {
        if (!ZoomConfig.INSTANCE.screenshotShutter || nowNanos - triggeredAt < TOTAL_NANOS) return false;
        triggeredAt = nowNanos;
        reopenClickPending = true;
        return true;
    }

    /** True once per shutter, on the first frame after the bars start to open. */
    public static boolean reopenClickDue(long nowNanos) {
        if (!reopenClickPending || nowNanos - triggeredAt < REOPEN_CLICK_NANOS) return false;
        reopenClickPending = false;
        return true;
    }

    /** When the shutter last started, from System.nanoTime(). */
    public static long lastTriggerNanos() {
        return triggeredAt;
    }

    /** How much of the screen height each bar covers, from 0 (open) to 0.5 (shut). */
    public static double coverage(long nowNanos) {
        long elapsed = nowNanos - triggeredAt;
        if (elapsed < 0 || elapsed >= TOTAL_NANOS) return 0;
        if (elapsed < CLOSE_NANOS) return 0.5 * easeOut((double) elapsed / CLOSE_NANOS);
        if (elapsed < CLOSE_NANOS + HOLD_NANOS) return 0.5;
        return 0.5 * (1 - easeOut((double) (elapsed - CLOSE_NANOS - HOLD_NANOS) / OPEN_NANOS));
    }

    private static double easeOut(double t) {
        return 1 - (1 - t) * (1 - t);
    }

    static void reset() {
        triggeredAt = Long.MIN_VALUE / 2;
        reopenClickPending = false;
    }
}
