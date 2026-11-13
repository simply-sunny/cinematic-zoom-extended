package mix.cinematiczoom;

import java.util.Set;

/** The keybinds that adjust a cinematic zoom while it's on screen. */
public final class ZoomKeys {
    private static final Set<String> CINEMATIC_CONTROLS = Set.of(
            "key.cinematiczoom.increase_bars", "key.cinematiczoom.decrease_bars",
            "key.cinematiczoom.previous_aspect", "key.cinematiczoom.next_aspect");

    private ZoomKeys() {
    }

    /**
     * Whether a key press belongs to the zoom alone. During a cinematic zoom, a key bound to one of
     * its controls goes only to those controls, so another mod sharing the key (Gamma Utils on the
     * arrow keys, say) doesn't also react. Outside a cinematic zoom every keybind gets it as usual.
     */
    public static boolean claimedByZoom(boolean cinematicActive, Iterable<String> boundNames) {
        if (!cinematicActive) return false;
        for (String name : boundNames) {
            if (isCinematicControl(name)) return true;
        }
        return false;
    }

    public static boolean isCinematicControl(String name) {
        return CINEMATIC_CONTROLS.contains(name);
    }
}
