package mix.cinematiczoom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ZoomConfig {
    public static final ZoomConfig INSTANCE = new ZoomConfig();

    private static final Logger LOGGER = LoggerFactory.getLogger("cinematiczoom");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path CONFIG_DIR = Path.of("config");
    private static Path PATH = CONFIG_DIR.resolve("cinematiczoom").resolve("config.json");

    /** Called by each loader before loading configuration. */
    public static void configureDirectory(Path directory) {
        CONFIG_DIR = java.util.Objects.requireNonNull(directory);
        PATH = CONFIG_DIR.resolve("cinematiczoom").resolve("config.json");
    }

    /** Moves what older versions left directly in the config folder. A file already in the mod's folder wins. */
    private static void moveOldFiles() {
        moveOld(CONFIG_DIR.resolve("cinematiczoom.json"), PATH);
        moveOld(CONFIG_DIR.resolve("cinematiczoom.json.v1.bak"), PATH.resolveSibling("backups").resolve("v1.json"));
    }

    private static void moveOld(Path from, Path to) {
        if (!Files.exists(from) || Files.exists(to)) return;
        try {
            Files.createDirectories(to.getParent());
            Files.move(from, to);
        } catch (IOException exception) {
            LOGGER.warn("Failed to move {} to {}", from, to, exception);
        }
    }

    public int configVersion = 2;
    public String aspectRatio = "2.39:1";
    public java.util.List<String> aspectProfiles = new java.util.ArrayList<>(AspectRatios.DEFAULTS);
    public boolean globalProfileCycling = false;
    public boolean animateProfileCycle = true;
    public BarAnimationMode barEnterMode = BarAnimationMode.LINEAR;
    public BarAnimationMode barExitMode = BarAnimationMode.LINEAR;
    public BarAnimationMode barSwitchMode = BarAnimationMode.LINEAR;
    public int barEnterMs = 240, barExitMs = 240, barSwitchMs = 240;
    public float barOpacity = 1f;
    public FovMode fovMode = FovMode.ACCURATE;
    public SpyglassZoom spyglassZoom = SpyglassZoom.OFF;
    public boolean muteMusicWhileZooming = true;
    public boolean wheelTicks = true;
    public boolean logarithmicScroll = true;
    private transient JsonObject originalJson = new JsonObject();
    private transient boolean saveAllowed = true;

    // Cinematic Zoom Profile
    public float cinematicStartingZoom = 3.0f;
    public float cinematicBarsPercent = 15.0f;
    public boolean arrowKeysAdjustBars = true;
    public float barAdjustStep = 1.0f;
    public boolean smoothBarChange = true;
    public boolean preserveBarAdjustment = false;
    public int cinematicZoomInMs = 240;
    public int cinematicZoomOutMs = 240;
    public ZoomCurve cinematicZoomInCurve = ZoomCurve.EXPONENTIAL;
    public ZoomCurve cinematicZoomOutCurve = ZoomCurve.EXPONENTIAL;
    public boolean cinematicHideHud = true;
    public boolean cinematicHideCrosshair = true;
    public boolean cinematicHideHands = true;
    public boolean cinematicCamera = true;
    public boolean cinematicScaleSensitivity = true;
    public boolean cinematicRemoveBobbing = true;
    public boolean cinematicToggle = false;
    public boolean cinematicMagnetize = false;
    public String cinematicSnapPoints = "";

    // Regular Zoom Profile (No Bars)
    public float regularStartingZoom = 3.0f;
    public int regularZoomInMs = 240;
    public int regularZoomOutMs = 240;
    public ZoomCurve regularZoomInCurve = ZoomCurve.EXPONENTIAL;
    public ZoomCurve regularZoomOutCurve = ZoomCurve.EXPONENTIAL;
    public boolean regularHideHud = true;
    public boolean regularHideCrosshair = true;
    public boolean regularHideHands = true;
    public boolean regularCinematicCamera = false;
    public boolean regularScaleSensitivity = true;
    public boolean regularRemoveBobbing = true;
    public boolean regularToggle = false;
    public boolean regularMagnetize = false;
    public String regularSnapPoints = "";

    // Shared / Wheel Settings
    public boolean mouseWheelEnabled = true;
    public boolean discreteScroll = false;
    public float discreteScrollStep = 1.0f;
    public boolean allowUltrawide = false;
    public float minZoomMultiplier = ZoomMath.MIN_MULTIPLIER;
    public float maxZoomMultiplier = 1.0f;
    public float wheelStep = 0.05f;

    public void load() {
        saveAllowed = true;
        copyFrom(new ZoomConfig());
        moveOldFiles();
        if (!Files.exists(PATH)) {
            originalJson = new JsonObject();
            try {
                ExternalConfigImporter.importInto(this, CONFIG_DIR);
            } catch (Exception exception) {
                LOGGER.warn("Failed to import external zoom configs", exception);
            }
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) throw new IOException("Empty configuration");
            originalJson = json.deepCopy();
            // Legacy aliases are parsed below; sanitize them independently as well.
            for (String name : new String[]{"startingZoom","baseZoomMultiplier","barsPercent","smoothMs","zoomInMs","zoomOutMs"}) {
                if (json.has(name)) try {
                    if (!Double.isFinite(json.get(name).getAsDouble())) json.remove(name);
                } catch (RuntimeException invalid) { json.remove(name); }
            }
            for (String name : new String[]{"hideHudDuringZoom","enableCinematicCamera","toggleMode"}) {
                if (json.has(name) && (!json.get(name).isJsonPrimitive() || !json.getAsJsonPrimitive(name).isBoolean())) json.remove(name);
            }
            // Validate each field independently so one bad option does not discard other preferences.
            ZoomConfig loaded = new ZoomConfig();
            for (java.lang.reflect.Field field : ZoomConfig.class.getFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || !json.has(field.getName())) continue;
                try {
                    Object value = GSON.fromJson(json.get(field.getName()), field.getGenericType());
                    if (value != null) {
                        if (field.getType() == boolean.class && !json.getAsJsonPrimitive(field.getName()).isBoolean()) throw new IllegalArgumentException("Expected boolean");
                        if (value instanceof Number number && !Double.isFinite(number.doubleValue())) throw new IllegalArgumentException("Expected finite number");
                        field.set(loaded, value);
                    } else json.remove(field.getName());
                } catch (Exception invalid) { LOGGER.warn("Invalid setting {}: using default", field.getName()); json.remove(field.getName()); }
            }
            if (loaded != null) {
                // Expand only the old default range; keep custom and new-profile limits.
                if (!json.has("cinematicStartingZoom") && !json.has("regularStartingZoom")
                        && (json.has("startingZoom") || json.has("baseZoomMultiplier") || json.has("smoothMs"))
                        && loaded.minZoomMultiplier == 0.10f) {
                    loaded.minZoomMultiplier = 1.0f / 30.0f;
                }
                if (json.has("allowUltrawide")) {
                    loaded.allowUltrawide = json.get("allowUltrawide").getAsBoolean();
                } else if (json.has("maxZoomMultiplier")) {
                    loaded.allowUltrawide = json.get("maxZoomMultiplier").getAsFloat() > 1.01f;
                } else {
                    loaded.allowUltrawide = false;
                }
                loaded.maxZoomMultiplier = loaded.allowUltrawide ? 2.0f : 1.0f;
                if (!json.has("cinematicHideHands")) {
                    loaded.cinematicHideHands = true;
                }
                if (!json.has("regularHideHud")) {
                    loaded.regularHideHud = true;
                }
                if (!json.has("regularHideHands")) {
                    loaded.regularHideHands = true;
                }
                if (!json.has("cinematicStartingZoom")) {
                    if (json.has("startingZoom")) {
                        loaded.cinematicStartingZoom = json.get("startingZoom").getAsFloat();
                    } else if (json.has("baseZoomMultiplier")) {
                        float oldMultiplier = json.get("baseZoomMultiplier").getAsFloat();
                        if (oldMultiplier <= 0f) {
                            loaded.cinematicStartingZoom = 1.0f;
                        } else if (Math.abs(oldMultiplier - 0.33f) < 0.01f) {
                            loaded.cinematicStartingZoom = 3.0f;
                        } else {
                            loaded.cinematicStartingZoom = Math.round(10.0f / oldMultiplier) / 10.0f;
                        }
                    }
                }
                if (!json.has("regularStartingZoom") && json.has("startingZoom")) {
                    loaded.regularStartingZoom = json.get("startingZoom").getAsFloat();
                }
                if (!json.has("cinematicBarsPercent") && json.has("barsPercent")) {
                    loaded.cinematicBarsPercent = json.get("barsPercent").getAsFloat();
                }
                if (json.has("smoothMs")) {
                    int oldSmooth = json.get("smoothMs").getAsInt();
                    if (!json.has("cinematicZoomInMs")) loaded.cinematicZoomInMs = oldSmooth;
                    if (!json.has("cinematicZoomOutMs")) loaded.cinematicZoomOutMs = oldSmooth;
                    if (!json.has("regularZoomInMs")) loaded.regularZoomInMs = oldSmooth;
                    if (!json.has("regularZoomOutMs")) loaded.regularZoomOutMs = oldSmooth;
                }
                if (json.has("zoomInMs") && !json.has("cinematicZoomInMs")) {
                    loaded.cinematicZoomInMs = json.get("zoomInMs").getAsInt();
                }
                if (json.has("zoomOutMs") && !json.has("cinematicZoomOutMs")) {
                    loaded.cinematicZoomOutMs = json.get("zoomOutMs").getAsInt();
                }
                if (json.has("hideHudDuringZoom") && !json.has("cinematicHideHud")) {
                    loaded.cinematicHideHud = json.get("hideHudDuringZoom").getAsBoolean();
                }
                if (json.has("cinematicHideHud") || json.has("hideHudDuringZoom")) {
                    if (!json.has("cinematicHideCrosshair")) {
                        loaded.cinematicHideCrosshair = loaded.cinematicHideHud;
                    }
                }
                if (json.has("regularHideHud") && !json.has("regularHideCrosshair")) {
                    loaded.regularHideCrosshair = loaded.regularHideHud;
                }
                if (json.has("enableCinematicCamera") && !json.has("cinematicCamera")) {
                    loaded.cinematicCamera = json.get("enableCinematicCamera").getAsBoolean();
                }
                if (json.has("toggleMode") && !json.has("cinematicToggle")) {
                    loaded.cinematicToggle = json.get("toggleMode").getAsBoolean();
                }

                if (!json.has("configVersion") || loaded.configVersion < 2) {
                    Path backup = PATH.resolveSibling("backups").resolve("v1.json");
                    if (!Files.exists(backup)) try { Files.createDirectories(backup.getParent()); Files.copy(PATH, backup); }
                    catch (IOException backupFailure) {
                        saveAllowed = false;
                        LOGGER.warn("Migration backup failed; preserving the existing file without further writes", backupFailure);
                    }
                    loaded.aspectRatio = AspectRatios.LEGACY;
                    loaded.aspectProfiles.add(AspectRatios.LEGACY);
                    loaded.fovMode = FovMode.LEGACY;
                    loaded.logarithmicScroll = false;
                    loaded.barEnterMs = loaded.cinematicZoomInMs;
                    loaded.barExitMs = loaded.cinematicZoomOutMs;
                    loaded.barSwitchMode = loaded.smoothBarChange ? BarAnimationMode.SMOOTH : BarAnimationMode.INSTANT;
                    // Preserve explicit limits: a saved default cannot be distinguished from a custom choice.
                }
                loaded.configVersion = 2;
                copyFrom(loaded);
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load config from {}", PATH, exception);
            try {
                Path backups = Files.createDirectories(PATH.resolveSibling("backups"));
                Path backup = Files.createTempFile(backups, "invalid-", ".json");
                Files.copy(PATH, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                originalJson = new JsonObject();
            } catch (IOException backupFailure) { saveAllowed = false; LOGGER.warn("Could not back up invalid configuration; saving disabled to preserve it", backupFailure); }
        }
        clamp();
    }

    public void save() {
        if (!saveAllowed) return;
        clamp();
        try {
            Files.createDirectories(PATH.getParent());
            JsonObject output = originalJson.deepCopy();
            GSON.toJsonTree(this).getAsJsonObject().entrySet().forEach(e -> output.add(e.getKey(), e.getValue()));
            Path temporary = Files.createTempFile(PATH.getParent(), "cinematiczoom-", ".tmp");
            try {
                try (Writer writer = Files.newBufferedWriter(temporary)) { GSON.toJson(output, writer); }
                try {
                    Files.move(temporary, PATH, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                    Files.move(temporary, PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } finally { Files.deleteIfExists(temporary); }
        } catch (IOException exception) {
            LOGGER.warn("Failed to save config to {}", PATH, exception);
        }
    }

    public void clamp() {
        aspectProfiles = AspectRatios.validate(aspectProfiles);
        if (aspectRatio == null || (!AspectRatios.LEGACY.equals(aspectRatio) && !Double.isFinite(AspectRatios.parse(aspectRatio)))) aspectRatio = aspectProfiles.get(0);
        if (!aspectProfiles.contains(aspectRatio)) aspectRatio = aspectProfiles.get(0);
        if (barEnterMode == null) barEnterMode = BarAnimationMode.LINEAR;
        if (barExitMode == null) barExitMode = BarAnimationMode.LINEAR;
        if (barSwitchMode == null) barSwitchMode = BarAnimationMode.LINEAR;
        if (fovMode == null) fovMode = FovMode.ACCURATE;
        if (spyglassZoom == null) spyglassZoom = SpyglassZoom.OFF;
        barEnterMs = Math.max(0, Math.min(60000, barEnterMs));
        barExitMs = Math.max(0, Math.min(60000, barExitMs));
        barSwitchMs = Math.max(0, Math.min(60000, barSwitchMs));
        barOpacity = (float)ZoomMath.finiteClamp(barOpacity,0,1,1);
        cinematicStartingZoom = (float)ZoomMath.finiteClamp(cinematicStartingZoom,.5,500,3);
        regularStartingZoom = (float)ZoomMath.finiteClamp(regularStartingZoom,.5,500,3);
        cinematicBarsPercent = (float)ZoomMath.finiteClamp(cinematicBarsPercent,0,50,15);
        minZoomMultiplier = (float)ZoomMath.finiteClamp(minZoomMultiplier,ZoomMath.MIN_MULTIPLIER,2,ZoomMath.MIN_MULTIPLIER);
        wheelStep = (float)ZoomMath.finiteClamp(wheelStep,.01,.25,.05);
        discreteScrollStep = (float)ZoomMath.finiteClamp(discreteScrollStep,.1,5,1);
        barAdjustStep = (float)ZoomMath.finiteClamp(barAdjustStep,.1,10,1);
        cinematicBarsPercent = Math.max(0f, Math.min(50f, cinematicBarsPercent));

        cinematicZoomInMs = Math.max(0, cinematicZoomInMs);
        cinematicZoomOutMs = Math.max(0, cinematicZoomOutMs);
        if (cinematicZoomInCurve == null) cinematicZoomInCurve = ZoomCurve.EXPONENTIAL;
        if (cinematicZoomOutCurve == null) cinematicZoomOutCurve = ZoomCurve.EXPONENTIAL;
        if (cinematicSnapPoints == null) cinematicSnapPoints = "";

        regularZoomInMs = Math.max(0, regularZoomInMs);
        regularZoomOutMs = Math.max(0, regularZoomOutMs);
        if (regularZoomInCurve == null) regularZoomInCurve = ZoomCurve.EXPONENTIAL;
        if (regularZoomOutCurve == null) regularZoomOutCurve = ZoomCurve.EXPONENTIAL;
        if (regularSnapPoints == null) regularSnapPoints = "";

        maxZoomMultiplier = allowUltrawide ? 2.0f : 1.0f;
        minZoomMultiplier = Math.max(ZoomMath.MIN_MULTIPLIER, Math.min(maxZoomMultiplier, minZoomMultiplier));

        float minStarting = 1.0f / maxZoomMultiplier;
        float maxStarting = 1.0f / minZoomMultiplier;

        cinematicStartingZoom = Math.max(minStarting, Math.min(maxStarting, cinematicStartingZoom));
        regularStartingZoom = Math.max(minStarting, Math.min(maxStarting, regularStartingZoom));

        discreteScrollStep = Math.max(0.1f, Math.min(5.0f, discreteScrollStep));
        wheelStep = Math.max(0.01f, Math.min(0.25f, wheelStep));
        barAdjustStep = Math.max(0.1f, Math.min(10.0f, barAdjustStep));
    }

    private void copyFrom(ZoomConfig other) {
        for (java.lang.reflect.Field field : ZoomConfig.class.getFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            try { field.set(this, field.get(other)); }
            catch (IllegalAccessException impossible) { throw new IllegalStateException(impossible); }
        }
    }
}
