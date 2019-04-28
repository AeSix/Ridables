package net.pl3x.bukkit.ridables.entity.controller;

import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import org.bukkit.Bukkit;

public class ControllerWASDFlyingWithSpacebar extends ControllerWASDFlying {
    public ControllerWASDFlyingWithSpacebar(RidableEntity entity) {
        super(entity);
    }

    public ControllerWASDFlyingWithSpacebar(RidableEntity entity, double groundSpeedModifier) {
        super(entity, groundSpeedModifier);
    }

    @Override
    public void tick(EntityPlayer rider) {
        float forward = getForward(rider) * 0.5F;
        float strafe = getStrafe(rider) * 0.25F;
        float vertical = 0;

        if (forward <= 0.0F) {
            forward *= 0.5F;
        }

        float speed = (float) ridable.getRidingSpeed();

        RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
        Bukkit.getPluginManager().callEvent(event);
        if (isJumping(rider) && !event.isCancelled() && !event.isHandled() && !ridable.onSpacebar()) {
            entity.setNoGravity(true);
            vertical = speed;
        } else {
            entity.setNoGravity(false);
        }

        if (entity.locY >= flyable.getMaxY()) {
            Vec3D mot = entity.getMot();
            entity.setMot(mot.x, mot.y - 0.2D, mot.z);
            vertical = -speed;
            forward = 0;
            strafe = 0;
        }

        if (entity.onGround) {
            speed *= groundSpeedModifier;
        }

        entity.o((float) (e = speed));
        entity.s(vertical);
        entity.t(strafe);
        entity.r(forward);

        f = getForward(entity);
        g = getStrafe(entity);

        Vec3D mot = entity.getMot();
        if (mot.y > 0.2D) {
            entity.setMot(mot.x, 0.2D, mot.z);
        } else if (mot.y > 0D) {
            entity.setMot(mot.x, mot.y * 0.8D, mot.z);
        }
    }
}
