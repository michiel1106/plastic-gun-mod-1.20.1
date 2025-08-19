package systems.brn.plasticgun.lib;

import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import static systems.brn.plasticgun.lib.Util.id;

public class CraftingItem extends SimpleItem{

    public CraftingItem(String name) {
        super(new Settings(), id(name), Items.STICK);
        Registry.register(Registries.ITEM, id(name), this);
    }
}
