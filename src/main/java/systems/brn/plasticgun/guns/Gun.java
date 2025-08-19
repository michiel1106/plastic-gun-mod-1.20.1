package systems.brn.plasticgun.guns;

import eu.pb4.polymer.core.api.item.PolymerItem;
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


        ItemStack stack = new ItemStack(this);
        NbtCompound nbt = stack.getOrCreateNbt();

        nbt.put("GunAmmo", ItemStack.EMPTY.writeNbt(new NbtCompound())); // if you want to store an ItemStack
        nbt.putInt("GunCooldown", 0);
        nbt.putInt("GunReloadCooldown", 0);

        NbtCompound display = nbt.getCompound("display");
        NbtList loreList = new NbtList();
        loreList.add(NbtString.of(Text.translatable("gun.description.caliber", caliber).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.damage_absolute", damage).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.speed", speed).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.clip_size", clipSize).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.reload_cooldown", reloadTarget).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.reload_cycles", reloadCount).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.shoot_cooldown", cooldownTarget).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.explosion_power", explosionPowerGun).getString()));
        loreList.add(NbtString.of(Text.translatable("gun.description.repulsion_power", repulsionPowerGun).getString()));
        display.put("Lore", loreList);
        nbt.put("display", display);

        stack.setNbt(nbt);


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

    public void reload(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            int currentReloadCooldown = stack.getOrCreateNbt().getInt("gun_reload_cooldown");
            if (currentReloadCooldown == 0) {
                NbtCompound orCreateNbt1 = stack.getOrCreateNbt();
                orCreateNbt1.putInt("gun_reload_cooldown", reloadTarget);
                stack.setNbt(orCreateNbt1);

                ItemStack bulletStack = findBulletStack(ammo, player);
                ItemStack chamber = ItemStack.EMPTY;

                if (stack.hasNbt()) {
                    NbtCompound nbt = stack.getNbt();
                    if (nbt.contains("gun_ammo")) {
                        NbtCompound ammoNbt = nbt.getCompound("gun_ammo");
                        chamber = ItemStack.fromNbt(ammoNbt); // converts NBT back to ItemStack
                    }
                }

// Optional: make a copy if needed
                chamber = chamber.copy();


                int bulletsInChamber = chamber.getCount();
                int currentReload = stack.getOrCreateNbt().getInt("gun_load");

                if (bulletStack != null && !bulletStack.isEmpty()) { //we have ammo
                    if (currentReload < reloadCount) { //still reloading

                        NbtCompound orCreateNbt = stack.getOrCreateNbt();
                        orCreateNbt.putInt("gun_load", currentReload + 1);

                        stack.setNbt(orCreateNbt);

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
                                if (stack.hasNbt()) {
                                    if (stack.getNbt() != null) {
                                        stack.getNbt().remove("gun_ammo");
                                    }
                                }
                            } else {

                                NbtCompound nbt = stack.getOrCreateNbt();
                                nbt.put("gun_ammo", chamber.writeNbt(new NbtCompound()));
                                nbt.put("gun_last_load", chamber.writeNbt(new NbtCompound()));

                                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.0f);
                            }
                            stack.getOrCreateNbt().putInt("gun_load", 1);


                        } else {
                            if (canInsertItemIntoInventory(player.getInventory(), chamber.copy()) == chamber.getCount()) { //can take out chamber
                                insertStackIntoInventory(player.getInventory(), chamber.copy());
                                chamber.setCount(0); //empty
                                int targetCount = Math.min(bulletStack.getCount(), clipSize);
                                chamber = bulletStack.copy();
                                chamber.setCount(targetCount);
                                if (chamber.isEmpty()) {

                                    if (stack.getNbt() != null) {
                                        stack.getNbt().remove("gun_ammo");
                                    }

                                } else {
                                    NbtCompound nbt = stack.getOrCreateNbt();
                                    nbt.put("gun_ammo", chamber.writeNbt(new NbtCompound()));
                                    nbt.put("gun_last_load", chamber.writeNbt(new NbtCompound()));



                                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 2.5f);
                                }
                                bulletStack.decrement(targetCount);

                                NbtCompound nbtCompound = stack.getOrCreateNbt();
                                nbtCompound.putInt("gun_load", 1);
                                stack.setNbt(nbtCompound);


                            }
                        }
                    }
                }
                if (player.isCreative()) {
                    ItemStack stackOfBullet = new ItemStack(ammo.stream().findFirst().get(), clipSize);


                    NbtCompound nbt = stack.getOrCreateNbt();
                    nbt.put("gun_ammo", stackOfBullet.writeNbt(new NbtCompound()));
                    nbt.put("gun_last_load", stackOfBullet.writeNbt(new NbtCompound()));
                }
                updateDamage(stack);
            }
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
        if (nbt != null && nbt.contains("gun_ammo")) {
            NbtCompound ammoNbt = nbt.getCompound("gun_ammo");
            chamber = ItemStack.fromNbt(ammoNbt); // converts NBT back to ItemStack
        }

        BulletItem bulletItem = null;
        for (BulletItem bulletTemp : bullets) {
            if (bulletTemp == chamber.getItem()) {
                bulletItem = bulletTemp;
                break;
            }
        }
        int numBullets = chamber.getCount();

        int currentReload = stack.getOrCreateNbt().getInt("gun_load");


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



        setLore(stack, loreList);

    }

    public static void setLore(ItemStack stack, List<Text> lines) {
        NbtCompound display = stack.getOrCreateSubNbt("display");

        NbtList loreList = new NbtList();
        for (Text line : lines) {
            // Lore must be JSON text
            loreList.add(NbtString.of("{\"text\":\"" + line + "\"}"));
        }

        display.put("Lore", loreList);
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

            int currentReload = stack.getOrCreateNbt().getInt("gun_load");
            int currentCooldown = stack.getOrCreateNbt().getInt("gun_cooldown");


            ItemStack chamber = ItemStack.EMPTY;


            NbtCompound nbt1 = stack.getNbt();
            if (nbt1.contains("gun_ammo")) {
                NbtCompound ammoNbt1 = nbt1.getCompound("gun_ammo");
                chamber = ItemStack.fromNbt(ammoNbt1); // converts NBT back to ItemStack
            }


            BulletItem bullet = null;
            if (itemBulletItemMap.containsKey(chamber.getItem())) {
                bullet = itemBulletItemMap.get(chamber.getItem());
            }

            if (!chamber.isEmpty() && currentReload == 1 && currentCooldown == 0) {
                BulletEntity bulletEntity = getBulletEntity(user, hand, bullet, chamber);
                world.spawnEntity(bulletEntity);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.1f, 1.2f);
                chamber.decrement(1);

                NbtCompound orCreateNbt = stack.getOrCreateNbt();
                orCreateNbt.putInt("gun_cooldown", cooldownTarget);

                stack.setNbt(orCreateNbt);

                stunLen = doRecoil(user);
                if (chamber.isEmpty()) {

                    NbtCompound orCreateNbt1 = stack.getOrCreateNbt();
                    if (orCreateNbt1.contains("gun_ammo")) {
                        orCreateNbt1.remove("gun_ammo");
                        stack.setNbt(orCreateNbt1);

                    }
                } else {

                    NbtCompound nbt12 = stack.getNbt();
                    if (nbt12.contains("gun_ammo")) {
                        NbtCompound ammoNbt1 = nbt12.getCompound("gun_ammo");
                        chamber = ItemStack.fromNbt(ammoNbt1); // converts NBT back to ItemStack
                        stack.setNbt(ammoNbt1);
                    }


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