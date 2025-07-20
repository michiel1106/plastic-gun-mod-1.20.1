package systems.brn.plasticgun.lib;

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import systems.brn.plasticgun.defence.WeaponArmor;

import java.util.*;

import static systems.brn.plasticgun.PlasticGun.*;

public class Util {

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static ItemStack findBulletStack(ArrayList<Item> bulletItem, ServerPlayerEntity player) {
        if (bulletItem == null || bulletItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (ItemStack itemStack : player.getInventory().getMainStacks()) {
            for (Item item : bulletItem) {
                if (item == itemStack.getItem()) {
                    return itemStack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static int canInsertItemIntoInventory(Inventory inventory, ItemStack itemStack) {
        // Get the player's inventory
        int maxInsert = 0;

        if (inventory instanceof PlayerInventory playerInventory) {
            // Iterate through the slots in the player's inventory
            for (int i = 0; i < playerInventory.getMainStacks().size(); i++) {
                ItemStack slotStack = playerInventory.getMainStacks().get(i);
                maxInsert = canInsertToStack(slotStack, itemStack, maxInsert);
            }
        } else {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack slotStack = inventory.getStack(i);
                maxInsert = canInsertToStack(slotStack, itemStack, maxInsert);
            }
        }

        return maxInsert; // Return the maximum insertion count
    }

    public static int canInsertToStack(ItemStack stack1, ItemStack stack2, int maxInsert) {
        if (stack1.isEmpty() || ItemStack.areItemsEqual(stack1, stack2)) {
            int remainingSpace = stack1.isEmpty() ? stack2.getMaxCount() : stack1.getMaxCount() - stack1.getCount();
            maxInsert += remainingSpace;
            // If the maximum insertion count is greater than or equal to the item count, return the item count
            if (maxInsert >= stack2.getCount()) {
                return stack2.getCount();
            }
        }
        return maxInsert;
    }

    public static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        return !stack1.isEmpty() && stack1.getItem() == stack2.getItem() && ItemStack.areItemsAndComponentsEqual(stack1, stack2);
    }

    public static void insertStackIntoInventory(Inventory inventory, ItemStack stack) {
        // First, try to merge with existing stacks
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (canCombine(slotStack, stack)) {
                int transferAmount = Math.min(stack.getCount(), slotStack.getMaxCount() - slotStack.getCount());
                if (transferAmount > 0) {
                    slotStack.increment(transferAmount);
                    stack.decrement(transferAmount);
                    inventory.markDirty();
                    if (stack.isEmpty()) {
                        return;
                    }
                }
            }
        }

        // Next, try to find an empty slot
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (slotStack.isEmpty()) {
                inventory.setStack(i, stack.copy());
                stack.setCount(0);
                inventory.markDirty();
                return;
            }
        }
    }

    public static List<Entity> getEntitiesAround(Entity entity, double radius) {
        Vec3d pos = entity.getPos();
        int minX = MathHelper.floor(pos.x - radius - 1.0);
        int maxX = MathHelper.floor(pos.x + radius + 1.0);
        int minY = MathHelper.floor(pos.y - radius - 1.0);
        int maxY = MathHelper.floor(pos.y + radius + 1.0);
        int minZ = MathHelper.floor(pos.z - radius - 1.0);
        int maxZ = MathHelper.floor(pos.z + radius + 1.0);
        Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        return entity.getWorld().getOtherEntities(entity, box);
    }

    public static void setProjectileData(List<DataTracker.SerializedEntry<?>> data, boolean initial, float scale, ItemStack itemStack) {
        if (initial) {
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 2));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.SCALE, new Vector3f(scale)));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.BILLBOARD, (byte) DisplayEntity.BillboardMode.CENTER.ordinal()));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM, itemStack));
        }
    }

    public static void hitDamage(Vec3d pos, double explosionPower, double repulsionPower, World worldTemp, @Nullable Entity entity, boolean isIncendiary, @Nullable ExplosionBehavior explosionBehavior) {
        if (worldTemp instanceof ServerWorld world) {
            if (explosionPower > 0) {
                world.createExplosion(entity, Explosion.createDamageSource(world, entity), explosionBehavior, pos.getX(), pos.getY(), pos.getZ(), (float) explosionPower, isIncendiary, ServerWorld.ExplosionSourceType.TNT);
            }
            if (repulsionPower > 0) {
                world.createExplosion(entity, null, new AdvancedExplosionBehavior(false, false, Optional.empty(), Optional.empty()), pos.getX(), pos.getY(), pos.getZ(), (float) repulsionPower, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_BREEZE_WIND_BURST);
            }
        }
    }

    public static double getFinalDamage(LivingEntity livingEntity, WeaponDamageType damageType, double damage) {
        for (WeaponArmor weaponArmor : weaponArmors) {
            if (weaponArmor.resistances.containsKey(damageType)) {

                ItemStack chestStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
                int currentDamage = chestStack.getDamage();
                int maxDamage = chestStack.getMaxDamage();
                double reducedDamage = 0;
                if (currentDamage < maxDamage) {
                    double coefficient = weaponArmor.resistances.get(damageType);
                    reducedDamage = (1 - coefficient) * damage;
                    damage *= coefficient;
                }

                int nextDamage = currentDamage + (int) reducedDamage;
                if (nextDamage >= maxDamage) {
                    chestStack.setCount(0);
                } else {
                    chestStack.setDamage(nextDamage);
                }
                livingEntity.equipStack(EquipmentSlot.CHEST, chestStack);
            }
        }
        return damage;
    }

    public static <T extends Item> Map<Item, T> generateItemMap(List<T> extendedItems) {
        Map<Item, T> itemMap = new HashMap<>();
        for (T item : extendedItems) {
            itemMap.put(item, item);
        }
        return itemMap;
    }

    public static void registerIntoClickEvents(Collection<? extends Item> items) {
        clickEventItems.addAll(items);
    }

    public static boolean shouldSendClickEvents(PlayerEntity player) {
        Item mainItem = player.getMainHandStack().getItem();
        Item offItem = player.getOffHandStack().getItem();
        return clickEventItems.contains(mainItem) || clickEventItems.contains(offItem);
    }

    public static int getDifficultyAdjustedChance(LocalDifficulty localDifficulty, World world) {
        Difficulty worldDifficulty = world.getDifficulty();

        // Calculate chance modifiers based on difficulties
        float localDifficultyFactor = localDifficulty.getLocalDifficulty();
        int worldDifficultyFactor = switch (worldDifficulty) {
            case PEACEFUL -> 0;
            case EASY -> 1;
            case NORMAL -> 2;
            case HARD -> 3;
        };

        // Determine the chance to equip a gun
        int baseChance = 20; // Base chance denominator
        return (int) (baseChance / (localDifficultyFactor * worldDifficultyFactor));
    }


    public static int selectWeaponIndex(Random random, LocalDifficulty localDifficulty, int size) {
        double difficultyFactor = localDifficulty.getClampedLocalDifficulty();
        double biasFactor = 1.0 - difficultyFactor; // Higher difficulty means lower index less likely

        // Bias towards lower indices
        double r = random.nextDouble() * biasFactor;
        int index = (int) (r * size * size);

        // Ensure index is within bounds
        return Math.min(index, size - 1);
    }

    public static void blockHitParticles(Vec3d pos, BlockState blockState, World worldTemp, double damage) {
        if (worldTemp instanceof ServerWorld world) {
            int particleCount = (int) damage * 4; // Number of particles
            double radius = 1;
            double heightOffset = 1;


            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);

                BlockStateParticleEffect blockStateParticleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState);
                world.spawnParticles(
                        blockStateParticleEffect,
                        pos.x,
                        pos.y,
                        pos.z,
                        1,  // Number of particles to spawn per location
                        xOffset,  // Offset in the x-direction
                        (heightOffset / particleCount) * i,  // Offset in the y-direction
                        zOffset,  // Offset in the z-direction
                        4   // Speed of particles
                );
            }
        }
    }

    public static void entityHitParticles(LivingEntity livingEntity, double damage) {
        if (livingEntity.getWorld() instanceof ServerWorld world) {
            if (livingEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) livingEntity;
                if (serverPlayerEntity.isCreative()) {
                    return;
                }
            }
            Vec3d pos = livingEntity.getPos();
            int particleCount = (int) damage * 4; // Number of particles
            double radius = livingEntity.getWidth() / 2 + 0.5;    // Radius of the circle
            double heightOffset = livingEntity.getHeight(); // Height offset from the entity's position


            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;
                double xOffset = radius * Math.cos(angle);
                double zOffset = radius * Math.sin(angle);

                BlockStateParticleEffect blockStateParticleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState());
                world.spawnParticles(
                        blockStateParticleEffect,
                        pos.x,
                        pos.y,
                        pos.z,
                        1,  // Number of particles to spawn per location
                        xOffset,  // Offset in the x-direction
                        (heightOffset / particleCount) * i,  // Offset in the y-direction
                        zOffset,  // Offset in the z-direction
                        4   // Speed of particles
                );
            }
        }
    }

    public static void addItemToLootTable(RegistryKey<LootTable> tableId, Item item, Integer weight) {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, wrapperLookup) -> {
            if (source.isBuiltin() && tableId.equals(key)) {
                tableBuilder.modifyPools(poolBuilder -> poolBuilder.with(ItemEntry.builder(item).weight(weight)));
            }
        });
    }

    public static int getAfterWeight(float coeff, int weight) {
        return (int) Math.ceil(coeff * weight);
    }
}