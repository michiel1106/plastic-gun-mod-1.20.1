package systems.brn.plasticgun.bullets;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.List;

import static systems.brn.plasticgun.lib.Util.id;

public class BulletItem extends SimpleItem {
    public final double damageCoefficient;
    public final int caliber;
    public final boolean isIncendiary;
    public final double explosionPowerCoefficient;
    public final double repulsionPowerCoefficient;
    public BulletItem(String path, int maxCount, double damageCoefficient, int caliber, boolean isIncendiary, double explosionPowerCoefficient, double repulsionPowerCoefficient) {
        super(
                new Settings()
                        .maxCount(maxCount)
                        .component(DataComponentTypes.LORE, new LoreComponent(List.of(
                                Text.translatable("gun.description.caliber", caliber),
                                Text.translatable("gun.description.speed", damageCoefficient),
                                Text.translatable("gun.description.explosion_coefficient", explosionPowerCoefficient),
                                Text.translatable("gun.description.repulsion_efficient", repulsionPowerCoefficient),
                                Text.translatable(isIncendiary ? "gun.description.incendiary_yes" :  "gun.description.incendiary_no")
                        ))
)
                ,
                id(path),
                Items.STICK);
        this.damageCoefficient = damageCoefficient;
        this.caliber = caliber;
        this.isIncendiary = isIncendiary;
        this.explosionPowerCoefficient = explosionPowerCoefficient;
        this.repulsionPowerCoefficient = repulsionPowerCoefficient;
        Registry.register(Registries.ITEM, this.identifier, this);
    }
}
