package net.pl3x.bukkit.ridables.entity.monster.skeleton;

import net.minecraft.server.v1_13_R2.DifficultyDamageScaler;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityArrow;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityIronGolem;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntitySkeletonAbstract;
import net.minecraft.server.v1_13_R2.EntitySkeletonWither;
import net.minecraft.server.v1_13_R2.EntityTurtle;
import net.minecraft.server.v1_13_R2.EntityWolf;
import net.minecraft.server.v1_13_R2.EnumDifficulty;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.GroupDataEntity;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.PathfinderGoalBowShoot;
import net.minecraft.server.v1_13_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_13_R2.SoundEffects;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.mob.WitherSkeletonConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.ai.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.ai.controller.LookController;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIAttackNearest;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIAvoidTarget;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIFleeSun;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIHurtByTarget;
import net.pl3x.bukkit.ridables.entity.ai.goal.AILookIdle;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIRestrictSun;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIShootBow;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIWanderAvoidWater;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIWatchClosest;
import net.pl3x.bukkit.ridables.entity.ai.goal.skeleton.AISkeletonMeleeAttack;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class RidableWitherSkeleton extends EntitySkeletonWither implements RidableEntity {
    public static final WitherSkeletonConfig CONFIG = new WitherSkeletonConfig();

    private PathfinderGoalBowShoot<EntitySkeletonAbstract> aiArrowAttack;
    private PathfinderGoalMeleeAttack aiMeleeAttack;

    private boolean burnInDayLight = true;

    public RidableWitherSkeleton(World world) {
        super(world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);
    }

    @Override
    public RidableType getType() {
        return RidableType.WITHER_SKELETON;
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
        getAttributeInstance(GenericAttributes.maxHealth).setValue(CONFIG.MAX_HEALTH);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(CONFIG.BASE_SPEED);
        getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(CONFIG.AI_MELEE_DAMAGE);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(CONFIG.AI_FOLLOW_RANGE);
    }

    // initAI - override vanilla AI
    @Override
    protected void n() {
        // from EntitySkeletonAbstract
        goalSelector.a(2, new AIRestrictSun(this));
        goalSelector.a(3, new AIFleeSun(this, 1.0D));
        goalSelector.a(3, new AIAvoidTarget<>(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
        goalSelector.a(5, new AIWanderAvoidWater(this, 1.0D));
        goalSelector.a(6, new AIWatchClosest(this, EntityHuman.class, 8.0F));
        goalSelector.a(6, new AILookIdle(this));
        targetSelector.a(1, new AIHurtByTarget(this, false));
        targetSelector.a(2, new AIAttackNearest<>(this, EntityHuman.class, true));
        targetSelector.a(3, new AIAttackNearest<>(this, EntityIronGolem.class, true));
        targetSelector.a(3, new AIAttackNearest<>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bC));
    }

    // canBeRiddenInWater
    @Override
    public boolean aY() {
        return CONFIG.RIDING_RIDE_IN_WATER;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cG() {
        return getRider() == null ? CONFIG.AI_JUMP_POWER : CONFIG.RIDING_JUMP_POWER;
    }

    // livingTick
    @Override
    public void movementTick() {
        burnInDayLight = getRider() == null ? CONFIG.AI_BURN_IN_DAYLIGHT : CONFIG.RIDING_BURN_IN_DAYLIGHT;
        super.movementTick();
        burnInDayLight = true;
    }

    @Override
    protected void mobTick() {
        Q = getRider() == null ? CONFIG.AI_STEP_HEIGHT : CONFIG.RIDING_STEP_HEIGHT;
        super.mobTick();
    }

    // travel
    @Override
    public void a(float strafe, float vertical, float forward) {
        super.a(strafe, vertical, forward);
        checkMove();
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

    // isInDayLight
    @Override
    public boolean dq() {
        return burnInDayLight && super.dq();
    }

    // attackEntityWithRangedAttack
    @Override
    public void a(EntityLiving target, float distanceFactor) {
        EntityArrow arrow = a(distanceFactor); // getArrow
        double x = target.locX - locX;
        double y = target.getBoundingBox().minY + (double) (target.length / 3.0F) - arrow.locY;
        double z = target.locZ - locZ;
        double distance = (double) MathHelper.sqrt(x * x + z * z);
        arrow.setDamage(CONFIG.AI_RANGED_DAMAGE);
        arrow.shoot(x, y + distance * (double) 0.2F, z, 1.6F, (float) (14 - world.getDifficulty().a() * 4)); // getId
        a(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F)); // playSound
        world.addEntity(arrow);
    }

    // setCombatTask
    @Override
    public void dz() {
        if (world != null) {
            if (aiArrowAttack == null) {
                aiArrowAttack = new AIShootBow<>(this, 1.0D, 20, 15.0F);
            }
            if (aiMeleeAttack == null) {
                aiMeleeAttack = new AISkeletonMeleeAttack(this, 1.2D, false);
            }
            goalSelector.a(aiMeleeAttack); // removeTask
            goalSelector.a(aiArrowAttack); // removeTask
            if (getItemInMainHand().getItem() == Items.BOW) {
                aiArrowAttack.b(world.getDifficulty() == EnumDifficulty.HARD ? 40 : 20); // setAttackCooldown
                goalSelector.a(4, aiArrowAttack); // addTask
            } else {
                goalSelector.a(4, aiMeleeAttack); // addTask
            }
        }
    }

    @Override
    @Nullable
    public GroupDataEntity prepare(DifficultyDamageScaler scaler, @Nullable GroupDataEntity data, @Nullable NBTTagCompound nbt) {
        GroupDataEntity groupData = super.prepare(scaler, data, nbt);
        getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(CONFIG.AI_MELEE_DAMAGE);
        dz(); // setCombatTask
        return groupData;
    }
}
