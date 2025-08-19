package systems.brn.plasticgun.grenades;

import eu.pb4.polymer.core.api.item.PolymerItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import systems.brn.plasticgun.grenades.GrenadeEntity;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.List;

import static systems.brn.plasticgun.lib.GunComponents.*;
import static systems.brn.plasticgun.lib.Util.*;

public class GrenadeItem extends SimpleItem implements PolymerItem {

    public final int speed;
    public final double explosionPower;
    public final double repulsionPower;
    public final int explosionTarget;
    public final boolean isIncendiary;
    public final boolean isFragmentation;
    public final int flashBangDuration;
    public final int stunDuration;
    public final int smokeTicks;
    public final int smokeRadius;
    public final int smokeCount;


    public GrenadeItem(String path,
                       int speed,
                       double explosionPower,
                       double repulsionPower,
                       int explosionTarget,
                       boolean isIncendiary,
                       boolean isFragmentation,
                       int flashBangDuration,
                       int stunDuration,
                       int smokeTicks,
                       int effectRadius,
                       int smokeCount) {
        super(
                new Item.Settings()
                        .maxCount(16)
                        .maxDamage(explosionTarget + 1), // grenades "fuse timer" as durability
                id(path),
                Items.STICK
        );
        this.explosionTarget = explosionTarget;
        this.isIncendiary = isIncendiary;
        Registry.register(Registries.ITEM, id(path), this);
        this.speed = speed;
        this.explosionPower = explosionPower;
        this.repulsionPower = repulsionPower;
        this.flashBangDuration = flashBangDuration;
        this.stunDuration = stunDuration;
        this.smokeTicks = smokeTicks;
        this.smokeRadius = effectRadius;
        this.smokeCount = smokeCount;
        this.isFragmentation = isFragmentation;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        tooltip.add(Text.translatable("gun.description.explosion_power", explosionPower));
        tooltip.add(Text.translatable("gun.description.repulsion_power", repulsionPower));
        tooltip.add(Text.translatable("gun.description.explosion_time", explosionTarget));
        tooltip.add(Text.translatable("gun.description.speed", speed));
        tooltip.add(Text.translatable(isIncendiary ? "gun.description.incendiary_yes" : "gun.description.incendiary_no"));
        tooltip.add(Text.translatable("gun.description.fragmentation_grenade", isFragmentation));
        tooltip.add(Text.translatable("gun.description.flashbang_duration", flashBangDuration));
        tooltip.add(Text.translatable("gun.description.stun_duration", stunDuration));
        tooltip.add(Text.translatable("gun.description.smoke_ticks", smokeTicks));
        tooltip.add(Text.translatable("gun.description.effect_radius", smokeRadius));
        tooltip.add(Text.translatable("gun.description.particle_count", smokeCount));
    }

    public void unpin(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            var nbt = stack.getOrCreateNbt();

            int timer = nbt.contains("grenade_timer") ? nbt.getInt("grenade_timer") : -1;
            if (timer == -1) {
                nbt.putInt("grenade_timer", explosionTarget);

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS,
                        1f, 2.5f);

                updateDamage(stack, this);
            }
        }
    }

    public static void updateDamage(ItemStack stack, GrenadeItem grenadeItem) {
        var nbt = stack.getOrCreateNbt();

        int timer = nbt.contains("grenade_timer") ? nbt.getInt("grenade_timer") : -1;
        if (timer < 0) {
            timer = grenadeItem.explosionTarget + 1;
        }

        // durability bar reflects fuse progress
        stack.setDamage((1 + grenadeItem.explosionTarget) - timer);
    }


    public void chuck(ServerWorld world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            var nbt = stack.getOrCreateNbt();

            int timer = nbt.contains("grenade_timer") ? nbt.getInt("grenade_timer") : -1;
            if (timer > 0) {
                // spawn grenade entity with timer
                turnIntoEntity(player, stack.copy(), speed, timer);

                if (!player.isCreative()) {
                    stack.decrement(1);
                }

                if (!stack.isEmpty()) {
                    stack.setDamage(0); // reset durability bar
                    user.setStackInHand(hand, stack);
                }
            }
        }
    }

    public void turnIntoEntity(ServerPlayerEntity player, @Nullable ItemStack stack, int speed, int timer) {
        GrenadeEntity grenadeEntity = new GrenadeEntity(player, stack, timer, 0.5f, speed, explosionPower, repulsionPower, isIncendiary, isFragmentation, flashBangDuration, stunDuration, smokeTicks, 8, smokeCount);
        player.getWorld().spawnEntity(grenadeEntity);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
    }

    public void turnIntoEntity(Entity entity, @Nullable ItemStack stack, int speed, int timer) {
        GrenadeEntity grenadeEntity = new GrenadeEntity(entity.getWorld(), entity.getPos(), stack, timer, 1f, explosionPower, repulsionPower, isIncendiary, isFragmentation, flashBangDuration, stunDuration, smokeTicks, 8, smokeCount);
        entity.getWorld().spawnEntity(grenadeEntity);
        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
    }

    public void checkExplosions(ServerWorld world, PlayerEntity playerEntity, ItemStack stackInSlot) {
        if (playerEntity instanceof ServerPlayerEntity player && !world.isClient()) {
            var nbt = stackInSlot.getOrCreateNbt();
            int timer = nbt.contains("grenade_timer") ? nbt.getInt("grenade_timer") : -1;

            if (timer == 0) {
                turnIntoEntity(player, getDefaultStack(), 0, 0);

                if (!player.isCreative()) {
                    stackInSlot.decrement(1);
                } else {
                    nbt.putInt("grenade_timer", -1); // reset grenade fuse
                    stackInSlot.setDamage(0);        // reset durability bar
                }
            }
        }
    }




}
