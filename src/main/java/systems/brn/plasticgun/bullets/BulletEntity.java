package systems.brn.plasticgun.bullets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.World;
import systems.brn.plasticgun.grenades.GrenadeExplosionBehavior;
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.lib.WeaponDamageType;
import xyz.nucleoid.packettweaker.PacketContext;

import java.lang.reflect.Method;
import java.util.List;

import static systems.brn.plasticgun.PlasticGun.*;
import static systems.brn.plasticgun.lib.Util.*;

public class BulletEntity extends PersistentProjectileEntity implements PolymerEntity {
    private final Gun gun;
    private final double explosionPower;
    private final double repulsionPower;
    private final boolean isIncendiary;
    private ItemStack itemStack = Items.ARROW.getDefaultStack();
    private final float scale;

    public BulletEntity(LivingEntity livingEntity, ItemStack stack, Hand hand, Gun gun, float scale, double damage, float speed, double explosionPower, double repulsionPower, boolean isIncendiary) {
        super(BULLET_ENTITY_TYPE, livingEntity.getPos().x, livingEntity.getPos().y + 1.75d, livingEntity.getPos().z, livingEntity.getWorld(), stack, livingEntity.getStackInHand(hand));
        this.setOwner(livingEntity);
        this.setVelocity(livingEntity, livingEntity.getPitch(), livingEntity.getYaw(), 0.0F, speed, 0);
        this.pickupType = PickupPermission.DISALLOWED;
        this.setDamage(damage);
        this.setSilent(true);
        this.gun = gun;
        this.scale = scale;
        this.setCustomPierceLevel((byte) 1);
        this.setItemStack(stack.copy());
        this.explosionPower = explosionPower;
        this.repulsionPower = repulsionPower;
        this.isIncendiary = isIncendiary;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        setProjectileData(data, initial, scale, this.itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
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
        this.explosionPower = 0;
        this.repulsionPower = 0;
        this.isIncendiary = false;
        this.scale = 1f;
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
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.setPosition(blockHitResult.getPos());
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockState block = this.getWorld().getBlockState(blockHitResult.getBlockPos());
            blockHitParticles(this.getPos(), block, this.getWorld(), this.damage * this.getVelocity().length());
            SoundEvent soundEvent = block.getSoundGroup().getHitSound();
            setSilent(false);
            playSound(soundEvent, 4.0F, 1.0F);
            setSilent(true);
        }
        this.setOnFire(true);
        super.onBlockHit(blockHitResult);
        this.setOnFire(false);
        hitDamage(blockHitResult.getPos(), explosionPower, repulsionPower, getWorld(), this, isIncendiary, new GrenadeExplosionBehavior());
        this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        this.setPosition(entityHitResult.getPos());
        setSilent(false);
        playSound(SoundEvents.BLOCK_BAMBOO_HIT, 4.0F, 1.0F);
        setSilent(true);

        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            this.setDamage(getFinalDamage(livingEntity, WeaponDamageType.BULLET, this.damage));
            entityHitParticles(livingEntity, this.damage * this.getVelocity().length());
        }

        super.onEntityHit(entityHitResult);
        hitDamage(entityHitResult.getPos(), explosionPower, repulsionPower, getWorld(), this, isIncendiary, new GrenadeExplosionBehavior());
        this.discard();
    }


}
