package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public abstract class SimpleItem extends SimplePolymerItem implements PolymerItem {
    private final Identifier polymerModel;
    protected final Identifier identifier;

    public SimpleItem(Settings settings, Identifier identifier, Item replacement) {
        super(settings, replacement);
        this.identifier = identifier;
        this.polymerModel = PolymerResourcePackUtils.getBridgedModelId(identifier.withPath("item/" + identifier.getPath()));
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.polymerModel;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext player) {
        return Items.STICK;
    }

}