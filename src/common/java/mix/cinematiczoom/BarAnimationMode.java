package mix.cinematiczoom;
public enum BarAnimationMode {
    INSTANT, LINEAR, SMOOTH;
    double apply(double t) { return this == INSTANT ? 1 : this == SMOOTH ? t*t*(3-2*t) : t; }
}
