package net.pl3x.bukkit.ridables.entity.controller;

import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class ControllerWASD extends ControllerMove {
    protected final RidableEntity ridable;
    protected final EntityInsentient entity;
    public EntityPlayer rider;
    public boolean override;

    public ControllerWASD(RidableEntity ridable) {
        super((EntityInsentient) ridable);
        this.entity = a;
        this.ridable = ridable;
    }

    private EntityPlayer updateRider() {
        if (entity.passengers.isEmpty()) {
            return rider = null;
        }
        Entity passenger = entity.passengers.get(0);
        return rider = passenger instanceof EntityPlayer ? (EntityPlayer) passenger : null;
    }

    // isUpdating
    @Override
    public boolean b() {
        return rider != null || super.b();
    }

    // tick
    @Override
    public void a() {
        if (updateRider() != null && !override) {
            tick(rider);
        } else {
            tick();
        }
    }

    public void tick() {
        super_tick();
    }

    protected void super_tick() {
        super.a();
    }

    public void tick(EntityPlayer rider) {
        float forward = getForward(rider) * 0.5F;
        float strafe = getStrafe(rider) * 0.25F;
        if (forward <= 0.0F) {
            forward *= 0.5F;
        }

        float yaw = rider.yaw;
        if (strafe != 0) {
            if (forward == 0) {
                yaw += strafe > 0 ? -90 : 90;
                forward = Math.abs(strafe * 2);
            } else {
                yaw += strafe > 0 ? -30 : 30;
                strafe /= 2;
                if (forward < 0) {
                    yaw += strafe > 0 ? -110 : 110;
                    forward *= -1;
                }
            }
        } else if (forward < 0) {
            yaw -= 180;
            forward *= -1;
        }
        ((LookController) entity.getControllerLook()).setOffsets(yaw - rider.yaw, 0);

        if (isJumping(rider)) {
            RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && !event.isHandled() && !ridable.onSpacebar() && entity.onGround) {
                entity.getControllerJump().jump();
            }
        }

        entity.o((float) (e = ridable.getRidingSpeed()));
        entity.r(forward);

        f = getForward(entity);
        g = getStrafe(entity);
    }

    private static Field jumping;

    static {
        try {
            jumping = EntityLiving.class.getDeclaredField("jumping");
            jumping.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if entity has their jump flag toggled on
     * <p>
     * This is true for players when they are pressing the spacebar
     *
     * @param entity Living entity to check
     * @return True if jump flag is toggled on
     */
    public static boolean isJumping(EntityLiving entity) {
        try {
            return jumping.getBoolean(entity);
        } catch (IllegalAccessException ignore) {
            return false;
        }
    }

    /**
     * Get an entity's strafe modifier
     *
     * @param entity Living entity
     * @return Strafe modifier
     */
    public static float getStrafe(EntityLiving entity) {
        return entity.bb; // was bh
    }

    /**
     * Get an entity's vertical modifier
     *
     * @param entity Living entity
     * @return Vertical modifier
     */
    public static float getVertical(EntityLiving entity) {
        return entity.bc; // was bi
    }

    /**
     * Get an entity's forward modifier
     *
     * @param entity Living entity
     * @return Forward modifier
     */
    public static float getForward(EntityLiving entity) {
        return entity.bd; // was bj
    }
}
