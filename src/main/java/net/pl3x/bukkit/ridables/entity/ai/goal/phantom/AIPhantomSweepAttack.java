package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;

public class AIPhantomSweepAttack extends AIPhantomMove {
    public AIPhantomSweepAttack(RidablePhantom phantom) {
        super(phantom);
    }

    // shouldExecute
    @Override
    public boolean a() {
        return phantom.getRider() == null && phantom.canAttack() && phantom.getGoalTarget() != null && phantom.phase == RidablePhantom.AttackPhase.SWOOP;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        if (phantom.getRider() != null) {
            return false;
        }
        if (!phantom.canAttack()) {
            return false;
        }
        EntityLiving target = phantom.getGoalTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof EntityHuman && (((EntityHuman) target).isSpectator() || ((EntityHuman) target).u())) {
            return false;
        }
        return a();
    }

    // startExecuting
    @Override
    public void c() {
    }

    // resetTask
    @Override
    public void d() {
        phantom.setGoalTarget(null);
        phantom.phase = RidablePhantom.AttackPhase.CIRCLE;
    }

    // tick
    @Override
    public void e() {
        EntityLiving entityliving = phantom.getGoalTarget();
        phantom.orbitOffset = new Vec3D(entityliving.locX, entityliving.locY + (double) entityliving.length * 0.5D, entityliving.locZ);
        if (phantom.getBoundingBox().g(0.20000000298023224D).c(entityliving.getBoundingBox())) {
            phantom.B(entityliving);
            phantom.phase = RidablePhantom.AttackPhase.CIRCLE;
            phantom.world.triggerEffect(1039, new BlockPosition(phantom), 0);
        } else if (phantom.positionChanged || phantom.hurtTicks > 0) {
            phantom.phase = RidablePhantom.AttackPhase.CIRCLE;
        }
    }
}
