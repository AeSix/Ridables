package net.pl3x.bukkit.ridables.entity.controller;

import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import org.bukkit.Bukkit;

public class ControllerWASDWater extends ControllerWASD {
    public ControllerWASDWater(RidableEntity entity) {
        super(entity);
    }

    @Override
    public void tick(EntityPlayer rider) {
        float forward = getForward(rider);
        float strafe = getStrafe(rider);
        float vertical = -(rider.pitch / 90);

        if (forward < 0.0F) {
            forward *= 0.25F;
            vertical = -vertical * 0.1F;
            strafe *= 0.25F;
        } else if (forward == 0) {
            vertical = 0F;
        }

        if (isJumping(rider)) {
            RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && !event.isHandled()) {
                ridable.onSpacebar();
            }
        }

        float speed = (float) (e = ridable.getRidingSpeed());

        entity.o(speed * 0.1F);
        entity.s(vertical * 1.5F * speed);
        entity.t(strafe * speed);
        entity.r(forward * speed);

        f = getForward(entity);
        g = getStrafe(entity);
    }
}
