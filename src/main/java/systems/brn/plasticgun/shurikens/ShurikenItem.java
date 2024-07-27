package systems.brn.plasticgun.shurikens;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.List;

import static systems.brn.plasticgun.lib.Util.id;

public class ShurikenItem extends SimpleItem implements PolymerItem {

    public final double damage;
    public final float speed;

    public ShurikenItem(String path, double damage, int durability, float speed) {
        super(
                new Settings()
                        .maxCount(16)
                        .maxDamage(durability)
                        .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                Text.translatable("gun.description.damage", damage),
                                Text.translatable("gun.description.speed", speed)
                        )))
                , id(path), Items.WOODEN_PICKAXE
        );
        Registry.register(Registries.ITEM, id(path), this);
        this.damage = damage;
        this.speed = speed;
    }


    public void chuck(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            ShurikenEntity ShurikenEntity = new ShurikenEntity(player, stack, speed, damage);
            world.spawnEntity(ShurikenEntity);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0f, 2.0f);
            if (!player.isCreative()) {
                stack.decrement(1);
            }
        }
    }
}
