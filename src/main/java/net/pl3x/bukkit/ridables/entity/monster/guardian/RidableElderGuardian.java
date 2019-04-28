package net.pl3x.bukkit.ridables.entity.monster.guardian;

import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityGuardian;
import net.minecraft.server.v1_14_R1.EntityGuardianElder;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySquid;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.ElderGuardianConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class RidableElderGuardian extends EntityGuardianElder implements RidableEntity {
    private static ElderGuardianConfig config;

    public RidableElderGuardian(EntityTypes<? extends EntityGuardianElder> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new RidableGuardian.GuardianWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.ELDER_GUARDIAN;
    }

    @Override
    public ElderGuardianConfig getConfig() {
        return (ElderGuardianConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        PathfinderGoalMoveTowardsRestriction moveTowardsRestriction = new PathfinderGoalMoveTowardsRestriction(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        };
        goalRandomStroll = new PathfinderGoalRandomStroll(this, 1.0D, 80) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        };
        PathfinderGoal guardianAttack = new PathfinderGoal() {
            private EntityLiving target;
            private int timer;

            public boolean a() { // shouldExecute
                target = getGoalTarget();
                return getRider() == null && target != null && target.isAlive();
            }

            public boolean b() { // shouldContinueExecuting
                return super.b();
            }

            public void c() { // startExecuting
                timer = -10;
                getNavigation().o(); // clearPath
                getControllerLook().a(target, 90.0F, 90.0F);
                impulse = true; // isAirBorne
            }

            public void d() { // resetTask
                RidableGuardian.setTargetedEntity(RidableElderGuardian.this, 0);
                setGoalTarget(null);
                goalRandomStroll.h(); // makeUpdate
            }

            public void e() { // tick
                getNavigation().o(); // clearPath
                getControllerLook().a(target, 90.0F, 90.0F); // setLookPositionWithEntity
                if (!hasLineOfSight(target)) {
                    setGoalTarget(null);
                    return;
                }
                ++timer;
                if (timer == 0) {
                    RidableGuardian.setTargetedEntity(RidableElderGuardian.this, target.getId());
                    world.broadcastEntityEffect(RidableElderGuardian.this, (byte) 21);
                } else if (timer >= l()) { // getAttackDuration
                    float damage = 3.0F;
                    if (world.getDifficulty() == EnumDifficulty.HARD) {
                        damage += 2.0F;
                    }
                    target.damageEntity(DamageSource.c(RidableElderGuardian.this, RidableElderGuardian.this), damage);
                    target.damageEntity(DamageSource.mobAttack(RidableElderGuardian.this), (float) getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
                    setGoalTarget(null);
                }
                super.e();
            }
        };

        goalSelector.a(4, guardianAttack);
        goalSelector.a(5, moveTowardsRestriction);
        goalSelector.a(7, goalRandomStroll);
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityGuardian.class, 12.0F, 0.01F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(9, new PathfinderGoalRandomLookaround(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });

        goalRandomStroll.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        moveTowardsRestriction.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        guardianAttack.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));

        targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 10, true, false,
                target -> (target instanceof EntityHuman || target instanceof EntitySquid) && target.h(RidableElderGuardian.this) > 9.0D) {
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
    protected void mobTick() {
        if (getRider() != null && getAirTicks() > 150) {
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
}
