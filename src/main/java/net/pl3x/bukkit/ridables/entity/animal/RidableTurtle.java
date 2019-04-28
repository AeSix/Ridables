package net.pl3x.bukkit.ridables.entity.animal;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.BlockTurtleEgg;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.CriterionTriggers;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityExperienceOrb;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTurtle;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.Item;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreed;
import net.minecraft.server.v1_14_R1.PathfinderGoalGotoTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_14_R1.RandomPositionGenerator;
import net.minecraft.server.v1_14_R1.SoundCategory;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.StatisticList;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.TurtleConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;

public class RidableTurtle extends EntityTurtle implements RidableEntity {
    private static TurtleConfig config;

    public RidableTurtle(EntityTypes<? extends EntityTurtle> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new TurtleWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.TURTLE;
    }

    @Override
    public TurtleConfig getConfig() {
        return (TurtleConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new AIPanic(this, 1.2D));
        goalSelector.a(1, new AIMate(this, 1.0D));
        goalSelector.a(1, new AILayEgg(this, 1.0D));
        goalSelector.a(2, new AIPlayerTempt(this, 1.1D, Blocks.SEAGRASS.getItem()));
        goalSelector.a(3, new AIGoToWater(this, 1.0D));
        goalSelector.a(4, new AIGoHome(this, 1.0D));
        goalSelector.a(7, new AITravel(this, 1.0D));
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(9, new AIWander(this, 1.0D, 100));
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return true;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        return getRider() == null ? super.cW() : config.RIDING_JUMP_POWER;
    }

