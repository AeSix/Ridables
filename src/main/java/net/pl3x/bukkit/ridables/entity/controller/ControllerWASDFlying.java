package net.pl3x.bukkit.ridables.entity.controller;

import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableFlyingEntity;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import org.bukkit.Bukkit;

public class ControllerWASDFlying extends ControllerWASD {
    protected final double groundSpeedModifier;
    protected final RidableFlyingEntity flyable;

    public ControllerWASDFlying(RidableEntity entity) {
        this(entity, 1D);
    }

    public ControllerWASDFlying(RidableEntity entity, double groundSpeedModifier) {
        super(entity);
        this.groundSpeedModifier = groundSpeedModifier;
        this.flyable = (RidableFlyingEntity) entity;
    }

    @Override
    public void tick(EntityPlayer rider) {
        float forward = Math.max(0, getForward(rider));
        float vertical = forward == 0 ? 0 : -(rider.pitch / 45);
        float strafe = getStrafe(rider);

        if (isJumping(rider)) {
            RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && !event.isHandled()) {
                ridable.onSpacebar();
            }
        }

        if (entity.locY >= flyable.getMaxY()) {
            Vec3D mot = entity.getMot();
            entity.setMot(mot.x, -0.05F, mot.z);
            vertical = 0;
            forward = 0;
            strafe = 0;
        }

        entity.setNoGravity(forward > 0);
        entity.o((float) (e = ridable.getRidingSpeed()));
        entity.s(vertical);
        entity.t(strafe);
        entity.r(forward);

        f = getForward(entity);
        g = getStrafe(entity);
    }
}
