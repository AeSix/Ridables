package net.pl3x.bukkit.ridables.entity.ai.goal.slime;

import com.destroystokyo.paper.event.entity.SlimeSwimEvent;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableSlime;
import org.bukkit.entity.Slime;

public class AISlimeSwim extends PathfinderGoal {
    private final RidableSlime slime;

    public AISlimeSwim(RidableSlime slime) {
        this.slime = slime;
        a(5); // setMutexBits
        slime.getNavigation().d(true);
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (slime.isInWater() || slime.ax()) {
            return slime.canWander() && new SlimeSwimEvent((Slime) slime.getBukkitEntity()).callEvent();
        }
        return false;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // tick
    @Override
    public void e() {
        if (slime.getRandom().nextFloat() < 0.8F) {
            slime.getControllerJump().a();
        }
        ((RidableSlime.SlimeWASDController) slime.getControllerMove()).setSpeed(1.2D);
    }
}
