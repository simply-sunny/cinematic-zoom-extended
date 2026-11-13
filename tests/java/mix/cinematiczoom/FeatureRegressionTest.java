package mix.cinematiczoom;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** Deterministic acceptance tests: no Minecraft, loader, wall-clock sleeps or rendering API. */
public final class FeatureRegressionTest {
    private static int checks;
    private static final ZoomConfig CFG=ZoomConfig.INSTANCE;
    public static void main(String[] args) throws Exception {
        for (int[] size : new int[][]{{1920,1080},{1280,720},{1600,1200},{1024,1024},{3440,1440},{800,600}}) {
            for (String ratio : AspectRatios.DEFAULTS) {
                BarGeometry g=AspectRatios.geometry(size[0],size[1],ratio,15);
                near(g.left(),g.right()); near(g.top(),g.bottom());
                double parsed=AspectRatios.parse(ratio);
                if(parsed>0) near(size[0]*(1-g.left()-g.right())/(size[1]*(1-g.top()-g.bottom())),parsed);
                check(!(g.left()>0 && g.top()>0),"steady bars must use one axis");
            }
        }
        BarGeometry cinema=AspectRatios.geometry(1920,1080,"2.39:1",15);
        near(cinema.top()*1080,(1080-1920/2.39)/2);
        near(AspectRatios.geometry(1920,1080,"4:3",15).left()*1920,240);
        near(AspectRatios.geometry(1920,1080,"1:1",15).left()*1920,420);
        check(!AspectRatios.geometry(0,1080,"1:1",15).visible(),"invalid dimensions");
        check(!AspectRatios.geometry(1920,1080,"NaN",15).visible(),"invalid ratio");
        check(AspectRatios.cycle(List.of("2:1","4:3"),"2:1",-1).equals("4:3"),"left wraps");
        check(AspectRatios.cycle(List.of("2:1","4:3"),"4:3",1).equals("2:1"),"right wraps");
        check(AspectRatios.cycle(List.of(),"gone",1).equals("2.39:1"),"empty fallback");
        check(AspectRatios.cycle(List.of("1:1"),"1:1",1).equals("1:1"),"single profile");
        check(AspectRatios.cycle(List.of("4:3"),"gone",-1).equals("4:3"),"deleted selection");
        near(AspectRatios.parse("2.76:1"),2.76);
        AtomicLong clock=new AtomicLong();
        BarAnimator bars=new BarAnimator(clock::get);
        bars.target(cinema,200,BarAnimationMode.LINEAR);
        clock.set(100_000_000); near(bars.value().top(),cinema.top()/2);
        BarGeometry before=bars.value();
        BarGeometry square=AspectRatios.geometry(1920,1080,"1:1",15);
        bars.target(square,200,BarAnimationMode.SMOOTH);
        near(bars.value().top(),before.top());
        clock.addAndGet(100_000_000); near(bars.value().top(),before.top()/2); near(bars.value().left(),square.left()/2);
        clock.addAndGet(100_000_000); check(bars.value().equals(square),"exact endpoint");
        bars.target(cinema,200,BarAnimationMode.INSTANT); check(bars.value().equals(cinema),"instant");
        bars.target(BarGeometry.NONE,0,BarAnimationMode.SMOOTH); check(!bars.value().visible(),"zero duration");
        for(double zoom:new double[]{.5,1,3,10,30,100,250,499,500}) for(double fov:new double[]{30,70,110,170}) {
            double actual=ZoomMath.fov(fov,1/zoom,FovMode.ACCURATE);
            check(Double.isFinite(actual) && actual>0 && actual<=175,"finite extreme FOV");
            if(actual<175) near(ZoomMath.multiplierForFov(fov,actual,FovMode.ACCURATE),Math.max(ZoomMath.MIN_MULTIPLIER,1/zoom));
        }
        near(ZoomMath.fov(70,.25,FovMode.LEGACY),17.5);
        defaults();
        CFG.regularStartingZoom=10; CFG.regularZoomInMs=200; CFG.regularZoomOutMs=400;
        CFG.regularZoomInCurve=CFG.regularZoomOutCurve=ZoomCurve.LINEAR;
        ZoomController zoom=new ZoomController(clock::get);
        zoom.update(ZoomMode.REGULAR); clock.addAndGet(100_000_000); zoom.updateFrame(); near(zoom.magnification(),5.5);
        double previous=zoom.magnification(); zoom.update(ZoomMode.NONE); near(zoom.magnification(),previous);
        clock.addAndGet(200_000_000); zoom.updateFrame(); near(zoom.magnification(),3.25);
        clock.addAndGet(200_000_000); zoom.updateFrame(); near(zoom.magnification(),1);
        defaults(); CFG.regularStartingZoom=3; CFG.regularZoomInMs=0; CFG.regularZoomOutMs=400;
        CFG.regularZoomOutCurve=ZoomCurve.LINEAR; CFG.discreteScroll=true;
        zoom.reset(); zoom.update(ZoomMode.REGULAR); near(zoom.magnification(),3);
        zoom.onWheel(-1); clock.addAndGet(200_000_000); zoom.updateFrame(); near(zoom.magnification(),2.5);
        zoom.onWheel(10000); near(zoom.magnification(),500);
        check(!zoom.barGeometry(1920,1080).visible(),"regular mode never has bars");
        check(!zoom.onWheel(Double.NaN) && !zoom.onWheel(Double.POSITIVE_INFINITY),"nonfinite wheel rejected");
        check(zoom.wheelZooms(),"the wheel zooms during a zoom");
        CFG.mouseWheelEnabled=false; check(!zoom.wheelZooms() && !zoom.onWheel(1),"scroll zoom off leaves the wheel free"); CFG.mouseWheelEnabled=true;
        zoom.reset(); check(!zoom.wheelZooms(),"the wheel is free with no zoom");
        defaults(); CFG.cinematicZoomInMs=1000; CFG.barEnterMs=200;
        zoom.reset(); zoom.update(ZoomMode.CINEMATIC); clock.addAndGet(200_000_000); zoom.updateFrame();
        near(zoom.barGeometry(1920,1080).top(),cinema.top()); check(zoom.magnification()<3,"bars finish independently");
        double mag=zoom.magnification();
        CFG.aspectRatio="4:3"; zoom.barGeometry(1920,1080); near(zoom.magnification(),mag);
        clock.addAndGet(240_000_000); near(zoom.barGeometry(1920,1080).left(),.125);
        zoom.update(ZoomMode.REGULAR); check(!zoom.barGeometry(1920,1080).visible(),"cinematic switch clears bars");
        // Zoom percentage readout: shown on start and on a wheel change, then faded out.
        defaults(); CFG.regularStartingZoom=3; zoom.reset(); zoom.update(ZoomMode.REGULAR);
        check(zoom.readoutText().equals("300%") && zoom.readoutAlpha()==255,"readout shows on zoom start");
        clock.addAndGet(1_750_000_000); check(zoom.readoutAlpha()>0 && zoom.readoutAlpha()<255,"readout fades");
        clock.addAndGet(500_000_000); check(zoom.readoutAlpha()==0,"readout hides");
        CFG.discreteScroll=true; zoom.onWheel(1); check(zoom.readoutText().equals("400%") && zoom.readoutAlpha()==255,"readout shows on scroll");
        clock.addAndGet(3_000_000_000L); CFG.regularStartingZoom=500; zoom.reset(); zoom.update(ZoomMode.REGULAR); clock.addAndGet(3_000_000_000L);
        zoom.onWheel(1); check(zoom.readoutAlpha()==0,"a scroll at the limit leaves the readout hidden");
        CFG.showZoomPercentage=false; zoom.onWheel(-1); check(zoom.readoutAlpha()==0,"readout off hides it");
        zoom.update(ZoomMode.NONE); CFG.showZoomPercentage=true; check(zoom.readoutAlpha()==0,"readout hides when the zoom ends");
        // Screenshot shutter: snaps shut, holds, opens, and ignores a second trigger mid-way.
        defaults(); ScreenshotShutter.reset(); long t=5_000_000_000L;
        check(ScreenshotShutter.trigger(t),"shutter starts"); near(ScreenshotShutter.coverage(t),0);
        near(ScreenshotShutter.coverage(t+ScreenshotShutter.CLOSE_NANOS),.5);
        check(ScreenshotShutter.coverage(t+ScreenshotShutter.CLOSE_NANOS+ScreenshotShutter.HOLD_NANOS+50_000_000)<.5,"shutter opens");
        near(ScreenshotShutter.coverage(t+ScreenshotShutter.TOTAL_NANOS),0);
        check(!ScreenshotShutter.trigger(t+50_000_000),"a second screenshot mid-shutter doesn't restart it");
        CFG.screenshotShutter=false; ScreenshotShutter.reset(); check(!ScreenshotShutter.trigger(t),"shutter off");
        ScreenshotShutter.reset();
        defaults(); CFG.cinematicStartingZoom=Float.NaN; CFG.minZoomMultiplier=Float.POSITIVE_INFINITY; CFG.barOpacity=Float.NaN; CFG.clamp();
        check(Float.isFinite(CFG.cinematicStartingZoom) && Float.isFinite(CFG.minZoomMultiplier) && Float.isFinite(CFG.barOpacity),"finite config sanitation");
        Path dir=Files.createTempDirectory("cinematiczoom-migration-");
        ZoomConfig.configureDirectory(dir);
        Path file=dir.resolve("cinematiczoom").resolve("config.json");
        Files.writeString(dir.resolve("cinematiczoom.json"),"{\"cinematicStartingZoom\":7,\"cinematicBarsPercent\":22,\"minZoomMultiplier\":0.2,\"customUnknown\":42}");
        CFG.load(); near(CFG.cinematicStartingZoom,5); near(CFG.cinematicBarsPercent,22);
        check(CFG.aspectRatio.equals("Legacy") && CFG.fovMode==FovMode.LEGACY,"legacy appearance preserved");
        check(Files.exists(file)&&!Files.exists(dir.resolve("cinematiczoom.json")),"old config moved into the mod's folder");
        check(Files.exists(file.resolveSibling("backups").resolve("v1.json")),"migration backup");
        CFG.save(); check(Files.readString(file).contains("customUnknown"),"unknown settings preserved");
        Files.writeString(file,"{\"configVersion\":2,\"regularStartingZoom\":8,\"barEnterMode\":\"FUTURE\",\"wheelStep\":\"oops\"}");
        CFG.load(); near(CFG.regularStartingZoom,8); check(CFG.barEnterMode==BarAnimationMode.SMOOTH,"unknown enum fallback");
        Files.writeString(file,"{\"configVersion\":2,\"regularStartingZoom\":9,\"allowUltrawide\":{},\"maxZoomMultiplier\":\"oops\",\"smoothMs\":[],\"cinematicHideHud\":true}");
        CFG.load(); near(CFG.regularStartingZoom,9); check(CFG.cinematicHideHud,"malformed aliases preserve valid fields");
        Files.writeString(file,"{\"configVersion\":2,\"aspectRatio\":\"Auto\",\"aspectProfiles\":[\"Auto\",\"2.39:1\",\"4:3\"]}");
        CFG.load(); check(CFG.aspectRatio.equals("2.39:1") && !CFG.aspectProfiles.contains("Auto"),"old Auto profile removed");
        Files.writeString(file,"{\"configVersion\":2,\"barEnterMode\":\"LINEAR\",\"barExitMode\":\"LINEAR\",\"barSwitchMode\":\"INSTANT\"}");
        CFG.load(); check(CFG.barEnterMode==BarAnimationMode.SMOOTH && CFG.barExitMode==BarAnimationMode.SMOOTH && CFG.barSwitchMode==BarAnimationMode.INSTANT,"1.2 Linear bars become Smooth, other choices kept");
        Files.writeString(file,"{\"configVersion\":3,\"barEnterMode\":\"LINEAR\"}");
        CFG.load(); check(CFG.barEnterMode==BarAnimationMode.LINEAR,"Linear chosen after 1.3 is kept");
        Files.delete(file); CFG.load(); near(CFG.regularStartingZoom,3); check(CFG.fovMode==FovMode.ACCURATE,"missing config restores fresh defaults");
        Files.writeString(file,"{broken"); CFG.load();
        try(var paths=Files.list(file.resolveSibling("backups"))) { check(paths.anyMatch(p->p.getFileName().toString().startsWith("invalid-")),"corrupt file backed up"); }
        ZoomConfig.configureDirectory(Path.of(System.getProperty("cinematiczoom.testConfig")));
        System.out.println(checks+" feature assertions passed");
    }
    private static void defaults() throws Exception {
        ZoomConfig fresh=new ZoomConfig();
        for(var field:ZoomConfig.class.getFields()) if(!java.lang.reflect.Modifier.isStatic(field.getModifiers())) field.set(CFG,field.get(fresh));
    }
    private static void near(double a,double b) { check(Math.abs(a-b)<.0001,"expected "+b+", got "+a); }
    private static void check(boolean pass,String message) { checks++; if(!pass) throw new AssertionError(message); }
}
