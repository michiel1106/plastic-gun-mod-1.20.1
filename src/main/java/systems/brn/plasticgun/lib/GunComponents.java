package systems.brn.plasticgun.lib;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;


public class GunComponents {


    public static void decrementComponent(String key, ItemStack stack) {
        NbtCompound orCreateNbt = stack.getOrCreateNbt();

        int component = orCreateNbt.getInt(key);
        if (component > 0) {
            component--;

            orCreateNbt.putInt(key, component);
            stack.setNbt(orCreateNbt);
        }
    }
}