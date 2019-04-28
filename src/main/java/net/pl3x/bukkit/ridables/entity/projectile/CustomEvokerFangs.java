package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.EntityEvokerFangs;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.entity.monster.RidableEvoker;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

public class CustomEvokerFangs extends EntityEvokerFangs implements CustomProjectile {
    private static Field a;
    private static Field b;
    private static Field c;

    static {
        try {
            a = EntityEvokerFangs.class.getDeclaredField("a");
            a.setAccessible(true);
            b = EntityEvokerFangs.class.getDeclaredField("b");
            b.setAccessible(true);
            c = EntityEvokerFangs.class.getDeclaredField("c");
            c.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    private final RidableEvoker evoker;
    private final EntityPlayer rider;

    public CustomEvokerFangs(EntityTypes<? extends EntityEvokerFangs> entitytypes, World world) {
        super(entitytypes, world);
        this.evoker = null;
        this.rider = null;
    }

    public CustomEvokerFangs(World world, double x, double y, double z, float rotationYaw, int warmupDelayTicks, RidableEvoker evoker, EntityPlayer rider) {
        super(world, x, y, z, rotationYaw, warmupDelayTicks, rider == null ? evoker : rider);
        this.evoker = evoker;
        this.rider = rider;
    }

    @Override
    public RidableEvoker getRidable() {
        return evoker;
    }

    @Override
    public Evoker getMob() {
        return evoker == null ? null : (Evoker) evoker.getBukkitEntity();
    }

    @Override
    public Player getRider() {
        return rider == null ? null : rider.getBukkitEntity();
    }

    @Override
    public void tick() {
        setFlag(6, this.bc());
        W();
        int warmupDelayTicks = getWarmupDelayTicks() - 1;
        setWarmupDelayTicks(warmupDelayTicks);
        if (warmupDelayTicks < 0) { // warmupDelayTicks
            if (warmupDelayTicks == -8) {
                List<EntityLiving> list = world.a(EntityLiving.class, getBoundingBox().grow(0.2D, 0.0D, 0.2D));
                for (EntityLiving entity : list) {
                    damage(entity); // damage
                }
            }
            if (!hasSentSpikeEvent()) { // sentSpikeEvent
                world.broadcastEntityEffect(this, (byte) 4);
                setSentSpikeEvent();
            }
            int lifeTicks = getLifeTicks() - 1;
            setLifeTicks(lifeTicks);
            if (lifeTicks < 0) { // lifeTicks
                die();
            }
        }
    }

    private void damage(EntityLiving target) {
        if (target == rider || target == evoker) {
            return; // do not hit self
        }
        EntityLiving owner = getOwner();
        if (target.isAlive() && !target.bl() && target != owner) {
            if (owner == null) {
                CraftEventFactory.entityDamage = this;
                target.damageEntity(DamageSource.MAGIC, rider == null ? RidableEvoker.CONFIG.AI_FANGS_DAMAGE : RidableEvoker.CONFIG.RIDING_FANGS_DAMAGE);
                CraftEventFactory.entityDamage = null;
            } else {
                if (owner.r(target)) {
                    return;
                }
                target.damageEntity(DamageSource.c(this, owner), rider == null ? RidableEvoker.CONFIG.AI_FANGS_DAMAGE : RidableEvoker.CONFIG.RIDING_FANGS_DAMAGE);
            }
        }
    }

    private int getWarmupDelayTicks() {
        try {
            return a.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setWarmupDelayTicks(int ticks) {
        try {
            a.setInt(this, ticks);
        } catch (IllegalAccessException ignore) {
        }
    }

    private boolean hasSentSpikeEvent() {
        try {
            return b.getBoolean(this);
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }

    private void setSentSpikeEvent() {
        try {
            b.setBoolean(this, true);
        } catch (IllegalAccessException ignore) {
        }
    }

    private int getLifeTicks() {
        try {
            return c.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setLifeTicks(int ticks) {
        try {
            c.setInt(this, ticks);
        } catch (IllegalAccessException ignore) {
        }
    }
}
