package net.pl3x.bukkit.ridables.entity.ai.goal.iron_golem;

import net.minecraft.server.v1_13_R2.PathfinderGoalOfferFlower;
import net.pl3x.bukkit.ridables.entity.animal.RidableIronGolem;

public class AIIronGolemOfferFlower extends PathfinderGoalOfferFlower {
    private final RidableIronGolem golem;

    public AIIronGolemOfferFlower(RidableIronGolem golem) {
        super(golem);
        this.golem = golem;
    }

    // shouldExecute
    @Override
    public boolean a() {
        return golem.getRider() == null && super.a();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return golem.getRider() == null && super.b();
    }
}
