package mix.cinematiczoom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

final class ZoomAudio {
    private ZoomAudio() {
    }

    static double musicVolume(MinecraftClient client) {
        return client.options.getSoundVolumeOption(SoundCategory.MUSIC).getValue();
    }

    static void setMusicVolume(MinecraftClient client, double volume) {
        client.options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(volume);
    }

    static void playTick(float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), pitch, ZoomSounds.TICK_VOLUME));
    }
}
