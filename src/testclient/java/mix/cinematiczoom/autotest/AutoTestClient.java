package mix.cinematiczoom.autotest;




import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;

/**
 * Test-only client entrypoint. It is compiled and registered only when Gradle is run with
 * {@code -PtestClient}; release jars never contain this class or entrypoint.
 *
 * <p>The helper loads a demo world, forces the cinematic zoom key down, saves a normal and a
 * zoomed screenshot through the game's own screenshot code, writes {@code autotest/status.txt},
 * and stops the client. All game classes are accessed reflectively so one source file works
 * across every supported Minecraft version.</p>
 */
public final class AutoTestClient {
    private static final String TARGET = System.getProperty("cinematiczoom.test.target", "unknown");
    private static final int WORLD_TIMEOUT_TICKS = 20 * 180;
    private static final int PHASE_TIMEOUT_TICKS = 20 * 12;
    private static final int WARMUP_TICKS = 200;
    private static final int ZOOM_WAIT_TICKS = 70;
    private static final int STOP_DELAY_TICKS = 30;
    private static final boolean BOOTSTRAP = Boolean.getBoolean("cinematiczoom.test.bootstrap");

    private enum Phase {
        WAIT_WORLD,
        WARMUP,
        WAIT_NORMAL,
        PRESS_ZOOM,
        WAIT_ZOOM,
        WAIT_ZOOMED,
        FEATURES,
        DONE,
        ERROR
    }

    private final File runDirectory = new File(System.getProperty("user.dir"));
    private final File outputDirectory = new File(runDirectory, "autotest");
    private final File normalFile = new File(new File(runDirectory, "screenshots"), TARGET + "-normal.png");
    private final File zoomedFile = new File(new File(runDirectory, "screenshots"), TARGET + "-zoomed.png");

    private Phase phase = Phase.WAIT_WORLD;
    private int ticks;
    private int featureIndex;
    private static final String[] FEATURES = {"aspect-4-3", "aspect-1-1", "regular-500x", "exit", "settings", "linear-midpoint", "smooth-midpoint", "spyglass", "replay"};
    private static final int SPYGLASS = 7;
    private static final int REPLAY = 8;
    /** While set, the test mixin replaces the game's FOV the way Flashback's replay camera does. */
    public static volatile boolean replayFov;
    public static final float REPLAY_FOV = 37f;
    // Flashback is Fabric-only, so only Fabric test runs load the replay stand-in mixin.
    private static final boolean REPLAY_TEST = Boolean.getBoolean("cinematiczoom.test.replay");
    private boolean rotated;
    private volatile boolean normalShotDone;
    private volatile boolean zoomShotDone;

    public void start() {
        outputDirectory.mkdirs();
        normalFile.delete();
        zoomedFile.delete();
        for (String feature : FEATURES) new File(new File(runDirectory, "screenshots"), TARGET + "-" + feature + ".png").delete();
        new File(outputDirectory, "status.txt").delete();
        log("starting automation for " + TARGET);

    }

    public void tick(Object client) {
        try {
            if (BOOTSTRAP) {
                bootstrapTick(client);
                return;
            }
            switch (phase) {
                case WAIT_WORLD -> waitForWorld(client);
                case WARMUP -> warmup(client);
                case WAIT_NORMAL -> waitForNormal(client);
                case PRESS_ZOOM -> pressZoom(client);
                case WAIT_ZOOM -> waitForZoom(client);
                case WAIT_ZOOMED -> waitForZoomed(client);
                case FEATURES -> featureTick(client);
                case DONE -> stopAfterDelay(client);
                case ERROR -> stopAfterError(client);
            }
        } catch (Throwable t) {
            fail("exception: " + t);
        }
    }

    private void bootstrapTick(Object client) {
        if (phase == Phase.DONE) {
            stopAfterDelay(client);
            return;
        }
        if (phase == Phase.ERROR) {
            stopAfterError(client);
            return;
        }
        if (++ticks > WORLD_TIMEOUT_TICKS) {
            fail("bootstrap world did not load within " + (WORLD_TIMEOUT_TICKS / 20) + "s");
            return;
        }
        Object world = getQuietly(client, "world", "level");
        Object player = getQuietly(client, "player");
        if (world != null && player != null) {
            writeStatus("ok\nbootstrap=true\n");
            log("bootstrap world ready");
            phase = Phase.DONE;
            ticks = 0;
            return;
        }
        if (ticks % 20 == 0) {
            pressTitleButton(client, "Play Demo World", "Continue", "Proceed to main menu");
        }
    }

