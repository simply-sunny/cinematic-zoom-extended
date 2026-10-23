package mix.cinematiczoom;
import java.util.function.LongSupplier;
/** Independent elapsed-time transition. Retargeting samples the old transition first. */
public final class BarAnimator {
    private final LongSupplier clock;
    private BarGeometry start=BarGeometry.NONE, target=BarGeometry.NONE;
    private long started;
    private double duration;
    private BarAnimationMode mode=BarAnimationMode.LINEAR;
    public BarAnimator(LongSupplier clock) { this.clock=clock; }
    public BarGeometry value() {
        double t=duration<=0 ? 1 : Math.max(0,Math.min(1,(clock.getAsLong()-started)/duration));
        return t>=1 ? target : start.interpolate(target,mode.apply(t));
    }
    public void target(BarGeometry next,int milliseconds,BarAnimationMode nextMode) {
        if(next.equals(target)) return;
        start=value(); target=next; started=clock.getAsLong();
        mode=nextMode==null ? BarAnimationMode.LINEAR : nextMode;
        duration=mode==BarAnimationMode.INSTANT ? 0 : Math.max(0,milliseconds)*1_000_000.0;
    }
    public void reset() { start=target=BarGeometry.NONE; duration=0; }
}
