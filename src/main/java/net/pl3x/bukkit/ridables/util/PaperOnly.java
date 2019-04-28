package net.pl3x.bukkit.ridables.util;

import net.minecraft.server.v1_14_R1.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

public class PaperOnly {
    public static boolean disableCreeperLingeringEffect(World world) {
        return world.paperConfig.disableCreeperLingeringEffect;
    }

    public static boolean callEndermanAttackPlayerEvent(Enderman enderman, Player player, boolean shouldAttack) {
        EndermanAttackPlayerEvent event = new EndermanAttackPlayerEvent(enderman, player);
        event.setCancelled(!shouldAttack);
        return event.callEvent();
    }
}
