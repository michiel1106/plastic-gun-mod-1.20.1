package systems.brn.plasticgun;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.guns.Gun;

import java.util.ArrayList;

import static systems.brn.plasticgun.lib.Util.id;

public class PlasticGun implements ModInitializer {

    public static final String MOD_ID = "plasticgun";

    public static final ArrayList<Gun> guns = new ArrayList<>();

    public static final ArrayList<BulletItem> bullets = new ArrayList<>();

    public static EntityType<BulletEntity> BULLET_ENTITY_TYPE;

    @Override
    public void onInitialize() {

        // Bullets - Batch 1 (Better Bullets First)
        bullets.add(new BulletItem("357_magnum", 1.2, 357));
        bullets.add(new BulletItem("32_acp_high_velocity", 0.9, 32));
        bullets.add(new BulletItem("45_acp_hollow_point", 1.1, 45));
        bullets.add(new BulletItem("9mm_jhp", 1.05, 9));
        bullets.add(new BulletItem("38_special_p", 1.1, 38));
        bullets.add(new BulletItem("762_tokarev_ap", 1.2, 762));

        // Bullets - Batch 2 (Standard Bullets)
        bullets.add(new BulletItem("357_standard", 1, 357));
        bullets.add(new BulletItem("32_acp", 0.8, 32));
        bullets.add(new BulletItem("45_acp", 1, 45));
        bullets.add(new BulletItem("9mm_parabellum", 0.9, 9));
        bullets.add(new BulletItem("38_special", 0.95, 38));
        bullets.add(new BulletItem("762_tokarev", 1.1, 762));

        // Guns
        guns.add(new Gun("357_revolver", 0.5, 3, 6, 40, 357));
        guns.add(new Gun("colt_1903", 0.4, 2, 8, 35, 32));
        guns.add(new Gun("colt_45", 0.7, 2, 7, 45, 45));
        guns.add(new Gun("colt_peacemaker", 0.6, 4, 6, 40, 45));
        guns.add(new Gun("p2022", 0.5, 2, 10, 38, 9));
        guns.add(new Gun("snub_nosed_revolver", 0.5, 3, 5, 35, 38));
        guns.add(new Gun("tokarev_tt_33", 0.6, 2, 8, 42, 762));



        BULLET_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                id("bullet"),
                EntityType.Builder.<BulletEntity>create(BulletEntity::new, SpawnGroup.MISC).build()
        );
        PolymerEntityUtils.registerType(BULLET_ENTITY_TYPE);

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
    }
}
