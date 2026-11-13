package mix.cinematiczoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

final class ZoomAudio {
    private ZoomAudio() {
    }

    static void click(float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }

    static void zoomIn() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SPYGLASS_USE, 1.0f, 1.0f));
    }

    static void zoomOut() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SPYGLASS_STOP_USING, 1.0f, 1.0f));
    }
}
