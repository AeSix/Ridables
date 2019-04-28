package net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube;

import com.destroystokyo.paper.event.entity.SlimeTargetLivingEntityEvent;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableMagmaCube;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;

public class AIMagmaCubeAttack extends PathfinderGoal {
    private final RidableMagmaCube magmaCube;
    private int timer;

    public AIMagmaCubeAttack(RidableMagmaCube magmaCube) {
        this.magmaCube = magmaCube;
        a(2); // setMutexBits
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (magmaCube.getRider() != null) {
            return false;
        }
        EntityLiving target = magmaCube.getGoalTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof EntityHuman && ((EntityHuman) target).abilities.isInvulnerable) {
            return false;
        }
        return magmaCube.canWander() && new SlimeTargetLivingEntityEvent((Slime) magmaCube.getBukkitEntity(), (LivingEntity) target.getBukkitEntity()).callEvent();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        if (magmaCube.getRider() != null) {
            return false;
        }
        EntityLiving target = magmaCube.getGoalTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof EntityHuman && ((EntityHuman) target).abilities.isInvulnerable) {
            return false;
        }
        if (--timer <= 0) {
            return false;
        }
        return magmaCube.canWander() && new SlimeTargetLivingEntityEvent((Slime) magmaCube.getBukkitEntity(), (LivingEntity) target.getBukkitEntity()).callEvent();
    }

    // startExecuting
    @Override
    public void c() {
        timer = 300;
        super.c();
    }

    // resetTask
    @Override
    public void d() {
        timer = 0;
        magmaCube.setGoalTarget(null);
    }

    // tick
    @Override
    public void e() {
        magmaCube.a(magmaCube.getGoalTarget(), 10.0F, 10.0F);
        ((RidableMagmaCube.MagmaCubeWASDController) magmaCube.getControllerMove()).setDirection(magmaCube.yaw, magmaCube.canDamagePlayer());
    }
}
