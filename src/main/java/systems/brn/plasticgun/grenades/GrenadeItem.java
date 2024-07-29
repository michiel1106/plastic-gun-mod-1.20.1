package systems.brn.plasticgun.grenades;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
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


    public GrenadeItem(String path, int speed, double explosionPower, double repulsionPower, int explosionTarget, boolean isIncendiary, boolean isFragmentation, int flashBangDuration, int stunDuration, int smokeTicks, int effectRadius, int smokeCount) {
        super(
                new Settings()
                        .maxCount(16)
                        .component(GRENADE_TIMER_COMPONENT, -1)
                        .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                Text.translatable("gun.description.explosion_power", explosionPower),
                                Text.translatable("gun.description.repulsion_power", repulsionPower),
                                Text.translatable("gun.description.explosion_time", explosionTarget),
                                Text.translatable("gun.description.speed", speed),
                                Text.translatable(isIncendiary ? "gun.description.incendiary_yes" :  "gun.description.incendiary_no"),
                                Text.translatable("gun.description.fragmentation_grenade", isFragmentation),
                                Text.translatable("gun.description.flashbang_duration", flashBangDuration),
                                Text.translatable("gun.description.stun_duration", stunDuration),
                                Text.translatable("gun.description.smoke_ticks", smokeTicks),
                                Text.translatable("gun.description.effect_radius", effectRadius),
                                Text.translatable("gun.description.particle_count", smokeCount)
                        )))
                        .maxDamage(explosionTarget + 1)
                , id(path), Items.STICK
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

    public void unpin(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            int timer = stack.getOrDefault(GRENADE_TIMER_COMPONENT, -1);
            if (timer == -1) {
                stack.set(GRENADE_TIMER_COMPONENT, explosionTarget);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1f, 2.5f);
                updateDamage(stack, this);
            }
        }
    }

    public static void updateDamage(ItemStack stack, GrenadeItem grenadeItem) {
        int timer = stack.getOrDefault(GRENADE_TIMER_COMPONENT, -1);
        if (timer < 0) {
            timer = grenadeItem.explosionTarget + 1;
        }
        stack.setDamage((1 + grenadeItem.explosionTarget) - timer);
    }


    public void chuck(ServerWorld world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            int timer = stack.getOrDefault(GRENADE_TIMER_COMPONENT, -1);
            if (timer > 0) {
                turnIntoEntity(player, stack.copy(), speed, timer);
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                    if (!stack.isEmpty()) {
                    stack.setDamage(0);
                }
            }
        }
    }

    public void turnIntoEntity(ServerPlayerEntity player, @Nullable ItemStack stack, int speed, int timer) {
        GrenadeEntity grenadeEntity = new GrenadeEntity(player, stack, timer, 0.5f, speed, explosionPower, repulsionPower, isIncendiary, isFragmentation, flashBangDuration, stunDuration, smokeTicks, 8, smokeCount);
        player.getServerWorld().spawnEntity(grenadeEntity);
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
    }

    public void turnIntoEntity(Entity entity, @Nullable ItemStack stack, int speed, int timer) {
        GrenadeEntity grenadeEntity = new GrenadeEntity(entity.getEntityWorld(), entity.getPos(), stack, timer, 1f, explosionPower, repulsionPower, isIncendiary, isFragmentation, flashBangDuration, stunDuration, smokeTicks, 8, smokeCount);
        entity.getEntityWorld().spawnEntity(grenadeEntity);
        entity.getEntityWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
    }

    public void checkExplosions(ServerWorld world, PlayerEntity playerEntity, ItemStack stackInSlot) {
        if (playerEntity instanceof ServerPlayerEntity player && !world.isClient()) {
            int timer = stackInSlot.getOrDefault(GRENADE_TIMER_COMPONENT, -1);
            if (timer == 0) {
                turnIntoEntity(player, getDefaultStack(), 0, 0);
                if (!player.isCreative()) {
                    stackInSlot.decrement(1);
                } else {
                    stackInSlot.set(GRENADE_TIMER_COMPONENT, -1);
                    stackInSlot.setDamage(0);
                }
            }
        }

    }
}
