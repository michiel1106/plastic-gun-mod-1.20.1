package systems.brn.plasticgun;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
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
import systems.brn.plasticgun.guns.Gun;
import systems.brn.plasticgun.lib.EventHandler;
import systems.brn.plasticgun.testing.DamageTester;

import java.util.ArrayList;

import static systems.brn.plasticgun.lib.Util.id;

public class PlasticGun implements ModInitializer {

    public static final String MOD_ID = "plasticgun";

    public static final ArrayList<Gun> guns = new ArrayList<>();

    public static final ArrayList<BulletItem> bullets = new ArrayList<>();

    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    public static EntityType<DamageTester> DAMAGE_TESTER_ENTITY_TYPE;

    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);;

    @Override
    public void onInitialize() {

        // Bullets - Batch 1 (Better Bullets First)
        bullets.add(new BulletItem("357_magnum", 1.4, 357,  false,0, 0));
        bullets.add(new BulletItem("32_acp_high_velocity", 0.9, 32,  false,0, 0));
        bullets.add(new BulletItem("45_acp_hollow_point", 1.2, 45,  false,0, 0));
        bullets.add(new BulletItem("9mm_jhp", 1.05, 9,  false,0, 0));
        bullets.add(new BulletItem("38_special_p", 1.3, 38,  false,0, 0));
        bullets.add(new BulletItem("762_tokarev_ap", 1.2, 762,  false,0, 0));

        // Bullets - Batch 2 (Standard Bullets)
        bullets.add(new BulletItem("357_standard", 1, 357,  false,0, 0));
        bullets.add(new BulletItem("32_acp", 0.8, 32,  false,0, 0));
        bullets.add(new BulletItem("45_acp", 1, 45,  false,0, 0));
        bullets.add(new BulletItem("9mm_parabellum", 0.9, 9,  false,0, 0));
        bullets.add(new BulletItem("38_special", 0.95, 38,  false,0, 0));
        bullets.add(new BulletItem("762_tokarev", 1.1, 762,  false,0, 0));

        bullets.add(new BulletItem("rpg_shell_incendiary", 1.1, 999, true, 1, 0));
        bullets.add(new BulletItem("rpg_shell", 1.1, 999,  false,1, 0));
        bullets.add(new BulletItem("force_container", 0, 888,  false,0, 1));

        // Guns
        guns.add(new Gun("357_revolver", 0.5, 8, 5, 6, 43, 357, 14,  false,0, 0));
        guns.add(new Gun("colt_1903", 0.28, 10, 5,  8, 38, 32, 5, false, 0, 0));
        guns.add(new Gun("colt_45", 0.3, 9, 5,  7, 48, 45, 5, false, 0, 0));
        guns.add(new Gun("colt_peacemaker", 0.3, 8, 5,  6, 43, 45, 5, false, 0, 0));
        guns.add(new Gun("p2022", 0.1, 12, 5,  10, 41, 9, 5, false, 0, 0));
        guns.add(new Gun("snub_nosed_revolver", 0.3, 7,  3, 5, 36, 38, 14, false, 0, 0));
        guns.add(new Gun("tokarev_tt_33", 0.2, 10, 5,  8, 45, 762, 5, false,  0, 0));
        guns.add(new Gun("ak_47", 0.15, 4, 5,  30, 45, 762, 2, false, 0, 0));
        guns.add(new Gun("awp", 0.3, 4, 20,  1, 75, 762, 20, true, 0, 0));

        guns.add(new Gun("rpg9", 2, 4, 20,  1, 10, 999, 8, false, 20, 0));
        guns.add(new Gun("forcegun", 0, 2, 5,  20, 10, 888, 0, false, 0, 20));


        BULLET_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("bullet"),
                EntityType.Builder.<BulletEntity>create(BulletEntity::new, SpawnGroup.MISC).build()
        );
        PolymerEntityUtils.registerType(BULLET_ENTITY_TYPE);

        DAMAGE_TESTER_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("damagetester"),
                EntityType.Builder.<DamageTester>create(DamageTester::new, SpawnGroup.MISC).build()
        );
        FabricDefaultAttributeRegistry.register(DAMAGE_TESTER_ENTITY_TYPE, DamageTester.createDamageTesterAttributes());
        PolymerEntityUtils.registerType(DAMAGE_TESTER_ENTITY_TYPE);


        // Detect item use
        UseItemCallback.EVENT.register(EventHandler::onItemUse);

        // Use a custom method to detect general hand swings
        // This is done by checking the player's actions in the tick event
        ServerTickEvents.END_WORLD_TICK.register(EventHandler::onWorldTick);

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        logger.info("Guns are loaded");
    }
}