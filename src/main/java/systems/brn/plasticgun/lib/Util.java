package systems.brn.plasticgun.lib;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
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

    public static int canInsertItemIntoInventory(Inventory inventory, ItemStack itemStack) {
        // Get the player's inventory
        int maxInsert = 0;

        if (inventory instanceof PlayerInventory playerInventory) {
            // Iterate through the slots in the player's inventory
            for (int i = 0; i < playerInventory.main.size(); i++) {
                ItemStack slotStack = playerInventory.main.get(i);
                maxInsert = canInsertToStack(slotStack, itemStack, maxInsert);
            }
        } else {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack slotStack = inventory.getStack(i);
                maxInsert = canInsertToStack(slotStack, itemStack, maxInsert);
            }
        }

        return maxInsert; // Return the maximum insertion count
    }

    public static int canInsertToStack(ItemStack stack1, ItemStack stack2, int maxInsert) {
        if (stack1.isEmpty() || ItemStack.areItemsEqual(stack1, stack2)) {
            int remainingSpace = stack1.isEmpty() ? stack2.getMaxCount() : stack1.getMaxCount() - stack1.getCount();
            maxInsert += remainingSpace;
            // If the maximum insertion count is greater than or equal to the item count, return the item count
            if (maxInsert >= stack2.getCount()) {
                return stack2.getCount();
            }
        }
        return maxInsert;
    }

    public static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        return !stack1.isEmpty() && stack1.getItem() == stack2.getItem() && ItemStack.areItemsAndComponentsEqual(stack1, stack2);
    }

    public static ItemStack insertStackIntoInventory(Inventory inventory, ItemStack stack) {
        // First, try to merge with existing stacks
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (canCombine(slotStack, stack)) {
                int transferAmount = Math.min(stack.getCount(), slotStack.getMaxCount() - slotStack.getCount());
                if (transferAmount > 0) {
                    slotStack.increment(transferAmount);
                    stack.decrement(transferAmount);
                    inventory.markDirty();
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // Next, try to find an empty slot
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (slotStack.isEmpty()) {
                inventory.setStack(i, stack.copy());
                stack.setCount(0);
                inventory.markDirty();
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }
}
