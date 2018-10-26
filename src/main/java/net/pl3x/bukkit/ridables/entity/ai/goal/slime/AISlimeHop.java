package net.pl3x.bukkit.ridables.entity.ai.goal.slime;

import com.destroystokyo.paper.event.entity.SlimeWanderEvent;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableSlime;
import org.bukkit.entity.Slime;

public class AISlimeHop extends PathfinderGoal {
    private final RidableSlime slime;

    public AISlimeHop(RidableSlime slime) {
        this.slime = slime;
        a(5); // setMutexBits
    }

    // shouldExecute
    public boolean a() {
        if (slime.getRider() != null) {
            return false;
        }
        return slime.canWander() && new SlimeWanderEvent((Slime) slime.getBukkitEntity()).callEvent();
    }

    // shouldContinueExecuting
    public boolean b() {
        return a();
    }

    // tick
    public void e() {
        ((RidableSlime.SlimeWASDController) slime.getControllerMove()).setSpeed(1.0D);
    }
}
