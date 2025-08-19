package systems.brn.plasticgun.packets;


import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;


import java.util.UUID;

public record ModDetect(UUID slapped) {
    public static final Identifier CHANNEL_ID = new Identifier("plasticgun", "moddetect");

    public static PacketByteBuf toBuf(ModDetect modDetect) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(modDetect.slapped);
        return buf;
    }

    public static ModDetect fromBuf(PacketByteBuf buf) {
        return new ModDetect(buf.readUuid());
    }
}
