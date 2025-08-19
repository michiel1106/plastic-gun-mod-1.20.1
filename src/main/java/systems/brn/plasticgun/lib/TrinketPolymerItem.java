package systems.brn.plasticgun.lib;


import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static systems.brn.plasticgun.lib.Util.id;

public class TrinketPolymerItem extends ArmorItem implements PolymerItem {

    public TrinketPolymerItem(Item.Settings settings, String name) {
        super(ArmorMaterials.LEATHER, Type.CHESTPLATE, settings);
     //  super(ArmorMaterials.LEATHER, Type.CHESTPLATE, settings, id(name));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (equipItem(user, stack)) {


            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }
        return super.use(world, user, hand);
    }

    public static boolean equipItem(PlayerEntity user, ItemStack stack) {
        return equipItem((LivingEntity) user, stack);
    }

    public static boolean equipItem(LivingEntity user, ItemStack stack) {
        user.equipStack(EquipmentSlot.CHEST, stack);
        user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1, 1);
        return false;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.STICK;
    }
}