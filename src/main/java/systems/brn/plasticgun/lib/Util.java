package systems.brn.plasticgun.lib;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import systems.brn.plasticgun.PlasticGun;
import systems.brn.plasticgun.defence.WeaponArmor;

import java.util.*;

import static net.minecraft.world.explosion.Explosion.getExposure;
import static systems.brn.plasticgun.PlasticGun.MOD_ID;
import static systems.brn.plasticgun.PlasticGun.weaponArmors;

public class Util {

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static ItemStack findBulletStack(ArrayList<Item> bulletItem, ServerPlayerEntity player) {
        if (bulletItem == null || bulletItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (ItemStack itemStack : player.getInventory().main) {
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
            for (int i = 0; i < playerInventory.main.size(); i++) {
                ItemStack slotStack = playerInventory.main.get(i);
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
        return entity.getEntityWorld().getOtherEntities(entity, box);
    }

    public static void applyKnockbackToEntities(Entity explodingEntity, Vec3d explosionPos, double power, double radius) {
        List<Entity> entities = getEntitiesAround(explodingEntity, radius);

        for (Entity entity : entities) {
            double distanceRatio = Math.sqrt(entity.squaredDistanceTo(explosionPos)) / power;
            if (distanceRatio > 1.0) {
                continue;
            }

            double dx = entity.getX() - explosionPos.x;
            double dy = entity.getY() - explosionPos.y;
            double dz = entity.getZ() - explosionPos.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance == 0.0) {
                continue;
            }

            dx /= distance;
            dy /= distance;
            dz /= distance;

            double knockbackStrength = (1.0 - distanceRatio) * getExposure(explosionPos, entity);
            double knockback = knockbackStrength * (1.0 - (entity instanceof LivingEntity livingEntity ? livingEntity.getAttributeValue(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE) : 0.0));

            Vec3d knockbackVec = new Vec3d(dx * knockback, dy * knockback, dz * knockback);
            entity.setVelocity(entity.getVelocity().add(knockbackVec));
        }
    }

    public static void setProjectileData(List<DataTracker.SerializedEntry<?>> data, boolean initial, float scale, ItemStack itemStack) {
        if (initial) {
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 2));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.SCALE, new Vector3f(scale)));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.BILLBOARD, (byte) DisplayEntity.BillboardMode.CENTER.ordinal()));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM, itemStack));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM_DISPLAY, ModelTransformationMode.FIXED.getIndex()));
        }
    }

    public static void hitDamage(Vec3d pos, double explosionPower, double repulsionPower, World worldTemp, @Nullable Entity entity, boolean isIncendiary, int radius, @Nullable ExplosionBehavior explosionBehavior) {
        if (worldTemp instanceof ServerWorld world) {
            if (explosionPower > 0) {
                world.createExplosion(entity, Explosion.createDamageSource(world, entity), explosionBehavior, pos.getX(), pos.getY(), pos.getZ(), (float) explosionPower, isIncendiary, ServerWorld.ExplosionSourceType.TNT);
            }
            if (repulsionPower > 0) {
                applyKnockbackToEntities(entity, pos, repulsionPower * 100, radius);
            }
        }
    }

    public static double getFinalDamage(LivingEntity livingEntity, WeaponDamageType damageType, double damage) {
        Optional<TrinketComponent> trinketComponentTemp = TrinketsApi.getTrinketComponent(livingEntity);
        if (trinketComponentTemp.isPresent()) {
            TrinketComponent trinketComponent = trinketComponentTemp.get();
            for (WeaponArmor weaponArmor : weaponArmors) {
                if (weaponArmor.resistances.containsKey(damageType)) {

                    List<Pair<SlotReference, ItemStack>> vestsComponents = trinketComponent.getEquipped(weaponArmor);
                    if (!vestsComponents.isEmpty()) {
                        Pair<SlotReference, ItemStack> vestComponent = vestsComponents.getFirst();
                        TrinketInventory trinketInventory = vestComponent.getLeft().inventory();
                        int currentDamage = vestComponent.getRight().getDamage();
                        int maxDamage = vestComponent.getRight().getMaxDamage();
                        double reducedDamage = 0;
                        if (currentDamage < maxDamage) {
                            double coefficient = weaponArmor.resistances.get(damageType);
                            reducedDamage = (1 - coefficient) * damage;
                            damage *= coefficient;
                        }

                        int nextDamage = currentDamage + (int) reducedDamage;
                        int inventoryIndex = vestComponent.getLeft().index();
                        ItemStack vestStack = trinketInventory.getStack(inventoryIndex);
                        if (nextDamage >= maxDamage) {
                            vestStack.setCount(0);
                        } else {
                            vestStack.setDamage(nextDamage);
                        }
                        trinketInventory.setStack(inventoryIndex, vestStack);
                    }
                }
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

    public static int getDifficultyAdjustedChance(LocalDifficulty localDifficulty, World world) {
        Difficulty worldDifficulty = world.getDifficulty();

        // Calculate chance modifiers based on difficulties
        float localDifficultyFactor = localDifficulty.getLocalDifficulty();
        int worldDifficultyFactor = switch (worldDifficulty) {
            case PEACEFUL -> 0;
            case EASY -> 1;
            case NORMAL -> 2;
            case HARD -> 3;
            default -> 1;
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
}
