package systems.brn.plasticgun.lib;


import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import static systems.brn.plasticgun.lib.Util.id;

public class TrinketPolymerItem extends SimpleItem {

    public TrinketPolymerItem(Item.Settings settings, String name) {
        super(settings.equippable(EquipmentSlot.CHEST), id(name), Items.STICK);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (equipItem(user, stack)) {
            return ActionResult.SUCCESS;
        }
        return super.use(world, user, hand);
    }

    public static boolean equipItem(PlayerEntity user, ItemStack stack) {
        return equipItem((LivingEntity) user, stack);
    }

    public static boolean equipItem(LivingEntity user, ItemStack stack) {
        user.equipStack(EquipmentSlot.CHEST, stack);
        user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value());
        return false;
    }
}