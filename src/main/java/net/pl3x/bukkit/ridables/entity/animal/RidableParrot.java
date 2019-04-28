package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityParrot;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalFollowEntity;
import net.minecraft.server.v1_14_R1.PathfinderGoalFollowOwnerParrot;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalPerch;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomFly;
import net.minecraft.server.v1_14_R1.PathfinderGoalSit;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.ParrotConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlyingWithSpacebar;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RidableParrot extends EntityParrot implements RidableEntity {
    private static ParrotConfig config;

    public RidableParrot(EntityTypes<? extends EntityParrot> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ParrotWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.PARROT;
    }

    @Override
    public ParrotConfig getConfig() {
        return (ParrotConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSit = new PathfinderGoalSit(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        };

        goalSelector.a(0, new PathfinderGoalPanic(this, 1.25D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(2, goalSit);
        goalSelector.a(2, new PathfinderGoalFollowOwnerParrot(this, 1.0D, 5.0F, 1.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(2, new PathfinderGoalRandomFly(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalPerch(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalFollowEntity(this, 1.0D, 3.0F, 7.0F) {
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

    @Override
    protected void mobTick() {
        if (getRider() != null) {
            setMot(getMot().add(0.0D, bi > 0 ? 0.07D * config.RIDING_VERTICAL : 0.04704D - config.RIDING_GRAVITY, 0.0D));
        }
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
            if (config.RIDING_ONLY_OWNER_CAN_RIDE && isTamed() && getOwner() != entityhuman) {
                return false; // only owner can ride
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

    class ParrotWASDController extends ControllerWASDFlyingWithSpacebar {
        private final RidableParrot parrot;

        ParrotWASDController(RidableParrot parrot) {
            super(parrot, 0.5D);
            this.parrot = parrot;
        }

        @Override
        public void tick() {
            if (h == Operation.MOVE_TO) {
                h = Operation.WAIT;
                parrot.setNoGravity(true);
                double x = b - parrot.locX;
                double y = c - parrot.locY;
                double z = d - parrot.locZ;
                if (x * x + y * y + z * z < 2.500000277905201E-7D) {
                    parrot.s(0.0F); // setMoveVertical
                    parrot.r(0.0F); // setMoveForward
                    return;
                }
                parrot.yaw = a(parrot.yaw, (float) (MathHelper.d(z, x) * Const.RAD2DEG) - 90.0F, 10.0F); // limitAngle
                float speed = (float) (e * parrot.getAttributeInstance(parrot.onGround ? GenericAttributes.MOVEMENT_SPEED : GenericAttributes.FLYING_SPEED).getValue());
                parrot.o(speed);
                parrot.pitch = a(parrot.pitch, (float) (-(MathHelper.d(y, (double) MathHelper.sqrt(x * x + z * z)) * Const.RAD2DEG)), 10.0F); // limitAngle
                parrot.s(y > 0.0D ? speed : -speed); // setMoveVertical
            } else {
                parrot.setNoGravity(false);
                parrot.s(0.0F); // setMoveVertical
                parrot.r(0.0F); // setMoveForward
            }
        }
    }
}
