package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("cinematiczoom_extended")
public final class CinematicZoomClient {
    public static KeyMapping ZOOM_KEYBIND, ZOOM_NO_BARS_KEYBIND, INCREASE_BARS_KEYBIND, DECREASE_BARS_KEYBIND, PREVIOUS_ASPECT_KEYBIND, NEXT_ASPECT_KEYBIND;
    public CinematicZoomClient() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        ZoomConfig.configureDirectory(FMLPaths.CONFIGDIR.get());
        ZoomConfig.INSTANCE.load();
        var bus=FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::keys);
        bus.addListener(this::overlays);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
        MinecraftForge.EVENT_BUS.addListener(this::fov);
        MinecraftForge.EVENT_BUS.addListener(this::scroll);
        MinecraftForge.EVENT_BUS.addListener(this::hideOverlay);
        MinecraftForge.EVENT_BUS.addListener(this::hands);
        MinecraftForge.EVENT_BUS.addListener(this::logout);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc,parent) -> ZoomSettingsScreen.create(parent)));
    }
    private KeyMapping key(String name,int code,RegisterKeyMappingsEvent event) {
        KeyMapping key=new KeyMapping("key.cinematiczoom."+name,code,"key.categories.cinematiczoom");
        event.register(key); return key;
    }
    private void keys(RegisterKeyMappingsEvent event) {
        ZOOM_KEYBIND=key("zoom",67,event); ZOOM_NO_BARS_KEYBIND=key("zoom_no_bars",86,event);
        INCREASE_BARS_KEYBIND=key("increase_bars",265,event); DECREASE_BARS_KEYBIND=key("decrease_bars",264,event);
        PREVIOUS_ASPECT_KEYBIND=key("previous_aspect",263,event); NEXT_ASPECT_KEYBIND=key("next_aspect",262,event);
    }
    private void tick(TickEvent.ClientTickEvent event) {
        if(event.phase==TickEvent.Phase.END) ZoomManager.tick(Minecraft.getInstance(),ZOOM_KEYBIND,ZOOM_NO_BARS_KEYBIND,INCREASE_BARS_KEYBIND,DECREASE_BARS_KEYBIND,PREVIOUS_ASPECT_KEYBIND,NEXT_ASPECT_KEYBIND);
    }
    private void fov(ViewportEvent.ComputeFov event) {
        ZoomManager.frameUpdate(); ZoomManager.setBaseFov(event.getFOV());
        event.setFOV(ZoomMath.fov(event.getFOV(),ZoomManager.getCurrentFovMul(),ZoomConfig.INSTANCE.fovMode));
    }
    private void scroll(InputEvent.MouseScrollingEvent event) {
        if(Minecraft.getInstance().screen==null && ZoomManager.onWheel(event.getScrollDelta())) event.setCanceled(true);
    }
    private void overlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("bars",(gui,graphics,partial,width,height)->ZoomManager.renderBars(graphics));
    }
    private void hideOverlay(RenderGuiOverlayEvent.Pre event) {
        String id=event.getOverlay().id().toString();
        if(id.equals("cinematiczoom_extended:bars")) return;
        if (id.equals("minecraft:crosshair") ? ZoomManager.shouldHideCrosshair() : ZoomManager.shouldHideHud()) event.setCanceled(true);
    }
    private void hands(RenderHandEvent event) { if(ZoomManager.shouldHideHands()) event.setCanceled(true); }
    private void logout(ClientPlayerNetworkEvent.LoggingOut event) { ZoomManager.reset(Minecraft.getInstance()); ZoomConfig.INSTANCE.save(); }
}
