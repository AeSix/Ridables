package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;
import net.pl3x.bukkit.ridables.util.Const;

public class AIPhantomOrbitPoint extends AIPhantomMove {
    protected float c;
    protected float d;
    protected float verticalChange;
    protected float direction;

    public AIPhantomOrbitPoint(RidablePhantom phantom) {
        super(phantom);
    }

    // shouldExecute
    @Override
    public boolean a() {
        return (phantom.getGoalTarget() == null || phantom.phase == RidablePhantom.AttackPhase.CIRCLE) && !phantom.isCirclingTotem();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // startExecuting
    @Override
    public void c() {
        d = 5.0F + phantom.getRandom().nextFloat() * 10.0F;
        verticalChange = -4.0F + phantom.getRandom().nextFloat() * 9.0F;
        direction = phantom.getRandom().nextBoolean() ? 1.0F : -1.0F;
        updateOffset();
    }

    // tick
    @Override
    public void e() {
        if (phantom.getRandom().nextInt(350) == 0) {
            verticalChange = -4.0F + phantom.getRandom().nextFloat() * 9.0F;
        }

        if (phantom.getRandom().nextInt(250) == 0) {
            ++d;
            if (d > 15.0F) {
                d = 5.0F;
                direction = -direction;
            }
        }

        if (phantom.getRandom().nextInt(450) == 0) {
            c = phantom.getRandom().nextFloat() * 2.0F * Const.PI_FLOAT;
            updateOffset();
        }

        if (g()) {
            updateOffset();
        }

        if (phantom.orbitOffset.y < phantom.locY && !phantom.world.isEmpty((new BlockPosition(phantom)).down(1))) {
            verticalChange = Math.max(1.0F, verticalChange);
            updateOffset();
        }

        if (phantom.orbitOffset.y > phantom.locY && !phantom.world.isEmpty((new BlockPosition(phantom)).up(1))) {
            verticalChange = Math.min(-1.0F, verticalChange);
            updateOffset();
        }

    }

    protected void updateOffset() {
        if (BlockPosition.ZERO.equals(phantom.orbitPosition)) {
            phantom.orbitPosition = new BlockPosition(phantom);
        }

        c += direction * 15.0F * Const.DEG2RAD;
        phantom.orbitOffset = (new Vec3D(phantom.orbitPosition)).add((double) (d * MathHelper.cos(c)), (double) (-4.0F + verticalChange), (double) (d * MathHelper.sin(c)));
    }
}
