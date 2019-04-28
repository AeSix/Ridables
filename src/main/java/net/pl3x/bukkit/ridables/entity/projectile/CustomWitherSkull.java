package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityWitherSkull;
import net.minecraft.server.v1_14_R1.MobEffect;
import net.minecraft.server.v1_14_R1.MobEffects;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.ProjectileHelper;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.entity.boss.RidableWither;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class CustomWitherSkull extends EntityWitherSkull implements CustomProjectile {
    private RidableWither wither;
    private EntityPlayer rider;
    private int f;

    public CustomWitherSkull(EntityTypes<? extends EntityWitherSkull> entitytypes, World world) {
        super(entitytypes, world);
    }

    public CustomWitherSkull(World world, RidableWither wither, EntityPlayer rider, double x, double y, double z) {
        super(world, rider == null ? wither : rider, x, y, z);
        this.wither = wither;
        this.rider = rider;
    }

    @Override
    public RidableWither getRidable() {
        return wither;
    }

    @Override
    public Wither getMob() {
        return wither == null ? null : (Wither) wither.getBukkitEntity();
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
        setFlag(6, this.bc());
        W();
        MovingObjectPosition mop = ProjectileHelper.a(this, true, ++f >= 25, shooter);
        if (mop != null && mop.entity != null) {
            if (mop.entity == wither || mop.entity == rider) {
                mop = null; // dont hit self
            } else if (CraftEventFactory.callProjectileCollideEvent(this, mop).isCancelled()) {
                mop = null;
            }
        }
        if (mop != null) {
            a(mop);
            if (dead) {
                CraftEventFactory.callProjectileHitEvent(this, mop);
            }
        }

        float speedMod = rider == null ? RidableWither.CONFIG.AI_SHOOT_SPEED : RidableWither.CONFIG.RIDING_SHOOT_SPEED;
        locX += motX * speedMod;
        locY += motY * speedMod;
        locZ += motZ * speedMod;
        ProjectileHelper.a(this, 0.2F);
        float f = k();
        if (isInWater()) {
            for (int i = 0; i < 4; ++i) {
                world.addParticle(Particles.e, locX - motX * 0.25D, locY - motY * 0.25D, locZ - motZ * 0.25D, motX, motY, motZ);
            }
            f = 0.8F;
        }
        motX += dirX;
        motY += dirY;
        motZ += dirZ;
        motX *= (double) f;
        motY *= (double) f;
        motZ *= (double) f;
        world.addParticle(i(), locX, locY + 0.5D, locZ, 0.0D, 0.0D, 0.0D);
        setPosition(locX, locY, locZ);
    }

    @Override
    protected void a(MovingObjectPosition mop) {
        float damage;
        float heal;
        int effectDuration;
        boolean grief;
        if (rider == null) {
            damage = RidableWither.CONFIG.AI_SHOOT_DAMAGE;
            heal = RidableWither.CONFIG.AI_SHOOT_HEAL_AMOUNT;
            effectDuration = RidableWither.CONFIG.AI_SHOOT_EFFECT_DURATION;
            grief = RidableWither.CONFIG.AI_SHOOT_GRIEF;
        } else {
            damage = RidableWither.CONFIG.RIDING_SHOOT_DAMAGE;
            heal = RidableWither.CONFIG.RIDING_SHOOT_HEAL_AMOUNT;
            effectDuration = RidableWither.CONFIG.RIDING_SHOOT_EFFECT_DURATION;
            grief = RidableWither.CONFIG.RIDING_SHOOT_GRIEF;
        }
        if (mop.entity != null && damage > 0) {
            boolean didDamage;
            if (shooter != null) {
                didDamage = mop.entity.damageEntity(DamageSource.projectile(this, shooter), damage);
                if (didDamage) {
                    if (mop.entity.isAlive()) {
                        a(shooter, mop.entity);
                    }
                    shooter.heal(heal, EntityRegainHealthEvent.RegainReason.WITHER);
                }
            } else {
                didDamage = mop.entity.damageEntity(DamageSource.MAGIC, damage);
            }
            if (didDamage && mop.entity instanceof EntityLiving) {
                if (effectDuration > 0) {
                    ((EntityLiving) mop.entity).addEffect(new MobEffect(MobEffects.WITHER, 20 * effectDuration, 1), EntityPotionEffectEvent.Cause.ATTACK);
                }
            }
        }
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(getBukkitEntity(), 1.0F, false);
        world.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            world.createExplosion(this, locX, locY, locZ, event.getRadius(), event.getFire(), grief);
        }
        die();
    }
}
