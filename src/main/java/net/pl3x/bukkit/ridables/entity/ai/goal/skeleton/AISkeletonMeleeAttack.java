package net.pl3x.bukkit.ridables.entity.ai.goal.skeleton;

import net.minecraft.server.v1_14_R1.EntityCreature;
import net.minecraft.server.v1_14_R1.EntitySkeletonAbstract;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.pl3x.bukkit.ridables.entity.RidableEntity;

public class AISkeletonMeleeAttack extends PathfinderGoalMeleeAttack {
    private final RidableEntity ridable;
    private final EntitySkeletonAbstract skeleton;

    public AISkeletonMeleeAttack(RidableEntity ridable, double speed, boolean useLongMemory) {
        super((EntityCreature) ridable, speed, useLongMemory);
        this.ridable = ridable;
        this.skeleton = (EntitySkeletonAbstract) ridable;
    }

    // shouldExecute
    @Override
    public boolean a() {
        return ridable.getRider() == null && super.a();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return ridable.getRider() == null && super.b();
    }

    // startExecuting
    @Override
    public void c() {
        super.c();
        skeleton.s(true); // setSwingingArms
    }

    // resetTask
    @Override
    public void d() {
        super.d();
        skeleton.s(false); // setSwingingArms
    }
}
