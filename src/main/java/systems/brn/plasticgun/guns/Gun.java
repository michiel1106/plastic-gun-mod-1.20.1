package systems.brn.plasticgun.guns;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.ArrayList;
import java.util.Set;

import static systems.brn.plasticgun.PlasticGun.bullets;
import static systems.brn.plasticgun.lib.GunComponents.*;
import static systems.brn.plasticgun.lib.Util.*;

public class Gun extends SimpleItem implements PolymerItem {

    public final double damage;
    public final int reloadCount;
    public final int clipSize;
    public final int speed;
    public final ArrayList<Item> ammo;
    public final int caliber;
    private final double explosionPowerGun;
    private final double repulsionPowerGun;
    private final int cooldownTarget;
    private final int reloadTarget;

    public Gun(String path, double damage, int reloadCount, int reloadTarget, int clipSize, int speed, int caliber, int cooldownTarget, boolean hasScope, double explosionPowerGun, double repulsionPowerGun) {
        super(
                new Settings()
                        .maxCount(1)
                        .component(GUN_AMMO_COMPONENT, ItemStack.EMPTY)
                        .component(GUN_COOLDOWN_COMPONENT, 0)
                        .component(GUN_RELOAD_COOLDOWN_COMPONENT, 0)
                        .maxDamage(clipSize + 1)
                , id(path), hasScope ? Items.SPYGLASS : Items.WOODEN_SWORD
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
        this.explosionPowerGun = explosionPowerGun;
        this.repulsionPowerGun = repulsionPowerGun;
        this.cooldownTarget = cooldownTarget;
        this.reloadTarget = reloadTarget + 1;
    }

    public void reload(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            int currentReloadCooldown = stack.getOrDefault(GUN_RELOAD_COOLDOWN_COMPONENT, 0);
            if (currentReloadCooldown == 0) {
                stack.set(GUN_RELOAD_COOLDOWN_COMPONENT, reloadTarget);
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
                                stack.remove(GUN_AMMO_COMPONENT);
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
                                    stack.remove(GUN_AMMO_COMPONENT);
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
            } else {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 2.0f);
            }
        }
    }

    public void updateDamage(ItemStack stack) {
        ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();
        int numBullets = chamber.getCount();
        int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
        if (currentReload != 1) {
            numBullets = -clipSize;
        }
        stack.setDamage((clipSize - numBullets) + 1);
    }

    public void doRecoil(ServerPlayerEntity player) {
        // Get the player's current position and yaw
        Vec3d pos = player.getPos();
        float yaw = player.getYaw();
        float newPitch = player.getPitch();
        newPitch -= player.getWorld().getRandom().nextBetween(1, 4);
        yaw -= player.getWorld().getRandom().nextBetween(-2, 2);

        player.teleport(player.getServerWorld(), pos.x, pos.y, pos.z, Set.of(PositionFlag.X_ROT, PositionFlag.Y_ROT), yaw, newPitch);
    }

    public void shoot(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
            int currentCooldown = stack.getOrDefault(GUN_COOLDOWN_COMPONENT, 0);
            ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();

            BulletItem bullet = null;
            for (BulletItem bulletTemp : bullets) {
                if (bulletTemp == chamber.getItem()) {
                    bullet = bulletTemp;
                    break;
                }
            }

            if (!chamber.isEmpty() && currentReload == 1 && currentCooldown == 0) {
                boolean isIncendiary = false;
                double explosionPower = explosionPowerGun;
                double repulsionPower = repulsionPowerGun;

                if (bullet != null) {
                    isIncendiary = bullet.isIncendiary;
                    explosionPower *= bullet.explosionPowerCoefficient;
                    repulsionPower *= bullet.repulsionPowerCoefficient;
                }

                BulletEntity bulletEntity = new BulletEntity(user.getPos(), player, chamber, user.getStackInHand(hand), this, 1f, damage, speed, explosionPower, repulsionPower, isIncendiary);
                world.spawnEntity(bulletEntity);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.1f, 1.2f);
                chamber.decrement(1);
                stack.set(GUN_COOLDOWN_COMPONENT, cooldownTarget);
                doRecoil(player);
                if (chamber.isEmpty()) {
                    stack.remove(GUN_AMMO_COMPONENT);
                } else {
                    stack.set(GUN_AMMO_COMPONENT, chamber);
                }
            } else if (cooldownTarget > 0) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 2.0f);
            } else if (currentReload > 1) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
            }
            updateDamage(stack);
        }
    }
}
