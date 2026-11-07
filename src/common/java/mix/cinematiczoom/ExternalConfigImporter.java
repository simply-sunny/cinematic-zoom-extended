package mix.cinematiczoom;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Imports settings from other zoom mods when Cinematic Zoom Extended has no config yet.
 *
 * <p>Other mods' files are only read, never changed or removed.</p>
 *
 * <p>Every JSON or TOML file whose path contains {@code zoom} is considered, so the importer is
 * not limited to the mods listed below. Zoomify, Ok Zoomer, and Just Zoom are mapped explicitly by
 * key name; other zoom mods are matched through the same name patterns. WI Zoom and Logical Zoom
 * keep no config file, so there is nothing to import from them.</p>
 */
public final class ExternalConfigImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger("cinematiczoom");

    private ExternalConfigImporter() {
    }

    /**
     * Applies the first recognized value for each setting. Later files never overwrite an earlier
     * match, which keeps the result deterministic when several zoom configs are present.
     *
     * @return the config files that contributed at least one setting
     */
    public static List<String> importInto(ZoomConfig config, Path configDir) {
        List<String> imported = new ArrayList<>();
        if (configDir == null || !Files.isDirectory(configDir)) {
            return imported;
        }

        Set<String> applied = new HashSet<>();
        for (Path source : findSources(configDir)) {
            Map<String, Object> values = read(source);
            if (values.isEmpty()) {
                continue;
            }
            int before = applied.size();
            apply(config, values, applied);
            if (applied.size() > before) {
                imported.add(configDir.relativize(source).toString());
            }
        }

        if (!imported.isEmpty()) {
            LOGGER.info("Imported zoom settings from {}", String.join(", ", imported));
        }
        return imported;
    }

    private static List<Path> findSources(Path configDir) {
        List<Path> sources = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(configDir, 2)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String relative = relative(configDir, path);
                        return relative.contains("zoom") && !relative.startsWith("cinematiczoom");
                    })
                    .sorted(Comparator.comparingInt(ExternalConfigImporter::priority)
                            .thenComparing(path -> path.toString().toLowerCase(Locale.ROOT)))
                    .forEach(sources::add);
        } catch (Exception exception) {
            LOGGER.warn("Could not scan {} for zoom configs", configDir, exception);
        }
        return sources;
    }

    private static String relative(Path configDir, Path path) {
        return configDir.relativize(path).toString().replace('\\', '/').toLowerCase(Locale.ROOT);
    }

    private static int priority(Path path) {
        String name = path.toString().toLowerCase(Locale.ROOT);
        if (name.contains("zoomify")) return 0;
        if (name.contains("ok_zoomer") || name.contains("okzoomer") || name.contains("ok-zoomer")) return 1;
        if (name.contains("wi_zoom") || name.contains("wizoom")) return 2;
        return 3;
    }

    private static Map<String, Object> read(Path path) {
        Map<String, Object> values = new LinkedHashMap<>();
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".json")) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                flatten("", root, values);
                return values;
            } catch (Exception ignored) {
                values.clear();
            }
        }

        try {
            return readKeyValues(path);
        } catch (Exception exception) {
            LOGGER.warn("Could not read zoom config {}", path, exception);
            return Map.of();
        }
    }

    private static void flatten(String prefix, JsonElement element, Map<String, Object> out) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                flatten(key, entry.getValue(), out);
            }
            return;
        }
        if (element.isJsonArray() || prefix.isEmpty()) {
            return;
        }

        if (element.getAsJsonPrimitive().isBoolean()) {
            out.put(prefix, element.getAsBoolean());
        } else if (element.getAsJsonPrimitive().isNumber()) {
            out.put(prefix, element.getAsNumber());
        } else {
            out.put(prefix, element.getAsString());
        }
    }

    /** Reads TOML, properties, or a JSON file that failed strict parsing. */
    private static Map<String, Object> readKeyValues(Path path) throws Exception {
        Map<String, Object> out = new LinkedHashMap<>();
        String section = "";
        for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = stripComment(raw).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
                continue;
            }

            int equals = line.indexOf('=');
            int colon = line.indexOf(':');
            int split = equals >= 0 && (colon < 0 || equals < colon) ? equals : colon;
            if (split < 0) {
                continue;
            }
            String key = line.substring(0, split).trim();
            if (key.isEmpty()) {
                continue;
            }
            Object value = scalar(line.substring(split + 1).trim());
            if (value != null) {
                out.put(section.isEmpty() ? key : section + "." + key, value);
            }
        }
        return out;
    }

    private static String stripComment(String line) {
        boolean single = false;
        boolean doubleQuoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '\'' && !doubleQuoted) {
                single = !single;
            } else if (character == '"' && !single) {
                doubleQuoted = !doubleQuoted;
            } else if (character == '#' && !single && !doubleQuoted) {
                return line.substring(0, index);
            }
        }
        return line;
    }

    private static Object scalar(String raw) {
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }

        String cleaned = value.replace("_", "");
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException ignored) {
            // Try a decimal below.
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private static void apply(ZoomConfig config, Map<String, Object> values, Set<String> applied) {
        if (!applied.contains("zoom")) {
            // Just Zoom stores a 0-100 % base zoom instead of a multiplier.
            Double basePercent = number(find(values, "baseZoomFactor", "base_zoom_factor"));
            if (basePercent != null && basePercent >= 0.0 && basePercent <= 100.0) {
                float zoom = (float) justZoomMagnification(basePercent);
                config.cinematicStartingZoom = zoom;
                config.regularStartingZoom = zoom;
                applied.add("zoom");
            }
        }

        if (!applied.contains("zoom")) {
            Double zoom = number(find(values, "initialZoom", "initial_zoom", "zoomAmount", "zoom_amount",
                    "zoomDivisor", "zoom_divisor", "zoomLevel", "zoom_level", "zoomFactor", "zoom_factor",
                    "zoomMultiplier", "zoom_multiplier", "defaultZoom", "default_zoom",
                    "defaultLevel", "default_level", "zoom"));
            if (zoom != null && zoom >= 1.0 && zoom <= 50.0) {
                config.cinematicStartingZoom = zoom.floatValue();
                config.regularStartingZoom = zoom.floatValue();
                applied.add("zoom");
            }
        }

        if (!applied.contains("regularZoom")) {
            Double zoom = number(find(values, "secondaryZoomAmount", "secondary_zoom_amount"));
            if (zoom != null && zoom >= 1.0 && zoom <= 50.0) {
                config.regularStartingZoom = zoom.floatValue();
                applied.add("regularZoom");
            }
        }

        if (!applied.contains("inMs")) {
            Integer ms = millis(find(values, "zoomInTime", "zoom_in_time", "zoomInMs", "zoom_in_ms",
                    "zoomInDuration", "zoom_in_duration", "startTransitionTicks", "start_transition_ticks",
                    "startZoomingAnimationSpeed", "start_zooming_animation_speed"));
            if (ms != null) {
                config.cinematicZoomInMs = ms;
                config.regularZoomInMs = ms;
                applied.add("inMs");
            }
        }

        if (!applied.contains("outMs")) {
            Integer ms = millis(find(values, "zoomOutTime", "zoom_out_time", "zoomOutMs", "zoom_out_ms",
                    "zoomOutDuration", "zoom_out_duration", "endTransitionTicks", "end_transition_ticks",
                    "stopZoomingAnimationSpeed", "stop_zooming_animation_speed"));
            if (ms != null) {
                config.cinematicZoomOutMs = ms;
                config.regularZoomOutMs = ms;
                applied.add("outMs");
            }
        }

        if (!applied.contains("inCurve")) {
            Hit hit = find(values, "zoomInTransition", "zoom_in_transition", "startTransition", "start_transition");
            String name = text(hit);
            if (name != null) {
                config.cinematicZoomInCurve = curve(name);
                config.regularZoomInCurve = config.cinematicZoomInCurve;
                applied.add("inCurve");
                if (isInstant(name)) {
                    config.cinematicZoomInMs = 0;
                    config.regularZoomInMs = 0;
                }
            }
        }

        if (!applied.contains("outCurve")) {
            Hit hit = find(values, "zoomOutTransition", "zoom_out_transition", "endTransition", "end_transition");
            String name = text(hit);
            if (name != null) {
                config.cinematicZoomOutCurve = curve(name);
                config.regularZoomOutCurve = config.cinematicZoomOutCurve;
                applied.add("outCurve");
                if (isInstant(name)) {
                    config.cinematicZoomOutMs = 0;
                    config.regularZoomOutMs = 0;
                }
            }
        }

        if (!applied.contains("toggle")) {
            String mode = text(find(values, "zoomKeyBehaviour", "zoom_key_behaviour", "zoomMode", "zoom_mode"));
            if (mode != null) {
                boolean toggle = mode.equalsIgnoreCase("TOGGLE");
                config.cinematicToggle = toggle;
                config.regularToggle = toggle;
                applied.add("toggle");
            }
        }

        if (!applied.contains("hideCrosshair")) {
            Boolean hide = bool(find(values, "hideCrosshair", "hide_crosshair"));
            if (hide != null) {
                config.cinematicHideCrosshair = hide;
                config.regularHideCrosshair = hide;
                applied.add("hideCrosshair");
            }
        }

        if (!applied.contains("hideHud")) {
            Boolean keep = bool(find(values, "persistentInterface", "persistent_interface"));
            if (keep == null) {
                String mode = text(find(values, "showHud", "show_hud"));
                if (mode != null) {
                    keep = mode.equalsIgnoreCase("ALWAYS");
                }
            }
            if (keep != null) {
                config.cinematicHideHud = !keep;
                config.regularHideHud = !keep;
                applied.add("hideHud");
            }
        }

        if (!applied.contains("hands")) {
            Boolean hide = bool(find(values, "hideArmsWhenZooming", "hide_arms_when_zooming",
                    "hideHand", "hide_hand", "hideHands", "hide_hands"));
            if (hide != null) {
                config.cinematicHideHands = hide;
                config.regularHideHands = hide;
                applied.add("hands");
            }
        }

        if (!applied.contains("regularHideHud")) {
            Boolean hide = bool(find(values, "secondaryHideHUDOnZoom", "secondary_hide_hud_on_zoom"));
            if (hide != null) {
                config.regularHideHud = hide;
                applied.add("regularHideHud");
            }
        }

        if (!applied.contains("bobbing")) {
            Boolean reduce = bool(find(values, "relativeViewBobbing", "relative_view_bobbing",
                    "reduceViewBobbing", "reduce_view_bobbing"));
            if (reduce != null) {
                config.cinematicRemoveBobbing = reduce;
                config.regularRemoveBobbing = reduce;
                applied.add("bobbing");
            }
        }

        if (!applied.contains("sensitivity")) {
            Double sensitivity = number(find(values, "relativeSensitivity", "relative_sensitivity"));
            Boolean reduce;
            if (sensitivity != null) {
                reduce = sensitivity > 0.0;
            } else {
                reduce = bool(find(values, "reduceSensitivity", "reduce_sensitivity",
                        "normalizeMouseSensitivityOnZoom", "normalize_mouse_sensitivity_on_zoom"));
            }
            if (reduce != null) {
                config.cinematicScaleSensitivity = reduce;
                config.regularScaleSensitivity = reduce;
                applied.add("sensitivity");
            }
        }

        if (!applied.contains("camera")) {
            Object value = raw(find(values, "cinematicCamera", "cinematic_camera",
                    "smoothCameraMovementOnZoom", "smooth_camera_movement_on_zoom"));
            if (value != null) {
                Boolean enabled = bool(value);
                if (enabled == null) {
                    Double speed = number(value);
                    enabled = speed != null && speed != 0.0;
                }
                config.cinematicCamera = enabled;
                config.regularCinematicCamera = enabled;
                applied.add("camera");
            }
        }

        if (!applied.contains("wheel")) {
            Boolean enabled = bool(find(values, "scrollZoom", "scroll_zoom", "zoomScrolling", "zoom_scrolling"));
            if (enabled != null) {
                config.mouseWheelEnabled = enabled;
                applied.add("wheel");
            }
        }
    }

    private static Hit find(Map<String, Object> values, String... names) {
        for (String name : names) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String key = entry.getKey();
                int dot = key.lastIndexOf('.');
                String leaf = dot >= 0 ? key.substring(dot + 1) : key;
                if (leaf.equalsIgnoreCase(name)) {
                    return new Hit(key, entry.getValue());
                }
            }
        }
        return null;
    }

    private static Object raw(Hit hit) {
        return hit == null ? null : hit.value;
    }

    private static Double number(Hit hit) {
        return hit == null ? null : number(hit.value);
    }

    private static Double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Boolean bool(Hit hit) {
        return hit == null ? null : bool(hit.value);
    }

    private static Boolean bool(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0.0;
        }
        if (value instanceof String text) {
            if (text.equalsIgnoreCase("true")) return Boolean.TRUE;
            if (text.equalsIgnoreCase("false")) return Boolean.FALSE;
        }
        return null;
    }

    private static String text(Hit hit) {
        if (hit == null || !(hit.value instanceof String text)) {
            return null;
        }
        return text;
    }

    private static Integer millis(Hit hit) {
        Double amount = number(hit);
        if (amount == null) {
            return null;
        }
        String key = hit.key.toLowerCase(Locale.ROOT);
        double milliseconds;
        if (key.endsWith("ms") || key.contains("_ms") || key.contains("millis")) {
            milliseconds = amount;
        } else if (key.contains("tick")) {
            milliseconds = amount * 50.0;
        } else {
            milliseconds = amount * 1000.0;
        }
        return (int) Math.round(Math.max(0.0, Math.min(10000.0, milliseconds)));
    }

    /**
     * Just Zoom stores base zoom as a 0-100 % slider. Converts it with Just Zoom's own FOV formula
     * at the default 70° FOV, then clamps to a sane multiplier.
     */
    private static double justZoomMagnification(double percentage) {
        double fraction = Math.max(0.0, Math.min(100.0, percentage)) / 100.0;
        double normalFov = 70.0;
        double zoomedFov = normalFov + (0.1 - normalFov) * fraction;
        double halfNormal = Math.toRadians(normalFov) * 0.5;
        double halfZoomed = Math.toRadians(zoomedFov) * 0.5;
        double magnification = Math.tan(halfNormal) / Math.tan(halfZoomed);
        return Math.max(1.0, Math.min(50.0, magnification));
    }

    private static ZoomCurve curve(String name) {
        String normalized = name.toUpperCase(Locale.ROOT);
        if (normalized.contains("LINEAR")) return ZoomCurve.LINEAR;
        if (normalized.contains("SINE")) return ZoomCurve.SINE;
        if (normalized.contains("IN_OUT") || normalized.contains("INOUT")) return ZoomCurve.EASE_IN_OUT;
        if (normalized.contains("EASE_IN") || normalized.contains("EASEIN")) return ZoomCurve.EASE_IN;
        if (normalized.contains("CUBIC") || normalized.contains("EASE_OUT")
                || normalized.contains("BALANCED") || normalized.contains("QUAD")) {
            return ZoomCurve.EASE_OUT;
        }
        return ZoomCurve.EXPONENTIAL;
    }

    private static boolean isInstant(String name) {
        return name.equalsIgnoreCase("INSTANT") || name.equalsIgnoreCase("NONE");
    }

    private static final class Hit {
        private final String key;
        private final Object value;

        private Hit(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
