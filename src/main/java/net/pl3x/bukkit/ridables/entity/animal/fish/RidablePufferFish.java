package net.pl3x.bukkit.ridables.entity.animal.fish;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityPufferFish;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumMonsterType;
import net.minecraft.server.v1_14_R1.IEntitySelector;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.PufferfishConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class RidablePufferFish extends EntityPufferFish implements RidableEntity {
    public static final Predicate<EntityLiving> ENEMY_MATCHER = (target) -> target != null
            && IEntitySelector.e.test(target) // canAITarget
            && target.getMonsterType() != EnumMonsterType.e; // EnumMonsterType.WATER

    private static PufferfishConfig config;

    private int spacebarCooldown = 0;

    public RidablePufferFish(EntityTypes<? extends EntityPufferFish> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new RidableCod.FishWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.PUFFERFISH;
    }

    @Override
    public PufferfishConfig getConfig() {
        return (PufferfishConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        // from EntityFish
        goalSelector.a(0, new PathfinderGoalPanic(this, 1.25D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(2, new PathfinderGoalAvoidTarget<EntityHuman>(this, EntityHuman.class, 8.0F, 1.6D, 1.4D, IEntitySelector.f::test) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new RidableCod.AIFishSwim(this));

        // from EntityPufferfish
        goalSelector.a(5, new PathfinderGoal() {
            public boolean a() { // shouldExecute
                return getRider() == null && !world.a(EntityLiving.class, getBoundingBox().g(2.0D), ENEMY_MATCHER).isEmpty();
            }

            public boolean b() { // shouldContinueExecuting
                return a(); // shouldExecute
            }

            public void c() { // startExecuting
                setPuffTimer(1);
                setDeflateTimer(0);
            }

            public void d() { // resetTask
                setPuffTimer(0);
            }
        });
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return true;
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        checkMove();
    }

    // onLivingUpdate
    @Override
    public void movementTick() {
        if (spacebarCooldown > 0) {
            spacebarCooldown--;
        }
        EntityPlayer rider = getRider();
        if (rider != null) {
            setMot(getMot().add(0.0D, 0.005D, 0.0D));
        }
        super.movementTick();
    }

    // onCollideWithPlayer
    @Override
    public void pickup(EntityHuman player) {
        // do not damage rider
        if (player != getRider()) {
            super.pickup(player);
        }
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
        if (spacebarCooldown == 0) {
            spacebarCooldown = 20;
            if (getPuffState() > 0) {
                setPuffState(0);
                setPuffTimer(0);
            } else {
                setPuffState(1);
                setPuffTimer(1);
            }
            return true;
        }
        return false;
    }

    private static Field puffTimer;
    private static Field deflateTimer;

    static {
        try {
            puffTimer = EntityPufferFish.class.getDeclaredField("c");
            puffTimer.setAccessible(true);
            deflateTimer = EntityPufferFish.class.getDeclaredField("d");
            deflateTimer.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    /**
     * Get puff timer
     *
     * @return Puff timer
     */
    public int getPuffTimer() {
        try {
            return puffTimer.getInt(this);
        } catch (IllegalAccessException ignore) {
            return 0;
        }
    }

    /**
     * Set puff timer
     *
     * @param time New puff timer
     */
    public void setPuffTimer(int time) {
        try {
            puffTimer.set(this, time);
        } catch (IllegalAccessException ignore) {
        }
    }

    /**
     * Get the deflate timer
     *
     * @return Deflate timer
     */
    public int getDeflateTimer() {
        try {
            return deflateTimer.getInt(this);
        } catch (IllegalAccessException ignore) {
            return 0;
        }
    }

    /**
     * Set the deflate timer
     *
     * @param time New deflate timer
     */
    public void setDeflateTimer(int time) {
        try {
            deflateTimer.setInt(this, time);
        } catch (IllegalAccessException ignore) {
        }
    }
}
