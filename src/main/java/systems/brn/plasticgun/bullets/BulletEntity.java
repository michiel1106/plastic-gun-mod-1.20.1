package systems.brn.plasticgun.bullets;

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.joml.Vector3f;
import systems.brn.plasticgun.guns.Gun;

import java.lang.reflect.Method;
import java.util.List;

import static systems.brn.plasticgun.PlasticGun.BULLET_ENTITY_TYPE;
import static systems.brn.plasticgun.PlasticGun.bullets;
import static systems.brn.plasticgun.lib.Util.applyKnockbackToEntities;

public class BulletEntity extends PersistentProjectileEntity implements PolymerEntity {
    private final Gun gun;
    private final double explosionPower;
    private final double repulsionPower;
    private final boolean isIncendiary;
    private ItemStack itemStack = Items.ARROW.getDefaultStack();
    private final float scale;

    public BulletEntity(Vec3d pos, ServerPlayerEntity player, ItemStack stack, ItemStack weapon, Gun gun, float scale, double damage, int speed, double explosionPower, double repulsionPower, boolean isIncendiary) {
        super(BULLET_ENTITY_TYPE, pos.x, pos.y + 1.5d, pos.z, player.getEntityWorld(), stack, weapon);
        this.setOwner(player);
        this.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, speed, 0);
        this.pickupType = PickupPermission.CREATIVE_ONLY;
        this.setDamage(damage);
        this.setSilent(true);
        this.gun = gun;
        this.scale = scale;
        this.setCustomPierceLevel((byte) 1);
        this.setItemStack(stack);
        this.explosionPower = explosionPower;
        this.repulsionPower = repulsionPower;
        this.isIncendiary = isIncendiary;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        if (initial) {
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.TELEPORTATION_DURATION, 2));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.SCALE, new Vector3f(scale)));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.BILLBOARD, (byte) DisplayEntity.BillboardMode.CENTER.ordinal()));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM, this.itemStack));
            data.add(DataTracker.SerializedEntry.of(DisplayTrackedData.Item.ITEM_DISPLAY, ModelTransformationMode.FIXED.getIndex()));
        }
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack itemStack() {
        return itemStack;
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
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.ITEM_DISPLAY;
    }

    private void hitDamage(Vec3d pos){
        if(explosionPower > 0) {
            getWorld().createExplosion(this, Explosion.createDamageSource(this.getWorld(), this), null, pos.getX(), pos.getY(), pos.getZ(), (float) explosionPower, isIncendiary, World.ExplosionSourceType.TNT);
        }
        if (repulsionPower > 0){
            applyKnockbackToEntities(this, pos, repulsionPower * 100, repulsionPower);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.setPosition(blockHitResult.getPos());
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
        hitDamage(blockHitResult.getPos());
        this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        this.setPosition(entityHitResult.getPos());
        setSilent(false);
        playSound(SoundEvents.BLOCK_BAMBOO_HIT, 4.0F, 1.0F);
        setSilent(true);

        super.onEntityHit(entityHitResult);
        hitDamage(entityHitResult.getPos());
        this.discard();
    }


}
