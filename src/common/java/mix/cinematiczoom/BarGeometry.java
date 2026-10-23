package mix.cinematiczoom;
/** Insets as fractions of framebuffer dimensions, reusable at any GUI scale. */
public record BarGeometry(double left, double right, double top, double bottom) {
    public static final BarGeometry NONE = new BarGeometry(0,0,0,0);
    public BarGeometry {
        left = ZoomMath.finiteClamp(left,0,.5,0); right = ZoomMath.finiteClamp(right,0,.5,0);
        top = ZoomMath.finiteClamp(top,0,.5,0); bottom = ZoomMath.finiteClamp(bottom,0,.5,0);
    }
    public BarGeometry interpolate(BarGeometry other, double t) {
        return new BarGeometry(left+(other.left-left)*t, right+(other.right-right)*t,
                top+(other.top-top)*t, bottom+(other.bottom-bottom)*t);
    }
    public boolean visible() { return left>0 || right>0 || top>0 || bottom>0; }
    /** Round outward and draw side bars only inside the remaining center to avoid alpha overlap. */
    public void draw(int width, int height, int color, RectangleConsumer renderer) {
        if (width<=0 || height<=0 || !visible()) return;
        int l=(int)Math.ceil(left*width), r=(int)Math.ceil(right*width);
        int t=(int)Math.ceil(top*height), b=(int)Math.ceil(bottom*height);
        if(t>0) renderer.fill(0,0,width,t,color);
        if(b>0) renderer.fill(0,height-b,width,height,color);
        if(l>0) renderer.fill(0,t,l,height-b,color);
        if(r>0) renderer.fill(width-r,t,width,height-b,color);
    }
    @FunctionalInterface public interface RectangleConsumer { void fill(int x1,int y1,int x2,int y2,int color); }
}
