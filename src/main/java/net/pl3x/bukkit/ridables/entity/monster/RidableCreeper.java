package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.DataWatcherObject;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityAreaEffectCloud;
import net.minecraft.server.v1_14_R1.EntityCat;
import net.minecraft.server.v1_14_R1.EntityCreeper;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityOcelot;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.Explosion;
import net.minecraft.server.v1_14_R1.MobEffect;
import net.minecraft.server.v1_14_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.PathfinderGoalSwell;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.configuration.mob.CreeperConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.PaperOnly;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.lang.reflect.Field;
import java.util.Collection;

public class RidableCreeper extends EntityCreeper implements RidableEntity {
    private static CreeperConfig config;

    private int spacebarCharge = 0;
    private int prevSpacebarCharge = 0;
    private int powerToggleDelay = 0;

    public RidableCreeper(EntityTypes<? extends EntityCreeper> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.CREEPER;
    }

    @Override
    public CreeperConfig getConfig() {
        return (CreeperConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(2, new PathfinderGoalSwell(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalAvoidTarget<EntityOcelot>(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalAvoidTarget<EntityCat>(this, EntityCat.class, 6.0F, 1.0D, 1.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalRandomLookaround(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(2, new PathfinderGoalHurtByTarget(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return config.RIDING_RIDE_IN_WATER;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        return getRider() == null ? super.cW() : (isIgnited() ? 0 : config.RIDING_JUMP_POWER);
    }

    @Override
    protected void mobTick() {
        if (powerToggleDelay > 0) {
            powerToggleDelay--;
        }
        EntityPlayer rider = getRider();
        if (rider != null) {
            K = config.RIDING_STEP_HEIGHT;
            if (ControllerWASD.getForward(rider) != 0 || ControllerWASD.getStrafe(rider) != 0) {
                spacebarCharge = 0;
                setIgnited(false);
            }
            if (spacebarCharge == prevSpacebarCharge) {
                spacebarCharge = 0;
            }
            prevSpacebarCharge = spacebarCharge;
        } else {
            K = 0.6F;
        }
        super.mobTick();
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        checkMove();
    }

    @Override
    public void tick() {
        if (isAlive()) {
            // we only want to know when to explode(), rest of logic is still in super.tick()
            int state = dW();
            int fuseTicks = getFuseTicks();
            fuseTicks += state;
            if (fuseTicks >= maxFuseTicks) {
                fuseTicks = maxFuseTicks;
                explode();
            }
            fuseTicks -= state; // dont let super.tick() double count the fuse
            setFuseTicks(fuseTicks);
        }
        super.tick();
    }

    // processInteract
    @Override
    public boolean a(EntityHuman entityhuman, EnumHand hand) {
        if (super.a(entityhuman, hand)) {
            return true; // handled by vanilla action
        }
        if (hand == EnumHand.MAIN_HAND && !entityhuman.isSneaking() && passengers.isEmpty() && !entityhuman.isPassenger()) {
            return tryRide(entityhuman, config.RIDING_SADDLE_REQUIRE, config.RIDING_SADDLE_CONSUME);
        }
        return false;
    }

    @Override
    public boolean removePassenger(Entity passenger) {
        if (passenger instanceof EntityPlayer && !passengers.isEmpty() && passenger == passengers.get(0)) {
            RidableDismountEvent event = new RidableDismountEvent(this, (Player) passenger.getBukkitEntity(), false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancellable()) {
                return false; // cancelled
            }
        }
        return super.removePassenger(passenger);
    }

    @Override
    public boolean onSpacebar() {
        if (powerToggleDelay > 0) {
            return true; // just toggled power, do not jump
        }
        spacebarCharge++;
        if (spacebarCharge > maxFuseTicks - 1) {
            spacebarCharge = 0;
            if (getRider().getBukkitEntity().hasPermission("ridables.special.creeper.powered")) {
                powerToggleDelay = 20;
                setPowered(!isPowered());
                setIgnited(false);
                return true;
            }
        }
        if (!isIgnited()) {
            EntityPlayer rider = getRider();
            if (rider != null && ControllerWASD.getForward(rider) == 0 && ControllerWASD.getStrafe(rider) == 0
                    && rider.getBukkitEntity().hasPermission("ridables.special.creeper.ignite")) {
                setIgnited(true);
                return true;
            }
        }
        return false;
    }

    /**
     * Make the creeper explode
     */
    public void explode() {
        boolean hasRider = getRider() != null;
        ExplosionPrimeEvent event;
        if (hasRider) {
            event = new ExplosionPrimeEvent(getBukkitEntity(), config.RIDING_EXPLOSION_RADIUS * (isPowered() ? 2.0F : 1.0F), config.RIDING_EXPLOSION_FIRE);
        } else {
            event = new ExplosionPrimeEvent(getBukkitEntity(), explosionRadius * (isPowered() ? 2.0F : 1.0F), false);
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            setFuseTicks(0);
            setIgnited(false);
            return;
        }

        killed = true; // isDying

        Explosion.Effect explosionEffect = (hasRider && config.RIDING_EXPLOSION_GRIEF) || world.getGameRules().getBoolean("mobGriefing") ? Explosion.Effect.DESTROY : Explosion.Effect.NONE;
        world.createExplosion(this, locX, locY, locZ, event.getRadius(), event.getFire(), explosionEffect);

        die();

        if (!hasRider || config.RIDING_EXPLOSION_LINGERING_CLOUD) {
            Collection<MobEffect> collection = getEffects();
            if (collection.isEmpty()) {
                return;
            }
            if (Ridables.isPaper() && PaperOnly.disableCreeperLingeringEffect(world)) {
                return;
            }
            EntityAreaEffectCloud cloud = new EntityAreaEffectCloud(world, locX, locY, locZ);
            cloud.setSource(this);
            cloud.setRadius(2.5F);
            cloud.setRadiusOnUse(-0.5F);
            cloud.setWaitTime(10);
            cloud.setDuration(cloud.getDuration() / 2);
            cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
            for (MobEffect mobEffect : collection) {
                cloud.a(new MobEffect(mobEffect)); // addEffect
            }
            world.addEntity(cloud);
        }
    }

    private static Field ignitedDW_field;
    private static Field fuseTicks_field;

    static {
        try {
            ignitedDW_field = EntityCreeper.class.getDeclaredField("d");
            ignitedDW_field.setAccessible(true);
            fuseTicks_field = EntityCreeper.class.getDeclaredField("fuseTicks");
            fuseTicks_field.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    public void setIgnited(boolean ignited) {
        try {
            getDataWatcher().set((DataWatcherObject<Boolean>) ignitedDW_field.get(this), ignited);
        } catch (IllegalAccessException ignore) {
        }
    }

    public int getFuseTicks() {
        try {
            return fuseTicks_field.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    public void setFuseTicks(int ticks) {
        try {
            fuseTicks_field.setInt(this, ticks);
        } catch (IllegalAccessException ignore) {
        }
    }
}
