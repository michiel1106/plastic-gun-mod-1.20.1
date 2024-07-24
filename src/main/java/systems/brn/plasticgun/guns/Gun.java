package systems.brn.plasticgun.guns;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.ArrayList;

import static systems.brn.plasticgun.PlasticGun.bullets;
import static systems.brn.plasticgun.lib.GunComponents.GUN_AMMO_COMPONENT;
import static systems.brn.plasticgun.lib.GunComponents.GUN_LOADING_COMPONENT;
import static systems.brn.plasticgun.lib.Util.*;

public class Gun extends SimpleItem implements PolymerItem {

    public final double damage;
    public final int reloadCount;
    public final int clipSize;
    public final int speed;
    public final ArrayList<Item> ammo;
    public final int caliber;

    public Gun(String path, double damage, int reloadCount, int clipSize, int speed, int caliber) {
        super(
                new Settings()
                        .maxCount(1)
                        .component(GUN_AMMO_COMPONENT, ItemStack.EMPTY)
                        .maxDamage(clipSize + 1)
                , id(path), Items.WOODEN_HOE
        );
        Item item = Registry.register(Registries.ITEM, id(path), this);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> content.add(item));
        this.damage = damage;
        this.reloadCount = reloadCount;
        this.clipSize = clipSize;
        this.speed = speed;
        ArrayList<Item> ammo = new ArrayList<>();
        for (BulletItem bullet : bullets) {
            if (bullet.caliber == caliber) {
                ammo.add(bullet);
            }
        }
        this.ammo = ammo;
        this.caliber = caliber;
    }

    public void reload(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            ItemStack bulletStack = findBulletStack(ammo, player);
            ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();
            int bulletsInChamber = chamber.getCount();
            int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);

            if (bulletStack != null && !bulletStack.isEmpty()) { //we have ammo
                if (currentReload < reloadCount) { //still reloading
                    stack.set(GUN_LOADING_COMPONENT, currentReload + 1);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, 0.1f, 1.8f);
                } else if (currentReload == reloadCount) { //now reload
                    int addedBullets = Math.min(bulletStack.getCount(), clipSize - bulletsInChamber); //how many
                    if (chamber.isEmpty() || chamber.getItem() == bulletStack.getItem()) {
                        if (chamber.isEmpty()) {
                            chamber = bulletStack.copy();
                        }
                        chamber.setCount(bulletsInChamber + addedBullets);
                        bulletStack.decrement(addedBullets);
                        if (chamber.isEmpty()) {
                            stack.set(GUN_AMMO_COMPONENT, ItemStack.EMPTY);
                        } else {
                            stack.set(GUN_AMMO_COMPONENT, chamber);
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.0f);
                        }
                        stack.set(GUN_LOADING_COMPONENT, 1);
                    } else {
                        if (canInsertItemIntoInventory(player.getInventory(), chamber.copy()) == chamber.getCount()) { //can take out chamber
                            insertStackIntoInventory(player.getInventory(), chamber.copy());
                            chamber.setCount(0); //empty
                            int targetCount = Math.min(bulletStack.getCount(), clipSize);
                            chamber = bulletStack.copy();
                            chamber.setCount(targetCount);
                            if (chamber.isEmpty()) {
                                stack.set(GUN_AMMO_COMPONENT, ItemStack.EMPTY);
                            } else {
                                stack.set(GUN_AMMO_COMPONENT, chamber);
                                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 2.5f);
                            }
                            bulletStack.decrement(targetCount);
                            stack.set(GUN_LOADING_COMPONENT, 1);
                        }
                    }
                }
            }
            if (player.isCreative()) {
                stack.set(GUN_AMMO_COMPONENT, new ItemStack(ammo.getFirst(), clipSize)); // Ensure ammo.get(0) is a valid item
            }
            updateDamage(stack);
        }
    }

    public void updateDamage(ItemStack stack) {
        ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();
        int numBullets = chamber.getCount();
        stack.setDamage(clipSize - numBullets + 1);
    }

    public void shoot(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
            ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY);

            if (!chamber.isEmpty() && currentReload == 1) {
                BulletEntity bulletEntity = new BulletEntity(user.getPos(), player, chamber, user.getStackInHand(hand), this, damage, speed);
                world.spawnEntity(bulletEntity);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.1f, 1.2f);
                chamber.decrement(1);
                if (chamber.isEmpty()) {
                    stack.set(GUN_AMMO_COMPONENT, ItemStack.EMPTY);
                } else {
                    stack.set(GUN_AMMO_COMPONENT, chamber);
                }
            } else {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
            }
            updateDamage(stack);
        }
    }
}