    private void waitForWorld(Object client) throws Exception {
        if (++ticks > WORLD_TIMEOUT_TICKS) {
            fail("world did not load within " + (WORLD_TIMEOUT_TICKS / 20) + "s");
            return;
        }
        Object world = get(client, "world", "level");
        Object player = get(client, "player");
        Object screen = currentScreen(client);
        if (screen != null && screen.getClass().getSimpleName().equals("LoadingErrorScreen") && ticks % 20 == 0) {
            Object errors = getQuietly(screen, "modLoadErrors");
            Object warnings = getQuietly(screen, "modLoadWarnings");
            log("loader diagnostics: errors=" + errors + " warnings=" + warnings);
            if (errors instanceof java.util.Collection<?> c && !c.isEmpty()) {
                fail("loader rejected mod: " + errors); return;
            }
        }
        if (world != null && player != null) {
            if (screen != null) {
                log("dismissing screen " + screen.getClass().getSimpleName());
                if (!pressScreenButton(screen, "Continue", "OK", "Yes", "Proceed", "Got it", "Back")) {
                    closeScreen(screen);
                }
                return;
            }
            prepareScene(client);
            phase = Phase.WARMUP;
            ticks = 0;
            log("world loaded");
            return;
        }
        if (ticks % 40 == 0) {
            log("waiting for world: screen=" + (screen == null ? "null" : screen.getClass().getName())
                    + " world=" + (world != null) + " player=" + (player != null));
        }
        if (world == null && ticks % 20 == 0) {
            pressTitleButton(client, "Play Demo World", "Continue", "Proceed to main menu");
        }
    }

    private void prepareScene(Object client) {
        try {
            disableDemo(client);
            disableTutorial(client);
            Object server = call(client, "getSingleplayerServer", "getServer");
            if (server == null) {
                log("no integrated server; leaving the world as generated");
                return;
            }
            Object commands = call(server, "getCommandManager", "getCommands");
            Object source = call(server, "getCommandSource", "createCommandSourceStack");
            if (commands == null || source == null) {
                log("no server command handler; leaving the world as generated");
                return;
            }
            String[] setup = {
                    "gamerule sendCommandFeedback false",
                    "gamerule doDaylightCycle false",
                    "gamerule doWeatherCycle false",
                    "gamerule doMobSpawning false",
                    "difficulty peaceful",
                    "time set noon",
                    "weather clear",
                    "gamemode creative @p",
                    "tp @p 0.5 120 5.5 180 0",
                    "forceload add -24 -44 24 12"
            };
            for (String command : setup) {
                onServerThread(server, () -> executeCommand(commands, source, command));
            }
            log("scene ready");
        } catch (Throwable t) {
            log("scene setup skipped: " + t);
        }
    }

    private void buildSceneFills(Object client) {
        try {
            Object server = call(client, "getSingleplayerServer", "getServer");
            if (server == null) {
                return;
            }
            Object commands = call(server, "getCommandManager", "getCommands");
            Object source = call(server, "getCommandSource", "createCommandSourceStack");
            if (commands == null || source == null) {
                return;
            }
            String[] fills = {
                    "fill -24 119 -44 24 119 12 minecraft:stone",
                    "fill -24 120 -44 24 132 -44 minecraft:white_concrete",
                    "fill -24 120 -44 -24 132 -44 minecraft:white_concrete",
                    "fill 24 120 -44 24 132 -44 minecraft:white_concrete",
                    "fill -24 132 -44 24 132 -44 minecraft:white_concrete",
                    "fill -16 120 -32 -16 128 -32 minecraft:red_concrete",
                    "fill 0 120 -32 0 128 -32 minecraft:lime_concrete",
                    "fill 16 120 -32 16 128 -32 minecraft:blue_concrete",
                    "fill -8 120 -20 -8 124 -20 minecraft:yellow_concrete",
                    "fill 8 120 -20 8 124 -20 minecraft:cyan_concrete"
            };
            for (String command : fills) {
                onServerThread(server, () -> executeCommand(commands, source, command));
            }
            log("scene blocks placed");
        } catch (Throwable t) {
            log("scene fill skipped: " + t);
        }
    }

    private void disableTutorial(Object client) {
        try {
            Object manager = call(client, "getTutorialManager", "getTutorial");
            if (manager != null) {
                for (Method method : manager.getClass().getMethods()) {
                    if (!method.getName().equals("setStep") || method.getParameterCount() != 1) {
                        continue;
                    }
                    Class<?> stepType = method.getParameterTypes()[0];
                    if (!stepType.isEnum()) {
                        continue;
                    }
                    for (Object value : stepType.getEnumConstants()) {
                        if (((Enum<?>) value).name().equals("NONE")) {
                            method.invoke(manager, value);
                            log("tutorial disabled");
                            break;
                        }
                    }
                }
            }
            Object toasts = call(client, "getToastManager");
            if (toasts != null) {
                call(toasts, "clear");
            }
        } catch (Throwable t) {
            log("could not disable tutorial: " + t);
        }
    }

    private void clearToasts(Object client) {
        try {
            Object toasts = call(client, "getToastManager");
            if (toasts != null) {
                call(toasts, "clear");
            }
        } catch (Throwable ignored) {
            // toasts are cosmetic
        }
    }

