package systems.brn.plasticgun.defence;

import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import systems.brn.plasticgun.lib.TrinketPolymerItem;
import systems.brn.plasticgun.lib.WeaponDamageType;

import java.util.HashMap;
import java.util.List;

import static systems.brn.plasticgun.lib.Util.id;

public class WeaponArmor extends TrinketPolymerItem implements Trinket {
    public final HashMap<WeaponDamageType, Double> resistances = new HashMap<>();

    public WeaponArmor(String name, int durability, double grenadeDamageCoefficient, double fragmentationDamageCoefficient, double bulletDamageCoefficient, double shurikenDamageCoefficient) {
        super(
                new Item.Settings().maxDamage(durability).maxCount(1).component(DataComponentTypes.LORE, new LoreComponent(List.of(
                        Text.translatable("gun.description.armor.bullet", (int) ((1 - bulletDamageCoefficient) * 100)),
                        Text.translatable("gun.description.armor.grenade", (int) ((1 - grenadeDamageCoefficient) * 100)),
                        Text.translatable("gun.description.armor.fragmentation_grenade", (int) ((1 - fragmentationDamageCoefficient) * 100)),
                        Text.translatable("gun.description.armor.shuriken", (int) ((1 - shurikenDamageCoefficient) * 100))
                ))).registryKey(RegistryKey.of(RegistryKeys.ITEM, id(name)))
                , name)
        ;
        Registry.register(Registries.ITEM, id(name), this);
        TrinketsApi.registerTrinket(this, this);
        resistances.put(WeaponDamageType.BULLET, bulletDamageCoefficient);
        resistances.put(WeaponDamageType.FRAGMENTATION_GRENADE, fragmentationDamageCoefficient);
        resistances.put(WeaponDamageType.GRENADE, grenadeDamageCoefficient);
        resistances.put(WeaponDamageType.SHURIKEN, shurikenDamageCoefficient);
    }
}