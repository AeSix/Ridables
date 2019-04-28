package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.DataWatcherObject;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityChicken;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityOcelot;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTurtle;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreed;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalLeapAtTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalOcelotAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.PathfinderGoalTempt;
import net.minecraft.server.v1_14_R1.RecipeItemStack;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.OcelotConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class RidableOcelot extends EntityOcelot implements RidableEntity {
    private static final RecipeItemStack TEMPTATION_ITEMS = RecipeItemStack.a(Items.COD, Items.SALMON);

    private static OcelotConfig config;

    private PathfinderGoalAvoidTarget<EntityHuman> aiAvoidEntity;

    public RidableOcelot(EntityTypes<? extends EntityOcelot> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.OCELOT;
    }

    @Override
    public OcelotConfig getConfig() {
        return (OcelotConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        PathfinderGoalTempt goalTempt = new PathfinderGoalTempt(this, 0.6D, TEMPTATION_ITEMS, true) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            protected boolean g() {
                return super.g() && !isTrusting(RidableOcelot.this);
            }
        };

        setGoalTempt(this, goalTempt);

        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(3, goalTempt);
        goalSelector.a(7, new PathfinderGoalLeapAtTarget(this, 0.3F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new PathfinderGoalOcelotAttack(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(9, new PathfinderGoalBreed(this, 0.8D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(10, new PathfinderGoalRandomStrollLand(this, 0.8D, 1.0000001E-5F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(11, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 10.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityChicken>(this, EntityChicken.class, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityTurtle>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bz) {
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
        return getRider() == null ? super.cW() : config.RIDING_JUMP_POWER;
    }

    @Override
    public void mobTick() {
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
            if (config.RIDING_REQUIRESTRUST && !isTrusting()) {
                return false; // ocelot is not trusting
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

    // setupTamedAI
    @Override
    protected void dV() {
        if (aiAvoidEntity == null) {
            aiAvoidEntity = new PathfinderGoalAvoidTarget<EntityHuman>(this, EntityHuman.class, 16.0F, 0.8D, 1.33D) {
                public boolean a() { // shouldExecute
                    return getRider() == null && super.a();
                }

                public boolean b() { // shouldContinueExecuting
                    return getRider() == null && super.b();
                }
            };
        }
        goalSelector.a(aiAvoidEntity); // removeTask
        if (!isTrusting()) {
            goalSelector.a(4, aiAvoidEntity); // addTask
        }
    }

    public boolean isTrusting() {
        return isTrusting(this);
    }

    private static Field goalTempt;
    private static Field isTrusting;

    static {
        try {
            goalTempt = EntityOcelot.class.getDeclaredField("bD");
            goalTempt.setAccessible(true);
            isTrusting = EntityOcelot.class.getDeclaredField("bA");
            isTrusting.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    private static void setGoalTempt(EntityOcelot ocelot, PathfinderGoalTempt goal) {
        try {
            goalTempt.set(ocelot, goal);
        } catch (IllegalAccessException ignore) {
        }
    }

    private static boolean isTrusting(EntityOcelot ocelot) {
        try {
            //noinspection unchecked
            return ocelot.getDataWatcher().get((DataWatcherObject<Boolean>) isTrusting.get(ocelot));
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }
}
