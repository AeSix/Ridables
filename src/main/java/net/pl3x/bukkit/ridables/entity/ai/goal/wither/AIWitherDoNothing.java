package net.pl3x.bukkit.ridables.entity.ai.goal.wither;

import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.boss.RidableWither;

public class AIWitherDoNothing extends PathfinderGoal {
    private final RidableWither wither;

    public AIWitherDoNothing(RidableWither wither) {
        this.wither = wither;
        a(7); // setMutexBits
    }

    // shouldExecute
    @Override
    public boolean a() {
        return wither.getRider() != null || wither.dz() > 0; // getInvulnerableTime
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }
}
