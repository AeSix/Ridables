package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityBlaze;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySnowball;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.animal.RidableSnowGolem;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.List;

public class CustomSnowball extends EntitySnowball implements CustomProjectile {
    private RidableSnowGolem snowGolem;
    private EntityPlayer rider;

    public CustomSnowball(World world) {
        super(world);
    }

    public CustomSnowball(World world, RidableSnowGolem snowGolem) {
        super(world, snowGolem);
    }

    public CustomSnowball(World world, RidableSnowGolem snowGolem, EntityPlayer rider, double x, double y, double z) {
        super(world, rider == null ? snowGolem : rider);
        this.snowGolem = snowGolem;
        this.rider = rider;
        setPosition(x, y, z);
    }

    @Override
    public RidableEntity getRidable() {
        return snowGolem;
    }

    @Override
    public Mob getMob() {
        return snowGolem.getBukkitMob();
    }

    @Override
    public Player getRider() {
        return rider == null ? null : rider.getBukkitEntity();
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        float distance = MathHelper.sqrt(x * x + y * y + z * z);
        motX = ((x / (double) distance) + random.nextGaussian() * (double) 0.0075F * (double) inaccuracy) * (double) speed;
        motY = ((y / (double) distance) + random.nextGaussian() * (double) 0.0075F * (double) inaccuracy) * (double) speed;
        motZ = ((z / (double) distance) + random.nextGaussian() * (double) 0.0075F * (double) inaccuracy) * (double) speed;
        lastYaw = yaw = (float) (MathHelper.c(x, z) * Const.RAD2DEG); // atan2
        lastPitch = pitch = (float) (MathHelper.c(y, (double) MathHelper.sqrt(x * x + z * z)) * Const.RAD2DEG); // atan2
    }

    @Override
    public void tick() {
        N = locX; // lastTickPosX
        O = locY; // lastTickPosY
        P = locZ; // lastTickPosZ
        setFlag(6, bc()); // isGlowing
        W(); // baseTick
        if (shake > 0) {
            --shake;
        }
        if (inGround) {
            inGround = false;
            motX *= (double) (random.nextFloat() * 0.2F);
            motY *= (double) (random.nextFloat() * 0.2F);
            motZ *= (double) (random.nextFloat() * 0.2F);
        }
        Vec3D vec3d1 = new Vec3D(locX, locY, locZ);
        Vec3D vec3d2 = new Vec3D(locX + motX, locY + motY, locZ + motZ);
        MovingObjectPosition mop = world.rayTrace(vec3d1, vec3d2);
        vec3d1 = new Vec3D(locX, locY, locZ);
        vec3d2 = new Vec3D(locX + motX, locY + motY, locZ + motZ);
        if (mop != null) {
            vec3d2 = new Vec3D(mop.pos.x, mop.pos.y, mop.pos.z);
        }
        Entity hitEntity = null;
        List<Entity> list = world.getEntities(this, getBoundingBox().b(motX, motY, motZ).g(1.0D)); // expand grow
        double closestDistance = 0.0D;
        for (Entity entity : list) {
            if (entity == rider || entity == snowGolem) {
                continue; // do not hit self or rider
            }
            if (entity.isInteractable()) { // canBeCollidedWith
                MovingObjectPosition mop1 = entity.getBoundingBox().g((double) 0.3F).b(vec3d1, vec3d2); // grow calculateIntercept
                if (mop1 != null) {
                    double distance = vec3d1.distanceSquared(mop1.pos);
                    if (distance < closestDistance || closestDistance == 0.0D) {
                        hitEntity = entity;
                        closestDistance = distance;
                    }
                }
            }
        }
        if (hitEntity != null) {
            mop = new MovingObjectPosition(hitEntity);
        }
        if (mop != null) {
            if (mop.type == MovingObjectPosition.EnumMovingObjectType.BLOCK && world.getType(mop.getBlockPosition()).getBlock() == Blocks.NETHER_PORTAL) {
                e(mop.getBlockPosition()); // setPortal getBlockPos
            } else {
                a(mop); // onImpact
            }
        }
        locX += motX;
        locY += motY;
        locZ += motZ;
        yaw = (float) (MathHelper.c(motX, motZ) * Const.RAD2DEG);
        pitch = (float) (MathHelper.c(motY, (double) MathHelper.sqrt(motX * motX + motZ * motZ)) * Const.RAD2DEG);

        while (pitch - lastPitch < -180.0F) {
            lastPitch -= 360.0F;
        }
        while (pitch - lastPitch >= 180.0F) {
            lastPitch += 360.0F;
        }
        while (yaw - lastYaw < -180.0F) {
            lastYaw -= 360.0F;
        }
        while (yaw - lastYaw >= 180.0F) {
            lastYaw += 360.0F;
        }
        pitch = lastPitch + (pitch - lastPitch) * 0.2F;
        yaw = lastYaw + (yaw - lastYaw) * 0.2F;
        float friction = 0.99F;
        if (isInWater()) {
            for (int i = 0; i < 4; ++i) {
                world.addParticle(Particles.e, locX - motX * 0.25D, locY - motY * 0.25D, locZ - motZ * 0.25D, motX, motY, motZ);
            }
            friction = 0.8F;
        }
        motX *= (double) friction;
        motY *= (double) friction;
        motZ *= (double) friction;
        if (!isNoGravity()) {
            motY -= (double) 0.03F;
        }
        setPosition(locX, locY, locZ);
    }

    // onImpact
    @Override
    protected void a(MovingObjectPosition mop) {
        if (mop.entity != null) {
            float damage = rider == null ? RidableSnowGolem.CONFIG.AI_SHOOT_DAMAGE : RidableSnowGolem.CONFIG.RIDING_SHOOT_DAMAGE;
            if (mop.entity instanceof EntityBlaze) {
                damage = 3.0F;
            }
            mop.entity.damageEntity(DamageSource.projectile(this, getShooter()), damage);
        }
        world.broadcastEntityEffect(this, (byte) 3);
        die();
    }
}
