package systems.brn.plasticgun.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import systems.brn.plasticgun.grenades.GrenadeEntity;
import systems.brn.plasticgun.grenades.GrenadeItem;
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.shurikens.ShurikenItem;

import static systems.brn.plasticgun.PlasticGun.*;
import static systems.brn.plasticgun.lib.GunComponents.*;

public class EventHandler {
    public static TypedActionResult<ItemStack> onItemUse(PlayerEntity playerEntity, World world, Hand hand) {
        ItemStack stack = playerEntity.getStackInHand(hand);
        if (!world.isClient) {
            Item stackInHand = playerEntity.getStackInHand(hand).getItem();
            if (itemGunMap.containsKey(stackInHand)) {
                itemGunMap.get(stackInHand).reload(world, playerEntity, hand);
            }
            if (itemGrenadeItemMap.containsKey(stackInHand)) {
                itemGrenadeItemMap.get(stackInHand).unpin(world, playerEntity, hand);
            }
            if (itemShurikenItemMap.containsKey(stackInHand)) {
                itemShurikenItemMap.get(stackInHand).chuck(world, playerEntity, hand);
            }

        }
        return TypedActionResult.pass(stack);
    }


    public static void onServerWorldTick(ServerWorld world) {
        // Iterate through all players to detect hand swings or item interactions
        for (PlayerEntity player : world.getPlayers()) {
            if (!world.isClient) {
                Hand hand = player.getActiveHand();
                ItemStack stackInHand = player.getStackInHand(hand);
                Item itemInHand = stackInHand.getItem();

                if (itemGunMap.containsKey(itemInHand)) {
                    decrementComponent(GUN_COOLDOWN_COMPONENT, stackInHand);
                    decrementComponent(GUN_RELOAD_COOLDOWN_COMPONENT, stackInHand);
                    if (player.handSwinging && player.handSwingTicks == -1) {
                        itemGunMap.get(itemInHand).shoot(world, player, hand);
                    }
                }
                if (itemGrenadeItemMap.containsKey(itemInHand)) {
                    itemGrenadeItemMap.get(itemInHand).chuck(world, player, hand);
                }
                if (itemShurikenItemMap.containsKey(itemInHand)) {
                    itemShurikenItemMap.get(itemInHand).chuck(world, player, hand);
                }

                PlayerInventory playerInventory = player.getInventory();
                for (int i = 1; i < playerInventory.main.size(); i++) {
                    ItemStack stackInSlot = playerInventory.main.get(i);
                    Item itemInSlot = stackInSlot.getItem();
                    if (itemGrenadeItemMap.containsKey(itemInSlot)) {
                        decrementComponent(GRENADE_TIMER_COMPONENT, stackInSlot);
                        GrenadeItem grenadeItem = itemGrenadeItemMap.get(itemInSlot);
                        GrenadeItem.updateDamage(stackInSlot, grenadeItem);
                        grenadeItem.checkExplosions(world, player, stackInSlot);
                    }
                }
            }
        }
        for (SkeletonEntity skeletonEntity : world.getEntitiesByType(EntityType.SKELETON, entity -> true)) {
            for (ItemStack itemStack : skeletonEntity.getEquippedItems()) {
                if (itemGunMap.containsKey(itemStack.getItem())) {
                    decrementComponent(GUN_COOLDOWN_COMPONENT, itemStack);
                    decrementComponent(GUN_RELOAD_COOLDOWN_COMPONENT, itemStack);
                }
            }
        }
    }

    public static void onEntityLoad(Entity entity, ServerWorld world) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack entityStack = itemEntity.getStack();
            Item item = entityStack.getItem();
            if (itemGrenadeItemMap.containsKey(item)) {
                GrenadeItem grenadeItem = itemGrenadeItemMap.get(item);
                Entity owner = itemEntity.getOwner();
                int timer = (1 + grenadeItem.explosionTarget) - entityStack.getDamage();
                if (timer <= grenadeItem.explosionTarget) {
                    if (owner instanceof ServerPlayerEntity player) {
                        GrenadeEntity grenadeEntity = new GrenadeEntity(player, entityStack, timer, 0.5f, 0, grenadeItem.explosionPower, grenadeItem.repulsionPower, grenadeItem.isIncendiary, grenadeItem.isFragmentation, grenadeItem.flashBangDuration, grenadeItem.stunDuration, grenadeItem.smokeTicks, grenadeItem.smokeRadius, grenadeItem.smokeCount);
                        grenadeEntity.setVelocity(entity.getVelocity());
                        world.spawnEntity(grenadeEntity);
                        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
                    } else {
                        GrenadeEntity grenadeEntity = new GrenadeEntity(world, entity.getPos(), entityStack, timer, 1f, grenadeItem.explosionPower, grenadeItem.repulsionPower, grenadeItem.isIncendiary, grenadeItem.isFragmentation, grenadeItem.flashBangDuration, grenadeItem.stunDuration, grenadeItem.smokeTicks, grenadeItem.smokeRadius, grenadeItem.smokeCount);
                        grenadeEntity.setVelocity(entity.getVelocity());
                        world.spawnEntity(grenadeEntity);
                    }
                    entity.discard();
                }
            }
        }
    }
}
