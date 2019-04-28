package net.pl3x.bukkit.ridables.entity.ai.goal.shulker;

import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.pl3x.bukkit.ridables.entity.monster.RidableShulker;
import net.pl3x.bukkit.ridables.entity.projectile.CustomShulkerBulletFromAI;

public class AIShulkerAttack extends PathfinderGoal {
    private final RidableShulker shulker;
    private int attackTime;

    public AIShulkerAttack(RidableShulker shulker) {
        this.shulker = shulker;
        a(3); // setMutexBits
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (shulker.getRider() != null) {
            return false;
        }
        EntityLiving target = shulker.getGoalTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        return shulker.world.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return a();
    }

    // startExecuting
    @Override
    public void c() {
        attackTime = 20;
        shulker.a(100); // updateArmorModifier
    }

    // resetTask
    @Override
    public void d() {
        shulker.a(0); // updateArmorModifier
    }

    // tick
    @Override
    public void e() {
        if (shulker.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return;
        }
        --attackTime;
        EntityLiving target = shulker.getGoalTarget();
        shulker.getControllerLook().a(target, 180.0F, 180.0F); // setLookPositionWithEntity
        if (shulker.h(target) < 400.0D) { // getDistanceSq
            if (attackTime <= 0) {
                attackTime = 20 + shulker.getRandom().nextInt(10) * 20 / 2;
                CustomShulkerBulletFromAI bullet = new CustomShulkerBulletFromAI(shulker.world, shulker, target, shulker.dy().k());
                shulker.world.addEntity(bullet); // getAttachmentFacing getAxis
                shulker.a(SoundEffects.ENTITY_SHULKER_SHOOT, 2.0F, (shulker.getRandom().nextFloat() - shulker.getRandom().nextFloat()) * 0.2F + 1.0F); // playSound
            }
        } else {
            shulker.setGoalTarget(null);
        }
    }
}
