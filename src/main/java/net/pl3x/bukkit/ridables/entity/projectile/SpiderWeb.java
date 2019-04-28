package net.pl3x.bukkit.ridables.entity.projectile;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityFallingBlock;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumMoveType;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class SpiderWeb extends EntityFallingBlock implements CustomProjectile {
    public static final IBlockData WEB = Blocks.COBWEB.getBlockData();

    private RidableEntity ridable;
    private EntityPlayer rider;

    public SpiderWeb(World world) {
        super(world);
    }

    public SpiderWeb(World world, RidableEntity ridable, EntityPlayer rider, double x, double y, double z) {
        super(world, x, y, z, WEB);
        this.ridable = ridable;
        this.rider = rider;
    }

    @Override
    public RidableEntity getRidable() {
        return ridable;
    }

    @Override
    public Mob getMob() {
        return ((EntityInsentient) ridable).getBukkitMob();
    }

    @Override
    public Player getRider() {
        return rider == null ? null : rider.getBukkitEntity();
    }

    @Override
    public void tick() {
        if (dead) {
            return;
        }

        if (ticksLived++ > 100) {
            die();
            return;
        }

        lastX = locX;
        lastY = locY;
        lastZ = locZ;

        if (!isNoGravity()) {
            motY -= (double) 0.04F;
        }

        move(EnumMoveType.SELF, motX, motY, motZ);

        if (!onGround) {
            if (D) { // collided (horizontally or vertically)
                if (C) { // collidedVertically
                    trySetAsBlock();
                } else if (positionChanged) { // collidedHorizontally
                    trySetAsBlock();
                }
            } else {
                for (Entity entity : world.getEntities(this, getBoundingBox())) {
                    if (entity != ridable && entity != rider) {
                        motX = motY = motZ = 0;
                        trySetAsBlock(entity);
                        return;
                    }
                }
            }
        } else {
            trySetAsBlock();
        }

        motX *= (double) 0.98F;
        motY *= (double) 0.98F;
        motZ *= (double) 0.98F;
    }

    // fall
    @Override
    public void c(float distance, float damageMultiplier) {
        // do nothing
    }

    // setInWeb
    @Override
    public void bh() {
        F = false; // isInWeb
    }

    public void trySetAsBlock() {
        trySetAsBlock(getPosition());
    }

    public void trySetAsBlock(BlockPosition pos) {
        IBlockData state = world.getType(pos);
        if (state.getBlock() == Blocks.MOVING_PISTON) {
            return; // let the piston push the web
        }
        die();
        boolean cancelled = CraftEventFactory.callEntityChangeBlockEvent((EntityInsentient) ridable, pos, WEB).isCancelled();
        if (!cancelled && rider != null) {
            cancelled = CraftEventFactory.callBlockPlaceEvent(world, rider, EnumHand.MAIN_HAND, CraftBlockState.getBlockState(world, pos), pos.getX(), pos.getY(), pos.getZ()).isCancelled();
        }
        if (state.getMaterial().isReplaceable() && !cancelled) {
            world.setTypeAndData(pos, WEB, 3);
        }
    }

    public void trySetAsBlock(Entity target) {
        BlockPosition pos = getPosition(target);
        trySetAsBlock(pos);
        if (target.getHeadHeight() > 1.5F) {
            trySetAsBlock(pos.up());
        }
        target.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public void shoot(double x, double y, double z, double speed) {
        double distance = (double) MathHelper.sqrt(x * x + y * y + z * z);
        motX = x / distance * speed;
        motY = y / distance * speed;
        motZ = z / distance * speed;
    }

    public BlockPosition getPosition() {
        return getPosition(this);
    }

    public BlockPosition getPosition(Entity entity) {
        return new BlockPosition((int) Math.round(entity.locX - 0.5D), (int) Math.round(entity.locY - 0.5D), (int) Math.round(entity.locZ - 0.5D));
    }
}
