package mix.cinematiczoom;
import java.util.*;
public final class AspectRatios {
    public static final String LEGACY = "Legacy";
    public static final List<String> DEFAULTS = List.of("Auto","2.39:1","2.35:1","2:1","1.85:1","16:9","16:10","4:3","1:1");
    private AspectRatios() {}
    public static double parse(String text) {
        if (text == null) return Double.NaN;
        if (text.equalsIgnoreCase("Auto")) return 0;
        try {
            String[] parts=text.trim().split(":",-1);
            double a=Double.parseDouble(parts[0]);
            double b=parts.length==2 ? Double.parseDouble(parts[1]) : 1;
            double ratio=a/b;
            return parts.length<=2 && a>0 && b>0 && Double.isFinite(ratio) && ratio>=.05 && ratio<=20 ? ratio : Double.NaN;
        } catch (NumberFormatException e) { return Double.NaN; }
    }
    public static List<String> validate(List<String> entries) {
        LinkedHashSet<String> valid=new LinkedHashSet<>();
        if(entries!=null) for(String entry:entries) {
            if(entry!=null && (LEGACY.equals(entry) || Double.isFinite(parse(entry)))) valid.add(entry.trim());
            if(valid.size()==64) break;
        }
        if(valid.isEmpty()) valid.add("Auto");
        return new ArrayList<>(valid);
    }
    public static String cycle(List<String> entries,String selected,int direction) {
        List<String> valid=validate(entries);
        int current=valid.indexOf(selected);
        if(current<0) return valid.get(0);
        return valid.get(Math.floorMod(current + Integer.signum(direction),valid.size()));
    }
    public static BarGeometry geometry(int width,int height,String profile,double legacyPercent) {
        if(width<=0 || height<=0) return BarGeometry.NONE;
        if(LEGACY.equals(profile)) {
            double inset=ZoomMath.finiteClamp(legacyPercent/100,0,.5,0);
            return new BarGeometry(0,0,inset,inset);
        }
        double ratio=parse(profile), actual=(double)width/height;
        if(!Double.isFinite(ratio) || ratio==0) return BarGeometry.NONE;
        if(ratio>actual) { double b=(1-actual/ratio)/2; return new BarGeometry(0,0,b,b); }
        double b=(1-ratio/actual)/2; return new BarGeometry(b,b,0,0);
    }
}
