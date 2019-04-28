package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPhantom;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.GroupDataEntity;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.PhantomConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.ai.goal.phantom.AIPhantomAttack;
import net.pl3x.bukkit.ridables.entity.ai.goal.phantom.AIPhantomFindTotem;
import net.pl3x.bukkit.ridables.entity.ai.goal.phantom.AIPhantomOrbitPoint;
import net.pl3x.bukkit.ridables.entity.ai.goal.phantom.AIPhantomOrbitTotem;
import net.pl3x.bukkit.ridables.entity.ai.goal.phantom.AIPhantomPickAttack;
import net.pl3x.bukkit.ridables.entity.ai.goal.phantom.AIPhantomSweepAttack;
import net.pl3x.bukkit.ridables.entity.item.CustomEnderCrystal;
import net.pl3x.bukkit.ridables.entity.projectile.PhantomFlames;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class RidablePhantom extends EntityPhantom implements RidableEntity {
    public static final PhantomConfig CONFIG = new PhantomConfig();

    public Vec3D orbitOffset;
    public BlockPosition orbitPosition;
    public AttackPhase phase;
    public BlockPosition totemPosition;

    public RidablePhantom(EntityTypes<? extends EntityPhantom> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new PhantomWASDController(this);
        lookController = new PhantomLookController(this);
        orbitOffset = Vec3D.a;
        orbitPosition = BlockPosition.ZERO;
        phase = AttackPhase.CIRCLE;
    }

    @Override
    public RidableType getType() {
        return RidableType.PHANTOM;
    }

    // canDespawn
    @Override
    public boolean isTypeNotPersistent() {
        return !hasCustomName() && !isLeashed();
    }

    @Override
    public void initAttributes() {
        super.initAttributes();
        getAttributeMap().b(RidableType.RIDING_SPEED); // registerAttribute
        getAttributeMap().b(RidableType.RIDING_MAX_Y); // registerAttribute
        reloadAttributes();
    }

    @Override
    public void reloadAttributes() {
        getAttributeInstance(RidableType.RIDING_SPEED).setValue(CONFIG.RIDING_SPEED);
        getAttributeInstance(RidableType.RIDING_MAX_Y).setValue(CONFIG.RIDING_FLYING_MAX_Y);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(CONFIG.BASE_SPEED);
        getAttributeInstance(GenericAttributes.maxHealth).setValue(CONFIG.MAX_HEALTH);
        getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(CONFIG.AI_MELEE_DAMAGE);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(CONFIG.AI_FOLLOW_RANGE);
    }

    // initAI - override vanilla AI
    @Override
    protected void n() {
        goalSelector.a(0, new AIPhantomFindTotem(this));
        goalSelector.a(1, new AIPhantomPickAttack(this));
        goalSelector.a(2, new AIPhantomSweepAttack(this));
        goalSelector.a(3, new AIPhantomOrbitTotem(this));
        goalSelector.a(3, new AIPhantomOrbitPoint(this));
        targetSelector.a(1, new AIPhantomAttack(this));
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return CONFIG.RIDING_RIDE_IN_WATER;
    }

    // onLivingUpdate
    @Override
    public void movementTick() {
        super.movementTick();
        boolean hasRider = getRider() != null;
        if ((hasRider && !CONFIG.RIDING_BURN_IN_SUNLIGHT) || (!hasRider && !CONFIG.AI_BURN_IN_SUNLIGHT)) {
            extinguish(); // dont burn in sunlight
        }
        if (!CONFIG.AI_ATTACK_IN_SUNLIGHT && isInDaylight()) {
            setGoalTarget(null, null, false);
        }
    }

    @Override
    protected void mobTick() {
        if (getRider() != null && getRider().bj == 0) {
            motY -= CONFIG.RIDING_GRAVITY;
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
        EntityPlayer rider = getRider();
        if (rider != null && rider.getBukkitEntity().hasPermission("ridables.shoot.phantom")) {
            shoot(getRider());
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        Location loc = ((LivingEntity) getBukkitEntity()).getEyeLocation();
        loc.setPitch(-loc.getPitch());
        Vector target = loc.getDirection().normalize().multiply(100).add(loc.toVector());

        PhantomFlames flames = new PhantomFlames(world, this, rider);
        flames.shoot(target.getX() - locX, target.getY() - locY, target.getZ() - locZ, 1.0F, 5.0F);
        world.addEntity(flames);
        return true;
    }

    public void setTotemPosition(BlockPosition pos) {
        if (CONFIG.AI_ENDER_CRYSTALS_ORBIT) {
            totemPosition = pos;
        }
    }

    public boolean isCirclingTotem() {
        return totemPosition != null;
    }

    public boolean canAttack() {
        return (CONFIG.AI_ATTACK_IN_SUNLIGHT || !isInDaylight()) && !isCirclingTotem();
    }

    // updatePhantomSize
    public void l() {
        int size = getSize();
        setSize(0.9F + 0.2F * (float) size, 0.5F + 0.1F * (float) size);
        getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue((double) (CONFIG.AI_MELEE_DAMAGE + size));
    }

    // readNBT
    @Override
    public void a(NBTTagCompound nbt) {
        super.a(nbt);
        if (nbt.hasKey("AX")) {
            orbitPosition = new BlockPosition(nbt.getInt("AX"), nbt.getInt("AY"), nbt.getInt("AZ"));
        }
    }

    // writeNBT
    @Override
    public void b(NBTTagCompound nbt) {
        super.b(nbt);
        nbt.setInt("AX", orbitPosition.getX());
        nbt.setInt("AY", orbitPosition.getY());
        nbt.setInt("AZ", orbitPosition.getZ());
    }

    // dropLoot
    @Override
    protected void a(boolean wasRecentlyHit, int lootingModifier, DamageSource damagesource) {
        if (killer == null && damagesource.getEntity() instanceof CustomEnderCrystal) {
            if (random.nextInt(5) < 1) { // 1 out of 5 chance (20%)
                a_(new ItemStack(Items.PHANTOM_MEMBRANE));
            }
        } else {
            super.a(wasRecentlyHit, lootingModifier, damagesource); // dropLoot
        }
    }

    @Override
    public GroupDataEntity prepare(DifficultyDamageScaler scaler, @Nullable GroupDataEntity group, @Nullable NBTTagCompound nbt) {
        orbitPosition = (new BlockPosition(this)).up(5);
        return super.prepare(scaler, group, nbt);
    }

    class PhantomLookController extends LookController {
        PhantomLookController(RidableEntity ridable) {
            super(ridable);
        }

        @Override
        public void tick(EntityPlayer rider) {
            setYawPitch(rider.yaw, -rider.pitch * 0.75F);
        }
    }

    class PhantomWASDController extends ControllerWASDFlying {
        private final RidablePhantom phantom;
        private float speed = 0.1F;

        PhantomWASDController(RidablePhantom phantom) {
            super(phantom);
            this.phantom = phantom;
        }

        @Override
        public void tick() {
            if (phantom.positionChanged) {
                phantom.yaw += 180.0F;
                speed = 0.1F;
            }

            float f = (float) (phantom.orbitOffset.x - phantom.locX);
            float f1 = (float) (phantom.orbitOffset.y - phantom.locY);
            float f2 = (float) (phantom.orbitOffset.z - phantom.locZ);
            double d0 = (double) MathHelper.c(f * f + f2 * f2); // sqrt
            double d1 = 1.0D - (double) MathHelper.e(f1 * 0.7F) / d0; // abs

            f = (float) ((double) f * d1);
            f2 = (float) ((double) f2 * d1);
            d0 = (double) MathHelper.c(f * f + f2 * f2); // sqrt
            double d2 = (double) MathHelper.c(f * f + f2 * f2 + f1 * f1); // sqrt
            float f3 = phantom.yaw;
            float f4 = (float) MathHelper.c((double) f2, (double) f); // atan2
            float f5 = MathHelper.g(phantom.yaw + 90.0F); // wrapDegrees
            float f6 = MathHelper.g(f4 * Const.RAD2DEG_FLOAT); // wrapDegrees

            phantom.yaw = MathHelper.c(f5, f6, 4.0F) - 90.0F; // approachDegrees
            phantom.aQ = phantom.yaw;
            if (MathHelper.d(f3, phantom.yaw) < 3.0F) { // degreesDifferenceAbs
                speed = MathHelper.b(speed, 1.8F, 0.005F * (1.8F / speed)); // approach
            } else {
                speed = MathHelper.b(speed, 0.2F, 0.025F); // approach
            }

            float f7 = (float) (-(MathHelper.c((double) (-f1), d0) * Const.RAD2DEG)); // atan2
            phantom.pitch = f7;
            float f8 = phantom.yaw + 90.0F;

            double d3 = (double) (speed * MathHelper.cos(f8 * Const.DEG2RAD_FLOAT)) * Math.abs((double) f / d2);
            double d4 = (double) (speed * MathHelper.sin(f8 * Const.DEG2RAD_FLOAT)) * Math.abs((double) f2 / d2);
            double d5 = (double) (speed * MathHelper.sin(f7 * Const.DEG2RAD_FLOAT)) * Math.abs((double) f1 / d2);

            phantom.motX += (d3 - phantom.motX) * 0.2D;
            phantom.motY += (d5 - phantom.motY) * 0.2D;
            phantom.motZ += (d4 - phantom.motZ) * 0.2D;
        }
    }

    public enum AttackPhase {
        CIRCLE, SWOOP
    }
}
