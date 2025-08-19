package systems.brn.plasticgun.guns;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.util.EnumSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import static systems.brn.plasticgun.PlasticGun.guns;
import static systems.brn.plasticgun.PlasticGun.itemGunMap;
import static systems.brn.plasticgun.lib.GunComponents.*;

public class WeaponShootGoal<T extends HostileEntity & RangedAttackMob> extends Goal {
    private final T actor;
    private final double speed;
    private final float squaredRange;
    private int targetSeeingTicker;
    private boolean movingToLeft;
    private boolean backward;
    private int combatTicks = -1;
    private int lockedTicks = 0;

    public WeaponShootGoal(T actor, double speed, float range) {
        this.actor = actor;
        this.speed = speed;
        this.squaredRange = range * range;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public boolean canStart() {
        return this.actor.getTarget() != null && this.isHoldingGun();
    }

    protected boolean isHoldingGun() {
        ItemStack mainHandStack = this.actor.getMainHandStack();
        ItemStack offhandHandStack = this.actor.getOffHandStack();
        return itemGunMap.containsKey(mainHandStack.getItem()) || itemGunMap.containsKey(offhandHandStack.getItem());
    }

    public boolean shouldContinue() {
        return (this.canStart() || !this.actor.getNavigation().isIdle()) && this.isHoldingGun();
    }

    public void start() {
        super.start();
        this.actor.setAttacking(true);
    }

    public void stop() {
        super.stop();
        this.actor.setAttacking(false);
        this.targetSeeingTicker = 0;
        this.actor.clearActiveItem();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public boolean isLookingAtEntity(Entity targetEntity, float yawThreshold, float pitchThreshold) {
        double deltaX = targetEntity.getX() - this.actor.getX();
        double deltaZ = targetEntity.getZ() - this.actor.getZ();
        double deltaY;

        if (targetEntity instanceof LivingEntity livingEntity) {
            deltaY = livingEntity.getEyeY() - this.actor.getEyeY();
        } else {
            deltaY = (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0 - this.actor.getEyeY();
        }

        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetYaw = (float) (MathHelper.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90.0F;
        float targetPitch = (float) (-(MathHelper.atan2(deltaY, distance) * (180 / Math.PI)));

        float currentYaw = this.actor.getYaw();
        float currentPitch = this.actor.getPitch();

        float yawDifference = MathHelper.wrapDegrees(currentYaw - targetYaw);
        float pitchDifference = MathHelper.wrapDegrees(currentPitch - targetPitch);

        return Math.abs(yawDifference) < yawThreshold && Math.abs(pitchDifference) < pitchThreshold;
    }

    public void tick() {
        LivingEntity livingEntity = this.actor.getTarget();
        if (livingEntity != null) {
            double d = this.actor.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            boolean canSeeTarget = this.actor.getVisibilityCache().canSee(livingEntity);
            boolean wasSeeingTarget = this.targetSeeingTicker > 0;

            // Update target seeing ticker based on visibility
            if (canSeeTarget != wasSeeingTarget) {
                this.targetSeeingTicker = 0;
            }
            this.targetSeeingTicker = canSeeTarget ? this.targetSeeingTicker + 1 : this.targetSeeingTicker - 1;

            // Manage navigation and combat ticks
            if (d <= this.squaredRange && this.targetSeeingTicker >= 20) {
                this.actor.getNavigation().stop();
                this.combatTicks++;
            } else {
                this.actor.getNavigation().startMovingTo(livingEntity, this.speed);
                this.combatTicks = -1;
            }

            // Randomize movement directions
            if (this.combatTicks >= 20) {
                if (this.actor.getRandom().nextFloat() < 0.3) {
                    this.movingToLeft = !this.movingToLeft;
                }
                if (this.actor.getRandom().nextFloat() < 0.3) {
                    this.backward = !this.backward;
                }
                this.combatTicks = 0;
            }

            // Update strafing behavior
            if (this.combatTicks > -1) {
                if (d > this.squaredRange * 0.75) {
                    this.backward = false;
                } else if (d < this.squaredRange * 0.25) {
                    this.backward = true;
                }
                this.actor.getMoveControl().strafeTo(this.backward ? -0.5F : 0.5F, this.movingToLeft ? 0.5F : -0.5F);
            }

            // Simplified looking at the target
            this.actor.lookAtEntity(livingEntity, 30.0F, 30.0F);
            if (isLookingAtEntity(livingEntity, 1, 1)) {
                lockedTicks++;
            } else {
                lockedTicks = 0;
            }

            Hand gunHand = this.actor.getActiveHand();
            ItemStack gunStack = this.actor.getStackInHand(gunHand);
            if (itemGunMap.containsKey(gunStack.getItem())) {
                Gun gun = itemGunMap.get(gunStack.getItem());
                // Handle item usage
                gun.reload(this.actor.getWorld(), this.actor, gunHand, this.actor.getRandom(), this.actor.getWorld().getLocalDifficulty(this.actor.getBlockPos()));
                if (!canSeeTarget && this.targetSeeingTicker < -60) {
                    this.actor.clearActiveItem();
                } else if (canSeeTarget) {
                    this.actor.clearActiveItem();
                    int currentReload = gunStack.getOrCreateNbt().getInt("gun_load");
                    int currentCooldown = gunStack.getOrCreateNbt().getInt("gun_cooldown");


                    ItemStack chamber = ItemStack.EMPTY;

                    NbtCompound nbt = gunStack.getNbt();
                    if (nbt != null && nbt.contains("gun_ammo")) {
                        NbtCompound ammoNbt = nbt.getCompound("gun_ammo");
                        chamber = ItemStack.fromNbt(ammoNbt).copy(); // converts NBT back to ItemStack
                    }

                    if (!chamber.isEmpty() && currentReload == 1 && currentCooldown == 0 && lockedTicks >= 10) {
                        if (this.actor.getWorld() instanceof ServerWorld serverWorld) {
                            this.targetSeeingTicker -= gun.shoot(serverWorld, this.actor, gunHand);
                        }
                    }
                }
            } else if (this.targetSeeingTicker >= -60) {
                ItemStack mainHand = this.actor.getStackInHand(Hand.MAIN_HAND);
                ItemStack offHand = this.actor.getStackInHand(Hand.OFF_HAND);
                if (itemGunMap.containsKey(mainHand.getItem())) {
                    this.actor.setCurrentHand(Hand.MAIN_HAND);
                }
                if (itemGunMap.containsKey(offHand.getItem())) {
                    this.actor.setCurrentHand(Hand.OFF_HAND);
                }
            }
        }
    }
}
