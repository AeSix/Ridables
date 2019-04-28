package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityEnderman;
import net.minecraft.server.v1_14_R1.EntityEndermite;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_14_R1.RayTrace;
import net.minecraft.server.v1_14_R1.TagsBlock;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.configuration.mob.EndermanConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.PaperOnly;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class RidableEnderman extends EntityEnderman implements RidableEntity {
    private static EndermanConfig config;

    private boolean skipTP;

    public RidableEnderman(EntityTypes<? extends EntityEnderman> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.ENDERMAN;
    }

    @Override
    public EndermanConfig getConfig() {
        return (EndermanConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        PathfinderGoal someNewGoal = new PathfinderGoal() {
            public boolean a() { // shouldExecute
                EntityLiving target = getGoalTarget();
                return getRider() == null && target instanceof EntityHuman && target.h(RidableEnderman.this) <= 256.0D && shouldAttack((EntityHuman) target);
            }

            public void c() { // startExecuting
                getNavigation().o();
            }
        };
        someNewGoal.a(EnumSet.of(PathfinderGoal.Type.JUMP, PathfinderGoal.Type.MOVE));
        goalSelector.a(1, someNewGoal);
        goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0D, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D, 0.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
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
        goalSelector.a(10, new PathfinderGoal() { // PathfinderGoalEndermanPlaceBlock
            public boolean a() { // shouldExecute
                return getCarried() != null && world.getGameRules().getBoolean("mobGriefing") && random.nextInt(2000) == 0 && getRider() == null;
            }

            public void e() { // tick
                int x = MathHelper.floor(locX - 1.0D + random.nextDouble() * 2.0D);
                int y = MathHelper.floor(locY + random.nextDouble() * 2.0D);
                int z = MathHelper.floor(locZ - 1.0D + random.nextDouble() * 2.0D);
                tryPlaceBlock(x, y, z);
            }
        });
        goalSelector.a(11, new PathfinderGoal() { // PathfinderGoalEndermanPickupBlock
            public boolean a() { // shouldExecute
                return getCarried() == null && world.getGameRules().getBoolean("mobGriefing") && getRandom().nextInt(20) == 0 && getRider() == null;
            }

            public void e() { // tick
                int x = MathHelper.floor(locX - 2.0D + random.nextDouble() * 4.0D);
                int y = MathHelper.floor(locY + random.nextDouble() * 3.0D);
                int z = MathHelper.floor(locZ - 2.0D + random.nextDouble() * 4.0D);
                tryTakeBlock(x, y, z);
            }
        });
        targetSelector.a(1, new AIEndermanTargetPlayerWhoLooked(this));
        targetSelector.a(2, new PathfinderGoalHurtByTarget(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<EntityEndermite>(this, EntityEndermite.class, 10, true, false,
                (target) -> target instanceof EntityEndermite && ((EntityEndermite) target).isPlayerSpawned()) {
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
        return config.RIDING_RIDE_IN_WATER && !config.RIDING_EJECT_WHEN_WET;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        return getRider() == null ? super.cW() : config.RIDING_JUMP_POWER;
    }

    // randomlyTeleport
    @Override
    public boolean dW() {
        return skipTP || super.dW();
    }

    public boolean teleportToEntity(Entity entity) {
        return super.a(entity);
    }

    @Override
    protected void mobTick() {
        boolean hasRider = getRider() != null;
        K = hasRider ? config.RIDING_STEP_HEIGHT : 1.0F;
        if (at()) { // isWet
            if (config.RIDING_EJECT_WHEN_WET && getRider() != null) {
                ejectPassengers();
            }
            if (hasRider) {
                if (config.RIDING_DAMAGE_WHEN_WET > 0F) {
                    damageEntity(DamageSource.DROWN, config.RIDING_DAMAGE_WHEN_WET);
                }
            } else {
                damageEntity(DamageSource.DROWN, 1.0F);
            }
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
    public boolean onClick(org.bukkit.block.Block block, BlockFace blockFace, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            return false; // ignore left clicks
        }

        EntityPlayer rider = getRider();
        if (rider == null || !rider.getBukkitEntity().hasPermission("ridables.special.enderman")) {
            return false;
        }

        if (getCarried() == null) {
            return tryTakeBlock(block.getX(), block.getY(), block.getZ());
        }

        block = block.getRelative(blockFace);
        return tryPlaceBlock(block.getX(), block.getY(), block.getZ());
    }

    public boolean tryTakeBlock(int x, int y, int z) {
        BlockPosition pos = new BlockPosition(x, y, z);
        IBlockData state = world.getType(pos);
        if (!state.getBlock().a(TagsBlock.ENDERMAN_HOLDABLE)) {
            return false; // not a holdable block
        }
        MovingObjectPositionBlock rayTrace = world.rayTrace(new RayTrace(
                new Vec3D((double) MathHelper.floor(locX) + 0.5D, (double) y + 0.5D, (double) MathHelper.floor(locZ) + 0.5D),
                new Vec3D((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D),
                RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, this));
        if (rayTrace.getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
            return false; // no target block in range
        }
        if (!rayTrace.getBlockPosition().equals(pos)) {
            return false; // block in the way
        }
        if (CraftEventFactory.callEntityChangeBlockEvent(this, pos, Blocks.AIR.getBlockData()).isCancelled()) {
            return false; // plugin cancelled
        }
        //setCarried(state); // original
        world.a(pos, false); // setAir
        setCarried(Block.b(state, world, pos)); // getValidBlockForPosition (fixes MC-124320)
        return true;
    }

    public boolean tryPlaceBlock(int x, int y, int z) {
        IBlockData carried = getCarried();
        if (carried == null) {
            return false; // not carrying a block
        }
        BlockPosition pos = new BlockPosition(x, y, z);
        if (!world.getType(pos).isAir()) {
            return false; // cannot place in non-air block
        }
        BlockPosition posDown = pos.down();
        IBlockData stateDown = world.getType(posDown);
        if (stateDown.isAir() || !Block.a(stateDown.getCollisionShape(world, posDown))) {
            return false; // cannot place on air or non-full cube
        }
        //IBlockData newState = carried; // original
        IBlockData newState = Block.b(carried, world, pos); // getValidBlockForPosition (fixes MC-124320)
        if (newState == null) {
            return false; // no valid blockstate for position
        }
        if (!newState.canPlace(world, pos)) {
            return false; // cannot place this block here
        }
        if (CraftEventFactory.callEntityChangeBlockEvent(this, pos, newState).isCancelled()) {
            return false; // plugin cancelled
        }
        world.setTypeAndData(pos, newState, 3);
        setCarried(null);
        return true;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        skipTP = getRider() != null && !config.RIDING_TELEPORT_WHEN_DAMAGED;
        boolean result = super.damageEntity(damagesource, f);
        skipTP = false;
        return result;
    }

    public boolean shouldAttack(EntityHuman player) {
        boolean shouldAttack = shouldAttack_real(player);
        if (Ridables.isPaper()) {
            return PaperOnly.callEndermanAttackPlayerEvent((Enderman) getBukkitEntity(), (Player) player.getBukkitEntity(), shouldAttack);
        }
        return shouldAttack;
    }

    private boolean shouldAttack_real(EntityHuman player) {
        if (player.inventory.armor.get(3).getItem() == Blocks.CARVED_PUMPKIN.getItem()) {
            return false;
        }
        Vec3D direction = new Vec3D(locX - player.locX, getBoundingBox().minY + getHeadHeight() - (player.locY + player.getHeadHeight()), locZ - player.locZ);
        return player.f(1.0F).d().b(direction.d()) > 1.0D - 0.025D / direction.f() && player.hasLineOfSight(this); // getLook normalize dotProduct normalize length
    }

    class AIEndermanTargetPlayerWhoLooked extends PathfinderGoalNearestAttackableTarget<EntityHuman> {
        private final RidableEnderman enderman;
        private EntityHuman target;
        private int aggroTime;
        private int teleportTime;
        private final PathfinderTargetCondition targetCondition;
        private final PathfinderTargetCondition ignoreSensesCondition = (new PathfinderTargetCondition()).c();

        AIEndermanTargetPlayerWhoLooked(RidableEnderman enderman) {
            super(enderman, EntityHuman.class, false);
            this.enderman = enderman;
            this.targetCondition = (new PathfinderTargetCondition()).a(k()).a((target) -> enderman.shouldAttack((EntityHuman) target));
        }

        // shouldExecute
        @Override
        public boolean a() {
            return getRider() == null && (target = enderman.world.a(targetCondition, enderman)) != null;
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            if (getRider() != null) {
                return false;
            }
            if (target != null) {
                if (!enderman.shouldAttack(target)) {
                    return false;
                } else {
                    enderman.a(target, 10.0F, 10.0F);
                    return true;
                }
            }
            if (c != null && ignoreSensesCondition.a(enderman, c)) {
                return true;
            }
            return super.b();
        }

        // startExecuting
        @Override
        public void c() {
            aggroTime = 5;
            teleportTime = 0;
        }

        // resetTask
        @Override
        public void d() {
            target = null;
            super.d();
        }

        // tick
        @Override
        public void e() {
            if (target != null) {
                if (--aggroTime <= 0) {
                    c = target;
                    target = null;
                    super.c();
                }
            } else {
                if (c != null && !enderman.isPassenger()) {
                    if (enderman.shouldAttack((EntityHuman) c)) {
                        if (c.h(enderman) < 16.0D) {
                            enderman.dW();
                        }
                        teleportTime = 0;
                    } else if (c.h(enderman) > 256.0D && teleportTime++ >= 30 && enderman.a(c)) {
                        teleportTime = 0;
                    }
                }
                super.e();
            }
        }
    }
}
