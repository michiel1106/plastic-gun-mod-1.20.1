package systems.brn.plasticgun.mixins;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import systems.brn.plasticgun.PlasticGun;
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.guns.WeaponShootGoal;

import static systems.brn.plasticgun.PlasticGun.guns;
import static systems.brn.plasticgun.PlasticGun.itemGunMap;
import static systems.brn.plasticgun.lib.Util.getDifficultyAdjustedChance;

@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonEntityGunMixin {
    @Unique
    protected boolean isHoldingGun() {
        AbstractSkeletonEntity thisObject = (AbstractSkeletonEntity) (Object) this;
        ItemStack mainHandStack = thisObject.getMainHandStack();
        ItemStack offhandHandStack = thisObject.getOffHandStack();
        return itemGunMap.containsKey(mainHandStack.getItem()) || itemGunMap.containsKey(offhandHandStack.getItem());
    }

    @Inject(method = "canUseRangedWeapon", at = @At("HEAD"), cancellable = true)
    public void canUseRangedWeapon(RangedWeaponItem weapon, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(weapon == Items.BOW || itemGunMap.containsKey(weapon));
    }

    @Inject(method = "updateAttackType", at = @At("HEAD"), cancellable = true)
    public void updateAttackType(CallbackInfo ci) {
        AbstractSkeletonEntity thisObject = (AbstractSkeletonEntity) (Object) this;

        // Remove existing attack goals
        thisObject.goalSelector.remove(thisObject.bowAttackGoal);
        thisObject.goalSelector.remove(thisObject.meleeAttackGoal);

        // Add WeaponShootGoal if holding a gun
        if (isHoldingGun()) {
            thisObject.goalSelector.add(4, new WeaponShootGoal<>(thisObject, 1.0, 15.0F));
        } else {
            // Keep existing logic for bows
            ItemStack itemStack = thisObject.getStackInHand(ProjectileUtil.getHandPossiblyHolding(thisObject, Items.BOW));
            if (itemStack.isOf(Items.BOW)) {
                int attackInterval = thisObject.getWorld().getDifficulty() == Difficulty.HARD ? 20 : 40;
                BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal = new BowAttackGoal<>(thisObject, 1.0, attackInterval, 15.0F);
                thisObject.goalSelector.add(4, bowAttackGoal);
            } else {
                // Default to melee attack if no bow
                MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(thisObject, 1.2, false) {
                    @Override
                    public void stop() {
                        super.stop();
                        thisObject.setAttacking(false);
                    }

                    @Override
                    public void start() {
                        super.start();
                        thisObject.setAttacking(true);
                    }
                };
                thisObject.goalSelector.add(4, meleeAttackGoal);
            }
        }

        ci.cancel();
    }

    @Inject(method = "initEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/AbstractSkeletonEntity;equipStack(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)V"), cancellable = true)
    protected void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        AbstractSkeletonEntity thisObject = (AbstractSkeletonEntity) (Object) this;

        // Get world difficulty
        World world = thisObject.getWorld();
        int difficultyAdjustedChance = getDifficultyAdjustedChance(localDifficulty, world);

        // Equip item based on adjusted chance
        if (random.nextInt(difficultyAdjustedChance) == 0) {
            thisObject.equipStack(EquipmentSlot.MAINHAND, new ItemStack(guns.get(random.nextInt(guns.size()))));
        } else {
            thisObject.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }

        ci.cancel();
    }

}
