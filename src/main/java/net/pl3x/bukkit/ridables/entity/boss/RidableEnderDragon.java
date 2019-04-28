package net.pl3x.bukkit.ridables.entity.boss;

import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.DragonControllerPhase;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityComplexPart;
import net.minecraft.server.v1_14_R1.EntityEnderCrystal;
import net.minecraft.server.v1_14_R1.EntityEnderDragon;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumMoveType;
import net.minecraft.server.v1_14_R1.Explosion;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IDragonController;
import net.minecraft.server.v1_14_R1.IEntitySelector;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.LootContextParameters;
import net.minecraft.server.v1_14_R1.LootTableInfo;
import net.minecraft.server.v1_14_R1.Material;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.TagsBlock;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.pl3x.bukkit.ridables.configuration.mob.EnderDragonConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.List;

public class RidableEnderDragon extends EntityEnderDragon implements RidableEntity {
    private static EnderDragonConfig config;

    private Explosion explosionSource = new Explosion(null, this, Double.NaN, Double.NaN, Double.NaN, Float.NaN, true, Explosion.Effect.DESTROY);
    private boolean hadRider;

    public RidableEnderDragon(EntityTypes<? extends EntityEnderDragon> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new EnderDragonWASDController(this);
        lookController = new EnderDragonLookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.ENDER_DRAGON;
    }

