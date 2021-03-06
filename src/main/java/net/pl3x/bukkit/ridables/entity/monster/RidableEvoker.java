package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityEvoker;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntityIronGolem;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntitySheep;
import net.minecraft.server.v1_13_R2.EntityVillager;
import net.minecraft.server.v1_13_R2.EnumDirection;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.VoxelShape;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.EvokerConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.ai.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.ai.controller.LookController;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIAttackNearest;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIAvoidTarget;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIHurtByTarget;
import net.pl3x.bukkit.ridables.entity.ai.goal.AISwim;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIWander;
import net.pl3x.bukkit.ridables.entity.ai.goal.AIWatchClosest;
import net.pl3x.bukkit.ridables.entity.ai.goal.evoker.AIEvokerCastingSpell;
import net.pl3x.bukkit.ridables.entity.ai.goal.evoker.AIEvokerFangsSpell;
import net.pl3x.bukkit.ridables.entity.ai.goal.evoker.AIEvokerSummonSpell;
import net.pl3x.bukkit.ridables.entity.ai.goal.evoker.AIEvokerWololoSpell;
import net.pl3x.bukkit.ridables.entity.projectile.CustomEvokerFangs;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class RidableEvoker extends EntityEvoker implements RidableEntity {
    public static final EvokerConfig CONFIG = new EvokerConfig();

    private static Field wololoTarget;

    static {
        try {
            wololoTarget = EntityEvoker.class.getDeclaredField("c");
            wololoTarget.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    private int spellCooldown = 0;

    public RidableEvoker(World world) {
        super(world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);
    }

    @Override
    public RidableType getType() {
        return RidableType.EVOKER;
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
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(CONFIG.AI_FOLLOW_RANGE);
    }

    // initAI - override vanilla AI
    @Override
    protected void n() {
        goalSelector.a(0, new AISwim(this));
        goalSelector.a(1, new AIEvokerCastingSpell(this));
        goalSelector.a(2, new AIAvoidTarget<>(this, EntityHuman.class, 8.0F, 0.6D, 1.0D));
        goalSelector.a(4, new AIEvokerSummonSpell(this));
        goalSelector.a(5, new AIEvokerFangsSpell(this));
        goalSelector.a(6, new AIEvokerWololoSpell(this));
        goalSelector.a(8, new AIWander(this, 0.6D));
        goalSelector.a(9, new AIWatchClosest(this, EntityHuman.class, 3.0F, 1.0F));
        goalSelector.a(10, new AIWatchClosest(this, EntityInsentient.class, 8.0F));
        targetSelector.a(1, new AIHurtByTarget(this, true, EntityEvoker.class));
        targetSelector.a(2, new AIAttackNearest<>(this, EntityHuman.class, true).b(300)); // setUnseenMemoryTicks
        targetSelector.a(3, new AIAttackNearest<>(this, EntityVillager.class, false).b(300)); // setUnseenMemoryTicks
        targetSelector.a(3, new AIAttackNearest<>(this, EntityIronGolem.class, false));
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

    public int getSpellTicks() {
        return b;
    }

    public void setSpellTicks(int ticks) {
        b = ticks;
    }

    public EntitySheep getWololoTarget() {
        try {
            return (EntitySheep) wololoTarget.get(this);
        } catch (IllegalAccessException ignore) {
        }
        return null;
    }

    public void setWololoTarget(EntitySheep sheep) {
        try {
            wololoTarget.set(this, sheep);
        } catch (IllegalAccessException ignore) {
        }
    }

    public int getHorizontalFaceSpeed() {
        return L();
    }

    public int getVerticalFaceSpeed() {
        return K();
    }

    @Override
    protected void mobTick() {
        if (spellCooldown > 0) {
            spellCooldown--;
        }
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

    @Override
    public boolean onClick(org.bukkit.entity.Entity entity, EnumHand hand) {
        return handleClick(hand);
    }

    @Override
    public boolean onClick(Block block, BlockFace blockFace, EnumHand hand) {
        return handleClick(hand);
    }

    @Override
    public boolean onClick(EnumHand hand) {
        return handleClick(hand);
    }

    private boolean handleClick(EnumHand hand) {
        if (spellCooldown == 0) {
            EntityPlayer rider = getRider();
            if (rider != null) {
                return castSpell(rider, hand == EnumHand.OFF_HAND);
            }
        }
        return false;
    }

    /**
     * Cast fangs spell at target location
     *
     * @param rider  Rider of this evoker
     * @param circle True if fangs spawn in circle, false for straight line
     * @return True if spell was successfully cast
     */
    public boolean castSpell(EntityPlayer rider, boolean circle) {
        spellCooldown = CONFIG.RIDING_FANGS_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("allow.special.evoker")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(25);
        double y = locY + direction.getY();
        castFangs(rider, direction.getX(), direction.getZ(), Math.min(y, locY), Math.max(y, locY) + 1.0D, circle);
        return true;
    }

    public void castFangs(EntityPlayer rider, double x, double z, double minY, double maxY, boolean circle) {
        float distance = (float) MathHelper.c((locZ + z) - locZ, (locX + x) - locX);
        if (circle) {
            for (int i = 0; i < 5; ++i) {
                float rotationYaw = distance + (float) i * Const.PI_FLOAT * 0.4F;
                spawnFang(rider, locX + (double) MathHelper.cos(rotationYaw) * 1.5D, locZ + (double) MathHelper.sin(rotationYaw) * 1.5D, minY, maxY, rotationYaw, 0);
            }
            for (int i = 0; i < 8; ++i) {
                float rotationYaw = distance + (float) i * Const.PI_FLOAT * 2.0F / 8.0F + ((float) Math.PI * 2F / 5F);
                spawnFang(rider, locX + (double) MathHelper.cos(rotationYaw) * 2.5D, locZ + (double) MathHelper.sin(rotationYaw) * 2.5D, minY, maxY, rotationYaw, 3);
            }
        } else {
            for (int i = 0; i < 16; ++i) {
                double d2 = 1.25D * (double) (i + 1);
                spawnFang(rider, locX + (double) MathHelper.cos(distance) * d2, locZ + (double) MathHelper.sin(distance) * d2, minY, maxY, distance, i);
            }
        }
    }

    private void spawnFang(EntityPlayer rider, double x, double z, double minY, double maxY, float rotationYaw, int warmupDelayTicks) {
        BlockPosition pos = new BlockPosition(x, maxY, z);
        do {
            if (!world.q(pos) && world.q(pos.down())) { // !isTopSolid(pos) && isTopSolid(pos.down())
                double yOffset = 0.0D;
                if (!world.isEmpty(pos)) { // !world.isAirBlock
                    VoxelShape shape = world.getType(pos).getCollisionShape(world, pos);
                    if (!shape.isEmpty()) {
                        yOffset = shape.c(EnumDirection.EnumAxis.Y); // shape.getEnd
                    }
                }
                CustomEvokerFangs fangs = new CustomEvokerFangs(world, x, (double) pos.getY() + yOffset, z, rotationYaw, warmupDelayTicks, this, rider);
                world.addEntity(fangs);
                break;
            }
            pos = pos.down();
        } while (pos.getY() >= MathHelper.floor(minY) - 1);
    }
}
