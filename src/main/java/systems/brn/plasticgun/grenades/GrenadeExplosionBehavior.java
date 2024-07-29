package systems.brn.plasticgun.grenades;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import systems.brn.plasticgun.lib.WeaponDamageType;

import static systems.brn.plasticgun.lib.Util.getFinalDamage;

public class GrenadeExplosionBehavior extends ExplosionBehavior {


    @Override
    public float calculateDamage(Explosion explosion, Entity entity) {
        float original = super.calculateDamage(explosion, entity);
        if (entity instanceof LivingEntity livingEntity) {
            original = (float) getFinalDamage(livingEntity, WeaponDamageType.GRENADE, original);
        }
        return original;
    }
}
