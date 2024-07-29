package systems.brn.plasticgun.grenades;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import systems.brn.plasticgun.lib.WeaponDamageType;

import java.util.ArrayList;

import static systems.brn.plasticgun.lib.Util.getFinalDamage;

public class FragmentationExplosionBehavior extends ExplosionBehavior {
    final static ArrayList<Block> blocks = new ArrayList<>() {{
        add(Blocks.AIR);
        add(Blocks.WHITE_STAINED_GLASS);
        add(Blocks.ORANGE_STAINED_GLASS);
        add(Blocks.MAGENTA_STAINED_GLASS);
        add(Blocks.LIGHT_BLUE_STAINED_GLASS);
        add(Blocks.YELLOW_STAINED_GLASS);
        add(Blocks.LIME_STAINED_GLASS);
        add(Blocks.PINK_STAINED_GLASS);
        add(Blocks.GRAY_STAINED_GLASS);
        add(Blocks.LIGHT_GRAY_STAINED_GLASS);
        add(Blocks.CYAN_STAINED_GLASS);
        add(Blocks.PURPLE_STAINED_GLASS);
        add(Blocks.BLUE_STAINED_GLASS);
        add(Blocks.BROWN_STAINED_GLASS);
        add(Blocks.GREEN_STAINED_GLASS);
        add(Blocks.RED_STAINED_GLASS);
        add(Blocks.BLACK_STAINED_GLASS);
        add(Blocks.TINTED_GLASS);

        add(Blocks.WHITE_STAINED_GLASS_PANE);
        add(Blocks.ORANGE_STAINED_GLASS_PANE);
        add(Blocks.MAGENTA_STAINED_GLASS_PANE);
        add(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        add(Blocks.YELLOW_STAINED_GLASS_PANE);
        add(Blocks.LIME_STAINED_GLASS_PANE);
        add(Blocks.PINK_STAINED_GLASS_PANE);
        add(Blocks.GRAY_STAINED_GLASS_PANE);
        add(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        add(Blocks.CYAN_STAINED_GLASS_PANE);
        add(Blocks.PURPLE_STAINED_GLASS_PANE);
        add(Blocks.BLUE_STAINED_GLASS_PANE);
        add(Blocks.BROWN_STAINED_GLASS_PANE);
        add(Blocks.GREEN_STAINED_GLASS_PANE);
        add(Blocks.RED_STAINED_GLASS_PANE);
        add(Blocks.BLACK_STAINED_GLASS_PANE);

    }};

    @Override
    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
        Block block = state.getBlock();
        return blocks.contains(block);
    }

    @Override
    public float calculateDamage(Explosion explosion, Entity entity) {
        float original = super.calculateDamage(explosion, entity);
        if (entity instanceof LivingEntity livingEntity) {
            original = (float) getFinalDamage(livingEntity, WeaponDamageType.FRAGMENTATION_GRENADE, original);
        }
        return original;
    }
}
