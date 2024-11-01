package systems.brn.plasticgun.companion;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.ColorHelper;
import systems.brn.plasticgun.packets.ModDetect;

import java.util.UUID;

import static systems.brn.plasticgun.PlasticGun.flashbangEffect;
public class ClientEvents {
    public static void join(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
        UUID joinUUID = UUID.randomUUID();
        packetSender.sendPacket(new ModDetect(joinUUID));
    }

    public static void HUDDraw(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (MinecraftClient.getInstance().player != null) {
            if (MinecraftClient.getInstance().player.hasStatusEffect(flashbangEffect)) {
                int width = drawContext.getScaledWindowWidth();
                int height = drawContext.getScaledWindowHeight();
                drawContext.fill(0, 0, width, height, ColorHelper.fromFloats(1, 1, 1, 1));
            }
        }
    }
}
