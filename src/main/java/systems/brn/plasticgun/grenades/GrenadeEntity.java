package systems.brn.plasticgun.grenades;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import systems.brn.plasticgun.throwables.ThrowableProjectile;

import java.util.List;

import static systems.brn.plasticgun.PlasticGun.GRENADE_ENTITY_TYPE;
import static systems.brn.plasticgun.lib.Util.*;

public class GrenadeEntity extends ThrowableProjectile implements PolymerEntity {
    public double explosionPower;
    public double repulsionPower;
    public boolean isIncendiary;
    public final boolean isFragmentation;
    public int timer;
    public int flashBangDuration;
    public int stunDuration;
    public int smokeTicks;
    public final int effectRadius;
    public final int smokeCount;

    public GrenadeEntity(World world, Vec3d pos, ItemStack itemStack, int timer, float scale, double explosionPower, double repulsionPower, boolean isIncendiary, boolean isFragmentation, int flashBangDuration, int stunDuration, int smokeTicks, int effectRadius, int smokeCount) {
        super(GRENADE_ENTITY_TYPE, world, pos, itemStack, scale, 0f, PickupPermission.DISALLOWED, (byte) 255);
        this.explosionPower = explosionPower;
        this.repulsionPower = repulsionPower;
        this.isIncendiary = isIncendiary;
        this.timer = timer;
        this.flashBangDuration = flashBangDuration;
        this.stunDuration = stunDuration;
        this.smokeTicks = smokeTicks;
        this.effectRadius = effectRadius;
        this.smokeCount = smokeCount;
        this.isFragmentation = isFragmentation;
    }

    public GrenadeEntity(ServerPlayerEntity player, ItemStack itemStack, int timer, float scale, int speed, double explosionPower, double repulsionPower, boolean isIncendiary, boolean isFragmentation, int flashBangDuration, int stunDuration, int smokeTicks, int effectRadius, int smokeCount) {
        super(GRENADE_ENTITY_TYPE, player, itemStack, scale, 1, 0d, PickupPermission.DISALLOWED, (byte) 255);
        this.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, speed, 0);
        this.explosionPower = explosionPower;
        this.repulsionPower = repulsionPower;
        this.isIncendiary = isIncendiary;
        this.timer = timer;
        this.flashBangDuration = flashBangDuration;
        this.stunDuration = stunDuration;
        this.smokeTicks = smokeTicks;
        this.effectRadius = effectRadius;
        this.smokeCount = smokeCount;
        this.isFragmentation = isFragmentation;
    }

    @Override
    public void tick() {
        super.tick();


        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();
        Vec3d vec3d = this.getVelocity();

        this.applyGravity();

        if (!this.getWorld().isClient) {
            this.noClip = !this.getWorld().isSpaceEmpty(this, this.getBoundingBox().contract(1.0E-7));
            if (this.noClip) {
                this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if (!this.isOnGround() || this.getVelocity().horizontalLengthSquared() > 9.999999747378752E-6 || (this.age + this.getId()) % 4 == 0) {
            this.move(MovementType.SELF, this.getVelocity());
            float f = 0.98F;
            if (this.isOnGround()) {
                f = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98F;
            }

            this.setVelocity(this.getVelocity().multiply(f, 0.98, f));
            if (this.isOnGround()) {
                Vec3d vec3d2 = this.getVelocity();
                if (vec3d2.y < 0.0) {
                    this.setVelocity(vec3d2.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        this.velocityDirty |= this.updateWaterState();
        if (!this.getWorld().isClient) {
            double d = this.getVelocity().subtract(vec3d).lengthSquared();
            if (d > 0.01) {
                this.velocityDirty = true;
            }
        }


        if (timer > 0) {
            timer -= 1;
        } else {
            explode();
        }
    }

    private void explode() {
        hitDamage(getPos(), explosionPower, repulsionPower, getWorld(), this, isIncendiary, isFragmentation ? new FragmentationExplosionBehavior() : new GrenadeExplosionBehavior());
        List<Entity> nearbyEntities = getEntitiesAround(this, effectRadius);
        if (stunDuration > 0) {
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, stunDuration, 255, true, false));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, stunDuration, 255, true, false));
                }
            }
            stunDuration = 0;
        }

        if (flashBangDuration > 0) {
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, flashBangDuration, 255, true, false));
                }
            }
            flashBangDuration = 0;
        }

        if (explosionPower > 0) {
            explosionPower = 0;
        }

        if (repulsionPower > 0) {
            repulsionPower = 0;
        }

        if (isIncendiary) {
            isIncendiary = false;
        }

        if (smokeTicks <= 0) {
            discard();
        } else {
            smokeTicks--;
            World worldTemp = getWorld();
            if (worldTemp instanceof ServerWorld world) {
                Random random = world.getRandom();
                for (int ix = 0; ix < smokeCount; ix++) {
                    double deltaRadius = effectRadius / 2d;
                    double speed = random.nextBetween(1, 20) / 100d;
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), smokeCount, deltaRadius, deltaRadius, deltaRadius, speed);
                }
            }
        }

    }

    public GrenadeEntity(EntityType<GrenadeEntity> entityType, World world) {
        super(entityType, world, Vec3d.ZERO, ItemStack.EMPTY, 1f, 0d, PickupPermission.DISALLOWED, (byte) 255);
        this.explosionPower = 0;
        this.repulsionPower = 0;
        this.isIncendiary = false;
        this.flashBangDuration = 0;
        this.stunDuration = 0;
        this.smokeTicks = 0;
        this.effectRadius = 0;
        this.smokeCount = 0;
        this.isFragmentation = false;
    }

}
