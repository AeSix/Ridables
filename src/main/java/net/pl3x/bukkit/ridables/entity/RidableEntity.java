package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.CriterionTriggers;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.Item;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.MobConfig;
import net.pl3x.bukkit.ridables.data.Bucket;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.event.RidableMountEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public interface RidableEntity {
    /**
     * Get the RidableType of this entity
     *
     * @return RidableType
     */
    RidableType getType();

    /**
     * Get the mob configuration
     *
     * @return Mob configuration
     */
    MobConfig getConfig();

    /**
     * Get the rider of this entity
     * <p>
     * Only the first passenger (index 0) is considered the rider in this context
     *
     * @return Current rider, otherwise null
     */
    default EntityPlayer getRider() {
        ControllerMove controller = ((EntityInsentient) this).getControllerMove();
        return controller instanceof ControllerWASD ? ((ControllerWASD) controller).rider : null;
    }

    double getRidingSpeed();

    /**
     * Try to mount player to this ridable.
     *
     * @param entityhuman   Player to add as passenger
     * @param requireSaddle Saddle required in hand
     * @param consumeSaddle Saddle in hand is consumed
     * @return True if action was handled
     */
    default boolean tryRide(EntityHuman entityhuman, boolean requireSaddle, boolean consumeSaddle) {
        ItemStack itemstack = entityhuman.b(EnumHand.MAIN_HAND);
        Item item = itemstack == null ? null : itemstack.getItem();
        if (item != null && (item == Items.BOW || item == Items.TRIDENT)) {
            return false; // not handled - do not ride if holding bow/trident
        }
        Player player = (Player) entityhuman.getBukkitEntity();
        if (requireSaddle && (item == null || item != Items.SADDLE)) {
            itemstack = entityhuman.b(EnumHand.OFF_HAND);
            if (itemstack == null || item != Items.SADDLE) {
                Lang.send(player, Lang.RIDE_REQUIRES_SADDLE);
                return false; // not handled - saddle is required
            }
        }
        if (!player.hasPermission("ridables.ride." + getType().getName())) {
            Lang.send(player, Lang.RIDE_NO_PERMISSION);
            return true; // handled (no perms)
        }
        RidableMountEvent mountEvent = new RidableMountEvent(this, player);
        Bukkit.getPluginManager().callEvent(mountEvent);
        if (mountEvent.isCancelled()) {
            return true; // handled (plugin cancelled)
        }
        if (requireSaddle && consumeSaddle) {
            itemstack.subtract(1);
        }
        entityhuman.startRiding((EntityInsentient) this);
        entityhuman.o(false); // setJumping - fixes jump on mount
        return true; // handled (mounted)
    }

    /**
     * Internal method used to fire PlayerMoveEvent when ridable was moved by rider
     * <p>
     * <b>DO NOT</b> call this method yourself! It could potentially break other
     * plugins that are listening to that event and/or add to the lag!
     * <p>
     * This method is disabled by default for all mobs.
     */
    default void checkMove() {
        if (!Config.RIDING_ENABLE_MOVE_EVENT) {
            return; // feature disabled
        }
        if (getRider() == null) {
            return; // no rider
        }
        EntityInsentient entity = (EntityInsentient) this;
        if (entity.locX == entity.lastX && entity.locY == entity.lastY && entity.locZ == entity.lastZ) {
            return; // did not move
        }
        World world = entity.getBukkitEntity().getWorld();
        Location to = new Location(world, entity.locX, entity.locY, entity.locZ);
        Location from = new Location(world, entity.lastX, entity.lastY, entity.lastZ);
        PlayerMoveEvent event = new PlayerMoveEvent(getRider().getBukkitEntity(), from, to);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !to.equals(event.getTo())) {
            entity.ejectPassengers();
        }
    }

    /**
     * Collect entity in a water bucket
     *
     * @param entityhuman Player collecting the mob
     * @param hand        Hand used to collect
     * @return True if action was handled, otherwise false
     */
    default boolean collectInWaterBucket(EntityHuman entityhuman, EnumHand hand) {
        Bucket bucket = getType().getWaterBucket();
        if (bucket == null) {
            return false; // no water bucket defined for type
        }
        EntityInsentient entity = (EntityInsentient) this;
        ItemStack itemstack = entityhuman.b(hand);
        if (itemstack.getItem() != Items.WATER_BUCKET) {
            return false; // not holding water bucket
        }
        Player player = (Player) entityhuman.getBukkitEntity();
        if (!player.hasPermission("ridables.collect." + getType().getName())) {
            Lang.send(player, Lang.COLLECT_NO_PERMISSION);
            return true; // handled (no perms)
        }
        ItemStack bucketItem = CraftItemStack.asNMSCopy(bucket.getItemStack());
        entity.a(SoundEffects.ITEM_BUCKET_FILL_FISH, 1.0F, 1.0F); // playSound
        itemstack.subtract(1);
        CriterionTriggers.j.a((EntityPlayer) entityhuman, bucketItem); // filled_bucket achievement
        if (itemstack.isEmpty()) {
            entityhuman.a(hand, bucketItem);
        } else if (!entityhuman.inventory.pickup(bucketItem)) {
            entityhuman.drop(bucketItem, false);
        }
        entity.die();
        return true; // handled (collected)
    }

    /**
     * This method is called when the spacebar is pressed by the current rider
     * <p>
     * This is used internally for triggering spacebar events other than jumping. It is advised to not use this method
     *
     * @return True if spacebar was handled
     */
    default boolean onSpacebar() {
        return false;
    }

    /**
     * This method is called when the current rider clicks on an entity
     * <p>
     * This is used internally for triggering click events on the creature. It is advised to not use this method
     *
     * @param entity The Entity clicked on
     * @param hand   Hand used to click
     * @return True if click was handled
     */
    default boolean onClick(Entity entity, EnumHand hand) {
        return false;
    }

    /**
     * This method is called when the current rider clicks on a block
     * <p>
     * This is used internally for triggering click events on the creature. It is advised to not use this method
     *
     * @param block     The Block clicked on
     * @param blockFace Face of black clicked
     * @param hand      Hand used to click
     * @return True if click was handled
     */
    default boolean onClick(Block block, BlockFace blockFace, EnumHand hand) {
        return false;
    }

    /**
     * This method is called when the current rider clicks in the air
     * <p>
     * This is used internally for triggering click events on the creature. It is advised to not use this method
     *
     * @param hand Hand used to click
     * @return True if click was handled
     */
    default boolean onClick(EnumHand hand) {
        return false;
    }

    /**
     * This method is called when the current rider performs any type of click action
     * <p>
     * This is called <b>before</b> all other click action methods are fired.
     * <p>
     * This is used internally for triggering click events on the creature. It is advised to not use this method
     *
     * @return True if click was handled
     */
    default boolean onClick() {
        return false;
    }
}
