package systems.brn.plasticgun;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.grenades.GrenadeEntity;
import systems.brn.plasticgun.grenades.GrenadeItem;
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.lib.EventHandler;
import systems.brn.plasticgun.lib.ItemGroups;
import systems.brn.plasticgun.shurikens.ShurikenEntity;
import systems.brn.plasticgun.shurikens.ShurikenItem;
import systems.brn.plasticgun.testing.DamageTester;

import java.util.ArrayList;

import static systems.brn.plasticgun.lib.Util.id;

public class PlasticGun implements ModInitializer {

    public static final String MOD_ID = "plasticgun";

    public static final ArrayList<Gun> guns = new ArrayList<>();

    public static final ArrayList<BulletItem> bullets = new ArrayList<>();

    public static final ArrayList<GrenadeItem> grenades = new ArrayList<>();

    public static final ArrayList<ShurikenItem> shurikens = new ArrayList<>();

    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    public static EntityType<GrenadeEntity> GRENADE_ENTITY_TYPE;

    public static EntityType<ShurikenEntity> SHURIKEN_ENTITY_TYPE;

    public static EntityType<DamageTester> DAMAGE_TESTER_ENTITY_TYPE;

    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        // Bullets - Batch 1 (Better Bullets First)
        bullets.add(new BulletItem("357_magnum", 99, 1.4, 357, false, 0, 0));
        bullets.add(new BulletItem("32_acp_high_velocity", 99, 0.9, 32, false, 0, 0));
        bullets.add(new BulletItem("45_acp_hollow_point", 99, 1.2, 45, false, 0, 0));
        bullets.add(new BulletItem("9mm_jhp", 99, 1.05, 9, false, 0, 0));
        bullets.add(new BulletItem("38_special_p", 99, 1.3, 38, false, 0, 0));
        bullets.add(new BulletItem("762_tokarev_ap", 99, 1.2, 762, false, 0, 0));

        // Bullets - Batch 2 (Standard Bullets)
        bullets.add(new BulletItem("357_standard", 99, 1, 357, false, 0, 0));
        bullets.add(new BulletItem("32_acp", 99, 0.8, 32, false, 0, 0));
        bullets.add(new BulletItem("45_acp", 99, 1, 45, false, 0, 0));
        bullets.add(new BulletItem("9mm_parabellum", 99, 0.9, 9, false, 0, 0));
        bullets.add(new BulletItem("38_special", 99, 0.95, 38, false, 0, 0));
        bullets.add(new BulletItem("762_tokarev", 99, 1.1, 762, false, 0, 0));

        bullets.add(new BulletItem("rpg_shell_incendiary", 4, 1.1, 999, true, 1, 0));
        bullets.add(new BulletItem("rpg_shell", 4, 1.1, 999, false, 1, 0));
        bullets.add(new BulletItem("force_container", 99, 0, 888, false, 0, 1));

        // Guns
        guns.add(new Gun("357_revolver", 1, 8, 5, 6, 45, 357, 14, false, 0, 0));
        guns.add(new Gun("colt_1903", 0.3, 10, 5, 8, 38, 32, 5, false, 0, 0));
        guns.add(new Gun("colt_45", 0.4, 9, 5, 7, 48, 45, 5, false, 0, 0));
        guns.add(new Gun("colt_peacemaker", 0.6, 8, 5, 6, 43, 45, 5, false, 0, 0));
        guns.add(new Gun("p2022", 0.2, 12, 5, 10, 41, 9, 5, false, 0, 0));
        guns.add(new Gun("snub_nosed_revolver", 0.4, 7, 3, 5, 36, 38, 14, false, 0, 0));
        guns.add(new Gun("tokarev_tt_33", 0.7, 10, 5, 8, 45, 762, 5, false, 0, 0));
        guns.add(new Gun("ak_47", 0.2, 4, 5, 30, 45, 762, 0, false, 0, 0));
        guns.add(new Gun("awp", 1, 4, 20, 1, 75, 762, 20, true, 0, 0));

        guns.add(new Gun("rpg9", 2, 4, 20, 1, 10, 999, 8, false, 20, 0));
        guns.add(new Gun("forcegun", 0, 2, 5, 20, 10, 888, 0, false, 0, 20));


        grenades.add(new GrenadeItem("grenade_m67", 1, 6.5f, 0.5f, 60, false, true, 0, 0, 0, 10, 0)); // M67 Fragmentation Grenade
        grenades.add(new GrenadeItem("grenade_mk2", 1, 7f, 0.5f, 60, false, true, 0, 0, 0, 10, 0)); // Mk 2 Fragmentation Grenade

