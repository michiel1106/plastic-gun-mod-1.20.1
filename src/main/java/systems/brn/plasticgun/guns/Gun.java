package systems.brn.plasticgun.guns;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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
import org.jetbrains.annotations.Nullable;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static systems.brn.plasticgun.PlasticGun.*;
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
        super(new Settings().maxCount(1).maxDamage(clipSize + 1), id(path), Items.WOODEN_SWORD);


        ItemStack stack = super.getDefaultStack();
        NbtCompound nbt = stack.getOrCreateNbt();

        // Initialize with empty ammo
        nbt.put("gun_ammo", ItemStack.EMPTY.writeNbt(new NbtCompound()));
        nbt.putInt("gun_cooldown", 0);
        nbt.putInt("gun_reload_cooldown", 0);
        nbt.putInt("gun_load", 1); // Start ready to fire


        this.verticalRecoilMin = verticalRecoilMin / 100f;
        this.verticalRecoilMax = verticalRecoilMax / 100f;
        this.velocityRecoilMin = velocityRecoilMin;
        this.velocityRecoilMax = velocityRecoilMax;
        this.horizontalRecoilMin = horizontalRecoilMin / 100f;
        this.horizontalRecoilMax = horizontalRecoilMax / 100f;
        if (verticalRecoilMin > verticalRecoilMax) {
            logger.error("verticalRecoilMin > verticalRecoilMax for {}", path);
        }
        if (horizontalRecoilMin > horizontalRecoilMax) {
            logger.error("horizontalRecoilMin > horizontalRecoilMax for {}", path);
        }
        if (velocityRecoilMin > velocityRecoilMax) {
            logger.error("velocityRecoilMin > velocityRecoilMax for {}", path);
        }
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


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> loreList, TooltipContext context) {
        super.appendTooltip(stack, world, loreList, context);

        int bulletsLeft = 0;
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("gun_ammo")) {
            bulletsLeft = ItemStack.fromNbt(nbt.getCompound("gun_ammo")).getCount();
        }

        loreList.add(Text.translatable("gun.description.caliber", caliber));
        loreList.add(Text.translatable("gun.description.bullets_left", bulletsLeft, clipSize));
        loreList.add(Text.translatable("gun.description.damage_absolute", damage));
        loreList.add(Text.translatable("gun.description.speed", speed));
        loreList.add(Text.translatable("gun.description.clip_size", clipSize));
        loreList.add(Text.translatable("gun.description.reload_cooldown", reloadTarget));
        loreList.add(Text.translatable("gun.description.reload_cycles", reloadCount));
        loreList.add(Text.translatable("gun.description.shoot_cooldown", cooldownTarget));
        loreList.add(Text.translatable("gun.description.explosion_power", explosionPowerGun));
        loreList.add(Text.translatable("gun.description.repulsion_power", repulsionPowerGun));


    }


    public void reload(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            NbtCompound nbt = stack.getOrCreateNbt();


            int currentReloadCooldown = nbt.getInt("gun_reload_cooldown");
            if (currentReloadCooldown > 0) return; // Still on cooldown

            // Set reload cooldown
            nbt.putInt("gun_reload_cooldown", reloadTarget);

            // Handle creative mode separately
            if (player.isCreative()) {
                ItemStack creativeAmmo = new ItemStack(ammo.get(0), clipSize);
                nbt.put("gun_ammo", creativeAmmo.writeNbt(new NbtCompound()));
                nbt.put("gun_last_load", creativeAmmo.writeNbt(new NbtCompound()));
                nbt.putInt("gun_load", 1); // Set to ready state
                updateDamage(stack);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.0f);
                return;
            }

            // Get current chamber contents
            ItemStack chamber = ItemStack.EMPTY;
            if (nbt.contains("gun_ammo")) {
                chamber = ItemStack.fromNbt(nbt.getCompound("gun_ammo"));
            }

            int bulletsInChamber = chamber.getCount();
            int currentReload = nbt.getInt("gun_load");

            // Find compatible ammo in player's inventory
            ItemStack bulletStack = findBulletStack(ammo, player);

            if (bulletStack != null && !bulletStack.isEmpty()) {
                if (currentReload < reloadCount) {
                    // Still reloading - increment counter
                    nbt.putInt("gun_load", currentReload + 1);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, 0.1f, 1.8f);
                } else if (currentReload >= reloadCount) {
                    // Complete the reload
                    int addedBullets = Math.min(bulletStack.getCount(), clipSize - bulletsInChamber);

                    if (addedBullets > 0) {
                        if (chamber.isEmpty() || chamber.getItem() == bulletStack.getItem()) {
                            // Same ammo type or empty chamber
                            if (chamber.isEmpty()) {
                                chamber = new ItemStack(bulletStack.getItem(), 0);
                            }
                            chamber.setCount(bulletsInChamber + addedBullets);
                            bulletStack.decrement(addedBullets);

                            nbt.put("gun_ammo", chamber.writeNbt(new NbtCompound()));
                            nbt.put("gun_last_load", chamber.writeNbt(new NbtCompound()));
                            nbt.putInt("gun_load", 1); // Reset to ready state

                            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.0f);
                        } else {
                            // Different ammo type - need to swap
                            if (player.getInventory().insertStack(chamber.copy())) {
                                chamber = new ItemStack(bulletStack.getItem(), 0);
                                int targetCount = Math.min(bulletStack.getCount(), clipSize);
                                chamber.setCount(targetCount);
                                bulletStack.decrement(targetCount);

                                nbt.put("gun_ammo", chamber.writeNbt(new NbtCompound()));
                                nbt.put("gun_last_load", chamber.writeNbt(new NbtCompound()));
                                nbt.putInt("gun_load", 1); // Reset to ready state

                                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 2.5f);
                            } else {
                                // Couldn't insert old ammo, cancel reload
                                System.out.println("1");
                                nbt.putInt("gun_load", 1);
                                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 0.5f);
                            }
                        }
                    } else {
                        // No bullets to add
                        nbt.putInt("gun_load", 1);
                        System.out.println("2");
                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 0.5f);
                    }
                }
            } else {
                // No ammo found
                nbt.putInt("gun_load", 1); // Reset to ready state to prevent getting stuck
                System.out.println("3");
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 0.5f);
            }

            updateDamage(stack);
        }
    }



    public void reload(World world, MobEntity mobEntity, Hand hand, Random random, LocalDifficulty localDifficulty) {
        if (!world.isClient()) {
            ItemStack stack = mobEntity.getStackInHand(hand);
            int currentReloadCooldown = stack.getOrCreateNbt().getInt("gun_reload_cooldown");

            ItemStack chamber = ItemStack.EMPTY;


            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("gun_ammo")) {
                NbtCompound ammoNbt = nbt.getCompound("gun_ammo");
                chamber = ItemStack.fromNbt(ammoNbt); // converts NBT back to ItemStack
            }


            int bulletsInChamber = chamber.getCount();

            int currentReload = stack.getOrCreateNbt().getInt("gun_load");


            if (currentReload == 1 && bulletsInChamber > 0) {
                return;
            }
            if (currentReloadCooldown == 0) {

                NbtCompound nbtCompound = stack.getOrCreateNbt();
                nbtCompound.putInt("gun_reload_cooldown", reloadTarget);
                stack.setNbt(nbtCompound);


                ItemStack oldChamber = ItemStack.EMPTY;


                NbtCompound nbt1 = stack.getNbt();
                if (nbt1.contains("gun_last_load")) {
                    NbtCompound ammoNbt1 = nbt1.getCompound("gun_last_load");
                    oldChamber = ItemStack.fromNbt(ammoNbt1); // converts NBT back to ItemStack
                }




                Item ammoItem = (oldChamber == null || oldChamber.isEmpty())
                        ? ammo.get(selectWeaponIndex(random, localDifficulty, ammo.size()))
                        : oldChamber.getItem();
                ItemStack bulletStack = new ItemStack(ammoItem, world.getRandom().nextBetween(1, ammoItem.getMaxCount()));
                if (!bulletStack.isEmpty()) { // we have ammo
                    if (currentReload < reloadCount) { // still reloading

                        NbtCompound orCreateNbt = stack.getOrCreateNbt();
                        orCreateNbt.putInt("gun_load", currentReload + 1);
                        stack.setNbt(orCreateNbt);


                    } else if (currentReload == reloadCount) { // now reload


                        NbtCompound nbt2 = stack.getOrCreateNbt();
                        nbt.put("gun_ammo", bulletStack.writeNbt(new NbtCompound()));
                        nbt.put("gun_last_load", bulletStack.writeNbt(new NbtCompound()));
                        stack.setNbt(nbt2);

                        NbtCompound orCreateNbt = stack.getOrCreateNbt();
                        orCreateNbt.putInt("gun_load", currentReload + 1);
                        stack.setNbt(orCreateNbt);
                    }
                }
            }
        }
    }
    public void updateDamage(ItemStack stack) {
        ItemStack chamber = ItemStack.EMPTY;
        NbtCompound nbt = stack.getNbt();

        if (nbt != null) {

            if (nbt.contains("gun_ammo")) {
                chamber = ItemStack.fromNbt(nbt.getCompound("gun_ammo"));
            }
        }

        int numBullets = chamber.getCount();
        int currentReload = nbt != null ? nbt.getInt("gun_load") : 1;

        // If still reloading, indicate gun is "empty"
        if (currentReload != 1) {
            numBullets = 0;
        }

        // Update the damage bar to reflect bullets left in the chamber
        stack.setDamage(clipSize - numBullets);
    }


    public int doRecoil(LivingEntity entity) {
       // if (entity.getWorld() instanceof ServerWorld serverWorld) {
       //     Random rng = entity.getWorld().getRandom();
       //     // Get the entity's current position and yaw
       //     Vec3d pos = entity.getPos();
       //     float yaw = entity.getYaw();
       //     float newPitch = entity.getPitch();
       //     Vec3d currentLook = entity.getRotationVector().multiply(-1);
       //     float yawChange = verticalRecoilMin + rng.nextFloat() * (verticalRecoilMax - verticalRecoilMin);
       //     float pitchChange = (float) (horizontalRecoilMin + rng.nextFloat() * (horizontalRecoilMax - horizontalRecoilMin));
       //     newPitch -= yawChange;
       //     yaw -= pitchChange;
       //     entity.teleport(serverWorld, pos.x, pos.y, pos.z, PositionFlag.ROT, yaw, newPitch, true);
       //     double velocityRecoil = rng.nextDouble() * (velocityRecoilMax - velocityRecoilMin);
       //     if (velocityRecoil > 0) {
       //         entity.setVelocity(currentLook.multiply(velocityRecoil));
       //     }
       //     return (int) ((abs(yawChange) + abs(pitchChange) + abs(velocityRecoil) + max(abs(yawChange), max(abs(pitchChange), abs(velocityRecoil)))) / 4f) * 40;
       // }
        return 0;
    }

    public int shoot(ServerWorld world, LivingEntity user, Hand hand) {
        int stunLen = 0;
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            NbtCompound nbt = stack.getOrCreateNbt();


            System.out.println("=== SHOOT DEBUG ===");
            System.out.println("All NBT keys: " + nbt.getKeys());


            int currentReload = nbt.getInt("gun_load");
            int currentCooldown = nbt.getInt("gun_cooldown");

            // Only shoot if ready (load state 1) and not on cooldown
            System.out.println(currentReload + "f");
            System.out.println(currentCooldown + " d");

            if (currentReload != 1 && currentCooldown > 0) {
                System.out.println("4");
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
                return 0;
            }

            ItemStack chamber = ItemStack.EMPTY;
            if (nbt.contains("gun_ammo")) {
                chamber = ItemStack.fromNbt(nbt.getCompound("gun_ammo"));
            }

            if (chamber.isEmpty() || chamber.getCount() == 0) {
                System.out.println("5");
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
                return 0;
            }

            BulletItem bullet = itemBulletItemMap.get(chamber.getItem());
            if (bullet == null) return 0;

            BulletEntity bulletEntity = getBulletEntity(user, hand, bullet, chamber);
            world.spawnEntity(bulletEntity);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.1f, 1.2f);

            // Decrement ammo
            chamber.decrement(1);
            nbt.putInt("gun_cooldown", cooldownTarget);

            if (chamber.getCount() <= 0) {
                nbt.remove("gun_ammo");
                // Start reload process automatically when empty
                nbt.putInt("gun_load", 2);
            } else {
                nbt.put("gun_ammo", chamber.writeNbt(new NbtCompound()));
            }

            stunLen = doRecoil(user);
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