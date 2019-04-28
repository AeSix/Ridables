package net.pl3x.bukkit.ridables.listener;

import net.minecraft.server.v1_14_R1.EnumHand;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.boss.RidableEnderDragon;
import net.pl3x.bukkit.ridables.entity.monster.RidableBlaze;
import net.pl3x.bukkit.ridables.entity.monster.RidableCreeper;
import net.pl3x.bukkit.ridables.entity.monster.RidableGhast;
import net.pl3x.bukkit.ridables.entity.projectile.CustomFireball;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class RidableListener implements Listener {
    @EventHandler
    public void onClickEnderDragon(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // dont fire twice
        }

        Entity dragon = event.getRightClicked();
        if (dragon.isDead() || !dragon.isValid() || !(dragon instanceof ComplexEntityPart)) {
            return; // not a valid dragon entity
        }

        RidableEntity ridable = RidableType.getRidable(((ComplexEntityPart) dragon).getParent());
        if (ridable == null) {
            return; // ridable dragon not enabled
        }

        // processInteract on dragon
        RidableEnderDragon nmsDragon = (RidableEnderDragon) ridable;
        if (nmsDragon.a(((CraftPlayer) event.getPlayer()).getHandle(), EnumHand.MAIN_HAND)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.FIREBALL) {
            net.minecraft.server.v1_14_R1.Entity nmsEntity = ((CraftEntity) event.getDamager()).getHandle();
            if (!(nmsEntity instanceof CustomFireball)) {
                return; // not our custom fireball
            }

            CustomFireball fireball = (CustomFireball) nmsEntity;
            if (fireball.getRidable() instanceof RidableGhast) {
                if (fireball.getRider() != null) {
                    event.setDamage(EntityDamageEvent.DamageModifier.BASE, ((RidableGhast) fireball.getRidable()).getConfig().RIDING_FIREBALL_EXPLOSION_DAMAGE);
                }
            } else if (fireball.getRidable() instanceof RidableBlaze) {
                if (fireball.getRider() != null) {
                    event.setDamage(EntityDamageEvent.DamageModifier.BASE, ((RidableBlaze) fireball.getRidable()).getConfig().RIDING_SHOOT_EXPLOSION_DAMAGE);
                }
            }
        } else {
            RidableEntity ridable = RidableType.getRidable(event.getDamager());
            if (ridable == null) {
                return; // not caused by a ridable
            }

            if (ridable.getType() == RidableType.CREEPER) {
                if (ridable.getRider() != null) {
                    event.setDamage(EntityDamageEvent.DamageModifier.BASE, ((RidableCreeper) ridable).getConfig().RIDING_EXPLOSION_DAMAGE);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (Config.COMMANDS_LIST.isEmpty()) {
            return; // disabled feature
        }

        if (RidableType.getRidableType(event.getPlayer().getVehicle()) == null) {
            return; // not riding a ridable
        }

        if (Config.COMMANDS_LIST_IS_WHITELIST != Config.COMMANDS_LIST.contains(
                event.getMessage()
                        .split(" ")[0]    // ignore command arguments
                        .substring(1)     // ignore beginning slash
                        .toLowerCase())   // ignore casing
        ) {
            Lang.send(event.getPlayer(), Lang.DISABLED_COMMAND_WHILE_RIDING);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // ensure player unmounts creature so they dont glitch
        event.getEntity().leaveVehicle();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // ensure player unmounts creature so it doesn't despawn with player
        event.getPlayer().leaveVehicle();
    }
}
