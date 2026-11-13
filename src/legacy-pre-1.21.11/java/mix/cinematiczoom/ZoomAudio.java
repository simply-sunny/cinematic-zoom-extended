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

    static void click(float pitch, float volume) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }
}
