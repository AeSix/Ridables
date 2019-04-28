package net.pl3x.bukkit.ridables.entity.monster.slime;

import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityIronGolem;
import net.minecraft.server.v1_14_R1.EntityMagmaCube;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.MagmaCubeConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube.AIMagmaCubeAttack;
import net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube.AIMagmaCubeFaceRandom;
import net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube.AIMagmaCubeHop;
import net.pl3x.bukkit.ridables.entity.ai.goal.magma_cube.AIMagmaCubeSwim;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.entity.Player;

public class RidableMagmaCube extends EntityMagmaCube implements RidableEntity {
    public static final MagmaCubeConfig CONFIG = new MagmaCubeConfig();

    private int spacebarCharge = 0;
    private int prevSpacebarCharge = 0;
    private float fallDistanceCharge = 0;

    public RidableMagmaCube(EntityTypes<? extends EntityMagmaCube> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new MagmaCubeWASDController(this);
        lookController = new LookController(this);
    }

    @Override
    public RidableType getType() {
        return RidableType.MAGMA_CUBE;
    }

    // canDespawn
    @Override
    public boolean isTypeNotPersistent() {
        return !hasCustomName() && !isLeashed();
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        getAttributeMap().b(RidableType.RIDING_SPEED); // registerAttribute
        reloadAttributes();
    }

    @Override
    public void reloadAttributes() {
        getAttributeInstance(RidableType.RIDING_SPEED).setValue(CONFIG.RIDING_SPEED);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(CONFIG.BASE_SPEED);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(CONFIG.AI_FOLLOW_RANGE);
    }

    // initAI - override vanilla AI
    @Override
    protected void n() {
        goalSelector.a(1, new AIMagmaCubeSwim(this));
        goalSelector.a(2, new AIMagmaCubeAttack(this));
        goalSelector.a(3, new AIMagmaCubeFaceRandom(this));
        goalSelector.a(5, new AIMagmaCubeHop(this));
        targetSelector.a(1, new AIFindNearestPlayer(this));
        targetSelector.a(3, new AIFindNearestEntity(this, EntityIronGolem.class));
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return CONFIG.RIDING_RIDE_IN_WATER;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cG() {
        return getRider() == null ? CONFIG.AI_JUMP_POWER : (CONFIG.RIDING_JUMP_POWER * getJumpCharge());
    }

    public boolean canDamagePlayer() {
        return dt();
    }

    @Override
    protected void mobTick() {
        if (spacebarCharge == prevSpacebarCharge) {
            spacebarCharge = 0;
        }
        prevSpacebarCharge = spacebarCharge;
        super.mobTick();
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        checkMove();
    }

    public float getJumpCharge() {
        float charge = 1F;
        if (getRider() != null && spacebarCharge > 0) {
            charge += 1F * (fallDistanceCharge = (spacebarCharge / 72F));
        } else {
            fallDistanceCharge = 0;
        }
        return charge;
    }

    // fall
    @Override
    public void c(float distance, float damageMultiplier) {
        if (getRider() != null && fallDistanceCharge > 0) {
            distance = distance - fallDistanceCharge;
        }
        super.c(distance, damageMultiplier);
    }

    // processInteract
    @Override
    public boolean a(EntityHuman entityhuman, EnumHand hand) {
        if (super.a(entityhuman, hand)) {
            return true; // handled by vanilla action
        }
        if (hand == EnumHand.MAIN_HAND && !entityhuman.isSneaking() && passengers.isEmpty() && !entityhuman.isPassenger()) {
            return tryRide(entityhuman, CONFIG.RIDING_SADDLE_REQUIRE, CONFIG.RIDING_SADDLE_CONSUME);
        }
        return false;
    }

    @Override
    public boolean removePassenger(Entity passenger, boolean notCancellable) {
        if (passenger instanceof EntityPlayer && !passengers.isEmpty() && passenger == passengers.get(0)) {
            if (!new RidableDismountEvent(this, (Player) passenger.getBukkitEntity(), notCancellable).callEvent() && !notCancellable) {
                return false; // cancelled
            }
        }
        return super.removePassenger(passenger, notCancellable);
    }

    @Override
    public boolean onSpacebar() {
        if (getRider().getBukkitEntity().hasPermission("ridables.special.magma_cube")) {
            spacebarCharge++;
            if (spacebarCharge > 50) {
                spacebarCharge -= 2;
            }
        }
        return false;
    }

    // getAttackStrength
    @Override
    protected int du() {
        // 1-  = small
        // 2-3 = medium
        // 4+  = large
        int size = getSize();
        if (size < 2) {
            return CONFIG.AI_MELEE_DAMAGE_SMALL;
        } else if (size < 4) {
            return CONFIG.AI_MELEE_DAMAGE_MEDIUM;
        } else {
            return CONFIG.AI_MELEE_DAMAGE_LARGE;
        }
    }

    @Override
    public void setSize(int i, boolean resetHealth) {
        super.setSize(i, resetHealth);
        int size = getSize();
        double maxHealth;
        if (size < 2) {
            maxHealth = CONFIG.MAX_HEALTH_SMALL;
        } else if (size < 4) {
            maxHealth = CONFIG.MAX_HEALTH_MEDIUM;
        } else {
            maxHealth = CONFIG.MAX_HEALTH_LARGE;
        }
        getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
        if (resetHealth) {
            setHealth(getMaxHealth());
        }
    }

    public static class MagmaCubeWASDController extends ControllerWASD {
        private final RidableMagmaCube magmaCube;
        private float yRot;
        private int jumpDelay;
        private boolean isAggressive;

        public MagmaCubeWASDController(RidableMagmaCube magmaCube) {
            super(magmaCube);
            this.magmaCube = magmaCube;
            yRot = magmaCube.yaw * Const.RAD2DEG_FLOAT;
        }

        public void setDirection(float yRot, boolean isAggressive) {
            this.yRot = yRot;
            this.isAggressive = isAggressive;
        }

        public void setSpeed(double speed) {
            e = speed;
            h = ControllerMove.Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            magmaCube.aQ = magmaCube.aS = magmaCube.yaw = a(magmaCube.yaw, yRot, 90.0F);
            if (h != ControllerMove.Operation.MOVE_TO) {
                magmaCube.r(0.0F); // forward
                return;
            }
            h = ControllerMove.Operation.WAIT;
            if (magmaCube.onGround) {
                magmaCube.o((float) (e * a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
                if (jumpDelay-- <= 0) {
                    jumpDelay = magmaCube.dr(); // getJumpDelay
                    if (isAggressive) {
                        jumpDelay /= 3;
                    }
                    magmaCube.getControllerJump().a(); // setJumping
                    if (magmaCube.dz()) { // makeSoundOnJump
                        magmaCube.a(magmaCube.dw(), magmaCube.cD(), ((magmaCube.getRandom().nextFloat() - magmaCube.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F); // playSound
                    }
                } else {
                    magmaCube.bh = 0.0F; // moveStrafing
                    magmaCube.bj = 0.0F; // moveForward
                    magmaCube.o(0.0F); // setSpeed
                }
                return;
            }
            magmaCube.o((float) (e * a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
        }
    }
}
