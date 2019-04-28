package net.pl3x.bukkit.ridables.listener;

import net.pl3x.bukkit.ridables.data.Bucket;
import net.pl3x.bukkit.ridables.entity.RidableType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WaterBucketListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlaceCreature(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.COD_BUCKET) {
            return; // not a valid creature bucket
        }

        PlayerInventory inv = event.getPlayer().getInventory();

        // get the bucket used
        ItemStack itemStack = inv.getItemInMainHand();
        EquipmentSlot hand = EquipmentSlot.HAND;
        Bucket bucket = Bucket.getBucket(itemStack);
        if (bucket == null) {
            itemStack = inv.getItemInOffHand();
            hand = EquipmentSlot.OFF_HAND;
            bucket = Bucket.getBucket(itemStack);
            if (bucket == null) {
                return; // not a valid creature bucket
            }
        }

        RidableType ridable = RidableType.getRidableType(bucket.getEntityType());
        if (ridable == null) {
            return; // not a valid creature
        }

        // spawn the creature
        Entity entity = ridable.spawn(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation());
        if (entity == null) {
            return; // could not spawn entity
        }

        // handle the bucket in hand
        itemStack.setAmount(Math.max(0, itemStack.getAmount() - 1));
        if (itemStack.getAmount() <= 0) {
            // replace with empty bucket
            if (hand == EquipmentSlot.HAND) {
                inv.setItemInMainHand(new ItemStack(Material.BUCKET));
            } else {
                inv.setItemInOffHand(new ItemStack(Material.BUCKET));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnCodFish(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.COD) {
            return; // not a cod
        }

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            return; // not from a bucket
        }

        if (Bucket.isFromBucket(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
