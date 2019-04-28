package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.BiomeBase;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockCarrots;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.DataWatcherObject;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityAgeable;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityMonster;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityRabbit;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityWolf;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.PathEntity;
import net.minecraft.server.v1_14_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreed;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalGotoTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.PathfinderGoalTempt;
import net.minecraft.server.v1_14_R1.RecipeItemStack;
import net.minecraft.server.v1_14_R1.SystemUtils;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.RabbitConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class RidableRabbit extends EntityRabbit implements RidableEntity {
    private static final RecipeItemStack TEMPTATION_ITEMS = RecipeItemStack.a(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION);

    private static RabbitConfig config;

    private boolean wasOnGround;

    public RidableRabbit(EntityTypes<? extends EntityRabbit> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new RabbitWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.RABBIT;
    }

    @Override
    public RabbitConfig getConfig() {
        return (RabbitConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalPanic(this, 2.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            public void e() { // tick
                super.e();
                RidableRabbit.this.d(b);
            }
        });
        goalSelector.a(2, new PathfinderGoalBreed(this, 0.8D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalTempt(this, 1.0D, TEMPTATION_ITEMS, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalAvoidTarget<EntityHuman>(this, EntityHuman.class, 8.0F, 2.2D, 2.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && getRabbitType() != 99 && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalAvoidTarget<EntityWolf>(this, EntityWolf.class, 10.0F, 2.2D, 2.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && getRabbitType() != 99 && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalAvoidTarget<EntityMonster>(this, EntityMonster.class, 4.0F, 2.2D, 2.2D) {
            public boolean a() { // shouldExecute
                return getRider() == null && getRabbitType() != 99 && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new PathfinderGoalGotoTarget(this, (double) 0.7F, 16) {
            private boolean wantsToRaid;
            private boolean canRaid;

            public boolean a() { // shouldExecute
                if (c <= 0) {
                    if (!world.getGameRules().getBoolean("mobGriefing")) {
                        return false;
                    }
                    canRaid = false;
                    wantsToRaid = true;
                }
                if (getRider() != null) {
                    return false;
                }
                return super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return canRaid && getRider() == null && super.b();
            }

            public void e() { // tick
                super.e();
                getControllerLook().a((double) e.getX() + 0.5D, (double) (e.getY() + 1), (double) e.getZ() + 0.5D, 10.0F, (float) M());
                if (!k()) {
                    return;
                }
                BlockPosition pos = e.up();
                IBlockData state = world.getType(pos);
                if (canRaid && state.getBlock() instanceof BlockCarrots) {
                    int age = state.get(BlockCarrots.AGE);
                    if (age == 0) {
                        if (CraftEventFactory.callEntityChangeBlockEvent(RidableRabbit.this, pos, Blocks.AIR.getBlockData()).isCancelled()) {
                            return;
                        }
                        world.setTypeAndData(pos, Blocks.AIR.getBlockData(), 2);
                        world.b(pos, true);
                    } else {
                        if (CraftEventFactory.callEntityChangeBlockEvent(RidableRabbit.this, pos, state.set(BlockCarrots.AGE, age - 1)).isCancelled()) {
                            return;
                        }
                        world.setTypeAndData(pos, state.set(BlockCarrots.AGE, age - 1), 2);
                        world.triggerEffect(2001, pos, Block.getCombinedId(state));
                    }
                    setCarrotTicks(RidableRabbit.this, 40);
                }
                canRaid = false;
                c = 10;
            }

            protected boolean a(IWorldReader world, BlockPosition pos) {
                Block block = world.getType(pos).getBlock();
                if (block == Blocks.FARMLAND && wantsToRaid && !canRaid) {
                    IBlockData state = world.getType(pos.up());
                    block = state.getBlock();
                    if (block instanceof BlockCarrots && ((BlockCarrots) block).isRipe(state)) {
                        canRaid = true;
                        return true;
                    }
                }
                return false;
            }
        });
        goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.6D) {
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
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return config.RIDING_RIDE_IN_WATER;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        if (getRider() == null) {
            if (positionChanged || moveController.b() && moveController.e() > locY + 0.5D) { // isUpdating getY
                return 0.5F; // (collided with block, or jumping to higher block)
            }
            PathEntity path = navigation.l(); // getPath
            if (path != null && path.f() < path.e()) { // getCurrentPathIndex getCurrentPathLength
                if (path.a(this).y > locY + 0.5D) { // getPosition
                    return 0.5F; // (jumping to higher block)
                }
            }
            return moveController.c() <= 0.6D ? 0.2F : 0.3F; // getSpeed (hopping around on level ground)
        }
        float forward = ControllerWASD.getForward(this);
        if (forward < 0) {
            r(forward * 2F); // setForward
        }
        return config.RIDING_JUMP_POWER;
    }

    @Override
    public void mobTick() {
        if (getRider() != null) {
            handleJumping();
            return;
        }
        super.mobTick();
    }

    private void handleJumping() {
        if (onGround) {
            ControllerJumpRabbit jumpHelper = (ControllerJumpRabbit) bt;
            if (!wasOnGround) {
                setJumping(false);
                jumpHelper.a(false); // setCanJump
            }
            if (!jumpHelper.d()) { // getIsJumping
                if (moveController.b()) { // isUpdating
                    dV(); // startJumping
                }
            } else if (!jumpHelper.d()) { // canJump
                jumpHelper.a(true); // setCanJump
            }
        }
        wasOnGround = onGround;
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
            if (!config.RIDING_RIDE_KILLER_BUNNY && getRabbitType() == 99) {
                return false; // do not ride killer bunny
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
    public RidableRabbit createChild(EntityAgeable entity) {
        int type;
        if (random.nextInt(20) == 0) {
            type = getRandomRabbitType(); // 5% new rabbit type
        } else {
            // 50/50 rabbit type from a parent
            if (entity instanceof EntityRabbit && random.nextBoolean()) {
                type = ((EntityRabbit) entity).getRabbitType();
            } else {
                type = getRabbitType();
            }
        }
        RidableRabbit baby = (RidableRabbit) EntityTypes.RABBIT.a(world);
        if (baby != null) {
            baby.setRabbitType(type);
        }
        return baby;
    }

    private int getRandomRabbitType() {
        if (config.AI_KILLER_CHANCE > 0D && config.AI_KILLER_CHANCE > random.nextDouble()) {
            return 99; // killer rabbit type
        }
        BiomeBase biome = world.getBiome(new BlockPosition(this));
        if (biome.o() == BiomeBase.Geography.DESERT) { // getCategory
            return 4; // 100% chance to be type 4 in desert (gold fur)
        }
        int chance = random.nextInt(100);
        if (biome.b() == BiomeBase.Precipitation.SNOW) { // getPrecipitation
            if (chance < 80) {
                return 1; // 80% chance to be type 1 in snow (white fur)
            } else {
                return 3; // 20% chance to be type 3 in snow (black and white fur)
            }
        }
        if (chance < 50) {
            return 0; // 50% chance to be type 0 (brown fur)
        }
        if (chance < 90) {
            return 5; // 40% chance to be type 5 (salt and pepper fur)
        } else {
            return 2; // 10% chance to be type 2 (black fur)
        }
    }

    @Override
    public void setRabbitType(int type) {
        if (type == 99) {
            getAttributeInstance(GenericAttributes.ARMOR).setValue(8.0D);
            goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.4D, true) {
                public boolean a() { // shouldExecute
                    return getRider() == null && super.a();
                }

                public boolean b() { // shouldContinueExecuting
                    return getRider() == null && super.b();
                }

                protected double a(EntityLiving target) { // getAttackReachSqr
                    return (double) (4.0F + target.getWidth());
                }
            });
            targetSelector.a(1, new PathfinderGoalHurtByTarget(this) {
                public boolean a() { // shouldExecute
                    return getRider() == null && super.a();
                }

                public boolean b() { // shouldContinueExecuting
                    return getRider() == null && super.b();
                }
            }.a(new Class[0]));
            targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true) {
                public boolean a() { // shouldExecute
                    return getRider() == null && super.a();
                }

                public boolean b() { // shouldContinueExecuting
                    return getRider() == null && super.b();
                }
            });
            targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityWolf>(this, EntityWolf.class, true) {
                public boolean a() { // shouldExecute
                    return getRider() == null && super.a();
                }

                public boolean b() { // shouldContinueExecuting
                    return getRider() == null && super.b();
                }
            });
            if (!hasCustomName()) {
                setCustomName(new ChatMessage(SystemUtils.a("entity", getKillerBunnyKey(this))));
            }
        }
        if (!hasCustomName() && config.TOAST_CHANCE > 0D && config.TOAST_CHANCE > random.nextDouble()) {
            setCustomName(new ChatMessage("Toast"));
        }
        setRabbitType(this, type);
    }

    static class RabbitWASDController extends ControllerWASD {
        private final RidableRabbit rabbit;
        private double nextJumpSpeed;

        RabbitWASDController(RidableRabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        @Override
        public void tick() {
            if (rabbit.onGround && !rabbit.jumping && !((EntityRabbit.ControllerJumpRabbit) rabbit.bt).c()) { // isJumping jumpHelper getIsJumping
                rabbit.d(0.0D); // setMovementSpeed
            } else if (b()) { // isUpdating
                rabbit.d(nextJumpSpeed); // setMovementSpeed
            }
            super_tick();
        }

        @Override
        public void a(double x, double y, double z, double speed) {
            if (rabbit.isInWater()) {
                speed = 1.5D;
            }
            super.a(x, y, z, speed);
            if (speed > 0.0D) {
                nextJumpSpeed = speed;
            }
        }
    }

    private static Field carrotTicks;
    private static Field killerBunny;
    private static Field rabbitType;

    static {
        try {
            carrotTicks = EntityRabbit.class.getDeclaredField("bG");
            carrotTicks.setAccessible(true);
            killerBunny = EntityRabbit.class.getDeclaredField("bA");
            killerBunny.setAccessible(true);
            rabbitType = EntityRabbit.class.getDeclaredField("bz");
            rabbitType.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    public static boolean isCarrotEaten(EntityRabbit rabbit) {
        try {
            return carrotTicks.getInt(rabbit) == 0;
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }

    public static void setCarrotTicks(EntityRabbit rabbit, int ticks) {
        try {
            carrotTicks.setInt(rabbit, ticks);
        } catch (IllegalAccessException ignore) {
        }
    }

    private static MinecraftKey getKillerBunnyKey(EntityRabbit rabbit) {
        try {
            return (MinecraftKey) killerBunny.get(rabbit);
        } catch (IllegalAccessException ignore) {
        }
        return new MinecraftKey("killer_bunny");
    }

    private static void setRabbitType(EntityRabbit rabbit, int type) {
        try {
            //noinspection unchecked
            rabbit.getDataWatcher().set((DataWatcherObject<Integer>) rabbitType.get(rabbit), type);
        } catch (IllegalAccessException ignore) {
        }
    }
}
