package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import systems.brn.plasticgun.PlasticGun;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.grenades.GrenadeItem;
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.shurikens.ShurikenItem;

import java.util.ArrayList;
import java.util.Collection;

import static systems.brn.plasticgun.lib.Util.id;

public class ItemGroups {
    public static final ItemGroup GUNS_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(PlasticGun.guns.getFirst()))
            .displayName(Text.translatable("guns.groups.guns"))
            .entries(((context, entries) -> {
                for (Gun gun : PlasticGun.guns) {
                    entries.add(gun);
                }
            }))
            .build();

    public static final ItemGroup AMMO_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(PlasticGun.bullets.getFirst()))
            .displayName(Text.translatable("guns.groups.ammo"))
            .entries(((context, entries) -> {
                for (BulletItem bulletItem : PlasticGun.bullets) {
                    entries.add(bulletItem);
                }
            }))
            .build();

    public static final ItemGroup SHURIKEN_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(PlasticGun.shurikens.getFirst()))
            .displayName(Text.translatable("guns.groups.shurikens"))
            .entries(((context, entries) -> {
                for (ShurikenItem shurikenItem : PlasticGun.shurikens) {
                    entries.add(shurikenItem);
                }
            }))
            .build();

    public static final ItemGroup GRENADES_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(PlasticGun.grenades.getFirst()))
            .displayName(Text.translatable("guns.groups.grenades"))
            .entries(((context, entries) -> {
                for (GrenadeItem grenadeItem : PlasticGun.grenades) {
                    entries.add(grenadeItem);
                }
            }))
            .build();

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(id("guns"), GUNS_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("ammo"), AMMO_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("shurikens"), SHURIKEN_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("grenades"), GRENADES_GROUP);
    }
}