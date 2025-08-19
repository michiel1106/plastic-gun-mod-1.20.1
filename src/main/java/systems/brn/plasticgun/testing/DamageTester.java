package systems.brn.plasticgun.testing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Collections;

public class DamageTester extends LivingEntity implements PolymerEntity {

    public DamageTester(EntityType<systems.brn.plasticgun.testing.DamageTester> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean damage( DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity player) {
            player.sendMessage(Text.literal("You damaged by " + amount), false);
            if (player.isSneaking()) {
                this.remove(RemovalReason.KILLED);
            }
        }
        return false;
    }



    public static DefaultAttributeContainer.Builder createDamageTesterAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return null;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return Items.WITHER_SKELETON_SKULL.getDefaultStack();
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {

    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }


    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity serverPlayerEntity) {
        return EntityType.ZOMBIE;
    }
}
