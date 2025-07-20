package systems.brn.plasticgun.throwables;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.lang.reflect.Method;
import java.util.List;

import static systems.brn.plasticgun.lib.Util.setProjectileData;

public class ThrowableProjectile extends PersistentProjectileEntity implements PolymerEntity {
    private ItemStack itemStack = Items.ARROW.getDefaultStack();
    public final EntityType<? extends PersistentProjectileEntity> entityType;
    private final float scale;

    public double prevX;
    public double prevY;
    public double prevZ;

    public ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, World world, Vec3d pos, ItemStack itemStack, float scale, double damage, PickupPermission pickupPermission, byte penetration) {
        super(entityType, pos.getX(), pos.getY() + 1.5d, pos.getZ(), world, itemStack, null);
        this.pickupType = pickupPermission;
        this.setDamage(damage);
        this.setSilent(true);
        this.scale = scale;
        this.entityType = entityType;
        this.setCustomPierceLevel(penetration);
        this.setItemStack(itemStack.copy());
    }

    public ThrowableProjectile(EntityType<? extends PersistentProjectileEntity> entityType, ServerPlayerEntity player, ItemStack itemStack, float scale, float speed, double damage, PickupPermission pickupPermission, byte penetration) {
        super(entityType, player.getPos().x, player.getPos().y + 1.5d, player.getPos().z, player.getWorld(), itemStack, itemStack);
        this.setOwner(player);
        this.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, speed, 0);
        this.pickupType = pickupPermission;
        this.setDamage(damage);
        this.setSilent(true);
        this.scale = scale;
        this.entityType = entityType;
        this.setCustomPierceLevel(penetration);
        this.setItemStack(itemStack);
    }


    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ITEM_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        setProjectileData(data, initial, scale, this.itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack itemStack() {
        return itemStack;
    }


    public void setCustomPierceLevel(byte level) {
        try {
            Method method = PersistentProjectileEntity.class.getDeclaredMethod("setPierceLevel", byte.class);
            method.setAccessible(true); // Allow access to private methods
            method.invoke(this, level);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return this.itemStack();
    }

}

