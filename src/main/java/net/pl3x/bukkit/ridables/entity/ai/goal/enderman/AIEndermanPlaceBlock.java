package net.pl3x.bukkit.ridables.entity.ai.goal.enderman;

import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.RidableEnderman;

public class AIEndermanPlaceBlock extends PathfinderGoal {
    private final RidableEnderman enderman;

    public AIEndermanPlaceBlock(RidableEnderman enderman) {
        this.enderman = enderman;
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (enderman.getRider() != null) {
            return false;
        }
        if (enderman.getCarried() == null) {
            return false;
        }
        if (!enderman.world.getGameRules().getBoolean("mobGriefing")) {
            return false;
        }
        return enderman.getRandom().nextInt(2000) == 0;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // tick
    @Override
    public void e() {
        enderman.tryPlaceBlock(
                MathHelper.floor(enderman.locX - 1.0D + enderman.getRandom().nextDouble() * 2.0D),
                MathHelper.floor(enderman.locY + enderman.getRandom().nextDouble() * 2.0D),
                MathHelper.floor(enderman.locZ - 1.0D + enderman.getRandom().nextDouble() * 2.0D));
    }
}
