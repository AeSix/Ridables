package net.pl3x.bukkit.ridables.listener;

import net.minecraft.server.v1_14_R1.EnumHand;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.event.RidableClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ClickListener implements Listener {
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // dont fire twice
        }

        EquipmentSlot hand = EquipmentSlot.HAND;
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                hand = EquipmentSlot.OFF_HAND;
            case LEFT_CLICK_BLOCK:
            case LEFT_CLICK_AIR:
                break;
            default:
                return; // only handle click events (not physical)
        }

        Entity vehicle = event.getPlayer().getVehicle();
        if (vehicle == null) {
            return; // not riding
        }

        RidableEntity ridable = RidableType.getRidable(vehicle);
        if (ridable == null) {
            return; // not ridable
        }

        RidableClickEvent clickEvent = new RidableClickEvent(ridable, event.getClickedBlock(), event.getBlockFace(), hand);
        Bukkit.getPluginManager().callEvent(clickEvent);
        if (clickEvent.isCancelled() || clickEvent.isHandled()) {
            if (clickEvent.isHandled()) {
                event.setCancelled(true);
            }
            return;
        }

        if (ridable.onClick()) {
            event.setCancelled(true);
            return;
        }

        boolean cancel = false;
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
                cancel = ridable.onClick(event.getClickedBlock(), event.getBlockFace(), EnumHand.MAIN_HAND);
                break;
            case RIGHT_CLICK_BLOCK:
                cancel = ridable.onClick(event.getClickedBlock(), event.getBlockFace(), EnumHand.OFF_HAND);
                break;
            case LEFT_CLICK_AIR:
                cancel = ridable.onClick(EnumHand.MAIN_HAND);
                break;
            case RIGHT_CLICK_AIR:
                cancel = ridable.onClick(EnumHand.OFF_HAND);
                break;
        }

        if (cancel) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // dont fire twice
        }

        Entity vehicle = event.getPlayer().getVehicle();
        if (vehicle == null) {
            return; // not riding
        }

        Entity clicked = event.getRightClicked();
        if (clicked == vehicle) {
            return; // clicked own vehicle
        }

        RidableEntity ridable = RidableType.getRidable(vehicle);
        if (ridable == null) {
            return; // not ridable
        }

        RidableClickEvent clickEvent = new RidableClickEvent(ridable, clicked, EquipmentSlot.OFF_HAND);
        Bukkit.getPluginManager().callEvent(clickEvent);
        if (clickEvent.isCancelled() || clickEvent.isHandled()) {
            if (clickEvent.isHandled()) {
                event.setCancelled(true);
            }
            return;
        }

        if (ridable.onClick() || ridable.onClick(clicked, EnumHand.OFF_HAND)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeftClickEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return; // not a player
        }

        Entity vehicle = event.getDamager().getVehicle();
        if (vehicle == null) {
            return; // not riding
        }

        Entity clicked = event.getEntity();
        if (clicked == vehicle) {
            return; // clicked own vehicle
        }

        RidableEntity ridable = RidableType.getRidable(vehicle);
        if (ridable == null) {
            return; // not ridable
        }

        RidableClickEvent clickEvent = new RidableClickEvent(ridable, clicked, EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(clickEvent);
        if (clickEvent.isCancelled() || clickEvent.isHandled()) {
            if (clickEvent.isHandled()) {
                event.setCancelled(true);
            }
            return;
        }

        if (ridable.onClick() || ridable.onClick(clicked, EnumHand.MAIN_HAND)) {
            event.setCancelled(true);
        }
    }
}
