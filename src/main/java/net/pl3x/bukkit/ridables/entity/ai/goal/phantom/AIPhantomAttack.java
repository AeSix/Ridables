package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalTarget;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.List;

public class AIPhantomAttack extends PathfinderGoal {
    private RidablePhantom phantom;
    private int ticks;

    public AIPhantomAttack(RidablePhantom phantom) {
        this.phantom = phantom;
        ticks = 20;
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (phantom.getRider() != null) {
            return false;
        }
        if (!phantom.canAttack()) {
            return false;
        }
        if (ticks > 0) {
            --ticks;
            return false;
        } else {
            ticks = 60;
            AxisAlignedBB axisalignedbb = phantom.getBoundingBox().grow(16.0D, 64.0D, 16.0D);
            List<EntityHuman> list = phantom.world.a(EntityHuman.class, axisalignedbb);
            if (!list.isEmpty()) {
                list.sort((e1, e2) -> e1.locY > e2.locY ? -1 : 1);
                for (EntityHuman entityhuman : list) {
                    if (phantom.canAttack() && PathfinderGoalTarget.a(phantom, entityhuman, false, false)) {
                        phantom.setGoalTarget(entityhuman, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return phantom.canAttack() && PathfinderGoalTarget.a(phantom, phantom.getGoalTarget(), false, false);
    }
}
