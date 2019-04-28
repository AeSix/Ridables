package net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube;

import com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent;
import net.minecraft.server.v1_14_R1.MobEffects;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableMagmaCube;
import org.bukkit.entity.Slime;

public class AIMagmaCubeFaceRandom extends PathfinderGoal {
    private final RidableMagmaCube magmaCube;
    private float chosenYaw;
    private int timer;

    public AIMagmaCubeFaceRandom(RidableMagmaCube magmaCube) {
        this.magmaCube = magmaCube;
        a(2); // setMutexBits
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (magmaCube.getRider() != null) {
            return false;
        }
        if (!magmaCube.canWander()) {
            return false;
        }
        return magmaCube.getGoalTarget() == null && (magmaCube.onGround || magmaCube.isInWater() || magmaCube.ax() || magmaCube.hasEffect(MobEffects.LEVITATION));
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // tick
    @Override
    public void e() {
        if (--timer <= 0) {
            timer = 40 + magmaCube.getRandom().nextInt(60);
            if (!magmaCube.canWander()) {
                return;
            }
            SlimeChangeDirectionEvent event = new SlimeChangeDirectionEvent((Slime) magmaCube.getBukkitEntity(), magmaCube.getRandom().nextInt(360));
            if (!event.callEvent()) {
                return;
            }
            chosenYaw = event.getNewYaw();
        }
        ((RidableMagmaCube.MagmaCubeWASDController) magmaCube.getControllerMove()).setDirection(chosenYaw, false);
    }
}
