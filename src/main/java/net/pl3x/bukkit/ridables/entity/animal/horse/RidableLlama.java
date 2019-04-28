package net.pl3x.bukkit.ridables.entity.animal.horse;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLlama;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityWolf;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreed;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalFollowParent;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLlamaFollow;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.PathfinderGoalTame;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.LlamaConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.event.RidableMountEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class RidableLlama extends EntityLlama implements RidableEntity {
    private static LlamaConfig config;

    public RidableLlama(EntityTypes<? extends EntityLlama> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.LLAMA;
    }

    @Override
    public LlamaConfig getConfig() {
        return (LlamaConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalTame(this, 1.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(2, new PathfinderGoalLlamaFollow(this, (double) 2.1F)); // allow this for caravans while riding
        goalSelector.a(3, new PathfinderGoalArrowAttack(this, 1.25D, 40, 20.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalPanic(this, 1.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalBreed(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.7D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new PathfinderGoalRandomLookaround(this) {
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
                if (e instanceof EntityLlama) {
                    EntityLlama llama = (EntityLlama) e;
                    if (didSpit(llama)) {
                        didSpit(llama, false);
                        return false;
                    }
                }
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityWolf>(this, EntityWolf.class, 16, false, true, (wolf) -> !((EntityWolf) wolf).isTamed()) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            protected double k() {
                return super.k() * 0.25D;
            }
        });
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return config.RIDING_RIDE_IN_WATER;
    }

    @Override
    public boolean isTamed() {
        return r(2) || (config.RIDING_BABIES && isBaby()); // getHorseWatchableBoolean
    }

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        return getRider() == null ? super.cW() : config.RIDING_JUMP_POWER;
    }

    @Override
    protected void mobTick() {
        K = getRider() == null ? 1.0F : config.RIDING_STEP_HEIGHT;
        super.mobTick();
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        //checkMove(); // not needed
    }

    // processInteract
    @Override
    public boolean a(EntityHuman entityhuman, EnumHand hand) {
        if (super.a(entityhuman, hand)) {
            return true; // handled by vanilla action
        }
        if (isBaby() && config.RIDING_BABIES && hand == EnumHand.MAIN_HAND && !entityhuman.isSneaking() && passengers.isEmpty() && !entityhuman.isPassenger()) {
            g(entityhuman); // mountTo
            return true;
        }
        return false;
    }

    // mountTo
    @Override
    public void g(EntityHuman entityhuman) {
        Player player = (Player) entityhuman.getBukkitEntity();
        if (!player.hasPermission("ridables.ride.llama")) {
            Lang.send(player, Lang.RIDE_NO_PERMISSION);
            return;
        }
        RidableMountEvent event = new RidableMountEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            super.g(entityhuman);
            entityhuman.o(false); // setJumping - fixes jump on mount
        }
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
    public boolean isLeashed() {
        return (config.RIDING_STARTS_CARAVAN && getRider() != null) || super.isLeashed();
    }

    @Override
    public Entity getLeashHolder() {
        EntityPlayer rider = getRider();
        return rider != null ? rider : super.getLeashHolder();
    }

    // hasCaravan
    @Override
    public boolean eI() {
        return (config.RIDING_STARTS_CARAVAN && getRider() != null) || super.eI();
    }

    private static Field didSpit;

    static {
        try {
            didSpit = EntityLlama.class.getDeclaredField("bM");
            didSpit.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    public static boolean didSpit(EntityLlama llama) {
        try {
            return didSpit.getBoolean(llama);
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }

    public static void didSpit(EntityLlama llama, boolean spit) {
        try {
            didSpit.setBoolean(llama, spit);
        } catch (IllegalAccessException ignore) {
        }
    }
}
