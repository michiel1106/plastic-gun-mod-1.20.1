package systems.brn.plasticgun.packets;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record Reload(UUID slapped) implements CustomPayload {
    public static final Id<Reload> PACKET_ID = new Id<>(CustomPayload.id("reload").id());
    public static final PacketCodec<RegistryByteBuf, Reload> PACKET_CODEC = Uuids.PACKET_CODEC.xmap(Reload::new, Reload::slapped).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
