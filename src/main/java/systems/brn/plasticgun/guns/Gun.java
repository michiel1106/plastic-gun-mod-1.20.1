package systems.brn.plasticgun.guns;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static systems.brn.plasticgun.PlasticGun.bullets;
import static systems.brn.plasticgun.PlasticGun.itemBulletItemMap;
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

    private final float verticalRecoilMin;
    private final float verticalRecoilMax;

    private final float velocityRecoilMin;
    private final float velocityRecoilMax;

    private final double horizontalRecoilMin;
    private final double horizontalRecoilMax;

    public Gun(String path, double damage, int reloadCount, int reloadTarget, int clipSize, int speed, int caliber, int cooldownTarget, double explosionPowerGun, double repulsionPowerGun, float verticalRecoilMin, float verticalRecoilMax, float velocityRecoilMin, float velocityRecoilMax, double horizontalRecoilMin, double horizontalRecoilMax) {
        super(
                new Settings()
                        .maxCount(1)
                        .component(GUN_AMMO_COMPONENT, ItemStack.EMPTY)
                        .component(GUN_COOLDOWN_COMPONENT, 0)
                        .component(GUN_RELOAD_COOLDOWN_COMPONENT, 0)
                        .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                Text.translatable("gun.description.caliber", caliber),
                                Text.translatable("gun.description.damage_absolute", damage),
                                Text.translatable("gun.description.speed", speed),
                                Text.translatable("gun.description.clip_size", clipSize),
                                Text.translatable("gun.description.reload_cooldown", reloadTarget),
                                Text.translatable("gun.description.reload_cycles", reloadCount),
                                Text.translatable("gun.description.shoot_cooldown", cooldownTarget),
                                Text.translatable("gun.description.explosion_power", explosionPowerGun),
                                Text.translatable("gun.description.repulsion_power", repulsionPowerGun)
                        )))
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, id(path)))
                        .maxDamage(clipSize + 1)
                , id(path), Items.WOODEN_SWORD
        );
        this.verticalRecoilMin = verticalRecoilMin;
        this.verticalRecoilMax = verticalRecoilMax;
        this.velocityRecoilMin = velocityRecoilMin;
        this.velocityRecoilMax = velocityRecoilMax;
        this.horizontalRecoilMin = horizontalRecoilMin;
        this.horizontalRecoilMax = horizontalRecoilMax;
        Registry.register(Registries.ITEM, id(path), this);
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
                                stack.set(GUN_LAST_LOADED_AMMO, chamber);
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
                                    stack.set(GUN_LAST_LOADED_AMMO, chamber);
                                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 2.5f);
                                }
                                bulletStack.decrement(targetCount);
                                stack.set(GUN_LOADING_COMPONENT, 1);
                            }
                        }
                    }
                }
                if (player.isCreative()) {
                    ItemStack stackOfBullet = new ItemStack(ammo.getFirst(), clipSize);
                    stack.set(GUN_AMMO_COMPONENT, stackOfBullet);
                    stack.set(GUN_LAST_LOADED_AMMO, stackOfBullet);
                }
                updateDamage(stack);
            }
        }
    }

    public void reload(World world, MobEntity mobEntity, Hand hand, Random random, LocalDifficulty localDifficulty) {
        if (!world.isClient()) {
            ItemStack stack = mobEntity.getStackInHand(hand);
            int currentReloadCooldown = stack.getOrDefault(GUN_RELOAD_COOLDOWN_COMPONENT, 0);
            ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY);
            int bulletsInChamber = chamber.getCount();
            int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
            if (currentReload == 1 && bulletsInChamber > 0) {
                return;
            }
            if (currentReloadCooldown == 0) {
                stack.set(GUN_RELOAD_COOLDOWN_COMPONENT, reloadTarget);
                ItemStack oldChamber = stack.getOrDefault(GUN_LAST_LOADED_AMMO, ItemStack.EMPTY);
                Item ammoItem = (oldChamber == null || oldChamber.isEmpty())
                        ? ammo.get(selectWeaponIndex(random, localDifficulty, ammo.size()))
                        : oldChamber.getItem();
                ItemStack bulletStack = new ItemStack(ammoItem, world.getRandom().nextBetween(1, ammoItem.getMaxCount()));
                if (!bulletStack.isEmpty()) { // we have ammo
                    if (currentReload < reloadCount) { // still reloading
                        stack.set(GUN_LOADING_COMPONENT, currentReload + 1);
                    } else if (currentReload == reloadCount) { // now reload
                        stack.set(GUN_AMMO_COMPONENT, bulletStack);
                        stack.set(GUN_LAST_LOADED_AMMO, bulletStack);
                        stack.set(GUN_LOADING_COMPONENT, 1);
                    }
                }
            }
        }
    }

    public void updateDamage(ItemStack stack) {
        ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();
        BulletItem bulletItem = null;
        for (BulletItem bulletTemp : bullets) {
            if (bulletTemp == chamber.getItem()) {
                bulletItem = bulletTemp;
                break;
            }
        }
        int numBullets = chamber.getCount();
        int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
        if (currentReload != 1) {
            numBullets = -clipSize;
        }
        stack.setDamage((clipSize - numBullets) + 1);

        List<Text> loreList = new ArrayList<>();

        loreList.add(Text.translatable("gun.description.caliber", caliber));
        loreList.add(Text.translatable("gun.description.damage_absolute", damage));
        loreList.add(Text.translatable("gun.description.speed", speed));
        loreList.add(Text.translatable("gun.description.clip_size", clipSize));
        loreList.add(Text.translatable("gun.description.reload_cooldown", reloadTarget));
        loreList.add(Text.translatable("gun.description.reload_cycles", reloadCount));
        loreList.add(Text.translatable("gun.description.shoot_cooldown", cooldownTarget));
        if (explosionPowerGun > 0) {
            loreList.add(Text.translatable("gun.description.explosion_power", explosionPowerGun));
        }
        if (repulsionPowerGun > 0) {
            loreList.add(Text.translatable("gun.description.repulsion_power", repulsionPowerGun));
        }
        loreList.add(Text.translatable("gun.description.magazine_count", numBullets, clipSize));
        if (!chamber.isEmpty() && bulletItem != null) {
            loreList.add(Text.translatable("gun.description.damage_with_coefficient", damage * bulletItem.damageCoefficient));
            loreList.add(Text.translatable("gun.description.damage_with_coefficient_muzzle_speed", speed, speed * damage * bulletItem.damageCoefficient));
            loreList.add(Text.translatable("gun.description.magazine_bullet", bulletItem.getName()));
            loreList.add(Text.translatable("gun.description.damage_coefficient", bulletItem.damageCoefficient));
            loreList.add(Text.translatable("gun.description.explosion_coefficient", bulletItem.explosionPowerCoefficient));
            if (bulletItem.isIncendiary) {
                loreList.add(Text.translatable("gun.description.incendiary"));
            }
        } else {
            loreList.add(Text.translatable("gun.description.magazine_bullet", "Empty"));
        }

        LoreComponent newLore = new LoreComponent(loreList);

        stack.set(DataComponentTypes.LORE, newLore);
    }

    public int doRecoil(LivingEntity entity) {
        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
            Random rng = entity.getWorld().getRandom();
            // Get the entity's current position and yaw
            Vec3d pos = entity.getPos();
            float yaw = entity.getYaw();
            float newPitch = entity.getPitch();
            Vec3d currentLook = entity.getRotationVector().multiply(-1);
            float yawChange = verticalRecoilMin + rng.nextFloat() * (verticalRecoilMax - verticalRecoilMin);
            float pitchChange = (float) (horizontalRecoilMin + rng.nextFloat() * (horizontalRecoilMax - horizontalRecoilMin));
            newPitch -= yawChange;
            yaw -= pitchChange;
            entity.teleport(serverWorld, pos.x, pos.y, pos.z, PositionFlag.ROT, yaw, newPitch, true);
            double velocityRecoil = rng.nextDouble() * (velocityRecoilMax - velocityRecoilMin);
            if (velocityRecoil > 0) {
                entity.setVelocity(currentLook.multiply(velocityRecoil));
            }
            return (int) ((abs(yawChange) + abs(pitchChange) + abs(velocityRecoil) + max(abs(yawChange), max(abs(pitchChange), abs(velocityRecoil)))) / 4f) * 40;
        }
        return 0;
    }

    public int shoot(ServerWorld world, LivingEntity user, Hand hand) {
        int stunLen = 0;
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
            int currentCooldown = stack.getOrDefault(GUN_COOLDOWN_COMPONENT, 0);
            ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();

            BulletItem bullet = null;
            if (itemBulletItemMap.containsKey(chamber.getItem())) {
                bullet = itemBulletItemMap.get(chamber.getItem());
            }

            if (!chamber.isEmpty() && currentReload == 1 && currentCooldown == 0) {
                BulletEntity bulletEntity = getBulletEntity(user, hand, bullet, chamber);
                world.spawnEntity(bulletEntity);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.1f, 1.2f);
                chamber.decrement(1);
                stack.set(GUN_COOLDOWN_COMPONENT, cooldownTarget);
                stunLen = doRecoil(user);
                if (chamber.isEmpty()) {
                    stack.remove(GUN_AMMO_COMPONENT);
                } else {
                    stack.set(GUN_AMMO_COMPONENT, chamber);
                }
            } else if (currentReload > 1) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
            }
            updateDamage(stack);
        }
        return stunLen;
    }

    private @NotNull BulletEntity getBulletEntity(LivingEntity entity, Hand hand, BulletItem bullet, ItemStack chamber) {
        boolean isIncendiary = false;
        double explosionPower = explosionPowerGun;
        double repulsionPower = repulsionPowerGun;

        if (bullet != null) {
            isIncendiary = bullet.isIncendiary;
            explosionPower *= bullet.explosionPowerCoefficient;
            repulsionPower *= bullet.repulsionPowerCoefficient;
        }

        return new BulletEntity(entity, chamber, hand, this, 0.25f, damage, speed, explosionPower, repulsionPower, isIncendiary);
    }
}