    @Override
    protected void mobTick() {
        K = getRider() == null ? 1.0F : config.RIDING_STEP_HEIGHT;
        if (isInWater() && getRider() != null) {
            setMot(getMot().add(0.0D, 0.005D, 0.0D));
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
        if (collectInWaterBucket(entityhuman, hand)) {
            return true; // handled
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

    private boolean hasEgg() {
        return dV();
    }

    private void setHasEgg(boolean hasEgg) {
        try {
            setHasEgg_method.invoke(this, hasEgg);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    private BlockPosition getHome() {
        try {
            return (BlockPosition) getHome_method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
        return new BlockPosition(this);
    }

    private boolean isGoingHome() {
        try {
            return (boolean) isGoingHome_method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
        return false;
    }

    private void setGoingHome(boolean goingHome) {
        try {
            setGoingHome_method.invoke(this, goingHome);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    private int getDiggingTicks() {
        try {
            return diggingTicks_field.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setDiggingTicks(int ticks) {
        try {
            diggingTicks_field.setInt(this, ticks);
        } catch (IllegalAccessException ignore) {
        }
    }

    private boolean isDigging() {
        return dW();
    }

    private void setDigging(boolean digging) {
        try {
            setDigging_method.invoke(this, digging);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    private BlockPosition getTravelPos() {
        try {
            return (BlockPosition) getTravelPos_method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
        return new BlockPosition(this);
    }

    private void setTravelPos(BlockPosition pos) {
        try {
            setTravelPos_method.invoke(this, pos);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    private void setTravelling(boolean travelling) {
        try {
            setTravelling_method.invoke(this, travelling);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    private static Method setHasEgg_method;
    private static Method getHome_method;
    private static Method isGoingHome_method;
    private static Method setGoingHome_method;
    private static Method setDigging_method;
    private static Method getTravelPos_method;
    private static Method setTravelPos_method;
    private static Method setTravelling_method;
    private static Field diggingTicks_field;

    static {
        try {
            setHasEgg_method = EntityTurtle.class.getDeclaredMethod("r", boolean.class);
            setHasEgg_method.setAccessible(true);
            getHome_method = EntityTurtle.class.getDeclaredMethod("dX");
            getHome_method.setAccessible(true);
            isGoingHome_method = EntityTurtle.class.getDeclaredMethod("dZ");
            isGoingHome_method.setAccessible(true);
            setGoingHome_method = EntityTurtle.class.getDeclaredMethod("t", boolean.class);
            setGoingHome_method.setAccessible(true);
            setDigging_method = EntityTurtle.class.getDeclaredMethod("s", boolean.class);
            setDigging_method.setAccessible(true);
            getTravelPos_method = EntityTurtle.class.getDeclaredMethod("dY");
            getTravelPos_method.setAccessible(true);
            setTravelPos_method = EntityTurtle.class.getDeclaredMethod("h", BlockPosition.class);
            setTravelPos_method.setAccessible(true);
            setTravelling_method = EntityTurtle.class.getDeclaredMethod("u", boolean.class);
            setTravelling_method.setAccessible(true);
            diggingTicks_field = EntityTurtle.class.getDeclaredField("bH");
            diggingTicks_field.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException ignore) {
        }
    }

    class TurtleWASDController extends ControllerWASD {
        private final RidableTurtle turtle;

        TurtleWASDController(RidableTurtle turtle) {
            super(turtle);
            this.turtle = turtle;
        }

        @Override
        public void tick(EntityPlayer rider) {
            //updateSpeed();
            if (turtle.isInWater()) {
                float forward = getForward(rider);
                float strafe = getStrafe(rider);
                float vertical = -(rider.pitch / 90);

                if (forward < 0.0F) {
                    forward *= 0.25F;
                    vertical = -vertical * 0.1F;
                    strafe *= 0.25F;
                } else if (forward == 0) {
                    vertical = 0F;
                }

                if (isJumping(rider)) {
                    RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled() && !event.isHandled()) {
                        ridable.onSpacebar();
                    }
                }

                float speed = (float) (e = ridable.getRidingSpeed());

                a.o(speed * 0.1F);
                a.s(vertical * 1.5F * speed);
                a.t(strafe * speed);
                a.r(forward * speed);

                f = getForward(a);
                g = getStrafe(a);
            } else {
                float forward = getForward(rider) * 0.5F;
                float strafe = getStrafe(rider) * 0.25F;
                if (forward <= 0.0F) {
                    forward *= 0.5F;
                }

                float yaw = rider.yaw;
                if (strafe != 0) {
                    if (forward == 0) {
                        yaw += strafe > 0 ? -90 : 90;
                        forward = Math.abs(strafe * 2);
                    } else {
                        yaw += strafe > 0 ? -30 : 30;
                        strafe /= 2;
                        if (forward < 0) {
                            yaw += strafe > 0 ? -110 : 110;
                            forward *= -1;
                        }
                    }
                } else if (forward < 0) {
                    yaw -= 180;
                    forward *= -1;
                }
                ((LookController) a.getControllerLook()).setOffsets(yaw - rider.yaw, 0);

                if (isJumping(rider)) {
                    RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled() && !event.isHandled() && !ridable.onSpacebar() && a.onGround) {
                        a.getControllerJump().jump();
                    }
                }

                e = ridable.getRidingSpeed() * 0.1F;

                a.o((float) e); // speed
                a.r(forward);

                f = getForward(a);
                g = getStrafe(a);
            }
        }

        @Override
        public void tick() {
            if (turtle.a(TagsFluid.WATER)) {
                turtle.setMot(turtle.getMot().add(0.0D, 0.005D, 0.0D));
            }
            if (h == ControllerMove.Operation.MOVE_TO && !turtle.getNavigation().n()) { // noPath
                double x = b - turtle.locX;
                double y = c - turtle.locY;
                double z = d - turtle.locZ;
                y /= (double) MathHelper.sqrt(x * x + y * y + z * z);
                turtle.aK = turtle.yaw = a(turtle.yaw, (float) (MathHelper.d(z, x) * Const.RAD2DEG) - 90.0F, 90.0F); // limitAngle
                float speed = (float) (e * turtle.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue());
                turtle.o(MathHelper.g(0.125F, turtle.da(), speed)); // setAIMoveSpeed
                turtle.setMot(turtle.getMot().add(0.0D, (double) turtle.da() * y * 0.1D, 0.0D));
            } else {
                turtle.o(0.0F); // setAIMoveSpeed
            }
        }
    }

    static class AIGoToWater extends PathfinderGoalGotoTarget {
        private final RidableTurtle turtle;

        private AIGoToWater(RidableTurtle turtle, double speed) {
            super(turtle, turtle.isBaby() ? 2.0D : speed, 24);
            this.turtle = turtle;
            this.f = -1;
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (turtle.getRider() != null) {
                return false;
            }
            if (turtle.isBaby() && !turtle.isInWater()) {
                return super.a();
            }
            return !turtle.isGoingHome() && !turtle.isInWater() && !turtle.hasEgg() && super.a();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return turtle.getRider() == null && !turtle.isInWater() && d <= 1200 && a(turtle.world, e); // timeoutCounter shouldMoveTo destinationBlock
        }

        // shouldMove
        @Override
        public boolean j() {
            return this.d % 160 == 0;
        }

        // shouldMoveTo
        @Override
        protected boolean a(IWorldReader world, BlockPosition pos) {
            return world.getType(pos).getBlock() == Blocks.WATER;
        }
    }

    static class AIWander extends PathfinderGoalRandomStroll {
        private final RidableTurtle turtle;

        private AIWander(RidableTurtle turtle, double speed, int chance) {
            super(turtle, speed, chance);
            this.turtle = turtle;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return turtle.getRider() == null && !a.isInWater() && !turtle.isGoingHome() && !turtle.hasEgg() && super.a();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return turtle.getRider() == null && super.b();
        }
    }

    static class AILayEgg extends PathfinderGoalGotoTarget {
        private final RidableTurtle turtle;

        AILayEgg(RidableTurtle turtle, double speed) {
            super(turtle, speed, 16);
            this.turtle = turtle;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return turtle.getRider() == null && turtle.hasEgg() && turtle.getHome().a(turtle.ch(), 9.0D) && super.a();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return turtle.getRider() == null && super.b() && turtle.hasEgg() && turtle.getHome().a(turtle.ch(), 9.0D);
        }

        // tick
        @Override
        public void e() {
            super.e();
            BlockPosition pos = new BlockPosition(turtle);
            if (!turtle.isInWater() && k()) {
                if (turtle.getDiggingTicks() < 1) {
                    turtle.setDigging(true);
                } else if (turtle.getDiggingTicks() > 200) {
                    int numOfEggs = turtle.random.nextInt(4) + 1;
                    if (!CraftEventFactory.callEntityChangeBlockEvent(turtle, e.up(), Blocks.TURTLE_EGG.getBlockData().set(BlockTurtleEgg.b, numOfEggs)).isCancelled()) {
                        turtle.world.a(null, pos, SoundEffects.ENTITY_TURTLE_LAY_EGG, SoundCategory.BLOCKS, 0.3F, 0.9F + turtle.world.random.nextFloat() * 0.2F);
                        turtle.world.setTypeAndData(e.up(), Blocks.TURTLE_EGG.getBlockData().set(BlockTurtleEgg.b, numOfEggs), 3);
                    }
                    turtle.setHasEgg(false);
                    turtle.setDigging(false);
                    turtle.setLoveTicks(600);
                }
                if (turtle.isDigging()) {
                    turtle.setDiggingTicks(turtle.getDiggingTicks() + 1);
                }
            }
        }

        // shouldMoveTo
        @Override
        protected boolean a(IWorldReader world, BlockPosition pos) {
            if (!world.isEmpty(pos.up())) {
                return false;
            } else {
                return world.getType(pos).getBlock() == Blocks.SAND;
            }
        }
    }

    static class AIMate extends PathfinderGoalBreed {
        private final RidableTurtle turtle;

        AIMate(RidableTurtle turtle, double speed) {
            super(turtle, speed);
            this.turtle = turtle;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return super.a() && !this.turtle.dV();
        }

        // spawnBaby
        @Override
        protected void g() {
            EntityPlayer player = animal.getBreedCause();
            if (player == null) {
                player = partner.getBreedCause();
            }
            if (player != null) {
                player.a(StatisticList.ANIMALS_BRED);
                CriterionTriggers.o.a(player, animal, partner, null);
            }
            turtle.setHasEgg(true);
            animal.resetLove();
            partner.resetLove();
            if (b.getGameRules().getBoolean("doMobLoot")) {
                b.addEntity(new EntityExperienceOrb(b, animal.locX, animal.locY, animal.locZ, animal.getRandom().nextInt(7) + 1));
            }

        }
    }

    static class AIPlayerTempt extends PathfinderGoal {
        private static final PathfinderTargetCondition condition = new PathfinderTargetCondition().a(10.0D).b().a();
        private final RidableTurtle turtle;
        private final double speed;
        private EntityHuman tempter;
        private int cooldown;
        private final Set<Item> temptItems;

        AIPlayerTempt(RidableTurtle turtle, double speed, Item item) {
            this.turtle = turtle;
            this.speed = speed;
            this.temptItems = Sets.newHashSet(item);
            a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (cooldown > 0) {
                --cooldown;
                return false;
            }
            if (turtle.getRider() != null) {
                return false;
            }
            tempter = turtle.world.a(condition, turtle);
            return tempter != null && (isTemptedBy(tempter.getItemInMainHand()) || isTemptedBy(tempter.getItemInOffHand()));

        }

        // resetTask
        @Override
        public void d() {
            tempter = null;
            turtle.getNavigation().o(); // clearPath
            cooldown = 100;
        }

        // tick
        @Override
        public void e() {
            turtle.getControllerLook().a(tempter, (float) (turtle.dA() + 20), (float) turtle.M()); // getHorizontalFaceSpeed getVerticalFaceSpeed
            if (turtle.h(tempter) < 6.25D) { // getDistanceSq
                turtle.getNavigation().o(); // clearPath
            } else {
                turtle.getNavigation().a(tempter, speed);
            }
        }

        private boolean isTemptedBy(ItemStack stack) {
            return temptItems.contains(stack.getItem());
        }
    }

    static class AIGoHome extends PathfinderGoal {
        private final RidableTurtle turtle;
        private final double speed;
        private boolean c;
        private int d;

        AIGoHome(RidableTurtle turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (turtle.getRider() != null) {
                return false;
            } else if (turtle.isBaby()) {
                return false;
            } else if (turtle.hasEgg()) {
                return true;
            } else if (turtle.getRandom().nextInt(700) != 0) {
                return false;
            } else {
                return !turtle.getHome().a(turtle.ch(), 64.0D);
            }
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return turtle.getRider() == null && !turtle.getHome().a(turtle.ch(), 7.0D) && !c && d <= 600;
        }

        // startExecuting
        @Override
        public void c() {
            turtle.setGoingHome(true);
            c = false;
            d = 0;
        }

        // resetTask
        @Override
        public void d() {
            turtle.setGoingHome(false);
        }

        // tick
        @Override
        public void e() {
            BlockPosition pos = turtle.getHome();
            boolean flag = pos.a(turtle.ch(), 16.0D);
            if (flag) {
                ++d;
            }
            if (turtle.getNavigation().n()) {
                Vec3D home = RandomPositionGenerator.a(turtle, 16, 3, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), (double) (Const.PI_FLOAT / 10F));
                if (home == null) {
                    home = RandomPositionGenerator.a(turtle, 8, 7, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()));
                }
                if (home != null && !flag && turtle.world.getType(new BlockPosition(home)).getBlock() != Blocks.WATER) {
                    home = RandomPositionGenerator.a(turtle, 16, 5, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()));
                }
                if (home == null) {
                    c = true;
                    return;
                }
                turtle.getNavigation().a(home.x, home.y, home.z, speed);
            }
        }
    }

    static class AITravel extends PathfinderGoal {
        private final RidableTurtle turtle;
        private final double speed;
        private boolean c;

        AITravel(RidableTurtle turtle, double speed) {
            this.turtle = turtle;
            this.speed = speed;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return turtle.getRider() == null && !turtle.isGoingHome() && !turtle.hasEgg() && turtle.isInWater();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return turtle.getRider() == null && !turtle.getNavigation().n() && !c && !turtle.isGoingHome() && !turtle.isInLove() && !turtle.hasEgg();
        }

        // startExecuting
        @Override
        public void c() {
            int x = turtle.random.nextInt(1025) - 512;
            int y = turtle.random.nextInt(9) - 4;
            int z = turtle.random.nextInt(1025) - 512;
            if ((double) y + turtle.locY > (double) (turtle.world.getSeaLevel() - 1)) {
                y = 0;
            }
            BlockPosition pos = new BlockPosition((double) x + turtle.locX, (double) y + turtle.locY, (double) z + turtle.locZ);
            turtle.setTravelPos(pos);
            turtle.setTravelling(true);
            c = false;
        }

        // resetTask
        @Override
        public void d() {
            turtle.setTravelling(false);
            super.d();
        }

        // tick
        @Override
        public void e() {
            if (turtle.getNavigation().n()) {
                BlockPosition pos = turtle.getTravelPos();
                Vec3D destination = RandomPositionGenerator.a(turtle, 16, 3, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), (double) (Const.PI_FLOAT / 10F));
                if (destination == null) {
                    destination = RandomPositionGenerator.a(turtle, 8, 7, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()));
                }
                if (destination != null) {
                    int x = MathHelper.floor(destination.x);
                    int z = MathHelper.floor(destination.z);
                    //noinspection deprecation
                    if (!turtle.world.isAreaLoaded(x - 34, 0, z - 34, x + 34, 0, z + 34)) {
                        destination = null;
                    }
                }
                if (destination == null) {
                    c = true;
                    return;
                }
                turtle.getNavigation().a(destination.x, destination.y, destination.z, speed);
            }
        }
    }

    static class AIPanic extends PathfinderGoalPanic {
        private final RidableTurtle turtle;

        AIPanic(RidableTurtle turtle, double speed) {
            super(turtle, speed);
            this.turtle = turtle;
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (turtle.getRider() != null) {
                return false;
            } else if (turtle.getLastDamager() == null && !turtle.isBurning()) {
                return false;
            } else {
                BlockPosition pos = a(turtle.world, turtle, 7, 4);
                if (pos != null) {
                    c = (double) pos.getX();
                    d = (double) pos.getY();
                    e = (double) pos.getZ();
                    return true;
                } else {
                    return this.g();
                }
            }
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return turtle.getRider() == null && super.b();
        }
    }
}
