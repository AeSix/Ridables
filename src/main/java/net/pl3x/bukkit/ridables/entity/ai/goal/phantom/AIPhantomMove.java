package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;

public abstract class AIPhantomMove extends PathfinderGoal {
    final RidablePhantom phantom;

    public AIPhantomMove(RidablePhantom phantom) {
        this.phantom = phantom;
        a(1);
    }

    protected boolean g() {
        return phantom.orbitOffset.c(phantom.locX, phantom.locY, phantom.locZ) < 4.0D;
    }
}
