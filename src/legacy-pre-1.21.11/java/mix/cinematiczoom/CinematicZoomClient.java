package mix.cinematiczoom;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class CinematicZoomClient implements ClientModInitializer {
    public static KeyBinding ZOOM_KEYBIND;
    public static KeyBinding ZOOM_NO_BARS_KEYBIND;
    public static KeyBinding INCREASE_BARS_KEYBIND;
    public static KeyBinding DECREASE_BARS_KEYBIND;
    public static KeyBinding PREVIOUS_ASPECT_KEYBIND;
    public static KeyBinding NEXT_ASPECT_KEYBIND;

    @Override
    public void onInitializeClient() {
        ZoomConfig.configureDirectory(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());
        ZoomConfig.INSTANCE.load();

        ZOOM_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinematiczoom.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "key.categories.cinematiczoom"
        ));
        ZOOM_NO_BARS_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinematiczoom.zoom_no_bars",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.cinematiczoom"
        ));
        INCREASE_BARS_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinematiczoom.increase_bars",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                "key.categories.cinematiczoom"
        ));
        DECREASE_BARS_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinematiczoom.decrease_bars",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                "key.categories.cinematiczoom"
        ));
        PREVIOUS_ASPECT_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinematiczoom.previous_aspect",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                "key.categories.cinematiczoom"
        ));
        NEXT_ASPECT_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cinematiczoom.next_aspect",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                "key.categories.cinematiczoom"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> ZoomManager.tick(
                client,
                ZOOM_KEYBIND,
                ZOOM_NO_BARS_KEYBIND,
                INCREASE_BARS_KEYBIND,
                DECREASE_BARS_KEYBIND,
                PREVIOUS_ASPECT_KEYBIND,
                NEXT_ASPECT_KEYBIND
        ));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ZoomManager.reset(client));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ZoomManager.reset(client);
            ZoomConfig.INSTANCE.save();
        });
    }
}
