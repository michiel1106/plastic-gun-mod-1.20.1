package systems.brn.plasticgun.companion;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ColorHelper;
import systems.brn.plasticgun.packets.ModDetect;
import systems.brn.plasticgun.packets.Reload;
import systems.brn.plasticgun.packets.Shoot;

import java.util.UUID;

import static systems.brn.plasticgun.PlasticGun.flashbangEffect;
import static systems.brn.plasticgun.lib.Util.shouldSendClickEvents;

public class ClientEvents {
    public static void tick(MinecraftClient minecraftClient) {
        if (minecraftClient.options.useKey.isPressed() && minecraftClient.player != null) {
            if (shouldSendClickEvents(minecraftClient.player)) {
                UUID reloadUUID = UUID.randomUUID();
                ClientPlayNetworking.send(new Reload(reloadUUID));
            }
        }
        if (minecraftClient.options.attackKey.isPressed() && minecraftClient.player != null) {
            if (shouldSendClickEvents(minecraftClient.player)) {
                UUID shootUUID = UUID.randomUUID();
                ClientPlayNetworking.send(new Shoot(shootUUID));
            }
        }
    }

    public static void join(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
        UUID joinUUID = UUID.randomUUID();
        packetSender.sendPacket(new ModDetect(joinUUID));
    }

    public static void HUDDraw(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (MinecraftClient.getInstance().player != null) {
            if (MinecraftClient.getInstance().player.hasStatusEffect(flashbangEffect)) {
                int width = drawContext.getScaledWindowWidth();
                int height = drawContext.getScaledWindowHeight();
                drawContext.fill(0, 0, width, height, ColorHelper.Argb.fromFloats(1, 1, 1, 1));
            }
        }
    }
}
