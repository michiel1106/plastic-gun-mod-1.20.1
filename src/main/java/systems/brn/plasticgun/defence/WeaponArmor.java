package systems.brn.plasticgun.defence;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.TrinketsApi;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import systems.brn.plasticgun.lib.TrinketPolymerItem;
import systems.brn.plasticgun.lib.WeaponDamageType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static systems.brn.plasticgun.lib.Util.id;

public class WeaponArmor extends TrinketPolymerItem implements Trinket {
    public final HashMap<WeaponDamageType, Double> resistances = new HashMap<>();



    public WeaponArmor(String name, int durability, double grenadeDamageCoefficient, double fragmentationDamageCoefficient, double bulletDamageCoefficient, double shurikenDamageCoefficient) {
        super(new Item.Settings().maxDamage(durability), name);

        // Register the item
        Registry.register(Registries.ITEM, id(name), this);

        // Register as a trinket
        TrinketsApi.registerTrinket(this, this);

        // Store resistances
        resistances.put(WeaponDamageType.BULLET, bulletDamageCoefficient);
        resistances.put(WeaponDamageType.FRAGMENTATION_GRENADE, fragmentationDamageCoefficient);
        resistances.put(WeaponDamageType.GRENADE, grenadeDamageCoefficient);
        resistances.put(WeaponDamageType.SHURIKEN, shurikenDamageCoefficient);


    }



    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        tooltip.add( Text.translatable("gun.description.armor.bullet", (int) ((1 - resistances.get(WeaponDamageType.BULLET)) * 100)));
        tooltip.add( Text.translatable("gun.description.armor.grenade", (int) ((1 - resistances.get(WeaponDamageType.GRENADE)) * 100)));
        tooltip.add( Text.translatable("gun.description.armor.fragmentation_grenade", (int) ((1 - resistances.get(WeaponDamageType.FRAGMENTATION_GRENADE)) * 100)));
        tooltip.add( Text.translatable("gun.description.armor.shuriken", (int) ((1 - resistances.get(WeaponDamageType.SHURIKEN)) * 100)));
        super.appendTooltip(stack, world, tooltip, context);

    }
}