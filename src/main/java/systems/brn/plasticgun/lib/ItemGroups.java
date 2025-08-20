package systems.brn.plasticgun.lib;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.networking.packets.PolymerItemGroupContent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import systems.brn.plasticgun.PlasticGun;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.defence.WeaponArmor;
import systems.brn.plasticgun.grenades.GrenadeItem;
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.shurikens.ShurikenItem;

import static systems.brn.plasticgun.lib.Util.id;

public class ItemGroups {



    public static final ItemGroup GUNS_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(PlasticGun.guns.stream().findFirst().get()))
            .displayName(Text.translatable("guns.groups.guns"))
            .entries(((context, entries) -> {
                for (Gun gun : PlasticGun.guns) {
                    entries.add(gun);
                }
            }))
            .build();






    public static final ItemGroup AMMO_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(PlasticGun.bullets.stream().findFirst().get()))
            .displayName(Text.translatable("guns.groups.ammo"))
            .entries(((context, entries) -> {
                for (BulletItem bulletItem : PlasticGun.bullets) {
                    entries.add(bulletItem);
                }
            }))
            .build();

    public static final ItemGroup SHURIKEN_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(PlasticGun.shurikens.stream().findFirst().get()))
            .displayName(Text.translatable("guns.groups.shurikens"))
            .entries(((context, entries) -> {
                for (ShurikenItem shurikenItem : PlasticGun.shurikens) {
                    entries.add(shurikenItem);
                }
            }))
            .build();

    public static final ItemGroup GRENADES_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(PlasticGun.grenades.stream().findFirst().get()))
            .displayName(Text.translatable("guns.groups.grenades"))
            .entries(((context, entries) -> {
                for (GrenadeItem grenadeItem : PlasticGun.grenades) {
                    entries.add(grenadeItem);
                }
            }))
            .build();

    public static final ItemGroup MATERIALS_GROUPS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(PlasticGun.craftingItems.stream().findFirst().get()))
            .displayName(Text.translatable("guns.groups.materials"))
            .entries(((context, entries) -> {
                for (CraftingItem craftingItem : PlasticGun.craftingItems) {
                    entries.add(craftingItem);
                }
            }))
            .build();

    public static final ItemGroup DEFENSE = FabricItemGroup.builder()
            .icon(() -> new ItemStack(PlasticGun.weaponArmors.stream().findFirst().get()))
            .displayName(Text.translatable("guns.groups.defense"))
            .entries(((context, entries) -> {
                for (WeaponArmor weaponArmor : PlasticGun.weaponArmors) {
                    entries.add(weaponArmor);
                }
            }))
            .build();

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(id("guns"), GUNS_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("ammo"), AMMO_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("shurikens"), SHURIKEN_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("grenades"), GRENADES_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("materials"), MATERIALS_GROUPS);
        PolymerItemGroupUtils.registerPolymerItemGroup(id("defense"), DEFENSE);
    }


}