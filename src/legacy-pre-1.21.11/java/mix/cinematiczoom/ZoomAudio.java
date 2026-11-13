package mix.cinematiczoom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

final class ZoomAudio {
    // The spyglass sounds under CZE's own names, which have no subtitle (assets/cinematiczoom/sounds.json).
    private static final SoundEvent ZOOM_IN = SoundEvent.of(Identifier.tryParse("cinematiczoom:zoom_in"));
    private static final SoundEvent ZOOM_OUT = SoundEvent.of(Identifier.tryParse("cinematiczoom:zoom_out"));

    private ZoomAudio() {
    }

    static void click(float pitch, float volume) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }

    static void zoomIn() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(ZOOM_IN, 1.0f, 1.0f));
    }

    static void zoomOut() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(ZOOM_OUT, 1.0f, 1.0f));
    }
}
