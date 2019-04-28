package net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube;

import com.destroystokyo.paper.event.entity.SlimeWanderEvent;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableMagmaCube;
import org.bukkit.entity.Slime;

public class AIMagmaCubeHop extends PathfinderGoal {
    private final RidableMagmaCube magmaCube;

    public AIMagmaCubeHop(RidableMagmaCube magmaCube) {
        this.magmaCube = magmaCube;
        a(5); // setMutexBits
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (magmaCube.getRider() != null) {
            return false;
        }
        return magmaCube.canWander() && new SlimeWanderEvent((Slime) magmaCube.getBukkitEntity()).callEvent();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // tick
    @Override
    public void e() {
        ((RidableMagmaCube.MagmaCubeWASDController) magmaCube.getControllerMove()).setSpeed(1.0D);
    }
}
