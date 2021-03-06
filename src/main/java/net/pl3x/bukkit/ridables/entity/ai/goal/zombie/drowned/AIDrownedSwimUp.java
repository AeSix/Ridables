package net.pl3x.bukkit.ridables.entity.ai.goal.zombie.drowned;

import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.RandomPositionGenerator;
import net.minecraft.server.v1_13_R2.Vec3D;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableDrowned;

public class AIDrownedSwimUp extends PathfinderGoal {
    private final RidableDrowned drowned;
    private final double speed;
    private final int targetY;
    private boolean obstructed;

    public AIDrownedSwimUp(RidableDrowned drowned, double speed, int targetY) {
        this.drowned = drowned;
        this.speed = speed;
        this.targetY = targetY;
    }

    // shouldExecute
    @Override
    public boolean a() {
        return drowned.getRider() == null && !drowned.world.L() && drowned.isInWater() && drowned.locY < (double) (targetY - 2);
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a() && !obstructed;
    }

    // startExecuting
    @Override
    public void c() {
        drowned.setSwimmingUp(true);
        obstructed = false;
    }

    // resetTask
    @Override
    public void d() {
        drowned.setSwimmingUp(false);
    }

    // tick
    @Override
    public void e() {
        if (drowned.locY < (double) (this.targetY - 1) && (drowned.getNavigation().p() || drowned.isCloseToPathTarget())) {
            Vec3D vec3d = RandomPositionGenerator.a(drowned, 4, 8, new Vec3D(drowned.locX, (double) (targetY - 1), drowned.locZ));
            if (vec3d == null) {
                obstructed = true;
                return;
            }
            drowned.getNavigation().a(vec3d.x, vec3d.y, vec3d.z, speed);
        }
    }
}
