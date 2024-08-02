package systems.brn.plasticgun.companion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class PlasticGunClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::tick);
        ClientPlayConnectionEvents.JOIN.register(ClientEvents::join);
        HudRenderCallback.EVENT.register(ClientEvents::HUDDraw);
    }
}
