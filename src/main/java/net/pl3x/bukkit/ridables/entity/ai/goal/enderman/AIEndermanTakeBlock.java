package net.pl3x.bukkit.ridables.entity.ai.goal.enderman;

import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.RidableEnderman;

public class AIEndermanTakeBlock extends PathfinderGoal {
    private final RidableEnderman enderman;

    public AIEndermanTakeBlock(RidableEnderman enderman) {
        this.enderman = enderman;
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (enderman.getRider() != null) {
            return false;
        }
        if (enderman.getCarried() != null) {
            return false;
        }
        if (!enderman.world.getGameRules().getBoolean("mobGriefing")) {
            return false;
        }
        return enderman.getRandom().nextInt(20) == 0;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // tick
    @Override
    public void e() {
        enderman.tryTakeBlock(
                MathHelper.floor(enderman.locX - 2.0D + enderman.getRandom().nextDouble() * 4.0D),
                MathHelper.floor(enderman.locY + enderman.getRandom().nextDouble() * 3.0D),
                MathHelper.floor(enderman.locZ - 2.0D + enderman.getRandom().nextDouble() * 4.0D));
    }
}
