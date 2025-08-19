package systems.brn.plasticgun.defence;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import systems.brn.plasticgun.lib.TrinketPolymerItem;
import systems.brn.plasticgun.lib.WeaponDamageType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static systems.brn.plasticgun.lib.Util.id;

public class WeaponArmor extends TrinketPolymerItem implements Trinket {
    public final HashMap<WeaponDamageType, Double> resistances = new HashMap<>();



    public WeaponArmor(String name, int durability, double grenadeDamageCoefficient, double fragmentationDamageCoefficient, double bulletDamageCoefficient, double shurikenDamageCoefficient) {
        super(new Item.Settings().maxDamage(durability), id(name).toString());

        // Register the item
        Registry.register(Registries.ITEM, id(name), this);

        // Register as a trinket
        TrinketsApi.registerTrinket(this, this);

        // Store resistances
        resistances.put(WeaponDamageType.BULLET, bulletDamageCoefficient);
        resistances.put(WeaponDamageType.FRAGMENTATION_GRENADE, fragmentationDamageCoefficient);
        resistances.put(WeaponDamageType.GRENADE, grenadeDamageCoefficient);
        resistances.put(WeaponDamageType.SHURIKEN, shurikenDamageCoefficient);
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        Trinket.super.tick(stack, slot, entity);
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        Trinket.super.onEquip(stack, slot, entity);
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        Trinket.super.onUnequip(stack, slot, entity);
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return Trinket.super.canEquip(stack, slot, entity);
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return Trinket.super.canUnequip(stack, slot, entity);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return Trinket.super.getModifiers(stack, slot, entity, uuid);
    }

    @Override
    public void onBreak(ItemStack stack, SlotReference slot, LivingEntity entity) {
        Trinket.super.onBreak(stack, slot, entity);
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return Trinket.super.getDropRule(stack, slot, entity);
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext context, @Nullable ServerPlayerEntity player) {
        return super.getPolymerItemStack(itemStack, context, player);
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return super.getPolymerCustomModelData(itemStack, player);
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return super.getPolymerArmorColor(itemStack, player);
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        super.modifyClientTooltip(tooltip, stack, player);
    }

    @Override
    public boolean showDefaultNameInItemFrames() {
        return super.showDefaultNameInItemFrames();
    }

    @Override
    public Item getPolymerReplacement(ServerPlayerEntity player) {
        return super.getPolymerReplacement(player);
    }

    @Override
    public boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return super.canSynchronizeToPolymerClient(player);
    }

    @Override
    public boolean canSyncRawToClient(ServerPlayerEntity player) {
        return super.canSyncRawToClient(player);
    }

    @Override
    public boolean handleMiningOnServer(ItemStack tool, BlockState targetBlock, BlockPos pos, ServerPlayerEntity player) {
        return super.handleMiningOnServer(tool, targetBlock, pos, player);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return super.allowNbtUpdateAnimation(player, hand, oldStack, newStack);
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return super.allowContinuingBlockBreaking(player, oldStack, newStack);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        return super.getAttributeModifiers(stack, slot);
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        return super.isSuitableFor(stack, state);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return super.getRecipeRemainder(stack);
    }

    @Override
    public boolean isEnabled(FeatureSet enabledFeatures) {
        return super.isEnabled(enabledFeatures);
    }
}