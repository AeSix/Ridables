package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityFox;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityPolarBear;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalFollowParent;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.configuration.mob.PolarBearConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RidablePolarBear extends EntityPolarBear implements RidableEntity {
    private static PolarBearConfig config;

    public RidablePolarBear(EntityTypes<? extends EntityPolarBear> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.POLAR_BEAR;
    }

    @Override
    public PolarBearConfig getConfig() {
        return (PolarBearConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.25D, true) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            public void d() { // resetTask
                setStanding(false);
                super.d();
            }

            protected void a(EntityLiving target, double distance) { // checkAndPerformAttack
                double reach = a(target);
                if (distance <= reach && b <= 0) { // attackTick
                    b = 20; // attackTick
                    C(target); // attackEntityAsMob
                    setStanding(false);
                } else if (distance <= reach * 2.0D) {
                    if (b <= 0) { // attackTick
                        setStanding(false);
                        b = 20; // attackTick
                    }

                    if (b <= 10) { // attackTick
                        setStanding(true);
                        dV(); // playWarningSound
                    }
                } else {
                    b = 20; // attackTick
                    setStanding(false);
                }
            }

            protected double a(EntityLiving target) { // getAttackReachSq
                return (double) (4.0F + target.getWidth());
            }
        });
        goalSelector.a(1, new PathfinderGoalPanic(this, 2.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && (isBaby() || isBurning()) && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.25D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(7, new PathfinderGoalRandomLookaround(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalHurtByTarget(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            public void c() { // startExecuting
                super.c();
                if (isBaby()) {
                    g();
                    d();
                }
            }

            protected void a(EntityInsentient bear, EntityLiving target) { // setEntityAttackTarget
                if (bear instanceof EntityPolarBear && !bear.isBaby()) {
                    super.a(bear, target);
                }
            }
        });
        targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, 20, true, true, null) {
            public boolean a() { // shouldExecute
                if (getRider() != null || isBaby()) {
                    return false;
                }
                if (super.a()) {
                    List<EntityPolarBear> nearbyBears = world.a(EntityPolarBear.class, getBoundingBox().grow(8.0D, 4.0D, 8.0D));
                    for (EntityPolarBear bear : nearbyBears) {
                        if (bear.isBaby()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            protected double k() { // getTargetDistance
                return super.k() * 0.5D;
            }
        });
        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<EntityFox>(this, EntityFox.class, 10, true, true, null) {
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
        return getRider() == null ? super.cW() : (isStanding() ? 0 : config.RIDING_JUMP_POWER);
    }

    @Override
    protected void mobTick() {
        K = getRider() == null ? 0.6F : config.RIDING_STEP_HEIGHT;
        super.mobTick();
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        checkMove();
    }

    // processInteract
    @Override
    public boolean a(EntityHuman entityhuman, EnumHand hand) {
        if (super.a(entityhuman, hand)) {
            return true; // handled by vanilla action
        }
        if (hand == EnumHand.MAIN_HAND && !entityhuman.isSneaking() && passengers.isEmpty() && !entityhuman.isPassenger()) {
            if (!config.RIDING_BABIES && isBaby()) {
                return false; // do not ride babies
            }
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
        if (config.RIDING_STAND_ON_SPACEBAR && !isStanding()) {
            EntityPlayer rider = getRider();
            if (rider != null && ControllerWASD.getForward(rider) == 0 && ControllerWASD.getStrafe(rider) == 0) {
                setStanding(true);
                a(SoundEffects.ENTITY_POLAR_BEAR_WARNING, 1.0F, 1.0F); // playSound
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        setStanding(false);
                    }
                }.runTaskLater(Ridables.getInstance(), 20); // stop standing in 1 second
                return true;
            }
        }
        return false;
    }

    private boolean isStanding() {
        return dW();
    }

    private void setStanding(boolean standing) {
        r(standing);
    }
}
