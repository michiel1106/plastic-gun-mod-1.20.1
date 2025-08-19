package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;

import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public abstract class SimpleItem extends SimplePolymerItem implements PolymerItem {
    private final Identifier polymerModel;
    protected final Identifier identifier;

    public SimpleItem(Settings settings, Identifier identifier, Item replacement) {
        super(settings, replacement);
        this.identifier = identifier;
        this.polymerModel = identifier;
    }


    public Identifier getPolymerModel() {
        return polymerModel;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.STICK;
    }

}