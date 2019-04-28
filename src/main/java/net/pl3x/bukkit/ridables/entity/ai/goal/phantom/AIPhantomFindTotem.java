package net.pl3x.bukkit.ridables.entity.ai.goal.phantom;

import net.minecraft.server.v1_14_R1.AttributeInstance;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityEnderCrystal;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.pl3x.bukkit.ridables.entity.item.CustomEnderCrystal;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;
import org.bukkit.World;

import java.util.List;

public class AIPhantomFindTotem extends PathfinderGoal {
    private final RidablePhantom phantom;
    private EntityEnderCrystal totem;

    public AIPhantomFindTotem(RidablePhantom phantom) {
        this.phantom = phantom;
    }

    // shouldExecute
    @Override
    public boolean a() {
        if (phantom.getRider() != null) {
            return false;
        }
        if (!RidablePhantom.CONFIG.AI_ENDER_CRYSTALS_ORBIT) {
            return false;
        }
        if (phantom.getBukkitEntity().getWorld().getEnvironment() != World.Environment.NORMAL) {
            return false;
        }
        double range = maxTargetRange();
        List<EntityEnderCrystal> crystals = phantom.world.a(EntityEnderCrystal.class, phantom.getBoundingBox().grow(range, range * 2, range), CustomEnderCrystal.IS_END_CRYSTAL);
        if (crystals.isEmpty()) {
            return false;
        }
        totem = crystals.get(phantom.getRandom().nextInt(crystals.size()));
        if (phantom.h(totem) > range * range) {
            totem = null;
            return false;
        }
        if (RidablePhantom.CONFIG.AI_ENDER_CRYSTALS_DAMAGE > 0.0F && !(totem instanceof CustomEnderCrystal)) {
            CustomEnderCrystal newTotem = new CustomEnderCrystal(totem.world, totem.locX, totem.locY, totem.locZ);
            newTotem.setShowingBottom(totem.isShowingBottom());
            totem.world.addEntity(newTotem);
            totem.die();
            totem = newTotem;
        }
        return true;
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        if (phantom.getRider() != null) {
            return false;
        }
        if (totem == null || !totem.isAlive()) {
            return false;
        }
        double range = maxTargetRange();
        return phantom.h(totem) <= range * range;
    }

    // startExecuting
    @Override
    public void c() {
        phantom.setTotemPosition(new BlockPosition(totem).add(0, phantom.getRandom().nextInt(10) + 10, 0));
    }

    // resetTask
    @Override
    public void d() {
        totem = null;
        phantom.setTotemPosition(null);
        super.d();
    }

    private double maxTargetRange() {
        AttributeInstance range = phantom.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        return range == null ? 16.0D : range.getValue();
    }
}
