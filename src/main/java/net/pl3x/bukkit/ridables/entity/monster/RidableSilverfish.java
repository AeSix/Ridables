package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockMonsterEggs;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityDamageSource;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySilverfish;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.SilverfishConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class RidableSilverfish extends EntitySilverfish implements RidableEntity {
    private static SilverfishConfig config;

    private AISilverfishWakeOthers aiWakeOthers;

    public RidableSilverfish(EntityTypes<? extends EntitySilverfish> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.SILVERFISH;
    }

    @Override
    public SilverfishConfig getConfig() {
        return (SilverfishConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        aiWakeOthers = new AISilverfishWakeOthers(this);

        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(3, aiWakeOthers);
        goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new AISilverfishHideInBlock(this));
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
    public boolean damageEntity(DamageSource source, float f) {
        if (isInvulnerable(source)) {
            return false;
        }
        if ((source instanceof EntityDamageSource || source == DamageSource.MAGIC) && aiWakeOthers != null) {
            aiWakeOthers.notifyHurt();
        }
        return super.damageEntity(source, f);
    }

    static class AISilverfishHideInBlock extends PathfinderGoalRandomStroll {
        private final RidableSilverfish silverfish;
        private EnumDirection facing;
        private boolean doMerge;

        AISilverfishHideInBlock(RidableSilverfish silverfish) {
            super(silverfish, 1.0D, 10);
            this.silverfish = silverfish;
            a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (silverfish.getRider() != null) {
                return false;
            }
            if (silverfish.getGoalTarget() != null) {
                return false;
            } else if (!silverfish.getNavigation().n()) {
                return false;
            } else {
                if (silverfish.world.getGameRules().getBoolean("mobGriefing") && silverfish.getRandom().nextInt(10) == 0) {
                    facing = EnumDirection.a(silverfish.getRandom());
                    IBlockData iblockdata = silverfish.world.getType(new BlockPosition(silverfish.locX, silverfish.locY + 0.5D, silverfish.locZ).shift(facing));
                    if (BlockMonsterEggs.j(iblockdata)) {
                        doMerge = true;
                        return true;
                    }
                }
                doMerge = false;
                return super.a();
            }
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return silverfish.getRider() == null && !doMerge && super.b();
        }

        // startExecuting
        @Override
        public void c() {
            if (!doMerge) {
                super.c();
            } else {
                BlockPosition pos = (new BlockPosition(silverfish.locX, silverfish.locY + 0.5D, silverfish.locZ)).shift(facing);
                IBlockData state = silverfish.world.getType(pos);
                if (BlockMonsterEggs.j(state)) {
                    if (CraftEventFactory.callEntityChangeBlockEvent(silverfish, pos, BlockMonsterEggs.e(state.getBlock())).isCancelled()) {
                        return;
                    }
                    silverfish.world.setTypeAndData(pos, BlockMonsterEggs.e(state.getBlock()), 3);
                    silverfish.doSpawnEffect();
                    silverfish.die();
                }
            }
        }
    }

    static class AISilverfishWakeOthers extends PathfinderGoal {
        private final RidableSilverfish silverfish;
        private int lookForFriends;

        AISilverfishWakeOthers(RidableSilverfish silverfish) {
            this.silverfish = silverfish;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return silverfish.getRider() == null && lookForFriends > 0;
        }

        // tick
        @Override
        public void e() {
            --lookForFriends;
            if (lookForFriends > 0) {
                return;
            }
            BlockPosition pos = new BlockPosition(silverfish);
            for (int y = 0; y <= 5 && y >= -5; y = (y <= 0 ? 1 : 0) - y) {
                for (int x = 0; x <= 10 && x >= -10; x = (x <= 0 ? 1 : 0) - x) {
                    for (int z = 0; z <= 10 && z >= -10; z = (z <= 0 ? 1 : 0) - z) {
                        BlockPosition pos1 = pos.b(x, y, z);
                        Block block = silverfish.world.getType(pos1).getBlock();
                        if (!(block instanceof BlockMonsterEggs)) {
                            continue;
                        }
                        if (CraftEventFactory.callEntityChangeBlockEvent(silverfish, pos1, Blocks.AIR.getBlockData()).isCancelled()) {
                            continue;
                        }
                        if (silverfish.world.getGameRules().getBoolean("mobGriefing")) {
                            silverfish.world.b(pos1, true);
                        } else {
                            silverfish.world.setTypeAndData(pos1, ((BlockMonsterEggs) block).d().getBlockData(), 3);
                        }
                        if (silverfish.getRandom().nextBoolean()) {
                            return;
                        }
                    }
                }
            }
        }

        void notifyHurt() {
            if (lookForFriends == 0) {
                lookForFriends = 20;
            }
        }
    }
}
