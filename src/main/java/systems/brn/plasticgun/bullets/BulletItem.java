package systems.brn.plasticgun.bullets;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import systems.brn.plasticgun.lib.SimpleItem;

import static systems.brn.plasticgun.lib.Util.id;

public class BulletItem extends SimpleItem {
    public final double damageCoefficient;
    public final int caliber;
    public final boolean isIncendiary;
    public final double explosionPowerCoefficient;
    public final double repulsionPowerCoefficient;
    public BulletItem(String path, double damageCoefficient, int caliber, boolean isIncendiary, double explosionPowerCoefficient, double repulsionPowerCoefficient) {
        super(new Settings().maxCount(99), id(path), Items.STICK);
        this.damageCoefficient = damageCoefficient;
        this.caliber = caliber;
        this.isIncendiary = isIncendiary;
        this.explosionPowerCoefficient = explosionPowerCoefficient;
        this.repulsionPowerCoefficient = repulsionPowerCoefficient;
        Item item = Registry.register(Registries.ITEM, this.identifier, this);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.add(item));
    }
}
