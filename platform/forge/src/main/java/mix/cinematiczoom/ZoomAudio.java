package mix.cinematiczoom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

final class ZoomAudio {
    private ZoomAudio() {
    }

    static double musicVolume(Minecraft client) {
        return client.options.getSoundSourceOptionInstance(SoundSource.MUSIC).get();
    }

    static void setMusicVolume(Minecraft client, double volume) {
        client.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(volume);
    }

    static void click(float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, volume));
    }
}
