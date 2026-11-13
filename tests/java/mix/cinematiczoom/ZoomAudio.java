package mix.cinematiczoom;

import net.minecraft.client.Minecraft;

/** Records what the game would hear, in place of the per-version sound calls. */
final class ZoomAudio {
    static double musicVolume = 0.8;
    static int musicChanges;
    static final java.util.List<Float> ticks = new java.util.ArrayList<>();

    private ZoomAudio() {
    }

    static double musicVolume(Minecraft client) {
        return musicVolume;
    }

    static void setMusicVolume(Minecraft client, double volume) {
        musicVolume = volume;
        musicChanges++;
    }

    static final java.util.List<Float> clicks = new java.util.ArrayList<>();

    static void click(float pitch, float volume) {
        if (volume == ZoomSounds.TICK_VOLUME) ticks.add(pitch);
        else clicks.add(pitch);
    }

    static void clear() {
        musicVolume = 0.8;
        musicChanges = 0;
        ticks.clear();
        clicks.clear();
    }
}