    @Override
    public EnderDragonConfig getConfig() {
        return (EnderDragonConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return false;
    }

    // canBeRidden
    @Override
    protected boolean n(Entity entity) {
        return j <= 0; // rideCooldown
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        checkMove();
    }

    // onLivingUpdate
    @Override
    public void movementTick() {
        // ender dragon doesnt use the controllers so call manually
        moveController.a();
        lookController.a();

        boolean hasRider = getRider() != null;
        if (hasRider) {
            if (!hadRider) {
                hadRider = true;
                noclip = false;
                //setSize(4.0F, 2.0F); gone?
            }

            a(-ControllerWASD.getStrafe(this), ControllerWASD.getVertical(this), -ControllerWASD.getForward(this), (float) getRidingSpeed() * 0.1F); // moveRelative
            move(EnumMoveType.PLAYER, getMot());

            setMot(getMot().a((double) 0.9F));

            // control wing flap speed on client
            Vec3D mot = getMot();
            getDragonControllerManager().setControllerPhase(mot.x * mot.x + mot.z * mot.z < 0.005F ? DragonControllerPhase.HOVER : DragonControllerPhase.HOLDING_PATTERN);
        } else if (hadRider) {
            hadRider = false;
            noclip = true;
            //setSize(16.0F, 8.0F); gone?
            getDragonControllerManager().setControllerPhase(DragonControllerPhase.HOLDING_PATTERN); // HoldingPattern
        }
        super_movementTick(hasRider);
    }

    @Override
    public boolean onSpacebar() {
        // TODO flames!
        return true;
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

    // onLivingUpdate (modified from super class)
    private void super_movementTick(boolean hasRider) {
        bI = bJ; // prevAnimTime = animTime
        if (getHealth() <= 0.0F) {
            if (hasRider) {
                ejectPassengers();
            }
            world.addParticle(Particles.EXPLOSION,
                    locX + ((random.nextFloat() - 0.5F) * 8.0F),
                    locY + 2.0D + ((random.nextFloat() - 0.5F) * 4.0F),
                    locZ + ((random.nextFloat() - 0.5F) * 8.0F),
                    0.0D, 0.0D, 0.0D);
            return;
        }
        updateDragonEnderCrystal();
        float animTime = (0.2F / (MathHelper.sqrt(b(getMot())) * 10.0F + 1.0F)) * (float) Math.pow(2.0D, getMot().y);
        if (!hasRider && getDragonControllerManager().a().a()) { // getCurrentPhase getIsStationary
            bJ += 0.1F; // animTime
        } else if (!hasRider && bK) { // isSlowed
            bJ += animTime * 0.5F; // animTime
        } else {
            bJ += animTime; // animTime
        }
        yaw = MathHelper.g(yaw); // wrapDegrees
        if (isNoAI()) {
            bJ = 0.5F; // animTime
            return;
        }
        if (d < 0) { // ringBufferIndex
            for (int i = 0; i < c.length; ++i) { // ringBuffer
                c[i][0] = (double) yaw; // ringBuffer
                c[i][1] = locY; // ringBuffer
            }
        }
        if (++d == c.length) { // ringBufferIndex ringBuffer
            d = 0; // ringBufferIndex
        }
        c[d][0] = (double) yaw; // ringBuffer ringBufferIndex
        c[d][1] = locY; // ringBuffer ringBufferIndex
        if (!hasRider) {
            IDragonController phase = getDragonControllerManager().a(); // getPhase
            phase.c(); // serverTick
            if (getDragonControllerManager().a() != phase) { // getCurrentPhase
                phase = getDragonControllerManager().a(); // getCurrentPhase
                phase.c(); // serverTick
            }
            Vec3D targetLoc = phase.g(); // getTargetLocation
            if (targetLoc != null && phase.getControllerPhase() != DragonControllerPhase.HOVER) {
                double x = targetLoc.x - locX;
                double y = targetLoc.y - locY;
                double z = targetLoc.z - locZ;
                double maxRiseOrFall = (double) phase.f(); // getMaxRiseOrFall
                double distance = (double) MathHelper.sqrt(x * x + z * z);
                if (distance > 0.0D) {
                    y = MathHelper.a(y / distance, -maxRiseOrFall, maxRiseOrFall); // clamp
                }
                setMot(getMot().add(0.0D, y * 0.01D, 0.0D));
                yaw = MathHelper.g(yaw); // wrapDegrees
                double d5 = MathHelper.a(MathHelper.g(180.0D - MathHelper.d(x, z) * Const.RAD2DEG - (double) yaw), -50.0D, 50.0D); // clamp wrapDegrees atan2
                Vec3D vecDiff = targetLoc.a(locX, locY, locZ).d(); // normalize
                Vec3D vecRot = new Vec3D((double) MathHelper.sin(yaw * Const.DEG2RAD_FLOAT), getMot().y, (double) (-MathHelper.cos(yaw * Const.DEG2RAD_FLOAT))).d(); // normalize
                float max = Math.max(((float) vecRot.b(vecDiff) + 0.5F) / 1.5F, 0.0F); // dotProduct
                be = (float) ((double) (be * 0.8F) + d5 * (double) phase.h()); // randomYawVelocity getYawFactor
                yaw += be * 0.1F; // randomYawVelocity
                float diff = (float) (2.0D / ((x * x + y * y + z * z) + 1.0D));
                a(0.06F * (max * diff + (1.0F - diff)), new Vec3D(0.0D, 0.0D, -1.0D)); // moveRelative
                if (bK) { // slowed
                    move(EnumMoveType.SELF, getMot().a((double) 0.8F));
                } else {
                    move(EnumMoveType.SELF, getMot());
                }
                double friction = 0.8D + 0.15D * (getMot().d().b(vecRot) + 1.0D) / 2.0D;
                setMot(getMot().d(friction, (double) 0.91F, friction));
            }
        }
        aK = yaw; // renderYawOffset
        Vec3D[] dragonParts = new Vec3D[children.length];
        for (int j = 0; j < children.length; ++j) {
            dragonParts[j] = new Vec3D(children[j].locX, children[j].locY, children[j].locZ);
        }
        float offset = (float) (getMovementOffsets(5)[1] - getMovementOffsets(10)[1]) * 10.0F * Const.DEG2RAD_FLOAT;
        float offsetCos = MathHelper.cos(offset);
        float offsetSin = MathHelper.sin(offset);
        float offsetYaw = yaw * Const.DEG2RAD_FLOAT;
        float offsetYawSin = MathHelper.sin(offsetYaw);
        float offsetYawCos = MathHelper.cos(offsetYaw);
        bC.tick(); // body
        bC.setPositionRotation(locX + (double) (offsetYawSin * 0.5F), locY, locZ - (double) (offsetYawCos * 0.5F), 0.0F, 0.0F);
        bG.tick(); // wing1
        bG.setPositionRotation(locX + (double) (offsetYawCos * 4.5F), locY + 2.0D, locZ + (double) (offsetYawSin * 4.5F), 0.0F, 0.0F);
        bH.tick(); // wing2
        bH.setPositionRotation(locX - (double) (offsetYawCos * 4.5F), locY + 2.0D, locZ - (double) (offsetYawSin * 4.5F), 0.0F, 0.0F);
        if (hurtTicks == 0) {
            collideWithEntities(world.getEntities(this, bG.getBoundingBox().grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D), IEntitySelector.e)); // wing1
            collideWithEntities(world.getEntities(this, bH.getBoundingBox().grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0.0D), IEntitySelector.e)); // wing2
            attackEntitiesInList(world.getEntities(this, bA.getBoundingBox().g(1.0D), IEntitySelector.e)); // head
            attackEntitiesInList(world.getEntities(this, bB.getBoundingBox().g(1.0D), IEntitySelector.e)); // neck
        }
        double[] movementOffsets = getMovementOffsets(5);
        float randomYaw = yaw * Const.DEG2RAD_FLOAT - (be * 0.01F); // randomYawVelocity
        float randomYawSin = MathHelper.sin(randomYaw);
        float randomYawCos = MathHelper.cos(randomYaw);
        bA.tick(); // head
        bB.tick(); // neck
        float headYOffset = getHeadYOffset();
        bA.setPositionRotation( // head
                locX + (double) (randomYawSin * 6.5F * offsetCos),
                locY + (double) headYOffset + (double) (offsetSin * 6.5F),
                locZ - (double) (randomYawCos * 6.5F * offsetCos),
                0.0F, 0.0F);
        bB.setPositionRotation( // neck
                locX + (double) (randomYawSin * 5.5F * offsetCos),
                locY + (double) headYOffset + (double) (offsetSin * 5.5F),
                locZ - (double) (randomYawCos * 5.5F * offsetCos),
                0.0F, 0.0F);
        for (int k = 0; k < 3; ++k) {
            EntityComplexPart dragonPart = null;
            if (k == 0) dragonPart = bD; // tail1
            if (k == 1) dragonPart = bE; // tail2
            if (k == 2) dragonPart = bF; // tail3
            double[] movementOffsets1 = getMovementOffsets(12 + k * 2);
            float f15 = yaw * Const.DEG2RAD_FLOAT + (float) MathHelper.g(movementOffsets1[0] - movementOffsets[0]) * Const.DEG2RAD_FLOAT; // wrapDegrees
            float f4 = (float) (k + 1) * 2.0F;
            dragonPart.tick();
            dragonPart.setPositionRotation(
                    locX - (double) ((offsetYawSin * 1.5F + MathHelper.sin(f15) * f4) * offsetCos),
                    locY + (movementOffsets1[1] - movementOffsets[1]) - (double) ((f4 + 1.5F) * offsetSin) + 1.5D,
                    locZ + (double) ((offsetYawCos * 1.5F + MathHelper.cos(f15) * f4) * offsetCos),
                    0.0F, 0.0F);
        }
        bK = !hasRider && destroyBlocks(bA.getBoundingBox()) | destroyBlocks(bB.getBoundingBox()) | destroyBlocks(bC.getBoundingBox()); // slowed head neck body
        if (getEnderDragonBattle() != null) {
            getEnderDragonBattle().b(this); // dragonUpdate
        }
        for (int k = 0; k < children.length; ++k) {
            children[k].lastX = dragonParts[k].x;
            children[k].lastY = dragonParts[k].y;
            children[k].lastZ = dragonParts[k].z;
        }
    }

