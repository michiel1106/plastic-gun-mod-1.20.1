package systems.brn.plasticgun.lib;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;


public class GunComponents {
    public static final ComponentType<ItemStack> GUN_AMMO_COMPONENT = register("gunammo", builder -> builder.codec(ItemStack.CODEC));
    public static final ComponentType<Integer> GUN_LOADING_COMPONENT = register("gunload", builder -> builder.codec(Codec.INT));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        ComponentType<T> componentType = Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                id,
                builderOperator.apply(ComponentType.builder()).build()
        );
        PolymerComponent.registerDataComponent(componentType);
        return componentType;
    }
}