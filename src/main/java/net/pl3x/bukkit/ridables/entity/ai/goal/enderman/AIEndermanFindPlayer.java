package net.pl3x.bukkit.ridables.entity.ai.goal.enderman;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.pl3x.bukkit.ridables.entity.monster.RidableEnderman;
import org.bukkit.entity.Enderman;

public class AIEndermanFindPlayer extends PathfinderGoalNearestAttackableTarget<EntityHuman> {
    private final RidableEnderman enderman;

    private EntityHuman target;
    private int aggroTime;
    private int teleportTime;

    public AIEndermanFindPlayer(RidableEnderman enderman) {
        super(enderman, EntityHuman.class, false);
        this.enderman = enderman;
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (enderman.getRider() != null) {
            return false;
        }
        double maxDistance = i(); // getTargetDistance
        target = enderman.world.a(enderman.locX, enderman.locY, enderman.locZ, maxDistance, maxDistance, null, (player) -> player != null && enderman.shouldAttack(player));
        return target != null;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        if (enderman.getRider() != null) {
            return false;
        }
        if (target != null) {
            if (!enderman.shouldAttack(target)) {
                return false;
            } else {
                enderman.a(target, 10.0F, 10.0F); // faceEntity
                return true;
            }
        } else {
            return (d != null && d.isAlive()) || super.b();
        }
    }

    // startExecuting
    @Override
    public void c() {
        aggroTime = 5;
        teleportTime = 0;
    }

    // resetTask
    @Override
    public void d() {
        target = null;
        super.d();
    }

    // tick
    @Override
    public void e() {
        if (target != null) {
            if (--aggroTime <= 0) {
                d = target;
                target = null;
                super.c();
            }
        } else {
            if (d != null) {
                if (enderman.shouldAttack(d)) {
                    if (d.h(enderman) < 16.0D && new EndermanEscapeEvent((Enderman) enderman.getBukkitEntity(), EndermanEscapeEvent.Reason.STARE).callEvent()) {
                        enderman.dz(); // teleportRandomly
                    }
                    teleportTime = 0;
                } else if (d.h(enderman) > 256.0D && teleportTime++ >= 30 && enderman.teleportToEntity(d)) {
                    teleportTime = 0;
                }
            }
            super.e();
        }
    }
}
