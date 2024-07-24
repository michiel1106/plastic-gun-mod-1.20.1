package systems.brn.plasticgun.bullets;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import systems.brn.plasticgun.guns.Gun;

import static net.minecraft.particle.ParticleTypes.CRIT;
import static systems.brn.plasticgun.PlasticGun.BULLET_ENTITY_TYPE;
import static systems.brn.plasticgun.PlasticGun.bullets;

public class BulletEntity extends PersistentProjectileEntity implements PolymerEntity {
    private final Gun gun;
    public BulletEntity(Vec3d pos, ServerPlayerEntity player, ItemStack stack, ItemStack weapon, Gun gun, double damage, int speed) {
        super(BULLET_ENTITY_TYPE, pos.x, pos.y + 1.5d, pos.z, player.getEntityWorld(), stack, weapon);
        this.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, speed, 0);
        this.pickupType = PickupPermission.CREATIVE_ONLY;
        player.setPitch(player.getPitch() + 5);
        this.setDamage(damage);
        this.setSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value());
        this.gun = gun;
    }

    public BulletEntity(EntityType<BulletEntity> entityType, World world) {
        super(entityType, world);
        this.gun = null;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        if (gun != null){
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
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Vec3d pos = entityHitResult.getPos();
        entityHitResult.getEntity().getEntityWorld().addParticle(CRIT, true, pos.x, pos.y, pos.z, 3, 0, 0);
        Vec3d diff = entityHitResult.getPos();
        diff.subtract(entityHitResult.getEntity().getPos());
        double height = diff.y;
        if (entityHitResult.getEntity() instanceof PlayerEntity && height >= 1.75 && height <= 2) {
            this.setDamage(2);
        }
        super.onEntityHit(entityHitResult);
    }

}
