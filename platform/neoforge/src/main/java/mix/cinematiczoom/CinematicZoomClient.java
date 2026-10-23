package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.*;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value="cinematiczoom_extended", dist=Dist.CLIENT)
public final class CinematicZoomClient {
    public static KeyMapping ZOOM_KEYBIND, ZOOM_NO_BARS_KEYBIND, INCREASE_BARS_KEYBIND, DECREASE_BARS_KEYBIND, PREVIOUS_ASPECT_KEYBIND, NEXT_ASPECT_KEYBIND;
    private static final KeyMapping.Category CATEGORY=new KeyMapping.Category(Identifier.parse("cinematiczoom:main"));
    public CinematicZoomClient(IEventBus bus, ModContainer container) {
        ZoomConfig.configureDirectory(FMLPaths.CONFIGDIR.get()); ZoomConfig.INSTANCE.load();
        bus.addListener(this::keys); bus.addListener(this::overlays);
        NeoForge.EVENT_BUS.addListener(this::tick); NeoForge.EVENT_BUS.addListener(this::fov);
        NeoForge.EVENT_BUS.addListener(this::scroll); NeoForge.EVENT_BUS.addListener(this::hideOverlay);
        NeoForge.EVENT_BUS.addListener(this::hands); NeoForge.EVENT_BUS.addListener(this::logout);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod,parent)->ZoomSettingsScreen.create(parent));
    }
    private KeyMapping key(String name,int code,RegisterKeyMappingsEvent event) {
        KeyMapping key=new KeyMapping("key.cinematiczoom."+name,code,CATEGORY); event.register(key); return key;
    }
    private void keys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        ZOOM_KEYBIND=key("zoom",67,event); ZOOM_NO_BARS_KEYBIND=key("zoom_no_bars",90,event);
        INCREASE_BARS_KEYBIND=key("increase_bars",265,event); DECREASE_BARS_KEYBIND=key("decrease_bars",264,event);
        PREVIOUS_ASPECT_KEYBIND=key("previous_aspect",263,event); NEXT_ASPECT_KEYBIND=key("next_aspect",262,event);
    }
    private void tick(ClientTickEvent.Post event) {
        ZoomManager.tick(Minecraft.getInstance(),ZOOM_KEYBIND,ZOOM_NO_BARS_KEYBIND,INCREASE_BARS_KEYBIND,DECREASE_BARS_KEYBIND,PREVIOUS_ASPECT_KEYBIND,NEXT_ASPECT_KEYBIND);
    }
    private void fov(ViewportEvent.ComputeFov event) {
        ZoomManager.frameUpdate(); ZoomManager.setBaseFov(event.getFOV());
        event.setFOV((float)ZoomMath.fov(event.getFOV(),ZoomManager.getCurrentFovMul(),ZoomConfig.INSTANCE.fovMode));
    }
    private void scroll(InputEvent.MouseScrollingEvent event) {
        if(Minecraft.getInstance().screen==null && ZoomManager.onWheel(event.getScrollDeltaY())) event.setCanceled(true);
    }
    private void overlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.parse("cinematiczoom_extended:bars"),(graphics,partial)->ZoomManager.renderBars(graphics));
    }
    private void hideOverlay(RenderGuiLayerEvent.Pre event) {
        String id=event.getName().toString();
        if(id.equals("cinematiczoom_extended:bars")) return;
        if (id.equals("minecraft:crosshair") ? ZoomManager.shouldHideCrosshair() : ZoomManager.shouldHideHud()) event.setCanceled(true);
    }
    private void hands(RenderHandEvent event) { if(ZoomManager.shouldHideHands()) event.setCanceled(true); }
    private void logout(ClientPlayerNetworkEvent.LoggingOut event) { ZoomManager.reset(Minecraft.getInstance()); ZoomConfig.INSTANCE.save(); }
}
