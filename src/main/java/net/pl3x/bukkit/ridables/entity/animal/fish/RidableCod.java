package net.pl3x.bukkit.ridables.entity.animal.fish;

import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityCod;
import net.minecraft.server.v1_14_R1.EntityFish;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IEntitySelector;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalFishSchool;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomSwim;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.CodConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDWater;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RidableCod extends EntityCod implements RidableEntity, RidableFishSchool {
    private static CodConfig config;

    public RidableCod(EntityTypes<? extends EntityCod> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new FishWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.COD;
    }

    @Override
    public CodConfig getConfig() {
        return (CodConfig) getType().getConfig();
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

        // from EntityFishSchool
        goalSelector.a(5, new PathfinderGoalFishSchool(this) {
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
        return true;
    }

    @Override
    public boolean isNotFollowing() {
        return dV();
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
        if (getRider() != null) {
            setMot(getMot().add(0.0D, 0.005D, 0.0D));
        }
        super.movementTick();
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

    static class FishWASDController extends ControllerWASDWater {
        private final EntityFish fish;

        FishWASDController(RidableEntity ridable) {
            super(ridable);
            this.fish = (EntityFish) ridable;
        }

        @Override
        public void tick() {
            if (fish.a(TagsFluid.WATER)) {
                fish.setMot(fish.getMot().add(0.0D, 0.005D, 0.0D));
            }
            if (h == ControllerMove.Operation.MOVE_TO && !fish.getNavigation().n()) {
                double x = b - fish.locX;
                double y = c - fish.locY;
                double z = d - fish.locZ;
                y /= (double) MathHelper.sqrt(x * x + y * y + z * z);
                fish.aK = fish.yaw = a(fish.yaw, (float) (MathHelper.d(z, x) * Const.RAD2DEG) - 90.0F, 90.0F);
                fish.o(MathHelper.g(0.125F, fish.da(), (float) (e * fish.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue())));
                fish.setMot(fish.getMot().add(0.0D, fish.da() * y * 0.1D, 0.0D));
            } else {
                fish.o(0.0F);
            }
        }
    }

    static class AIFishSwim extends PathfinderGoalRandomSwim {
        private final RidableEntity ridable;
        private final RidableFishSchool schoolFish;

        AIFishSwim(RidableEntity ridable) {
            super((EntityFish) ridable, 1.0D, 40);
            this.ridable = ridable;
            this.schoolFish = ridable instanceof RidableFishSchool ? (RidableFishSchool) ridable : null;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return ridable.getRider() == null && (schoolFish == null || schoolFish.isNotFollowing()) && super.a();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return ridable.getRider() == null && super.b();
        }
    }
}
