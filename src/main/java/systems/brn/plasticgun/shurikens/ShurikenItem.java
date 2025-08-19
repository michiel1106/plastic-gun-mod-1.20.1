package systems.brn.plasticgun.shurikens;

import eu.pb4.polymer.core.api.item.PolymerItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.List;

import static systems.brn.plasticgun.lib.Util.id;

public class ShurikenItem extends SimpleItem implements PolymerItem {

    public final double damage;
    public final float speed;

    public ShurikenItem(String path, double damage, int durability, float speed) {
        super(new Settings().maxCount(16).maxDamage(durability), id(path), Items.WOODEN_PICKAXE);
        Registry.register(Registries.ITEM, id(path), this);
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        tooltip.add(Text.translatable("gun.description.damage", damage));
        tooltip.add(Text.translatable("gun.description.speed", speed));
        tooltip.add(Text.translatable("gun.description.damage_with_coefficient_max_speed", speed, speed * damage));
        super.appendTooltip(stack, world, tooltip, context);
    }

    public void chuck(ServerWorld world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player && !world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            ShurikenEntity ShurikenEntity = new ShurikenEntity(player, stack, speed, damage);
            world.spawnEntity(ShurikenEntity);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0f, 2.0f);
            if (!player.isCreative()) {
                stack.decrement(1);
            }
        }
    }
}
