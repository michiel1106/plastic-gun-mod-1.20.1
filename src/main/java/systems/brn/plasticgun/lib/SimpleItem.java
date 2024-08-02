package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import systems.brn.plasticgun.PlasticGun;

public abstract class SimpleItem extends SimplePolymerItem implements PolymerItem {
    private final PolymerModelData polymerModel;
    protected final Identifier identifier;

    public SimpleItem(Settings settings, Identifier identifier, Item replacement) {
        super(settings, replacement);
        this.identifier = identifier;
        this.polymerModel = PolymerResourcePackUtils.requestModel(replacement, identifier.withPath("item/" + identifier.getPath()));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if(PlasticGun.clientsWithMod.contains(player)){
            return this;
        }
        return this.polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerModel.value();
    }

}