        grenades.add(new GrenadeItem("grenade_an_m14", 1, 5f, 0.5f, 40, true, false, 0, 0, 0, 8, 0)); // AN-M14 Incendiary Grenade
        grenades.add(new GrenadeItem("grenade_m3d", 1, 10f, 0.5f, 60, true, true, 0, 0, 0, 10, 0)); // M34 White Phosphorus Incendiary Fragmentation Grenade

        grenades.add(new GrenadeItem("grenade_m18", 1, 0.1f, 0.2f, 50, false, false, 0, 0, 100, 15, 0)); // M18 Smoke Grenade
        grenades.add(new GrenadeItem("grenade_l109a1", 1, 6.5f, 0.5f, 60, false, true, 0, 0, 0, 10, 0)); // L109A1 Fragmentation Grenade

        grenades.add(new GrenadeItem("grenade_m84", 1, 0.5f, 0.2f, 120, false, false, 10, 10, 5, 12, 10)); // M84 Stun Grenade (Flashbang)
        grenades.add(new GrenadeItem("grenade_rgd_5", 1, 6.5f, 0.5f, 60, false, true, 0, 0, 0, 10, 0)); // RGD-5 Fragmentation Grenade

        grenades.add(new GrenadeItem("grenade_thermite", 1, 4f, 0.3f, 80, true, false, 0, 0, 0, 8, 15)); // Thermite Grenade
        grenades.add(new GrenadeItem("grenade_f1", 1, 7f, 0.5f, 60, false, true, 0, 0, 0, 10, 0)); // F1 Soviet Fragmentation Grenade

        grenades.add(new GrenadeItem("grenade_mk3a2", 1, 6f, 0.4f, 60, false, false, 0, 0, 0, 10, 0)); // Mk3A2 Offensive Grenade
        grenades.add(new GrenadeItem("grenade_m7a3", 1, 0.1f, 0.2f, 90, false, false, 0, 0, 50, 8, 40)); // M7A3 CS Gas Grenade

        grenades.add(new GrenadeItem("grenade_no_69", 1, 5.5f, 0.4f, 60, false, false, 0, 0, 0, 10, 0)); // No. 69 British Offensive Grenade

        grenades.add(new GrenadeItem("grenade_rgo", 1, 6.5f, 0.5f, 60, false, true, 0, 0, 0, 10, 0)); // RGO Fragmentation Grenade
        grenades.add(new GrenadeItem("grenade_k417", 1, 7f, 0.5f, 70, false, true, 0, 0, 0, 10, 0)); // K417 Fragmentation Grenade


        shurikens.add(new ShurikenItem("wooden_shuriken", 1, 5, 4f));
        shurikens.add(new ShurikenItem("stone_shuriken", 2, 5, 4f));
        shurikens.add(new ShurikenItem("iron_shuriken", 4, 5, 4f));
        shurikens.add(new ShurikenItem("golden_shuriken", 3, 5, 4f));
        shurikens.add(new ShurikenItem("diamond_shuriken", 4, 5, 4f));
        shurikens.add(new ShurikenItem("netherite_shuriken", 8, 5, 4f));


        GRENADE_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("grenade"),
                EntityType.Builder.<GrenadeEntity>create(GrenadeEntity::new, SpawnGroup.MISC).build()
        );
        PolymerEntityUtils.registerType(GRENADE_ENTITY_TYPE);

        BULLET_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("bullet"),
                EntityType.Builder.<BulletEntity>create(BulletEntity::new, SpawnGroup.MISC).build()
        );
        PolymerEntityUtils.registerType(BULLET_ENTITY_TYPE);

        SHURIKEN_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("shuriken"),
                EntityType.Builder.<ShurikenEntity>create(ShurikenEntity::new, SpawnGroup.MISC).build()
        );
        PolymerEntityUtils.registerType(SHURIKEN_ENTITY_TYPE);

        DAMAGE_TESTER_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("damagetester"),
                EntityType.Builder.create(DamageTester::new, SpawnGroup.MISC).build()
        );
        FabricDefaultAttributeRegistry.register(DAMAGE_TESTER_ENTITY_TYPE, DamageTester.createDamageTesterAttributes());
        PolymerEntityUtils.registerType(DAMAGE_TESTER_ENTITY_TYPE);


        // Detect item use
        UseItemCallback.EVENT.register(EventHandler::onItemUse);

        // Use a custom method to detect general hand swings
        // This is done by checking the player's actions in the tick event
        ServerTickEvents.END_WORLD_TICK.register(EventHandler::onServerWorldTick);

        ServerEntityEvents.ENTITY_LOAD.register(EventHandler::onEntityLoad);

        ItemGroups.register();

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        logger.info("Guns are loaded");
    }
}