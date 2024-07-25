package systems.brn.plasticgun.bullets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import systems.brn.plasticgun.guns.Gun;

import java.lang.reflect.Method;

import static systems.brn.plasticgun.PlasticGun.BULLET_ENTITY_TYPE;
import static systems.brn.plasticgun.PlasticGun.bullets;

public class BulletEntity extends PersistentProjectileEntity implements PolymerEntity {
    private final Gun gun;

    public BulletEntity(Vec3d pos, ServerPlayerEntity player, ItemStack stack, ItemStack weapon, Gun gun, double damage, int speed) {
        super(BULLET_ENTITY_TYPE, pos.x, pos.y + 1.5d, pos.z, player.getEntityWorld(), stack, weapon);
        this.setOwner(player);
        this.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, speed, 0);
        this.pickupType = PickupPermission.CREATIVE_ONLY;
        this.setDamage(damage);
        this.setSilent(true);
        this.gun = gun;
        this.setCustomPierceLevel((byte) 1);
    }

    public void setCustomPierceLevel(byte level) {
        try {
            Method method = PersistentProjectileEntity.class.getDeclaredMethod("setPierceLevel", byte.class);
            method.setAccessible(true); // Allow access to private methods
            method.invoke(this, level);
        } catch (Exception ignored) {
        }
    }

    public BulletEntity(EntityType<BulletEntity> entityType, World world) {
        super(entityType, world);
        this.gun = null;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        if (gun != null) {
            return gun.ammo.getFirst().getDefaultStack();
        } else {
            return bullets.getFirst().getDefaultStack();
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.ARROW;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockState block = this.getWorld().getBlockState(blockHitResult.getBlockPos());

            SoundEvent soundEvent = block.getSoundGroup().getHitSound();
            setSilent(false);
            playSound(soundEvent, 4.0F, 1.0F);
            setSilent(true);
        }
        this.setOnFire(true);
        super.onBlockHit(blockHitResult);
        this.setOnFire(false);
        this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        setSilent(false);
        playSound(SoundEvents.BLOCK_BAMBOO_HIT, 4.0F, 1.0F);
        setSilent(true);

        super.onEntityHit(entityHitResult);
        this.discard();
    }


}
