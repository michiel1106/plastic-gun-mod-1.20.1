package systems.brn.plasticgun.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import systems.brn.plasticgun.grenades.GrenadeItem;

import java.util.Arrays;

import static systems.brn.plasticgun.PlasticGun.grenades;
import static systems.brn.plasticgun.lib.Util.getDifficultyAdjustedChance;
import static systems.brn.plasticgun.lib.Util.selectWeaponIndex;

@Mixin(ZombieEntity.class)
public abstract class ZombieGrenadeMixin extends MobEntity {

    @Shadow
    public abstract void tick();

    // Constructor required for the Mixin
    protected ZombieGrenadeMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initEquipment", at = @At("HEAD"), cancellable = true)
    protected void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        super.initEquipment(random, localDifficulty);

        World world = this.getWorld();
        int difficultyAdjustedChance = getDifficultyAdjustedChance(localDifficulty, world);

        // Equip item based on adjusted chance
        if (random.nextInt(difficultyAdjustedChance) == 0) {
            int i = random.nextInt(20);
            ItemStack stackToEquip;

            if (i < 8) {
                stackToEquip = new ItemStack(Items.IRON_SWORD);
            } else if (i < 14) {
                int grenadeIndex = selectWeaponIndex(random, localDifficulty, grenades.size());
                stackToEquip = new ItemStack(grenades.get(grenadeIndex));
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                this.setEquipmentDropChance(slot, 0f);
                }
            } else {
                stackToEquip = new ItemStack(Items.IRON_SHOVEL);
            }

            this.equipStack(EquipmentSlot.MAINHAND, stackToEquip);
        }
        ci.cancel();
    }

    @Inject(method = "dropEquipment", at = @At("RETURN"))
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        ItemStack mainHandItem = this.getEquippedStack(EquipmentSlot.MAINHAND);
        ItemStack offHandItem = this.getEquippedStack(EquipmentSlot.OFFHAND);
        for (GrenadeItem grenadeItem : grenades) {
            if (mainHandItem.getItem() == grenadeItem) {
                grenadeItem.turnIntoEntity(this, mainHandItem, 0, grenadeItem.explosionTarget);
                break;
            }
            if (offHandItem.getItem() == grenadeItem) {
                grenadeItem.turnIntoEntity(this, offHandItem, 0, grenadeItem.explosionTarget);
                break;
            }
        }
    }
}
