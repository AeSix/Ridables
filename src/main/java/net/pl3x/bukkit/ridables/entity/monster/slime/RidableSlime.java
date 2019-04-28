package net.pl3x.bukkit.ridables.entity.monster.slime;

import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityIronGolem;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySlime;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.SlimeConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.ai.goal.slime.AISlimeAttack;
import net.pl3x.bukkit.ridables.entity.ai.goal.slime.AISlimeFaceRandom;
import net.pl3x.bukkit.ridables.entity.ai.goal.slime.AISlimeHop;
import net.pl3x.bukkit.ridables.entity.ai.goal.slime.AISlimeSwim;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.entity.Player;

public class RidableSlime extends EntitySlime implements RidableEntity {
    public static final SlimeConfig CONFIG = new SlimeConfig();

    private int spacebarCharge = 0;
    private int prevSpacebarCharge = 0;
    private float fallDistanceCharge = 0;

    public RidableSlime(EntityTypes<? extends EntitySlime> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new SlimeWASDController(this);
        lookController = new LookController(this);
    }

    @Override
    public RidableType getType() {
        return RidableType.SLIME;
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
        goalSelector.a(1, new AISlimeSwim(this));
        goalSelector.a(2, new AISlimeAttack(this));
        goalSelector.a(3, new AISlimeFaceRandom(this));
        goalSelector.a(5, new AISlimeHop(this));
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
        if (getRider().getBukkitEntity().hasPermission("ridables.special.slime")) {
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

    public static class SlimeWASDController extends ControllerWASD {
        private final RidableSlime slime;
        private float yRot;
        private int jumpDelay;
        private boolean isAggressive;

        public SlimeWASDController(RidableSlime slime) {
            super(slime);
            this.slime = slime;
            yRot = slime.yaw * Const.RAD2DEG_FLOAT;
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
            slime.aQ = slime.aS = slime.yaw = a(slime.yaw, yRot, 90.0F);
            if (h != ControllerMove.Operation.MOVE_TO) {
                slime.r(0.0F); // forward
                return;
            }
            h = ControllerMove.Operation.WAIT;
            if (slime.onGround) {
                slime.o((float) (e * a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
                if (jumpDelay-- <= 0) {
                    jumpDelay = slime.dr(); // getJumpDelay
                    if (isAggressive) {
                        jumpDelay /= 3;
                    }
                    slime.getControllerJump().a(); // setJumping
                    if (slime.dz()) { // makeSoundOnJump
                        slime.a(slime.dw(), slime.cD(), ((slime.getRandom().nextFloat() - slime.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F); // playSound
                    }
                } else {
                    slime.bh = 0.0F; // moveStrafing
                    slime.bj = 0.0F; // moveForward
                    slime.o(0.0F); // setSpeed
                }
                return;
            }
            slime.o((float) (e * a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()));
        }
    }
}
