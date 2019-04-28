package net.pl3x.bukkit.ridables.entity.monster.zombie;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityCreature;
import net.minecraft.server.v1_14_R1.EntityDrowned;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityIronGolem;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPigZombie;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTurtle;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityVillagerAbstract;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.GeneratorAccess;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_14_R1.PathfinderGoalGotoTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.PathfinderGoalRemoveBlock;
import net.minecraft.server.v1_14_R1.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_14_R1.RandomPositionGenerator;
import net.minecraft.server.v1_14_R1.SoundCategory;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.DrownedConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.CustomThrownTrident;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

public class RidableDrowned extends EntityDrowned implements RidableEntity {
    private static DrownedConfig config;

    private int shootCooldown = 0;
    private boolean swimmingUp;

    public RidableDrowned(EntityTypes<? extends EntityDrowned> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new DrownedWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }

        RidableZombie.setBreakDoorsGoal(this, new PathfinderGoalBreakDoor(this, difficulty -> difficulty == EnumDifficulty.HARD) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
    }

    @Override
    public RidableType getType() {
        return RidableType.DROWNED;
    }

    @Override
    public DrownedConfig getConfig() {
        return (DrownedConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        // from EntityZombie
        goalSelector.a(4, new PathfinderGoalRemoveBlock(Blocks.TURTLE_EGG, this, 1.0D, 3) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            public void a(GeneratorAccess world, BlockPosition pos) { // playBreakingSound
                world.a(null, pos, SoundEffects.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5F, 0.9F + getRandom().nextFloat() * 0.2F);
            }

            public void a(World world, BlockPosition pos) { // playBrokenSound
                world.a(null, pos, SoundEffects.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
            }

            public double h() { // getTargetDistanceSq
                return 1.14D;
            }
        });
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
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
        l();
    }

    // initExtraAI
    @Override
    protected void l() {
        goalSelector.a(1, new AIDrownedGoToWater(this, 1.0D));
        goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0D, 40, 10.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a() && getItemInMainHand().getItem() == Items.TRIDENT;
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            public void c() { // startExecuting
                super.c();
                q(true); // setArmsRaised
                RidableDrowned.this.c(EnumHand.MAIN_HAND);
            }

            public void d() { // resetTask
                super.d();
                dp();
                q(false); // setArmsRaised
            }
        });
        goalSelector.a(2, new PathfinderGoalZombieAttack(this, 1.0D, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a() && h(getGoalTarget());
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b() && h(getGoalTarget());
            }
        });
        goalSelector.a(5, new PathfinderGoalGotoTarget(this, 1.0D, 8, 2) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a() && !world.J() && isInWater() && locY >= (double) (world.getSeaLevel() - 3);
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }

            public void c() { // startExecuting
                r(false);
                navigation = RidableDrowned.this.c;
                super.c();
            }

            protected boolean a(IWorldReader world, BlockPosition pos) {
                BlockPosition up = pos.up();
                if (world.isEmpty(up) && world.isEmpty(up.up())) {
                    return world.getType(pos).a(world, pos, RidableDrowned.this);
                }
                return false;
            }
        });
        goalSelector.a(6, new AIDrownedSwimUp(this, 1.0D, this.world.getSeaLevel()));
        goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalHurtByTarget(this, EntityDrowned.class) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        }.a(EntityPigZombie.class));
        targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, 10, true, false, this::h) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<EntityVillagerAbstract>(this, EntityVillagerAbstract.class, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<EntityIronGolem>(this, EntityIronGolem.class, true) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(5, new PathfinderGoalNearestAttackableTarget<EntityTurtle>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bz) {
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
    protected void mobTick() {
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        K = getRider() == null ? 1.0F : config.RIDING_STEP_HEIGHT;
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
    public boolean onClick(org.bukkit.entity.Entity entity, EnumHand hand) {
        return handleClick();
    }

    @Override
    public boolean onClick(Block block, BlockFace blockFace, EnumHand hand) {
        return handleClick();
    }

    @Override
    public boolean onClick(EnumHand hand) {
        return handleClick();
    }

    private boolean handleClick() {
        if (shootCooldown == 0) {
            EntityPlayer rider = getRider();
            if (rider != null) {
                if (!config.RIDING_SHOOT_REQUIRE_TRIDENT || hasTrident()) {
                    return throwTrident(rider);
                }
            }
        }
        return false;
    }

    public boolean throwTrident(EntityPlayer rider) {
        shootCooldown = config.RIDING_SHOOT_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.drowned")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(25).add(new Vector(0, 3, 0));

        CustomThrownTrident trident = new CustomThrownTrident(this.world, this, rider, new ItemStack(Items.TRIDENT));
        trident.shoot(direction.getX(), direction.getY(), direction.getZ(), (float) (1.6D * config.RIDING_SHOOT_SPEED), 0);
        world.addEntity(trident);

        a(SoundEffects.ENTITY_DROWNED_SHOOT, 1.0F, 1.0F);

        return true;
    }

    /**
     * Check if this drowned has a trident in it's hands
     *
     * @return True if trident in either hand
     */
    public boolean hasTrident() {
        return getEquipment(EnumItemSlot.MAINHAND).getItem() == Items.TRIDENT ||
                getEquipment(EnumItemSlot.OFFHAND).getItem() == Items.TRIDENT;
    }

    // isSwimmingUp
    private boolean eg() {
        if (swimmingUp) {
            return true;
        } else {
            EntityLiving target = getGoalTarget();
            return target != null && target.isInWater();
        }
    }

    // setSwimmingUp
    @Override
    public void r(boolean swimmingUp) {
        this.swimmingUp = swimmingUp;
    }

    public boolean isCloseToPathTarget() {
        return ea();
    }

    static class DrownedWASDController extends ControllerWASD {
        private final RidableDrowned drowned;

        DrownedWASDController(RidableDrowned drowned) {
            super(drowned);
            this.drowned = drowned;
        }

        @Override
        public void tick() {
            EntityLiving target = drowned.getGoalTarget();
            if (drowned.eg() && drowned.isInWater()) {
                if (target != null && target.locY > drowned.locY || drowned.swimmingUp) {
                    drowned.setMot(drowned.getMot().add(0.0D, 0.002D, 0.0D));
                }
                if (h != Operation.MOVE_TO || drowned.getNavigation().n()) {
                    drowned.o(0.0F);
                    return;
                }
                double x = b - drowned.locX;
                double y = c - drowned.locY;
                double z = d - drowned.locZ;
                y /= (double) MathHelper.sqrt(x * x + y * y + z * z);
                drowned.aK = drowned.yaw = a(drowned.yaw, (float) (MathHelper.d(z, x) * Const.RAD2DEG) - 90.0F, 90.0F);
                float speed = (float) (e * drowned.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue());
                speed = MathHelper.g(0.125F, drowned.da(), speed);
                drowned.o(speed);
                drowned.setMot(drowned.getMot().add((double) speed * x * 0.005D, (double) speed * y * 0.1D, (double) speed * z * 0.005D));
            } else {
                if (!drowned.onGround) {
                    drowned.setMot(drowned.getMot().add(0.0D, -0.008D, 0.0D));
                }
                super_tick();
            }
        }
    }

    static class AIDrownedGoToWater extends PathfinderGoal {
        private final RidableEntity ridable;
        private final EntityCreature entity;
        private double x;
        private double y;
        private double z;
        private final double speed;
        private final World world;

        AIDrownedGoToWater(RidableEntity ridable, double speed) {
            this.ridable = ridable;
            this.entity = (EntityCreature) ridable;
            this.speed = speed;
            this.world = entity.world;
            a(EnumSet.of(Type.MOVE)); // setMutexBits
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (ridable.getRider() != null) {
                return false;
            }
            if (!world.J()) { // isDayTime
                return false;
            }
            if (entity.isInWater()) {
                return false;
            }
            BlockPosition pos = findWater();
            if (pos == null) {
                return false;
            } else {
                x = pos.getX();
                y = pos.getY();
                z = pos.getZ();
                return true;
            }
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return ridable.getRider() == null && !entity.getNavigation().n();
        }

        // startExecuting
        @Override
        public void c() {
            entity.getNavigation().a(x, y, z, speed);
        }

        @Nullable
        private BlockPosition findWater() {
            Random rand = entity.getRandom();
            BlockPosition pos = new BlockPosition(entity.locX, entity.getBoundingBox().minY, entity.locZ);
            for (int i = 0; i < 10; ++i) {
                BlockPosition randPos = pos.b(rand.nextInt(20) - 10, 2 - rand.nextInt(8), rand.nextInt(20) - 10);
                if (world.getType(randPos).getBlock() == Blocks.WATER) {
                    return randPos;
                }
            }
            return null;
        }
    }

    static class AIDrownedSwimUp extends PathfinderGoal {
        private final RidableDrowned drowned;
        private final double speed;
        private final int targetY;
        private boolean obstructed;

        AIDrownedSwimUp(RidableDrowned drowned, double speed, int targetY) {
            this.drowned = drowned;
            this.speed = speed;
            this.targetY = targetY;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return drowned.getRider() == null && !drowned.world.J() && drowned.isInWater() && drowned.locY < (double) (targetY - 2);
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return a() && !obstructed;
        }

        // startExecuting
        @Override
        public void c() {
            drowned.r(true); // setSwimmingUp
            obstructed = false;
        }

        // resetTask
        @Override
        public void d() {
            drowned.r(false); // setSwimmingUp
        }

        // tick
        @Override
        public void e() {
            if (drowned.locY < (double) (targetY - 1) && (drowned.getNavigation().n() || drowned.isCloseToPathTarget())) {
                Vec3D vec3d = RandomPositionGenerator.a(drowned, 4, 8, new Vec3D(drowned.locX, (double) (targetY - 1), drowned.locZ));
                if (vec3d == null) {
                    obstructed = true;
                    return;
                }
                drowned.getNavigation().a(vec3d.x, vec3d.y, vec3d.z, speed);
            }
        }
    }
}
