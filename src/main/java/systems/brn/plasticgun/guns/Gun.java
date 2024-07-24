package systems.brn.plasticgun.guns;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import systems.brn.plasticgun.bullets.BulletEntity;
import systems.brn.plasticgun.bullets.BulletItem;
import systems.brn.plasticgun.lib.SimpleItem;

import java.util.ArrayList;

import static systems.brn.plasticgun.PlasticGun.bullets;
import static systems.brn.plasticgun.lib.GunComponents.GUN_AMMO_COMPONENT;
import static systems.brn.plasticgun.lib.GunComponents.GUN_LOADING_COMPONENT;
import static systems.brn.plasticgun.lib.Util.*;

public class Gun extends SimpleItem implements PolymerItem {

    public final double damage;
    public final int reloadCount;
    public final int clipSize;
    public final int speed;
    public final ArrayList<Item> ammo;
    public final int caliber;

    public Gun(String path, double damage, int reloadCount, int clipSize, int speed, int caliber) {
        super(new Settings().maxCount(1).component(GUN_AMMO_COMPONENT, ItemStack.EMPTY).maxDamage(clipSize + 1), id(path));
        Item item = Registry.register(Registries.ITEM, id(path), this);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> content.add(item));
        this.damage = damage;
        this.reloadCount = reloadCount;
        this.clipSize = clipSize;
        this.speed = speed;
        ArrayList<Item> ammo = new ArrayList<>();
        for (BulletItem bullet : bullets) {
            if(bullet.caliber == caliber){
                ammo.add(bullet);
            }
        }
        this.ammo = ammo;
        this.caliber = caliber;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player) {
            use(context.getWorld(), player, context.getHand());
        }
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            int currentReload = stack.getOrDefault(GUN_LOADING_COMPONENT, 1);
            ItemStack chamber = stack.getOrDefault(GUN_AMMO_COMPONENT, ItemStack.EMPTY).copy();
            int numBullets = chamber.getCount();
            if (numBullets > 0) {
                BulletEntity bulletEntity = new BulletEntity(user.getPos(), player, chamber, user.getStackInHand(hand), this, damage, speed);
                world.spawnEntity(bulletEntity);
                chamber.decrement(1);
                stack.set(GUN_AMMO_COMPONENT, chamber);

            } else {
                ItemStack bulletStack = findBulletStack(ammo, player);
                if (bulletStack != null && !bulletStack.isEmpty() && !isCreative(player)) {
                    if (currentReload < reloadCount) {
                        stack.set(GUN_LOADING_COMPONENT, currentReload + 1);
                    } else if (currentReload == reloadCount) {
                        int addedBullets = Math.min(bulletStack.getCount(), clipSize);
                        bulletStack.decrement(addedBullets);
                        ItemStack clipStack = bulletStack.copy();
                        clipStack.setCount(Math.min(clipStack.getCount(), clipSize));
                        stack.set(GUN_AMMO_COMPONENT, clipStack);
                        stack.set(GUN_LOADING_COMPONENT, 1);
                    }
                }
                if (player.isCreative()) {
                    stack.set(GUN_AMMO_COMPONENT, new ItemStack(ammo.getFirst(), clipSize));
                }
            }
            getDefaultStack().setDamage(clipSize - numBullets);
        }
        return super.use(world, user, hand);
    }

    private static boolean isCreative(ServerPlayerEntity player) {
        return player.isCreative();
    }

}
