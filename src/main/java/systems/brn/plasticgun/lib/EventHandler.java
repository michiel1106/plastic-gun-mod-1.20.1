package systems.brn.plasticgun.lib;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import systems.brn.plasticgun.guns.Gun;

import static systems.brn.plasticgun.PlasticGun.guns;

public class EventHandler {
    public static TypedActionResult<ItemStack> onItemUse(PlayerEntity playerEntity, World world, Hand hand) {
        ItemStack stack = playerEntity.getStackInHand(hand);
        if (!world.isClient) {
            Item stackInHand = playerEntity.getStackInHand(hand).getItem();
            for (Gun gun : guns) {
                if (gun == stackInHand) {
                    gun.reload(world, playerEntity, hand);
                    break;
                }
            }
        }
        return TypedActionResult.pass(stack);
    }


    public static void onWorldTick(World world) {
        // Iterate through all players to detect hand swings or item interactions
        for (PlayerEntity player : world.getPlayers()) {
            if (!world.isClient && player.handSwinging && player.handSwingTicks == 1) {
                Hand hand = player.getActiveHand();
                Item stackInHand = player.getStackInHand(hand).getItem();
                for (Gun gun : guns) {
                    if (gun == stackInHand) {
                        gun.shoot(world, player, hand);
                        break;
                    }
                }
            }
        }
    }
}
