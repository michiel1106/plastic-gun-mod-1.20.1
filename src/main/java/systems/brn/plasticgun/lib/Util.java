package systems.brn.plasticgun.lib;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

import static systems.brn.plasticgun.PlasticGun.MOD_ID;

public class Util {

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static ItemStack findBulletStack(ArrayList<Item> bulletItem, ServerPlayerEntity player) {
        if (bulletItem == null || bulletItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (ItemStack itemStack : player.getInventory().main) {
            for (Item item : bulletItem) {
                if (item == itemStack.getItem()) {
                    return itemStack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
