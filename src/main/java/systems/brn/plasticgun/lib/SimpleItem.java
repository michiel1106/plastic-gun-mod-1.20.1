package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleItem extends SimplePolymerItem implements PolymerItem {
    private final PolymerModelData polymerModel;
    protected final Identifier identifier;

    public SimpleItem(Settings settings, Identifier identifier) {
        super(settings, Items.BARRIER);
        this.identifier = identifier;
        this.polymerModel = PolymerResourcePackUtils.requestModel(Items.BARRIER, identifier.withPath("item/" + identifier.getPath()));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.value();
    }

}