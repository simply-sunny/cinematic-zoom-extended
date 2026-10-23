package mix.cinematiczoom.autotest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
public final class FabricTestClient implements ClientModInitializer {
    public void onInitializeClient() {
        AutoTestClient test = new AutoTestClient(); test.start();
        ClientTickEvents.END_CLIENT_TICK.register(test::tick);
    }
}
