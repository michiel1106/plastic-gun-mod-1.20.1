package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;

import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public abstract class SimpleItem extends SimplePolymerItem implements PolymerItem {
    private final Identifier polymerModel;
    protected final Identifier identifier;

    PolymerModelData modelData;

    public SimpleItem(Settings settings, Identifier identifier, Item replacement) {
        super(settings, replacement);
        this.identifier = identifier;
        this.polymerModel = identifier;
        modelData = PolymerResourcePackUtils.requestModel(Items.STICK,  new Identifier(identifier.getNamespace(), "item/" + identifier.getPath()));
    }


    @Override
    public Item getPolymerReplacement(ServerPlayerEntity player) {
        return super.getPolymerReplacement(player);
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return modelData.value();
    }

    public Identifier getPolymerModel() {
        return polymerModel;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.STICK;
    }

}