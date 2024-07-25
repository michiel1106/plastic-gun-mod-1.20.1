package systems.brn.plasticgun.lib;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.LoggerFactory;
import systems.brn.plasticgun.guns.Gun;

import static systems.brn.plasticgun.PlasticGun.*;
import static systems.brn.plasticgun.lib.GunComponents.*;

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
            if (!world.isClient) {
                Hand hand = player.getActiveHand();
                ItemStack stackInHand = player.getStackInHand(hand);
                Item itemInHand = stackInHand.getItem();
                for (Gun gun : guns) {
                    if (gun == itemInHand) {
                        decrementComponent(GUN_COOLDOWN_COMPONENT, stackInHand);
                        decrementComponent(GUN_RELOAD_COOLDOWN_COMPONENT, stackInHand);

                        if (player.handSwinging && player.handSwingTicks == -1) {
                            gun.shoot(world, player, hand);
                        }
                        break;
                    }
                }
            }
        }
    }
}
