package systems.brn.plasticgun.packets;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record Shoot(UUID slapped) implements CustomPayload {
    public static final Id<Shoot> PACKET_ID = new Id<>(CustomPayload.id("shoot").id());
    public static final PacketCodec<RegistryByteBuf, Shoot> PACKET_CODEC = Uuids.PACKET_CODEC.xmap(Shoot::new, Shoot::slapped).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
