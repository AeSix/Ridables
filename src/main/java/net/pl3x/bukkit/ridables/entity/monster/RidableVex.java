package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityCreature;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityRaider;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityVex;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.VexConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableFlyingEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;

public class RidableVex extends EntityVex implements RidableEntity, RidableFlyingEntity {
    private static VexConfig config;

    public RidableVex(EntityTypes<? extends EntityVex> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new VexWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.VEX;
    }

    @Override
    public VexConfig getConfig() {
        return (VexConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    public int getMaxY() {
        return config.RIDING_FLYING_MAX_Y;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(4, new AIChargeAttack());
        goalSelector.a(8, new AIMoveRandom());
        goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 3.0F, 1.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalHurtByTarget(this, EntityRaider.class) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        }.a(new Class[0]));
        targetSelector.a(2, new AICopyOwnerTarget(this));
        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true) {
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
    public void movementTick() {
        noclip = getRider() == null || config.RIDING_NO_CLIP;
        super.movementTick();
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

    // fall
    @Override
    public void b(float f, float f1) {
        // no fall damage
    }

    static class VexWASDController extends ControllerWASDFlying {
        private final RidableVex vex;

        VexWASDController(RidableVex vex) {
            super(vex);
            this.vex = vex;
        }

        @Override
        public void tick(EntityPlayer rider) {
            super.tick(rider);
            vex.noclip = config.RIDING_NO_CLIP;
        }

        @Override
        public void tick() {
            if (h == ControllerMove.Operation.MOVE_TO) {
                Vec3D velocity = new Vec3D(b - vex.locX, c - vex.locY, d - vex.locZ);
                double distance = velocity.f();
                if (distance < vex.getBoundingBox().a()) { // getAverageEdgeLength
                    h = ControllerMove.Operation.WAIT;
                    vex.setMot(vex.getMot().a(0.5D));
                } else {
                    vex.setMot(vex.getMot().e(velocity.a(e * 0.05D / distance)));
                    if (vex.getGoalTarget() == null) {
                        Vec3D mot = vex.getMot();
                        vex.aK = vex.yaw = -((float) MathHelper.d(mot.x, mot.z)) * Const.RAD2DEG_FLOAT;
                    } else {
                        vex.aK = vex.yaw = -((float) MathHelper.d(vex.getGoalTarget().locX - vex.locX, vex.getGoalTarget().locZ - vex.locZ)) * Const.RAD2DEG_FLOAT;
                    }
                }
            }
        }
    }

    class AICopyOwnerTarget extends PathfinderGoalTarget {
        private final PathfinderTargetCondition b = (new PathfinderTargetCondition()).c().e();

        AICopyOwnerTarget(EntityCreature entitycreature) {
            super(entitycreature, false);
        }

        // shouldExecute
        @Override
        public boolean a() {
            return getRider() == null && l() != null && l().getGoalTarget() != null && a(l().getGoalTarget(), this.b);
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return getRider() == null && super.b();
        }

        // startExecuting
        @Override
        public void c() {
            setGoalTarget(l().getGoalTarget(), EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
            super.c();
        }
    }

    class AIMoveRandom extends PathfinderGoal {
        AIMoveRandom() {
            a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        // shouldExecute
        @Override
        public boolean a() {
            return getRider() == null && !getControllerMove().b() && random.nextInt(7) == 0;
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return false;
        }

        // tick
        @Override
        public void e() {
            BlockPosition pos = dW();
            if (pos == null) {
                pos = new BlockPosition(RidableVex.this);
            }
            for (int i = 0; i < 3; ++i) {
                BlockPosition pos1 = pos.b(random.nextInt(15) - 7, random.nextInt(11) - 5, random.nextInt(15) - 7);
                if (world.isEmpty(pos1)) {
                    moveController.a((double) pos1.getX() + 0.5D, (double) pos1.getY() + 0.5D, (double) pos1.getZ() + 0.5D, 0.25D);
                    if (getGoalTarget() == null) {
                        getControllerLook().a((double) pos1.getX() + 0.5D, (double) pos1.getY() + 0.5D, (double) pos1.getZ() + 0.5D, 180.0F, 20.0F);
                    }
                    break;
                }
            }
        }
    }

    class AIChargeAttack extends PathfinderGoal {
        AIChargeAttack() {
            a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        // shouldExecute
        @Override
        public boolean a() {
            return getRider() == null && getGoalTarget() != null && !getControllerMove().b() && random.nextInt(7) == 0 && h(getGoalTarget()) > 4.0D;
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return getRider() == null && getControllerMove().b() && isCharging() && getGoalTarget() != null && getGoalTarget().isAlive();
        }

        // startExecuting
        @Override
        public void c() {
            EntityLiving target = getGoalTarget();
            Vec3D vec3d = target.j(1.0F);
            moveController.a(vec3d.x, vec3d.y, vec3d.z, 1.0D);
            setCharging(true);
            RidableVex.this.a(SoundEffects.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
        }

        // resetTask
        @Override
        public void d() {
            setCharging(false);
        }

        // tick
        @Override
        public void e() {
            EntityLiving target = getGoalTarget();
            if (getBoundingBox().c(target.getBoundingBox())) {
                C(target);
                setCharging(false);
            } else {
                double d0 = h(target);
                if (d0 < 9.0D) {
                    Vec3D vec3d = target.j(1.0F);
                    moveController.a(vec3d.x, vec3d.y, vec3d.z, 1.0D);
                }
            }
        }
    }
}
