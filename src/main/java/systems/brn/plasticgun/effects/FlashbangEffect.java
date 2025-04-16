package systems.brn.plasticgun.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import systems.brn.plasticgun.PlasticGun;
import xyz.nucleoid.packettweaker.PacketContext;

import static systems.brn.plasticgun.PlasticGun.flashbangEffect;
import static systems.brn.plasticgun.PlasticGun.stunEffect;

public class FlashbangEffect extends StatusEffect implements PolymerStatusEffect {
    public FlashbangEffect() {
        // category: StatusEffectCategory - describes if the effect is helpful (BENEFICIAL), harmful (HARMFUL) or useless (NEUTRAL)
        // color: int - Color is the color assigned to the effect (in RGB)
        super(StatusEffectCategory.HARMFUL, 0xe9b8b3);
    }

    // Called every tick to check if the effect can be applied or not
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // In our case, we just make it return true so that it applies the effect every tick
        return true;
    }

    // Called when the effect is applied.
    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        return super.applyUpdateEffect(world, entity, amplifier);
    }
    @Override
    public StatusEffect getPolymerReplacement(StatusEffect potion, PacketContext packetContext) {
        if (PlasticGun.clientsWithMod.contains(packetContext.getPlayer())){
            return stunEffect.value();
        }
        return StatusEffects.BLINDNESS.value();
    }
}