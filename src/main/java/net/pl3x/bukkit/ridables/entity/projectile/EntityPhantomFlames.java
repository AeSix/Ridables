package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.DamageSource;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityLlamaSpit;
import net.minecraft.server.v1_13_R1.IProjectile;
import net.minecraft.server.v1_13_R1.Material;
import net.minecraft.server.v1_13_R1.MathHelper;
import net.minecraft.server.v1_13_R1.MovingObjectPosition;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.Particles;
import net.minecraft.server.v1_13_R1.Vec3D;
import net.minecraft.server.v1_13_R1.World;
import net.minecraft.server.v1_13_R1.WorldServer;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.EntityRidablePhantom;

public class EntityPhantomFlames extends EntityLlamaSpit implements IProjectile {
    private EntityRidablePhantom phantom;
    private EntityHuman rider;
    private int life;

    public EntityPhantomFlames(World world) {
        super(world);
        setSize(1.0F, 1.0F);
    }

    public EntityPhantomFlames(World world, EntityRidablePhantom phantom, EntityHuman rider) {
        this(world);
        this.phantom = phantom;
        this.rider = rider;
        setPosition(phantom.locX - (double) (phantom.width + 1.0F) * 0.5D * (double) MathHelper.sin(phantom.aQ * 0.017453292F),
                phantom.locY + (double) phantom.getHeadHeight() - 0.5000000149011612D,
                phantom.locZ + (double) (phantom.width + 1.0F) * 0.5D * (double) MathHelper.cos(phantom.aQ * 0.017453292F));
        setInvisible(true);
    }

    public void tick() {
        detectCollisions();

        updatePosition();

        for (int i = 0; i < 5; i++) {
            ((WorldServer) world).sendParticles(null, Particles.y,
                    locX + random.nextFloat() / 2 - 0.25F,
                    locY + random.nextFloat() / 2 - 0.25F,
                    locZ + random.nextFloat() / 2 - 0.25F,
                    0, motX, motY, motZ, 0.1);
        }

        if (!world.a(getBoundingBox(), Material.AIR)) {
            die();
        }
        if (++life > 15) {
            die();
        }
    }

    private void updatePosition() {
        locX += motX;
        locY += motY;
        locZ += motZ;
        yaw = (float) (MathHelper.c(motX, motZ) * 57.2957763671875D);
        pitch = (float) (MathHelper.c(motY, (double) MathHelper.sqrt(motX * motX + motZ * motZ)) * 57.2957763671875D);
        while (pitch - lastPitch < -180.0F)
            lastPitch -= 360.0F;
        while (pitch - lastPitch >= 180.0F)
            lastPitch += 360.0F;
        while (yaw - lastYaw < -180.0F)
            lastYaw -= 360.0F;
        while (yaw - lastYaw >= 180.0F)
            lastYaw += 360.0F;
        pitch = lastPitch + (pitch - lastPitch) * 0.2F;
        yaw = lastYaw + (yaw - lastYaw) * 0.2F;
        if (!world.a(getBoundingBox(), Material.WATER)) {
            motX *= 0.9900000095367432D;
            motY *= 0.9900000095367432D;
            motZ *= 0.9900000095367432D;
        }
        if (!isNoGravity()) {
            motY -= 0.02999999865889549D;
        }
        setPosition(locX, locY, locZ);
    }

    private void detectCollisions() {
        double reach = 10.0;

        Vec3D minVec = new Vec3D(locX, locY, locZ);
        Vec3D maxVec = new Vec3D(locX + motX * reach, locY + motY * reach, locZ + motZ * reach);

        MovingObjectPosition rayTraceResult = world.rayTrace(minVec, maxVec);

        minVec = new Vec3D(locX, locY, locZ);
        maxVec = new Vec3D(locX + motX * reach, locY + motY * reach, locZ + motZ * reach);

        if (rayTraceResult != null) {
            maxVec = new Vec3D(rayTraceResult.pos.x, rayTraceResult.pos.y, rayTraceResult.pos.z);
        }

        EntityLiving hitEntity = getHitEntity(minVec, maxVec);
        if (hitEntity != null && rider != null) {
            hitEntity.damageEntity(DamageSource.a(this, rider).c(), Config.PHANTOM_SHOOT_DAMAGE);
            hitEntity.setOnFire(100);
            die();
        }
    }

    private EntityLiving getHitEntity(Vec3D vec3d, Vec3D vec3d1) {
        EntityLiving entity = null;
        double d0 = 0.0D;
        for (Entity entity1 : world.getEntities(this, getBoundingBox().b(motX, motY, motZ).g(1.0D))) {
            if (entity1 != phantom && entity1 != rider && entity1 instanceof EntityLiving) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox().g(0.5D);
                MovingObjectPosition movingobjectposition = axisalignedbb.b(vec3d, vec3d1);
                if (movingobjectposition != null) {
                    double d1 = vec3d.distanceSquared(movingobjectposition.pos);
                    if (d1 < d0 || d0 == 0.0D) {
                        entity = (EntityLiving) entity1;
                        d0 = d1;
                    }
                }
            }
        }
        return entity;
    }

    public void shoot(double d0, double d1, double d2, float f, float f1) {
        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        motX = d0 = (d0 / (double) f2) * f;
        motY = d1 = (d1 / (double) f2) * f;
        motZ = d2 = (d2 / (double) f2) * f;
        lastYaw = yaw = (float) (MathHelper.c(d0, d2) * 57.2957763671875D);
        lastPitch = pitch = (float) (MathHelper.c(d1, (double) MathHelper.sqrt(d0 * d0 + d2 * d2)) * 57.2957763671875D);
    }

    // entityInit
    protected void x_() {
    }

    // readEntityFromNBT
    protected void a(NBTTagCompound nbt) {
    }

    // writeEntityToNBT
    protected void b(NBTTagCompound nbttagcompound) {
    }
}