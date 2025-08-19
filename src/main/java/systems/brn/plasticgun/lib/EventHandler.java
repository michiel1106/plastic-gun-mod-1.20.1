package systems.brn.plasticgun.lib;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import systems.brn.plasticgun.grenades.GrenadeEntity;
import systems.brn.plasticgun.grenades.GrenadeItem;
import systems.brn.plasticgun.packets.ModDetect;

import java.util.function.Predicate;

import static systems.brn.plasticgun.PlasticGun.*;
import static systems.brn.plasticgun.lib.GunComponents.*;

public class EventHandler {
    public static TypedActionResult<ItemStack> onItemUse(PlayerEntity playerEntity, World world, Hand hand) {
        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
            if (!world.isClient) {
                rightClickWithItem(serverPlayerEntity, hand);
            }
        }

        TypedActionResult<ItemStack> pass = TypedActionResult.pass(playerEntity.getMainHandStack());
        return pass;
    }

    public static void rightClickWithItem(ServerPlayerEntity serverPlayerEntity, Hand hand) {
        if (serverPlayerEntity.getWorld() instanceof ServerWorld world) {
            Item stackInHand = serverPlayerEntity.getStackInHand(hand).getItem();
            if (itemGunMap.containsKey(stackInHand)) {
                itemGunMap.get(stackInHand).reload(world, serverPlayerEntity, hand);
            }
            if (itemGrenadeItemMap.containsKey(stackInHand)) {
                itemGrenadeItemMap.get(stackInHand).unpin(world, serverPlayerEntity, hand);
            }
            if (itemShurikenItemMap.containsKey(stackInHand)) {
                itemShurikenItemMap.get(stackInHand).chuck(world, serverPlayerEntity, hand);
            }
        }

    }

    public static void leftClickWithItem(ServerPlayerEntity serverPlayerEntity, Hand hand) {
        if (serverPlayerEntity.getWorld() instanceof ServerWorld world) {
            ItemStack stackInHand = serverPlayerEntity.getStackInHand(hand);
            Item itemInHand = stackInHand.getItem();
            if (itemGrenadeItemMap.containsKey(itemInHand)) {
                itemGrenadeItemMap.get(itemInHand).chuck(world, serverPlayerEntity, hand);
            } else if (itemShurikenItemMap.containsKey(itemInHand)) {
                itemShurikenItemMap.get(itemInHand).chuck(world, serverPlayerEntity, hand);
            } else if (itemGunMap.containsKey(itemInHand)) {
                itemGunMap.get(itemInHand).shoot(world, serverPlayerEntity, hand);
            }
        }
    }

    public static void tickItemUpdate(ServerPlayerEntity serverPlayerEntity) {
        if (serverPlayerEntity.getWorld() instanceof ServerWorld world) {
            Hand hand = serverPlayerEntity.getActiveHand();
            ItemStack stackInHand = serverPlayerEntity.getStackInHand(hand);
            Item itemInHand = stackInHand.getItem();
            if (itemGunMap.containsKey(itemInHand)) {
                decrementComponent("gun_cooldown", stackInHand);
                decrementComponent("gun_reload_cooldown", stackInHand);
            }

            PlayerInventory playerInventory = serverPlayerEntity.getInventory();
            for (int i = 1; i < playerInventory.main.size(); i++) {
                ItemStack stackInSlot = playerInventory.main.get(i);
                Item itemInSlot = stackInSlot.getItem();
                if (itemGrenadeItemMap.containsKey(itemInSlot)) {
                    decrementComponent("grenade_tuner", stackInSlot);

                    GrenadeItem grenadeItem = itemGrenadeItemMap.get(itemInSlot);
                    GrenadeItem.updateDamage(stackInSlot, grenadeItem);
                    grenadeItem.checkExplosions(world, serverPlayerEntity, stackInSlot);
                }
            }
        }
    }


    public static void mobTickUpdate(ServerWorld world) {
        Predicate<Entity> allEntities = entity -> true;
        for (SkeletonEntity skeletonEntity : world.getEntitiesByType(EntityType.SKELETON, allEntities)) {
            ItemStack itemStack = skeletonEntity.getActiveItem();
                if (itemGunMap.containsKey(itemStack.getItem())) {
                    decrementComponent("gun_cooldown", itemStack);
                    decrementComponent("gun_reload_cooldown", itemStack);
                }
        }
    }

    public static void onServerWorldTick(ServerWorld world) {
        // Iterate through all players to detect hand swings or item interactions
        if (!world.isClient) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                Hand hand = player.getActiveHand();
                if (player.handSwinging && player.handSwingTicks == -1) {
                    leftClickWithItem(player, hand);
                }
                tickItemUpdate(player);
            }
            mobTickUpdate(world);
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

    public static void disconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer
            minecraftServer) {
        ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
        clientsWithMod.remove(player);
    }


    public static void onClientConfirm(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (!clientsWithMod.contains(player)) {

            clientsWithMod.add(player);
        }
    }
}
