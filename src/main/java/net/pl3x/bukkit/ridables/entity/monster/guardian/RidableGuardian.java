package net.pl3x.bukkit.ridables.entity.monster.guardian;

import net.minecraft.server.v1_14_R1.ControllerLook;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityGuardian;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySquid;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.GuardianConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDWater;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

public class RidableGuardian extends EntityGuardian implements RidableEntity {
    private static GuardianConfig config;

    public RidableGuardian(EntityTypes<? extends EntityGuardian> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new GuardianWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.GUARDIAN;
    }

    @Override
    public GuardianConfig getConfig() {
        return (GuardianConfig) getType().getConfig();
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
        PathfinderGoal guardianAttack = new PathfinderGoal() {
            private EntityLiving target;
            private int timer;

            public boolean a() { // shouldExecute
                target = getGoalTarget();
                return getRider() == null && target != null && target.isAlive();
            }

            public boolean b() { // shouldContinueExecuting
                return super.b() && h(target) > 9.0D;
            }

            public void c() { // startExecuting
                timer = -10;
                getNavigation().o(); // clearPath
                getControllerLook().a(target, 90.0F, 90.0F);
                impulse = true; // isAirBorne
            }

            public void d() { // resetTask
                RidableGuardian.setTargetedEntity(RidableGuardian.this, 0);
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
                    RidableGuardian.setTargetedEntity(RidableGuardian.this, target.getId());
                    world.broadcastEntityEffect(RidableGuardian.this, (byte) 21);
                } else if (timer >= l()) { // getAttackDuration
                    float damage = 1.0F;
                    if (world.getDifficulty() == EnumDifficulty.HARD) {
                        damage += 2.0F;
                    }
                    target.damageEntity(DamageSource.c(RidableGuardian.this, RidableGuardian.this), damage);
                    target.damageEntity(DamageSource.mobAttack(RidableGuardian.this), (float) getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
                    setGoalTarget(null);
                }
                super.e();
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
                target -> (target instanceof EntityHuman || target instanceof EntitySquid) && target.h(RidableGuardian.this) > 9.0D) {
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
            setMot(getMot().add(0.0F, 0.005F, 0.0F));
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

    static class GuardianWASDController extends ControllerWASDWater {
        private final EntityGuardian guardian;

        GuardianWASDController(RidableEntity guardian) {
            super(guardian);
            this.guardian = (EntityGuardian) guardian;
        }

        @Override
        public void tick() {
            if (h == Operation.MOVE_TO && !guardian.getNavigation().n()) {
                Vec3D velocity = new Vec3D(b - guardian.locX, c - guardian.locY, d - guardian.locZ);
                double distance = velocity.f();
                double x = velocity.x / distance;
                double y = velocity.y / distance;
                double z = velocity.z / distance;
                guardian.aK = guardian.yaw = a(guardian.yaw, (float) (MathHelper.d(velocity.z, velocity.x) * Const.RAD2DEG) - 90.0F, 90.0F);
                float speed = MathHelper.g(0.125F, guardian.da(), (float) (e * guardian.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
                guardian.o(speed);
                double d0 = Math.sin((double) (guardian.ticksLived + guardian.getId()) * 0.5D) * 0.05D;
                double d1 = Math.cos((double) (guardian.yaw * Const.DEG2RAD_FLOAT));
                double d2 = Math.sin((double) (guardian.yaw * Const.DEG2RAD_FLOAT));
                guardian.setMot(guardian.getMot().add(d0 * d1, (Math.sin((double) (guardian.ticksLived + guardian.getId()) * 0.75D) * 0.05D) * (d2 + d1) * 0.25D + (double) speed * y * 0.1D, d0 * d2));
                ControllerLook lookController = guardian.getControllerLook();
                double x2 = guardian.locX + x * 2.0D;
                double y2 = (double) guardian.getHeadHeight() + guardian.locY + y / distance;
                double z2 = guardian.locZ + z * 2.0D;
                double lx = lookController.d();
                double ly = lookController.e();
                double lz = lookController.f();
                if (!lookController.c()) { // getIsLooking
                    lx = x2;
                    ly = y2;
                    lz = z2;
                }
                guardian.getControllerLook().a(MathHelper.d(0.125D, lx, x2), MathHelper.d(0.125D, ly, y2), MathHelper.d(0.125D, lz, z2), 10.0F, 40.0F);
                RidableGuardian.setMoving(guardian, true);
            } else {
                guardian.o(0.0F);
                RidableGuardian.setMoving(guardian, false);
            }
        }
    }

    private static Method setMoving;
    private static Method setTargetedEntity;

    static {
        try {
            setMoving = EntityGuardian.class.getDeclaredMethod("r", boolean.class);
            setMoving.setAccessible(true);
            setTargetedEntity = EntityGuardian.class.getDeclaredMethod("a", int.class);
            setTargetedEntity.setAccessible(true);
        } catch (NoSuchMethodException ignore) {
        }
    }

    private static void setMoving(EntityGuardian guardian, boolean moving) {
        try {
            setMoving.invoke(guardian, moving);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }

    static void setTargetedEntity(EntityGuardian guardian, int id) {
        try {
            setTargetedEntity.invoke(guardian, id);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
    }
}
