package mix.cinematiczoom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

final class ZoomAudio {
    private ZoomAudio() {
    }

    static void click(float pitch, float volume) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }

    static void zoomIn() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.ITEM_SPYGLASS_USE, 1.0f, 1.0f));
    }

    static void zoomOut() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0f, 1.0f));
    }
}
