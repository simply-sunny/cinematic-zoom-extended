package mix.cinematiczoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

final class ZoomAudio {
    // The spyglass sounds under CZE's own names, which have no subtitle (assets/cinematiczoom/sounds.json).
    private static final SoundEvent ZOOM_IN = SoundEvent.createVariableRangeEvent(new ResourceLocation("cinematiczoom:zoom_in"));
    private static final SoundEvent ZOOM_OUT = SoundEvent.createVariableRangeEvent(new ResourceLocation("cinematiczoom:zoom_out"));

    private ZoomAudio() {
    }

    static void click(float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }

    static void zoomIn() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ZOOM_IN, 1.0f, 1.0f));
    }

    static void zoomOut() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ZOOM_OUT, 1.0f, 1.0f));
    }
}
