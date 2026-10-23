package mix.cinematiczoom;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
@Mod("cinematiczoom_extended")
public final class CinematicZoomClient {
    public static KeyMapping ZOOM_KEYBIND,ZOOM_NO_BARS_KEYBIND,INCREASE_BARS_KEYBIND,DECREASE_BARS_KEYBIND,PREVIOUS_ASPECT_KEYBIND,NEXT_ASPECT_KEYBIND;
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(Identifier.parse("cinematiczoom:main"));
    public CinematicZoomClient() {
        if(FMLEnvironment.dist!=Dist.CLIENT) return;
        ZoomConfig.configureDirectory(FMLPaths.CONFIGDIR.get()); ZoomConfig.INSTANCE.load();
        RegisterKeyMappingsEvent.BUS.addListener(this::keys);
        AddGuiOverlayLayersEvent.BUS.addListener(this::layers);
        TickEvent.ClientTickEvent.Post.BUS.addListener(this::tick);
        ViewportEvent.ComputeFov.BUS.addListener(this::fov);
        InputEvent.MouseScrollingEvent.BUS.addListener(this::scroll);
        RenderHandEvent.BUS.addListener((java.util.function.Predicate<RenderHandEvent>) event -> ZoomManager.shouldHideHands());
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> {ZoomManager.reset(Minecraft.getInstance());ZoomConfig.INSTANCE.save();});
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,()->new ConfigScreenHandler.ConfigScreenFactory((mc,parent)->ZoomSettingsScreen.create(parent)));
    }
    private KeyMapping key(String name,int code,RegisterKeyMappingsEvent event) {
        KeyMapping key=new KeyMapping("key.cinematiczoom."+name,code,CATEGORY);event.register(key);return key;
    }
    private void layers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(Identifier.parse("cinematiczoom_extended:bars"), (graphics, time) -> ZoomManager.renderBars(graphics));
    }
    private void keys(RegisterKeyMappingsEvent event) {
        ZOOM_KEYBIND=key("zoom",67,event);ZOOM_NO_BARS_KEYBIND=key("zoom_no_bars",90,event);
        INCREASE_BARS_KEYBIND=key("increase_bars",265,event);DECREASE_BARS_KEYBIND=key("decrease_bars",264,event);
        PREVIOUS_ASPECT_KEYBIND=key("previous_aspect",263,event);NEXT_ASPECT_KEYBIND=key("next_aspect",262,event);
    }
    private void tick(TickEvent.ClientTickEvent.Post event) {
        ZoomManager.tick(Minecraft.getInstance(),ZOOM_KEYBIND,ZOOM_NO_BARS_KEYBIND,INCREASE_BARS_KEYBIND,DECREASE_BARS_KEYBIND,PREVIOUS_ASPECT_KEYBIND,NEXT_ASPECT_KEYBIND);
    }
    private void fov(ViewportEvent.ComputeFov event) {
        ZoomManager.frameUpdate();ZoomManager.setBaseFov(event.getFOV());
        event.setFOV((float)ZoomMath.fov(event.getFOV(),ZoomManager.getCurrentFovMul(),ZoomConfig.INSTANCE.fovMode));
    }
    private boolean scroll(InputEvent.MouseScrollingEvent event) {
        return Minecraft.getInstance().screen==null && ZoomManager.onWheel(event.getDeltaY());
    }
}
