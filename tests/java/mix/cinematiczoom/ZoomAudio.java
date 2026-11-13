package mix.cinematiczoom;

import net.minecraft.client.Minecraft;

/** Records what the game would hear, in place of the per-version sound calls. */
final class ZoomAudio {
    static final java.util.List<Float> ticks = new java.util.ArrayList<>();

    private ZoomAudio() {
    }

    static final java.util.List<Float> clicks = new java.util.ArrayList<>();

    static void click(float pitch, float volume) {
        if (volume == ZoomSounds.TICK_VOLUME) ticks.add(pitch);
        else clicks.add(pitch);
    }

    static int zoomIns, zoomOuts;

    static void zoomIn() {
        zoomIns++;
    }

    static void zoomOut() {
        zoomOuts++;
    }

    static void clear() {
        zoomIns = zoomOuts = 0;
        ticks.clear();
        clicks.clear();
    }
}
