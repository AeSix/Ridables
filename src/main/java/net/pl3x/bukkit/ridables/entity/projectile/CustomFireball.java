package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLargeFireball;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.Explosion;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.MovingObjectPositionEntity;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.ProjectileHelper;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class CustomFireball extends EntityLargeFireball implements CustomProjectile {
    private final RidableEntity ridable;
    private final EntityPlayer rider;
    private final double speed;
    private final double damage;
    private final boolean grief;
    private int ticksAlive;

    public CustomFireball(EntityTypes<? extends EntityLargeFireball> entitytypes, World world) {
        super(entitytypes, world);
        this.ridable = null;
        this.rider = null;
        this.speed = 1.0F;
        this.damage = 0;
        this.grief = false;
    }

    public CustomFireball(World world, RidableEntity ridable, EntityPlayer rider, double x, double y, double z, double speed, double damage, boolean grief) {
        super(world, rider == null ? (EntityLiving) ridable : rider, x, y, z);
        this.ridable = ridable;
        this.rider = rider;
        this.speed = speed;
        this.damage = damage;
        this.grief = grief;

        setPositionRotation(shooter.locX, shooter.locY, shooter.locZ, shooter.yaw, shooter.pitch);
        setPosition(locX, locY, locZ);

        double d3 = (double) MathHelper.sqrt(x * x + y * y + z * z);
        this.dirX = x / d3 * 0.1D;
        this.dirY = y / d3 * 0.1D;
        this.dirZ = z / d3 * 0.1D;
    }

    @Override
    public RidableEntity getRidable() {
        return ridable;
    }

    @Override
    public Mob getMob() {
        return ridable == null ? null : (Mob) ((EntityInsentient) ridable).getBukkitEntity();
    }

    @Override
    public Player getRider() {
        return rider == null ? null : rider.getBukkitEntity();
    }

    @Override
    public void tick() {
        if (shooter != null && shooter.dead || !world.isLoaded(new BlockPosition(this))) {
            die();
            return;
        }
        setFlag(6, bl());
        entityBaseTick();
        setOnFire(1);
        MovingObjectPosition mop = ProjectileHelper.a(this, true, ++ticksAlive >= 25, shooter, RayTrace.BlockCollisionOption.COLLIDER);
        if (mop.getType() != MovingObjectPosition.EnumMovingObjectType.MISS) {
            a(mop);
            if (dead) {
                CraftEventFactory.callProjectileHitEvent(this, mop);
            }
        }
        Vec3D mot = getMot();
        locX += mot.x * speed;
        locY += mot.y * speed;
        locZ += mot.z * speed;
        ProjectileHelper.a(this, 0.2F);
        float f = k();
        if (isInWater()) {
            for (int i = 0; i < 4; ++i) {
                world.addParticle(Particles.BUBBLE, locX - mot.x * 0.25D, locY - mot.y * 0.25D, locZ - mot.z * 0.25D, mot.x, mot.y, mot.z);
            }
            f = 0.8F;
        }
        setMot(mot.add(dirX, dirY, dirZ).a((double) f));
        world.addParticle(i(), locX, locY + 0.5D, locZ, 0.0D, 0.0D, 0.0D);
        setPosition(locX, locY, locZ);
    }

    // onImpact
    @Override
    protected void a(MovingObjectPosition mop) {
        if (mop.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY) {
            if (damage > 0) {
                Entity entity = ((MovingObjectPositionEntity) mop).getEntity();
                EntityLiving owner = rider != null ? rider : shooter;
                entity.damageEntity(DamageSource.fireball(this, owner), (float) damage);
                a(owner, entity);
            }
        }
        ExplosionPrimeEvent event = new ExplosionPrimeEvent((Explosive) CraftEntity.getEntity(world.getServer(), this));
        world.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            boolean flag = grief || world.getGameRules().getBoolean("mobGriefing");
            world.createExplosion(this, locX, locY, locZ, event.getRadius(), event.getFire(), flag ? Explosion.Effect.DESTROY : Explosion.Effect.NONE);
        }
        die();
    }
}
