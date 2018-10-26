package net.pl3x.bukkit.ridables.entity.ai.goal.slime;

import com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent;
import net.minecraft.server.v1_13_R2.MobEffects;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableSlime;
import org.bukkit.entity.Slime;

public class AISlimeFaceRandom extends PathfinderGoal {
    private final RidableSlime slime;
    private float chosenYaw;
    private int timer;

    public AISlimeFaceRandom(RidableSlime slime) {
        this.slime = slime;
        a(2); // setMutexBits
    }

    // shouldExecute
    public boolean a() {
        if (slime.getRider() != null) {
            return false;
        }
        if (!slime.canWander()) {
            return false;
        }
        return slime.getGoalTarget() == null && (slime.onGround || slime.isInWater() || slime.ax() || slime.hasEffect(MobEffects.LEVITATION));
    }

    // shouldContinueExecuting
    public boolean b() {
        return a();
    }

    // tick
    public void e() {
        if (--timer <= 0) {
            timer = 40 + slime.getRandom().nextInt(60);
            if (!slime.canWander()) {
                return;
            }
            SlimeChangeDirectionEvent event = new SlimeChangeDirectionEvent((Slime) slime.getBukkitEntity(), slime.getRandom().nextInt(360));
            if (!event.callEvent()) {
                return;
            }
            chosenYaw = event.getNewYaw();
        }
        ((RidableSlime.SlimeWASDController) slime.getControllerMove()).setDirection(chosenYaw, false);
    }
}
