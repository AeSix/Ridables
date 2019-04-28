package net.pl3x.bukkit.ridables.entity.ai.goal.shulker;

import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityShulker;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.pl3x.bukkit.ridables.entity.monster.RidableShulker;

public class AIShulkerAttackPlayer extends PathfinderGoalNearestAttackableTarget<EntityHuman> {
    private final RidableShulker shulker;

    public AIShulkerAttackPlayer(RidableShulker shulker) {
        super(shulker, EntityHuman.class, true);
        this.shulker = shulker;
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (shulker.getRider() != null) {
            return false;
        }
        if (shulker.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            return false;
        }
        return super.a();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return shulker.getRider() == null && super.b();
    }

    // getTargetableArea
    @Override
    protected AxisAlignedBB a(double distance) {
        EnumDirection direction = ((EntityShulker) e).dy(); // taskOwner getAttachmentFacing
        if (direction.k() == EnumDirection.EnumAxis.X) { // getAxis
            return e.getBoundingBox().grow(4.0D, distance, distance); // taskOwner
        }
        if (direction.k() == EnumDirection.EnumAxis.Z) { // getAxis
            return e.getBoundingBox().grow(distance, distance, 4.0D); // taskOwner
        }
        return e.getBoundingBox().grow(distance, 4.0D, distance); // taskOwner
    }
}
