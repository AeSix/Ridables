package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityEvoker;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.VoxelShape;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.EvokerConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.CustomEvokerFangs;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RidableEvoker extends EntityEvoker implements RidableEntity {
    private static EvokerConfig config;

    private int spellCooldown = 0;

    public RidableEvoker(EntityTypes<? extends EntityEvoker> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.EVOKER;
    }

    @Override
    public EvokerConfig getConfig() {
        return (EvokerConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        // TODO - tame these new AI pathfinders
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
        if (spellCooldown > 0) {
            spellCooldown--;
        }
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
    public boolean onClick(org.bukkit.entity.Entity entity, EnumHand hand) {
        return handleClick(hand);
    }

    @Override
    public boolean onClick(org.bukkit.block.Block block, BlockFace blockFace, EnumHand hand) {
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
        spellCooldown = config.RIDING_FANGS_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("ridables.special.evoker")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(25);
        double y = locY + direction.getY();
        castFangs(rider, direction.getX(), direction.getZ(), Math.min(y, locY), Math.max(y, locY) + 1.0D, circle);
        return true;
    }

    public void castFangs(EntityPlayer rider, double x, double z, double minY, double maxY, boolean circle) {
        float distance = (float) MathHelper.d((locZ + z) - locZ, (locX + x) - locX);
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
            BlockPosition posDown = pos.down();
            if (Block.d(world.getType(posDown), world, posDown, EnumDirection.UP)) {
                double yOffset = 0.0D;
                if (!world.isEmpty(pos)) { // !world.isAirBlock
                    VoxelShape shape = world.getType(pos).getCollisionShape(world, pos);
                    if (!shape.isEmpty()) {
                        yOffset = shape.c(EnumDirection.EnumAxis.Y); // shape.getEnd
                    }
                }
                world.addEntity(new CustomEvokerFangs(world, x, (double) pos.getY() + yOffset, x, rotationYaw, warmupDelayTicks, this, rider));
                break;
            }
            pos = posDown;
        } while (pos.getY() >= MathHelper.floor(minY) - 1);
    }
}
