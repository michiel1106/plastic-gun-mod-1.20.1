package systems.brn.plasticgun.shurikens;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import systems.brn.plasticgun.lib.WeaponDamageType;
import systems.brn.plasticgun.throwables.ThrowableProjectile;

import static systems.brn.plasticgun.PlasticGun.*;
import static systems.brn.plasticgun.lib.Util.*;

public class ShurikenEntity extends ThrowableProjectile implements PolymerEntity {
    public ShurikenEntity(ServerPlayerEntity player, ItemStack itemStack, float speed, double damage) {
        super(SHURIKEN_ENTITY_TYPE, player, itemStack, 0.5f, speed, damage, PickupPermission.ALLOWED, (byte) 0);
    }

    public ShurikenEntity(EntityType<ShurikenEntity> bulletEntityEntityType, World world) {
        super(bulletEntityEntityType, world, Vec3d.ZERO, ItemStack.EMPTY, 1f, 0d, PickupPermission.DISALLOWED, (byte) 255);
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
            ItemStack itemStack = itemStack();
            int maxDamage = itemStack.getMaxDamage();
            int currentDamage = itemStack.getDamage();
            currentDamage += 1;
            if (currentDamage >= maxDamage) {
                discard();
            } else {
                itemStack.setDamage(currentDamage);
            }
        }
        super.onBlockHit(blockHitResult);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
            this.setDamage(getFinalDamage(livingEntity, WeaponDamageType.SHURIKEN, this.damage));
            entityHitParticles(livingEntity, this.damage);
        }

        super.onEntityHit(entityHitResult);
    }
}
