package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;
import net.pl3x.bukkit.ridables.util.Const;

public class AIPhantomOrbitTotem extends AIPhantomOrbitPoint {
    public AIPhantomOrbitTotem(RidablePhantom phantom) {
        super(phantom);
    }

    // shouldExecute
    @Override
    public boolean a() {
        return phantom.getRider() == null && phantom.isCirclingTotem();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    @Override
    protected void updateOffset() {
        c += direction * 15.0F * Const.DEG2RAD;
        phantom.orbitOffset = (new Vec3D(phantom.totemPosition)).add((double) (d * MathHelper.cos(c)), (double) (-4.0F + verticalChange), (double) (d * MathHelper.sin(c)));
    }
}