    private void updateDragonEnderCrystal() {
        if (currentEnderCrystal != null) {
            if (currentEnderCrystal.dead) {
                currentEnderCrystal = null;
            } else if (ticksLived % 10 == 0 && getHealth() < getMaxHealth()) {
                EntityRegainHealthEvent event = new EntityRegainHealthEvent(getBukkitEntity(), 1.0F, EntityRegainHealthEvent.RegainReason.ENDER_CRYSTAL);
                world.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    setHealth((float) (getHealth() + event.getAmount()));
                }
            }
        }
        if (random.nextInt(10) == 0) {
            List<EntityEnderCrystal> nearbyCrystals = world.a(EntityEnderCrystal.class, getBoundingBox().g(32.0D)); // getEntitiesWithinAABB
            EntityEnderCrystal closestCrystal = null;
            double closestDistance = Double.MAX_VALUE;
            for (EntityEnderCrystal crystal : nearbyCrystals) {
                double distance = crystal.h(this); // getDistanceSq
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCrystal = crystal;
                }
            }
            currentEnderCrystal = closestCrystal;
        }
    }

    private boolean destroyBlocks(AxisAlignedBB aabb) {
        int minX = MathHelper.floor(aabb.minX);
        int minY = MathHelper.floor(aabb.minY);
        int minZ = MathHelper.floor(aabb.minZ);
        int maxX = MathHelper.floor(aabb.maxX);
        int maxY = MathHelper.floor(aabb.maxY);
        int maxZ = MathHelper.floor(aabb.maxZ);
        boolean hitHardBlock = false;
        boolean brokeBlock = false;
        List<org.bukkit.block.Block> destroyedBlocks = new java.util.ArrayList<>();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPosition pos = new BlockPosition(x, y, z);
                    IBlockData state = world.getType(pos);
                    if (state.isAir() || state.getMaterial() == Material.FIRE) {
                        continue;
                    }
                    Block block = state.getBlock();
                    if (this.world.getGameRules().getBoolean("mobGriefing") && !TagsBlock.DRAGON_IMMUNE.isTagged(block)) {
                        brokeBlock = true;
                        destroyedBlocks.add(CraftBlock.at(world, pos));
                    } else {
                        hitHardBlock = true;
                    }
                }
            }
        }
        org.bukkit.entity.Entity dragon = getBukkitEntity();
        EntityExplodeEvent event = new EntityExplodeEvent(dragon, dragon.getLocation(), destroyedBlocks, 0F);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return hitHardBlock;
        }
        if (event.getYield() == 0F) {
            // Yield zero ==> no drops
            for (org.bukkit.block.Block block : event.blockList()) {
                world.a(new BlockPosition(block.getX(), block.getY(), block.getZ()), false); // setAir
            }
        } else {
            for (org.bukkit.block.Block block : event.blockList()) {
                org.bukkit.Material blockId = block.getType();
                if (blockId == org.bukkit.Material.AIR) {
                    continue;
                }
                CraftBlock craftBlock = ((CraftBlock) block);
                BlockPosition pos = craftBlock.getPosition();
                Block nmsBlock = craftBlock.getNMS().getBlock();
                if (nmsBlock.a(explosionSource)) {
                    TileEntity tileentity = nmsBlock.isTileEntity() ? world.getTileEntity(pos) : null;
                    LootTableInfo.Builder lootTable = (new LootTableInfo.Builder((WorldServer) world))
                            .a(world.random)
                            .set(LootContextParameters.POSITION, pos)
                            .set(LootContextParameters.TOOL, ItemStack.a)
                            .set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / event.getYield())
                            .setOptional(LootContextParameters.BLOCK_ENTITY, tileentity);
                    Block.b(craftBlock.getNMS(), lootTable);
                }
                nmsBlock.wasExploded(world, pos, explosionSource);
                world.a(pos, false); // setAir
            }
        }
        if (brokeBlock) {
            world.triggerEffect(2008, new BlockPosition(
                    minX + random.nextInt(maxX - minX + 1),
                    minY + random.nextInt(maxY - minY + 1),
                    minZ + random.nextInt(maxZ - minZ + 1)), 0);
        }
        return hitHardBlock;
    }

    private void collideWithEntities(List<Entity> entities) {
        double xDiff = (bC.getBoundingBox().minX + bC.getBoundingBox().maxX) / 2.0D; // body
        double zDiff = (bC.getBoundingBox().minZ + bC.getBoundingBox().maxZ) / 2.0D; // body
        for (Entity entity : entities) {
            if (entity instanceof EntityLiving) {
                double x = entity.locX - xDiff;
                double z = entity.locZ - zDiff;
                double distance = x * x + z * z;
                entity.f(x / distance * 4.0D, (double) 0.2F, z / distance * 4.0D); // addVelocity
                if (!getDragonControllerManager().a().a() && ((EntityLiving) entity).cs() < entity.ticksLived - 2) { // getCurrentPhase getIsStationary
                    entity.damageEntity(DamageSource.mobAttack(this), 5.0F);
                    a(this, entity); // applyEnchantments
                }
            }
        }
    }

    private void attackEntitiesInList(List<Entity> entities) {
        for (Entity target : entities) {
            if (target instanceof EntityLiving) {
                target.damageEntity(DamageSource.mobAttack(this), 10.0F);
                a(this, target); // applyEnchantments
            }
        }
    }

    private float getHeadYOffset() {
        if (getDragonControllerManager().a().a()) { // getCurrentPhase getIsStationary
            return (float) -1.0D;
        }
        return (float) (getMovementOffsets(5)[1] - getMovementOffsets(0)[1]);
    }

    private double[] getMovementOffsets(int i) {
        return a(i, 1.0F); // getMovementOffsets
    }

    class EnderDragonLookController extends LookController {
        EnderDragonLookController(RidableEntity ridable) {
            super(ridable);
        }

        @Override
        public void tick() {
            // dragon doesn't use the controller. do nothing
        }

        @Override
        public void tick(EntityPlayer rider) {
            setYawPitch(rider.yaw - 180F, rider.pitch * 0.5F);
        }
    }

    class EnderDragonWASDController extends ControllerWASDFlying {
        EnderDragonWASDController(RidableEnderDragon dragon) {
            super(dragon);
        }

        @Override
        public void tick() {
            // dragon doesn't use the controller. do nothing
        }
    }
}
