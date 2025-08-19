package systems.brn.plasticgun.bullets;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
                new Item.Settings().maxCount(maxCount),
                id(path),
                Items.STICK // fallback item for SimpleItem
        );

        this.damageCoefficient = damageCoefficient;
        this.caliber = caliber;
        this.isIncendiary = isIncendiary;
        this.explosionPowerCoefficient = explosionPowerCoefficient;
        this.repulsionPowerCoefficient = repulsionPowerCoefficient;
        Registry.register(Registries.ITEM, this.identifier, this);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("gun.description.caliber", caliber));
        tooltip.add(Text.translatable("gun.description.speed", damageCoefficient));
        tooltip.add(Text.translatable("gun.description.explosion_coefficient", explosionPowerCoefficient));
        tooltip.add(Text.translatable("gun.description.repulsion_efficient", repulsionPowerCoefficient));
        tooltip.add(Text.translatable(isIncendiary ? "gun.description.incendiary_yes" : "gun.description.incendiary_no"));
    }
}
