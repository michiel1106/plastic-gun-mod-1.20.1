package systems.brn.plasticgun.packets;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record ModDetect(UUID slapped) implements CustomPayload {
    public static final CustomPayload.Id<ModDetect> PACKET_ID = new CustomPayload.Id<>(CustomPayload.id("moddetect").id());
    public static final PacketCodec<RegistryByteBuf, ModDetect> PACKET_CODEC = Uuids.PACKET_CODEC.xmap(ModDetect::new, ModDetect::slapped).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
