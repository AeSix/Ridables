package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.EnchantmentManager;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityArrow;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLightning;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityThrownTrident;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.FluidCollisionOption;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.VoxelShape;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableDrowned;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class CustomThrownTrident extends EntityThrownTrident implements CustomProjectile {
    private static Field az;
    private static Field aB;
    private static Field ax;
    private static Field despawnCounter;

    static {
        try {
            az = EntityArrow.class.getDeclaredField("az"); // inBlockState
            az.setAccessible(true);
            aB = EntityArrow.class.getDeclaredField("aB"); // ticksInAir
            aB.setAccessible(true);
            ax = EntityThrownTrident.class.getDeclaredField("ax"); // dealtDamage
            ax.setAccessible(true);
            despawnCounter = EntityArrow.class.getDeclaredField("despawnCounter");
            despawnCounter.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    private final RidableDrowned drowned;
    private final EntityPlayer rider;

    public CustomThrownTrident(EntityTypes<? extends EntityThrownTrident> entitytypes, World world) {
        super(entitytypes, world);
        this.drowned = null;
        this.rider = null;
        this.fromPlayer = PickupStatus.DISALLOWED;
    }

    public CustomThrownTrident(World world, RidableDrowned drowned, EntityPlayer rider, ItemStack itemStack) {
        super(world, rider == null ? drowned : rider, itemStack);
        this.drowned = drowned;
        this.rider = rider;
        this.fromPlayer = PickupStatus.DISALLOWED;
        setShooter(rider);
    }

    @Override
    public RidableDrowned getRidable() {
        return drowned;
    }

    @Override
    public Drowned getMob() {
        return drowned == null ? null : (Drowned) drowned.getBukkitEntity();
    }

    @Override
    public Player getRider() {
        return rider == null ? null : rider.getBukkitEntity();
    }

    @Override
    public void tick() {
        if (c > 4) { // timeInGround
            setDamageBeenDealt();
        }
        setFlag(6, bc());
        W();
        boolean flag = q();
        if (lastPitch == 0.0F && lastYaw == 0.0F) {
            float f = MathHelper.sqrt(motX * motX + motZ * motZ);
            lastYaw = yaw = (float) (MathHelper.c(motX, motZ) * Const.RAD2DEG);
            lastPitch = pitch = (float) (MathHelper.c(motY, (double) f) * Const.RAD2DEG);
        }
        BlockPosition pos = new BlockPosition(tileX, tileY, tileZ);
        IBlockData blockState = world.getType(pos);
        if (!blockState.isAir() && !flag) {
            VoxelShape voxelshape = blockState.getCollisionShape(world, pos);
            if (!voxelshape.isEmpty()) {
                for (AxisAlignedBB aabb : voxelshape.d()) {
                    if (aabb.a(pos).b(new Vec3D(locX, locY, locZ))) {
                        inGround = true;
                        break;
                    }
                }
            }
        }
        if (shake > 0) {
            --shake;
        }
        if (ao()) {
            extinguish();
        }
        if (inGround && !flag) {
            if (getInBlockState() != blockState && world.getCubes(null, getBoundingBox().g(0.05D))) {
                inGround = false;
                motX *= (double) (random.nextFloat() * 0.2F);
                motY *= (double) (random.nextFloat() * 0.2F);
                motZ *= (double) (random.nextFloat() * 0.2F);
                resetDespawnCounter();
                setTicksInAir(0);
            } else {
                f();
            }
            ++c;
        } else {
            c = 0;
            setTicksInAir(getTicksInAir() + 1);
            Vec3D vec3d = new Vec3D(locX, locY, locZ);
            Vec3D vec3d1 = new Vec3D(locX + motX, locY + motY, locZ + motZ);
            MovingObjectPosition mop = world.rayTrace(vec3d, vec3d1, FluidCollisionOption.NEVER, true, false);
            vec3d = new Vec3D(locX, locY, locZ);
            vec3d1 = new Vec3D(locX + motX, locY + motY, locZ + motZ);
            if (mop != null) {
                vec3d1 = new Vec3D(mop.pos.x, mop.pos.y, mop.pos.z);
            }
            Entity entity = a(vec3d, vec3d1);
            if (entity != null) {
                mop = new MovingObjectPosition(entity);
            }
            if (mop != null && (mop.entity == drowned || mop.entity == rider)) {
                mop = null; // do not hit self
            }
            if (mop != null && mop.entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) mop.entity;
                Entity shooter = getShooter();
                if (shooter instanceof EntityHuman && !((EntityHuman) shooter).a(entityhuman)) {
                    mop = null;
                }
            }
            if (mop != null && mop.entity != null && CraftEventFactory.callProjectileCollideEvent(this, mop).isCancelled()) {
                mop = null;
            }
            if (mop != null && !flag) {
                a(mop);
                impulse = true;
            }
            if (isCritical()) {
                for (int i = 0; i < 4; ++i) {
                    world.addParticle(Particles.h, locX + motX * (double) i / 4.0D, locY + motY * (double) i / 4.0D, locZ + motZ * (double) i / 4.0D, -motX, -motY + 0.2D, -motZ);
                }
            }
            locX += motX;
            locY += motY;
            locZ += motZ;
            float f1 = MathHelper.sqrt(motX * motX + motZ * motZ);
            if (flag) {
                yaw = (float) (MathHelper.c(-motX, -motZ) * Const.RAD2DEG);
            } else {
                yaw = (float) (MathHelper.c(motX, motZ) * Const.RAD2DEG);
            }
            pitch = (float) (MathHelper.c(motY, (double) f1) * Const.RAD2DEG);
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
            float f2 = 0.99F;
            if (isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    world.addParticle(Particles.e, locX - motX * 0.25D, locY - motY * 0.25D, locZ - motZ * 0.25D, motX, motY, motZ);
                }
                f2 = this.p();
            }
            motX *= (double) f2;
            motY *= (double) f2;
            motZ *= (double) f2;
            if (!isNoGravity() && !flag) {
                motY -= 0.05D;
            }
            setPosition(locX, locY, locZ);
            checkBlockCollisions();
        }
    }

    // findEntityOnPath
    @Nullable
    @Override
    protected Entity a(Vec3D var1, Vec3D var2) {
        return hasDamageBeenDealt() ? null : super.a(var1, var2);
    }

    // onHitEntity
    @Override
    protected void b(MovingObjectPosition mop) {
        ItemStack tridentStack = getItemStack();
        Entity hitEntity = mop.entity;
        float damage = rider == null ? RidableDrowned.config.AI_TRIDENT_DAMAGE : RidableDrowned.config.RIDING_SHOOT_DAMAGE;
        if (hitEntity instanceof EntityLiving) {
            damage += EnchantmentManager.a(tridentStack, ((EntityLiving) hitEntity).getMonsterType());
        }
        Entity shooter = getShooter();
        DamageSource source = DamageSource.a(this, (shooter == null ? this : shooter));
        setDamageBeenDealt();
        SoundEffect soundEffect = SoundEffects.ITEM_TRIDENT_HIT;
        if (hitEntity.damageEntity(source, damage) && hitEntity instanceof EntityLiving) {
            if (shooter instanceof EntityLiving) {
                EnchantmentManager.a((EntityLiving) hitEntity, shooter);
                EnchantmentManager.b((EntityLiving) shooter, hitEntity);
            }
            a((EntityLiving) hitEntity);
        }
        motX *= -0.01D;
        motY *= -0.1D;
        motZ *= -0.01D;
        float soundVolume = 1.0F;
        if (world.Y()) {
            if ((rider != null && RidableDrowned.config.RIDING_SHOOT_CHANNELING) || EnchantmentManager.h(tridentStack)) {
                BlockPosition pos = hitEntity.getChunkCoordinates();
                if (world.e(pos)) {
                    EntityLightning lightning = new EntityLightning(world, (double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, false);
                    lightning.d(shooter instanceof EntityPlayer ? (EntityPlayer) shooter : null);
                    world.strikeLightning(lightning);
                    soundEffect = SoundEffects.ITEM_TRIDENT_THUNDER;
                    soundVolume = 5.0F;
                }
            }
        }
        a(soundEffect, soundVolume, 1.0F);
    }

    // onCollideWithPlayer
    @Override
    public void d(EntityHuman player) {
        // do not pick up
    }

    private IBlockData getInBlockState() {
        try {
            return (IBlockData) az.get(this); // inBlockState
        } catch (IllegalAccessException ignore) {
        }
        return null;
    }

    private int getTicksInAir() {
        try {
            return aB.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setTicksInAir(int ticks) {
        try {
            aB.setInt(this, ticks);
        } catch (IllegalAccessException ignore) {
        }
    }

    private void resetDespawnCounter() {
        try {
            despawnCounter.setInt(this, 0);
        } catch (IllegalAccessException ignore) {
        }
    }

    private boolean hasDamageBeenDealt() {
        try {
            return ax.getBoolean(this);
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }

    private void setDamageBeenDealt() {
        try {
            ax.setBoolean(this, true);
        } catch (IllegalAccessException ignore) {
        }
    }
}
