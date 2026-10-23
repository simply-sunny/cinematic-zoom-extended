package mix.cinematiczoom;
/** Camera math shared by every loader. Angles are in degrees. */
public final class ZoomMath {
    public static final double MAX_MAGNIFICATION = 500;
    public static final float MIN_MULTIPLIER = 1f / 500f;
    private ZoomMath() {}
    public static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
    public static double fov(double base, double multiplier, FovMode mode) {
        base = finiteClamp(base, 0.01, 175, 70);
        multiplier = finiteClamp(multiplier, MIN_MULTIPLIER, 2, 1);
        double value = mode == FovMode.LEGACY ? base * multiplier
                : Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(base) / 2) * multiplier));
        return finiteClamp(value, 0.00001, 175, base);
    }
    public static double multiplierForFov(double base, double fov, FovMode mode) {
        return mode == FovMode.LEGACY ? fov / base
                : Math.tan(Math.toRadians(fov)/2) / Math.tan(Math.toRadians(base)/2);
    }
}
