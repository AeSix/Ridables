package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.HeightMap;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;

public class AIPhantomPickAttack extends PathfinderGoal {
    private final RidablePhantom phantom;
    private int ticks;

    public AIPhantomPickAttack(RidablePhantom phantom) {
        this.phantom = phantom;
    }

    // shouldExecute
    @Override
    public boolean a() {
        return phantom.getRider() == null && phantom.canAttack() && PathfinderGoalTarget.a(phantom, phantom.getGoalTarget(), false, false);
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // startExecuting
    @Override
    public void c() {
        ticks = 10;
        phantom.phase = RidablePhantom.AttackPhase.CIRCLE;
        update();
    }

    // resetTask
    @Override
    public void d() {
        phantom.orbitPosition = phantom.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, phantom.orbitPosition).up(10 + phantom.getRandom().nextInt(20));
    }

    // tick
    @Override
    public void e() {
        if (phantom.phase == RidablePhantom.AttackPhase.CIRCLE) {
            --ticks;
            if (ticks <= 0 && phantom.canAttack()) {
                phantom.phase = RidablePhantom.AttackPhase.SWOOP;
                update();
                ticks = (8 + phantom.getRandom().nextInt(4)) * 20;
                phantom.a(SoundEffects.ENTITY_PHANTOM_SWOOP, 10.0F, 0.95F + phantom.getRandom().nextFloat() * 0.1F);
            }
        }
    }

    private void update() {
        phantom.orbitPosition = (new BlockPosition(phantom.getGoalTarget())).up(20 + phantom.getRandom().nextInt(20));
        if (phantom.orbitPosition.getY() < phantom.world.getSeaLevel()) {
            phantom.orbitPosition = new BlockPosition(phantom.orbitPosition.getX(), phantom.world.getSeaLevel() + 1, phantom.orbitPosition.getZ());
        }
    }
}
