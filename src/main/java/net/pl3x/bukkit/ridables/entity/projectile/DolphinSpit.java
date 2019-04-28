package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLlamaSpit;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.IProjectile;
import net.minecraft.server.v1_14_R1.Material;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.MovingObjectPositionEntity;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.ProjectileHelper;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.pl3x.bukkit.ridables.entity.animal.RidableDolphin;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DolphinSpit extends EntityLlamaSpit implements IProjectile, CustomProjectile {
    private RidableDolphin dolphin;
    private EntityHuman rider;
    private NBTTagCompound nbt;
    private int life;

    public DolphinSpit(EntityTypes<? extends EntityLlamaSpit> entitytypes, World world) {
        super(entitytypes, world);
    }

    public DolphinSpit(World world, RidableDolphin dolphin, EntityHuman rider) {
        this(EntityTypes.LLAMA_SPIT, world);
        this.dolphin = dolphin;
        this.rider = rider;
        setPosition(dolphin.locX - (double) (dolphin.getWidth() + 1.0F) * 0.5D * (double) MathHelper.sin(dolphin.aK * Const.DEG2RAD_FLOAT),
                dolphin.locY + (double) dolphin.getHeadHeight() - (double) 0.5F,
                dolphin.locZ + (double) (dolphin.getWidth() + 1.0F) * 0.5D * (double) MathHelper.cos(dolphin.aK * Const.DEG2RAD_FLOAT));
    }

    @Override
    public RidableDolphin getRidable() {
        return dolphin;
    }

    @Override
    public Dolphin getMob() {
        return dolphin == null ? null : (Dolphin) dolphin.getBukkitEntity();
    }

    @Override
    public Player getRider() {
        return rider == null ? null : (Player) rider.getBukkitEntity();
    }

    @Override
    public void tick() {
        if (nbt != null) {
            restoreOwnerFromSave();
        }

        detectCollisions();

        updatePosition();

        for (int i = 0; i < 5; i++) {
            ((WorldServer) world).sendParticles(null, Particles.BUBBLE,
                    locX + random.nextFloat() / 2 - 0.25F,
                    locY + random.nextFloat() / 2 - 0.25F,
                    locZ + random.nextFloat() / 2 - 0.25F,
                    1, 0, 0, 0, 0, true);
        }

        if (!(world.a(getBoundingBox(), Material.AIR) || world.a(getBoundingBox(), Material.WATER))) {
            die(); // die if not in air or water
        }
        if (++life > 15) {
            die();
        }
    }

    private void updatePosition() {
        Vec3D mot = getMot();
        locX += mot.x;
        locY += mot.y;
        locZ += mot.z;
        yaw = (float) (MathHelper.d(mot.x, mot.z) * Const.RAD2DEG);
        pitch = (float) (MathHelper.d(mot.y, (double) MathHelper.sqrt(mot.x * mot.x + mot.z * mot.z)) * Const.RAD2DEG);
        while (pitch - lastPitch < -180.0F)
            lastPitch -= 360.0F;
        while (pitch - lastPitch >= 180.0F)
            lastPitch += 360.0F;
        while (yaw - lastYaw < -180.0F)
            lastYaw -= 360.0F;
        while (yaw - lastYaw >= 180.0F)
            lastYaw += 360.0F;
        pitch = MathHelper.g(0.2F, lastPitch, pitch);
        yaw = MathHelper.g(0.2F, lastYaw, yaw);
        if (!world.a(getBoundingBox(), Material.WATER)) {
            setMot(getMot().a((double) 0.99F));
        }
        if (!isNoGravity()) {
            setMot(getMot().add(0.0D, (double) -0.3F, 0.0D));
        }
        setPosition(locX, locY, locZ);
    }

    private void detectCollisions() {
        Vec3D mot = this.getMot();
        MovingObjectPosition rayTrace = ProjectileHelper.a(this, getBoundingBox().a(mot).g(1.0D), (entity) ->
                !entity.t() && entity != shooter && entity != rider, RayTrace.BlockCollisionOption.OUTLINE, true);

        if (rayTrace != null) {
            CraftEventFactory.callProjectileHitEvent(this, rayTrace);
            MovingObjectPosition.EnumMovingObjectType type = rayTrace.getType();
            if (type == MovingObjectPosition.EnumMovingObjectType.ENTITY && shooter != null) {
                ((MovingObjectPositionEntity) rayTrace).getEntity().damageEntity(DamageSource.a(this, rider != null ? rider : shooter).c(), 1.0F);
            } else if (type == MovingObjectPosition.EnumMovingObjectType.BLOCK && !world.isClientSide) {
                die();
            }
        }
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        Vec3D motion = (new Vec3D(x, y, z)).d().add(
                this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy,
                this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy,
                this.random.nextGaussian() * (double) 0.0075F * (double) inaccuracy
        ).a((double) speed);

        setMot(motion);

        lastYaw = yaw = (float) (MathHelper.d(motion.x, z) * Const.RAD2DEG);
        lastPitch = pitch = (float) (MathHelper.d(motion.y, (double) MathHelper.sqrt(b(motion))) * Const.RAD2DEG);
    }

    // entityInit
    @Override
    protected void initDatawatcher() {
    }

    // readEntityFromNBT
    @Override
    protected void a(NBTTagCompound nbt) {
        if (nbt.hasKeyOfType("Owner", 10)) {
            this.nbt = nbt.getCompound("Owner");
        }

    }

    // writeEntityToNBT
    @Override
    protected void b(NBTTagCompound nbttagcompound) {
        if (dolphin != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.a("OwnerUUID", dolphin.getUniqueID());
            nbttagcompound.set("Owner", nbt);
        }
    }

    // restoreOwnerFromSave
    private void restoreOwnerFromSave() {
        if (nbt != null && nbt.b("OwnerUUID")) {
            UUID uuid = nbt.a("OwnerUUID");
            for (RidableDolphin entity : world.a(RidableDolphin.class, getBoundingBox().g(15.0D))) {
                if (dolphin.getUniqueID().equals(uuid)) {
                    this.dolphin = entity;
                    break;
                }
            }
        }
        nbt = null;
    }
}