    private void disableDemo(Object client) {
        try {
            Field demo = findField(client.getClass(), "demo");
            if (demo != null && demo.getType() == boolean.class) {
                demo.setAccessible(true);
                demo.setBoolean(client, false);
                log("demo mode disabled");
            }
        } catch (Throwable t) {
            log("could not disable demo mode: " + t);
        }
    }

    /** Commands load chunks, so running them on the client thread can hang it if the server stops. */
    private static void onServerThread(Object server, Runnable command) {
        ((java.util.concurrent.Executor) server).execute(command);
    }

    private static void executeCommand(Object handler, Object source, String command) {
        for (Method method : handler.getClass().getMethods()) {
            String name = method.getName();
            if (!name.equals("executeWithPrefix") && !name.equals("performPrefixedCommand") && !name.equals("parseAndExecute") && !name.equals("execute")) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length != 2 || parameters[1] != String.class || !parameters[0].isInstance(source)) {
                continue;
            }
            try {
                method.invoke(handler, source, command);
                return;
            } catch (ReflectiveOperationException ignored) {
                // try the next matching command method
            }
        }
        log("command not executed: " + command);
    }

    private void warmup(Object client) throws Exception {
        focus(client);
        if (ticks == 1 || ticks % 40 == 0) {
            log("input: active=" + callStatic("mix.cinematiczoom.ZoomManager", "isZoomActive")
                    + " hideHud=" + callStatic("mix.cinematiczoom.ZoomManager", "shouldHideHud")
                    + " keyPressed=" + zoomKeyPressed()
                    + " rawC=" + rawKeyDown(client, 67)
                    + " focused=" + call(client, "isWindowFocused"));
        }
        if (++ticks > WARMUP_TICKS + PHASE_TIMEOUT_TICKS) {
            fail("scene or input did not settle");
            return;
        }
        Object screen = currentScreen(client);
        if (screen != null) {
            String name = screen.getClass().getSimpleName();
            if (!name.contains("Loading") && !name.contains("Receiving") && !name.contains("Downloading")) {
                log("dismissing in-game screen " + name);
                if (!pressScreenButton(screen, "Continue Playing", "Continue", "Back to Game", "OK", "Yes", "Proceed", "Got it", "Back")) {
                    closeScreen(screen);
                }
            }
            return;
        }
        if (ticks % 20 == 0) {
            preparePlayer(client);
        }
        if (ticks == 20) {
            buildSceneFills(client);
        }
        if (ticks % 10 == 0) {
            clearToasts(client);
        }
        if (!rotated) {
            rotated = true;
            LevelView.faceForward(get(client, "player"));
        }
        if (ticks >= WARMUP_TICKS) {
            if ((!sceneSettled(client) || !normalReady(client)) && ticks < WARMUP_TICKS + PHASE_TIMEOUT_TICKS) {
                if (ticks % 40 == 0) {
                    log("waiting for a clean normal frame: " + LevelView.describe(get(client, "player")));
                }
                return;
            }
            if (!sceneSettled(client) || !normalReady(client)) {
                fail("scene or input did not settle before normal capture");
                return;
            }
            takeScreenshot(client, "normal");
            phase = Phase.WAIT_NORMAL;
            ticks = 0;
        }
    }

    private void preparePlayer(Object client) {
        try {
            Object server = call(client, "getSingleplayerServer", "getServer");
            if (server == null) {
                return;
            }
            Object commands = call(server, "getCommandManager", "getCommands");
            Object source = call(server, "getCommandSource", "createCommandSourceStack");
            if (commands == null || source == null) {
                return;
            }
            executeCommand(commands, source, "gamemode creative @p");
            executeCommand(commands, source, "tp @p 0.5 120 5.5 180 0");
        } catch (Throwable t) {
            log("player setup skipped: " + t);
        }
    }

    private static boolean normalReady(Object client) throws Exception {
        Object active = callStatic("mix.cinematiczoom.ZoomManager", "isZoomActive");
        Object pressed = zoomKeyPressed();
        Object raw = rawKeyDown(client, 67);
        return !Boolean.TRUE.equals(active)
                && !Boolean.TRUE.equals(pressed)
                && !Integer.valueOf(1).equals(raw);
    }

    private static boolean sceneSettled(Object client) throws Exception {
        Object player = get(client, "player");
        if (player == null) {
            return false;
        }
        Object x = call(player, "getX");
        Object y = call(player, "getY");
        Object z = call(player, "getZ");
        if (!(x instanceof Number) || !(y instanceof Number) || !(z instanceof Number)) {
            return true;
        }
        boolean inPlace = Math.abs(((Number) x).doubleValue() - 0.5) < 2.0
                && Math.abs(((Number) y).doubleValue() - 120.0) < 3.0
                && Math.abs(((Number) z).doubleValue() - 5.5) < 2.0;
        Object ground = call(player, "isOnGround", "onGround");
        boolean grounded = !(ground instanceof Boolean) || (Boolean) ground;
        return inPlace && grounded;
    }

    private void waitForNormal(Object client) {
        if (normalShotDone || normalFile.isFile() || ++ticks > PHASE_TIMEOUT_TICKS) {
            if (!normalFile.isFile() && !normalShotDone) {
                fail("normal screenshot was not written");
                return;
            }
            phase = Phase.PRESS_ZOOM;
            ticks = 0;
        }
    }

    private void pressZoom(Object client) throws Exception {
        focus(client);
        setZoomKeyDown(true);
        phase = Phase.WAIT_ZOOM;
        ticks = 0;
        log("zoom key held: pressed=" + zoomKeyPressed()
                + " active=" + callStatic("mix.cinematiczoom.ZoomManager", "isZoomActive"));
    }

    private void waitForZoom(Object client) throws Exception {
        focus(client);
        setZoomKeyDown(true);
        if (++ticks >= ZOOM_WAIT_TICKS) {
            if (!Boolean.TRUE.equals(callStatic("mix.cinematiczoom.ZoomManager", "isZoomActive"))) {
                Object open = currentScreen(client);
                fail("cinematic zoom did not activate: screen=" + (open == null ? null : open.getClass().getSimpleName())
                        + " windowActive=" + call(client, "isWindowActive", "isWindowFocused")); return;
            }
            takeScreenshot(client, "zoomed");
            phase = Phase.WAIT_ZOOMED;
            ticks = 0;
        }
    }

    private void waitForZoomed(Object client) throws Exception {
        setZoomKeyDown(true);
        if (zoomShotDone || zoomedFile.isFile() || ++ticks > PHASE_TIMEOUT_TICKS) {
            setZoomKeyDown(false);
            if (!zoomedFile.isFile()) {
                fail("zoomed screenshot was not written");
                return;
            }
            phase = Phase.FEATURES;
            ticks = 0;
            featureIndex = 0;

        }
    }

    private void featureTick(Object client) throws Exception {
        focus(client);
        mix.cinematiczoom.ZoomConfig cfg = mix.cinematiczoom.ZoomConfig.INSTANCE;
        setZoomKeyDown(false);
        if (featureIndex == REPLAY && !REPLAY_TEST) featuresDone = true;
        if (featuresDone) {
            if (++ticks == 1) pressScreenshotKey(client);
            if (ticks >= 40) {
                if (mix.cinematiczoom.ScreenshotShutter.lastTriggerNanos() == Long.MIN_VALUE / 2) { fail("screenshot shutter did not start"); return; }
                writeStatus("ok\nfeatures=" + FEATURES.length + "\n");
                phase = Phase.DONE;
                ticks = 0;
            }
            return;
        }
        setKeyDown("ZOOM_NO_BARS_KEYBIND", false);
        if (++ticks == 1) {
            if (featureIndex >= 5) {
                Object open = currentScreen(client);
                if (open != null) closeScreen(open);
                cfg.barEnterMs = 10000;
                cfg.barEnterMode = featureIndex == 5 ? mix.cinematiczoom.BarAnimationMode.LINEAR : mix.cinematiczoom.BarAnimationMode.SMOOTH;
                cfg.aspectRatio = "2.39:1";
            }
            cfg.aspectProfiles = new java.util.ArrayList<>(java.util.List.of("2.39:1","4:3","1:1"));
            cfg.aspectRatio = featureIndex == 0 ? "2.39:1" : featureIndex == 1 ? "4:3" : "1:1";
            if (featureIndex >= 5) cfg.aspectRatio = "2.39:1";
            cfg.regularStartingZoom = 500;
            cfg.minZoomMultiplier = mix.cinematiczoom.ZoomMath.MIN_MULTIPLIER;
            if (featureIndex == REPLAY) {
                cfg.barEnterMs = 0;
                replayFov = true;
            }
            if (featureIndex == SPYGLASS) {
                cfg.barEnterMs = 0;
                cfg.spyglassZoom = mix.cinematiczoom.SpyglassZoom.CINEMATIC;
                runCommand(client, "item replace entity @p weapon.mainhand with spyglass");
            }
        }
        // Release both modes before entering a new one, preserving starting-zoom semantics.
        if (ticks > 10 && (featureIndex < 2 || (featureIndex >= 5 && featureIndex != SPYGLASS))) setZoomKeyDown(true);
        if (featureIndex == SPYGLASS) setUseKeyDown(client, ticks > 10 && ticks <= 100);
        if (ticks > 10 && featureIndex == 2) setKeyDown("ZOOM_NO_BARS_KEYBIND", true);
        if (featureIndex < 2) setKeyDown("NEXT_ASPECT_KEYBIND", ticks >= 20 && ticks < 30);
        if (featureIndex == 4 && ticks == 20) {
            Object screen;
            try {
                Class<?> settings = Class.forName("mix.cinematiczoom.ZoomSettingsScreen");
                Method create = java.util.Arrays.stream(settings.getMethods()).filter(m -> m.getName().equals("create")).findFirst().orElseThrow();
                screen = create.invoke(null, new Object[]{null});
            } catch (ClassNotFoundException fabric) {
                Object integration = Class.forName("mix.cinematiczoom.integrations.ModMenuIntegration").getConstructor().newInstance();
                Object factory = integration.getClass().getMethod("getModConfigScreenFactory").invoke(integration);
                Class<?> factoryType = Class.forName("com.terraformersmc.modmenu.api.ConfigScreenFactory");
                Method create = java.util.Arrays.stream(factoryType.getMethods()).filter(m -> m.getName().equals("create")).findFirst().orElseThrow();
                screen = create.invoke(factory, new Object[]{null});
            }
            Object screenOwner = client;
            Method setScreen = java.util.Arrays.stream(screenOwner.getClass().getMethods()).filter(m -> m.getName().equals("setScreen") && m.getParameterCount()==1).findFirst().orElse(null);
            if (setScreen == null) {
                screenOwner = get(client, "gui");
                setScreen = java.util.Arrays.stream(screenOwner.getClass().getMethods()).filter(m -> m.getName().equals("setScreen") && m.getParameterCount()==1).findFirst().orElseThrow();
            }
            setScreen.invoke(screenOwner, screen);
        }
        if (ticks == 100) {
            if (featureIndex < 2 && !cfg.aspectRatio.equals(featureIndex == 0 ? "4:3" : "1:1")) throw new IllegalStateException("profile key did not cycle exactly once");
            if (featureIndex == 2) {
                double multiplier = ((Number)callStatic("mix.cinematiczoom.ZoomManager", "getCurrentFovMul")).doubleValue();
                if (Math.abs(multiplier - .002) > .000001) throw new IllegalStateException("500x was not reached: " + multiplier);
            }
            if (featureIndex == 4 && currentScreen(client) == null) throw new IllegalStateException("configuration screen did not open");
            if (featureIndex == SPYGLASS) {
                Object player = get(client, "player");
                if (!Boolean.TRUE.equals(call(player, "isUsingItem"))) throw new IllegalStateException("spyglass was not in use");
                if (!Boolean.TRUE.equals(callStatic("mix.cinematiczoom.ZoomManager", "isZoomActive"))) throw new IllegalStateException("spyglass did not start the zoom");
                if (Boolean.TRUE.equals(call(player, "isScoping", "isUsingSpyglass"))) throw new IllegalStateException("vanilla spyglass scope was not replaced");
            }
            if (featureIndex == REPLAY) {
                double base = ((Number) callStatic("mix.cinematiczoom.ZoomManager", "getBaseFov")).doubleValue();
                double multiplier = ((Number) callStatic("mix.cinematiczoom.ZoomManager", "getCurrentFovMul")).doubleValue();
                if (Math.abs(base - REPLAY_FOV) > .001) throw new IllegalStateException("zoom did not see the replay FOV: " + base);
                if (multiplier > .5) throw new IllegalStateException("zoom did not apply over the replay FOV: " + multiplier);
            }
            takeScreenshot(client, FEATURES[featureIndex]);
        }
        File capture = new File(new File(runDirectory,"screenshots"), TARGET + "-" + FEATURES[featureIndex] + ".png");
        if (ticks > 100 && capture.isFile()) {
            replayFov = false;
            if (featureIndex == SPYGLASS) {
                setUseKeyDown(client, false);
                cfg.spyglassZoom = mix.cinematiczoom.SpyglassZoom.OFF;
            }
            if (++featureIndex == FEATURES.length) {
                featuresDone = true;
            }
            ticks=0;
        }
        if (ticks > 200) fail("feature screenshot timed out");
    }

    private void stopAfterDelay(Object client) {
        if (++ticks >= STOP_DELAY_TICKS) {
            stop(client);
        }
    }

    private void stopAfterError(Object client) {
        if (++ticks >= STOP_DELAY_TICKS) {
            stop(client);
        }
    }

    private void takeScreenshot(Object client, String kind) throws Exception {
        clearToasts(client);
        log("state: zoomActive=" + callStatic("mix.cinematiczoom.ZoomManager", "isZoomActive")
                + " hideHud=" + callStatic("mix.cinematiczoom.ZoomManager", "shouldHideHud")
                + " mode=" + get(client, "currentScreen", "screen"));
        Object target = renderTarget(client);
        if (target == null) {
            throw new IllegalStateException("no render target");
        }
        Class<?> screenshotClass = screenshotClass();
        Method method = screenshotMethod(screenshotClass, target);
        if (method == null) {
            throw new IllegalStateException("no screenshot method on " + screenshotClass.getName());
        }
        String name = TARGET + "-" + kind + ".png";
        Consumer<Object> consumer = message -> {
            if ("normal".equals(kind)) {
                normalShotDone = true;
            } else {
                zoomShotDone = true;
            }
            log(kind + " screenshot callback: " + message);
        };
        Object[] args = method.getParameterCount() == 5
                ? new Object[]{runDirectory, name, target, 1, consumer}
                : new Object[]{runDirectory, name, target, consumer};
        method.invoke(null, args);
        log("requested " + kind + " screenshot");
    }

    private static Class<?> screenshotClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.client.Screenshot");
        } catch (ClassNotFoundException ignored) {
            return Class.forName("net.minecraft.client.util.ScreenshotRecorder");
        }
    }

    private static Method screenshotMethod(Class<?> type, Object target) {
        Method fallback = null;
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!method.getName().equals("saveScreenshot") && !method.getName().equals("grab")) {
                continue;
            }
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length < 4 || parameters[0] != File.class || parameters[1] != String.class) {
                continue;
            }
            if (!Consumer.class.isAssignableFrom(parameters[parameters.length - 1])) {
                continue;
            }
            if (!parameters[2].isInstance(target)) {
                continue;
            }
            if (parameters.length == 5 && parameters[3] == int.class) {
                return method;
            }
            if (parameters.length == 4) {
                fallback = method;
            }
        }
        return fallback;
    }

    private static Object renderTarget(Object client) throws Exception {
        Object target = call(client, "getFramebuffer", "getMainRenderTarget");
        if (target != null) {
            return target;
        }
        Object renderer = get(client, "gameRenderer");
        if (renderer == null) {
            renderer = call(client, "getGameRenderer");
        }
        if (renderer != null) {
            target = call(renderer, "mainRenderTarget", "getMainRenderTarget", "getFramebuffer");
        }
        return target;
    }

    private static void focus(Object client) throws Exception {
        Object options = getQuietly(client, "options");
        if (options != null) {
            Field pause = findField(options.getClass(), "pauseOnLostFocus");
            if (pause != null && pause.getType() == boolean.class) pause.setBoolean(options, false);
        }
        Field active = findField(client.getClass(), "windowActive", "windowFocused");
        if (active != null && active.getType() == boolean.class) active.setBoolean(client, true);
        Object screen = currentScreen(client);
        if (screen != null && screen.getClass().getSimpleName().equals("PauseScreen")) closeScreen(screen);
        // Mark the window focused without asking the OS, so test windows stay behind other apps.
        Object window = call(client, "getWindow");
        if (window == null) {
            return;
        }
        Field focused = findField(window.getClass(), "focused");
        if (focused != null && focused.getType() == boolean.class) {
            focused.setBoolean(window, true);
        }
    }

    private boolean featuresDone;

    /** Saves a screenshot the way the screenshot key does, through the overload the shutter hooks. */
    private static void pressScreenshotKey(Object client) throws Exception {
        Object target = renderTarget(client);
        for (Method method : screenshotClass().getMethods()) {
            Class<?>[] p = method.getParameterTypes();
            if (Modifier.isStatic(method.getModifiers()) && (method.getName().equals("grab") || method.getName().equals("saveScreenshot"))
                    && p.length == 3 && p[0] == File.class && p[1].isInstance(target) && Consumer.class.isAssignableFrom(p[2])) {
                Consumer<Object> consumer = message -> log("key screenshot callback: " + message);
                method.invoke(null, new File(System.getProperty("user.dir")), target, consumer);
                return;
            }
        }
        throw new IllegalStateException("no screenshot-key method");
    }

    private static void runCommand(Object client, String command) throws Exception {
        Object server = call(client, "getSingleplayerServer", "getServer");
        Object commands = call(server, "getCommandManager", "getCommands");
        Object source = call(server, "getCommandSource", "createCommandSourceStack");
        onServerThread(server, () -> executeCommand(commands, source, command));
    }

    private static void setUseKeyDown(Object client, boolean down) throws Exception {
        Object key = get(get(client, "options"), "keyUse", "useKey");
        Method setter = findMethod(key.getClass(), "setPressed", boolean.class);
        if (setter == null) setter = findMethod(key.getClass(), "setDown", boolean.class);
        setter.invoke(key, down);
    }

    private static void setZoomKeyDown(boolean down) throws Exception {
        setKeyDown("ZOOM_KEYBIND", down);
    }

    private static void setKeyDown(String fieldName, boolean down) throws Exception {
        Class<?> clientClass = Class.forName("mix.cinematiczoom.CinematicZoomClient");
        Field keyField = clientClass.getField(fieldName);
        Object key = keyField.get(null);
        if (key == null) {
            return;
        }
        Method setter = findMethod(key.getClass(), "setPressed", boolean.class);
        if (setter == null) {
            setter = findMethod(key.getClass(), "setDown", boolean.class);
        }
        if (setter != null) {
            setter.invoke(key, down);
            return;
        }
        Field pressed = findField(key.getClass(), "pressed", "isDown");
        if (pressed != null) {
            pressed.setBoolean(key, down);
        }
    }

    private static Object zoomKeyPressed() {
        try {
            Class<?> clientClass = Class.forName("mix.cinematiczoom.CinematicZoomClient");
            Object key = clientClass.getField("ZOOM_KEYBIND").get(null);
            return key == null ? null : call(key, "isPressed", "isDown");
        } catch (Throwable t) {
            return "error:" + t.getClass().getSimpleName();
        }
    }

    private static Object rawKeyDown(Object client, int keyCode) {
        try {
            Object window = call(client, "getWindow");
            Object handle = call(window, "getHandle");
            if (!(handle instanceof Number number)) {
                return "no-handle";
            }
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            Method getKey = glfw.getMethod("glfwGetKey", long.class, int.class);
            return getKey.invoke(null, number.longValue(), keyCode);
        } catch (Throwable t) {
            return "error:" + t.getClass().getSimpleName();
        }
    }

    private static Object currentScreen(Object client) {
        try {
            Object gui = getQuietly(client, "gui");
            Object screen = call(gui, "screen");
            if (screen != null) {
                return screen;
            }
        } catch (Throwable ignored) {
            // fall through to the field used by older versions
        }
        return getQuietly(client, "currentScreen", "screen");
    }

    private static boolean demoStarted;

    private static boolean pressTitleButton(Object client, String... labels) {
        try {
            Object screen = currentScreen(client);
            if (screen == null) {
                return false;
            }
            if (!demoStarted && screen.getClass().getSimpleName().equals("TitleScreen")) {
                try {
                    Method method = screen.getClass().getDeclaredMethod("confirmDemo", boolean.class);
                    method.setAccessible(true);
                    method.invoke(screen, false);
                    demoStarted = true;
                    log("started the demo world");
                    return true;
                } catch (NoSuchMethodException ignored) {
                    // fall through to the button list
                }
            }
            return pressScreenButton(screen, labels);
        } catch (Throwable t) {
            log("could not press title button: " + t);
        }
        return false;
    }

    private static boolean pressScreenButton(Object screen, String... labels) {
        try {
            Object children = call(screen, "children");
            if (!(children instanceof Iterable<?>)) {
                children = getQuietly(screen, "children");
            }
            if (!(children instanceof Iterable<?> iterable)) {
                return false;
            }
            for (Object child : iterable) {
                Object active = getQuietly(child, "active");
                if (Boolean.FALSE.equals(active)) {
                    continue;
                }
                String label = buttonLabel(child);
                if (label == null) {
                    continue;
                }
                String lower = label.toLowerCase();
                boolean matches = false;
                for (String wanted : labels) {
                    if (lower.contains(wanted.toLowerCase())) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) {
                    continue;
                }
                for (Method method : child.getClass().getMethods()) {
                    if (!method.getName().equals("onPress")) {
                        continue;
                    }
                    Class<?>[] parameters = method.getParameterTypes();
                    if (parameters.length == 0) {
                        method.invoke(child);
                        log("pressed screen button: " + label);
                        return true;
                    }
                    if (parameters.length == 1) {
                        Object input = null;
                        try {
                            input = Class.forName("net.minecraft.client.input.KeyEvent")
                                    .getConstructor(int.class, int.class, int.class)
                                    .newInstance(257, 0, 0);
                        } catch (Throwable ignored) {
                            // the handler may not use the input at all
                        }
                        method.invoke(child, input);
                        log("pressed screen button: " + label);
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
            log("could not press screen button: " + t);
        }
        return false;
    }

    private static void closeScreen(Object screen) {
        try {
            for (String name : new String[]{"onClose", "close"}) {
                Method method = findMethod(screen.getClass(), name);
                if (method != null && method.getParameterCount() == 0) {
                    method.invoke(screen);
                    log("closed screen " + screen.getClass().getSimpleName());
                    return;
                }
            }
        } catch (Throwable t) {
            log("could not close screen: " + t);
        }
    }

    private static String buttonLabel(Object child) {
        Object message = null;
        try {
            message = call(child, "getMessage", "message");
            if (message == null) {
                message = getQuietly(child, "message");
            }
            if (message != null) {
                Object text = call(message, "getString", "asString");
                if (text != null) {
                    return text.toString();
                }
                String component = componentString(message);
                if (component != null) {
                    return component;
                }
            }
        } catch (Throwable ignored) {
            // fall back to the component's toString
        }
        return message == null ? null : message.toString();
    }

    private static String componentString(Object message) {
        for (String type : new String[]{
                "net.minecraft.network.chat.Component",
                "net.minecraft.text.Text",
                "net.minecraft.network.chat.MutableComponent",
                "net.minecraft.text.MutableText"
        }) {
            try {
                Method method = Class.forName(type).getMethod("getString");
                Object value = method.invoke(message);
                if (value != null) {
                    return value.toString();
                }
            } catch (Throwable ignored) {
                // try the next component interface
            }
        }
        return null;
    }

    private static void pressEnter(Object client) {
        try {
            Object keyboard = getQuietly(client, "keyboardHandler", "keyboard");
            if (keyboard == null) {
                return;
            }
            long handle = windowHandle(client);
            if (handle == 0L) {
                return;
            }
            int key = 257; // GLFW_KEY_ENTER
            for (Method method : keyboard.getClass().getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 5
                        && parameters[0] == long.class
                        && parameters[1] == int.class
                        && parameters[2] == int.class
                        && parameters[3] == int.class
                        && parameters[4] == int.class) {
                    method.invoke(keyboard, handle, key, 0, 1, 0);
                    method.invoke(keyboard, handle, key, 0, 0, 0);
                    log("sent Enter to the title screen");
                    return;
                }
                if (parameters.length == 3
                        && parameters[0] == long.class
                        && parameters[1] == int.class
                        && parameters[2].getSimpleName().equals("KeyEvent")) {
                    Object event = parameters[2].getConstructor(int.class, int.class, int.class)
                            .newInstance(key, 0, 0);
                    method.invoke(keyboard, handle, 1, event);
                    method.invoke(keyboard, handle, 0, event);
                    log("sent Enter to the title screen");
                    return;
                }
            }
        } catch (Throwable t) {
            log("could not send Enter: " + t);
        }
    }

    private static long windowHandle(Object client) {
        try {
            Object window = call(client, "getWindow");
            Object handle = call(window, "handle", "getHandle", "getWindow");
            if (handle instanceof Number number && number.longValue() != 0L) {
                return number.longValue();
            }
        } catch (Throwable ignored) {
            // try GLFW next
        }
        try {
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            Object handle = glfw.getMethod("glfwGetCurrentContext").invoke(null);
            return handle instanceof Number number ? number.longValue() : 0L;
        } catch (Throwable t) {
            return 0L;
        }
    }

    private static void stop(Object client) {
        for (String name : new String[]{"scheduleStop", "stop", "close"}) {
            try {
                Method method = client.getClass().getMethod(name);
                method.invoke(client);
                log("stop requested via " + name);
                return;
            } catch (ReflectiveOperationException ignored) {
                // try the next method name
            }
        }
        log("no stop method found; exiting");
        System.exit(0);
    }

    private void fail(String reason) {
        if (phase == Phase.ERROR) {
            return;
        }
        phase = Phase.ERROR;
        ticks = 0;
        log("ERROR: " + reason);
        writeStatus("error\n" + reason + "\n");
        try {
            setZoomKeyDown(false);
        } catch (Throwable ignored) {
            // best effort during shutdown
        }
    }

    private void writeStatus(String text) {
        try {
            Files.writeString(new File(outputDirectory, "status.txt").toPath(), text, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log("could not write status: " + e);
        }
    }

    private static void log(String message) {
        System.out.println("[autotest] " + message);
    }

    private static Object get(Object target, String... names) throws Exception {
        if (target == null) {
            return null;
        }
        Field field = findField(target.getClass(), names);
        return field == null ? null : field.get(target);
    }

    private static Object getQuietly(Object target, String... names) {
        try {
            return get(target, names);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object call(Object target, String... names) throws Exception {
        if (target == null) {
            return null;
        }
        for (String name : names) {
            Class<?> type = target.getClass();
            while (type != null) {
                try {
                    Method method = type.getDeclaredMethod(name);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (NoSuchMethodException ignored) {
                    type = type.getSuperclass();
                }
            }
        }
        return null;
    }

    private static Object callStatic(String className, String name) {
        try {
            Method method = Class.forName(className).getMethod(name);
            return method.invoke(null);
        } catch (ReflectiveOperationException e) {
            return "error:" + e.getClass().getSimpleName();
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameters) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String... names) {
        for (String name : names) {
            Class<?> current = type;
            while (current != null) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }
        }
        return null;
    }

    /** Keeps the camera pointed at the horizon so the zoom difference is easy to see. */
    private static final class LevelView {
        private static void faceForward(Object player) throws Exception {
            if (player == null) {
                return;
            }
            setFloat(player, 180.0f, "setYaw", "setYRot");
            setFloat(player, 0.0f, "setPitch", "setXRot");
        }

        private static void setFloat(Object target, float value, String... names) throws Exception {
            for (String name : names) {
                Method method = findMethod(target.getClass(), name, float.class);
                if (method != null) {
                    method.invoke(target, value);
                    return;
                }
            }
        }

        private static String describe(Object player) {
            if (player == null) {
                return "player=null";
            }
            try {
                return "x=" + call(player, "getX")
                        + " y=" + call(player, "getY")
                        + " z=" + call(player, "getZ")
                        + " ground=" + call(player, "isOnGround", "onGround");
            } catch (Exception e) {
                return "player=" + e;
            }
        }
    }
}
