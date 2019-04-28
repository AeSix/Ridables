package net.pl3x.bukkit.ridables.entity.boss;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.BossBattleServer;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityWither;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumMonsterType;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IEntitySelector;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.WitherConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.ai.goal.wither.AIWitherDoNothing;
import net.pl3x.bukkit.ridables.entity.projectile.CustomWitherSkull;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

public class RidableWither extends EntityWither implements RidableEntity {
    public static final WitherConfig CONFIG = new WitherConfig();
    private static final Predicate<Entity> NOT_UNDEAD = (entity) -> entity instanceof EntityLiving &&
            ((EntityLiving) entity).getMonsterType() != EnumMonsterType.UNDEAD && ((EntityLiving) entity).df();

    private int shootCooldown = 0;

    public RidableWither(EntityTypes<? extends EntityWither> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASDFlying(this);
        lookController = new LookController(this);
    }

    @Override
    public RidableType getType() {
        return RidableType.WITHER;
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        getAttributeMap().b(RidableType.RIDING_SPEED); // registerAttribute
        getAttributeMap().b(RidableType.RIDING_MAX_Y); // registerAttribute
        reloadAttributes();
    }

    @Override
    public void reloadAttributes() {
        getAttributeInstance(RidableType.RIDING_SPEED).setValue(CONFIG.RIDING_SPEED);
        getAttributeInstance(RidableType.RIDING_MAX_Y).setValue(CONFIG.RIDING_FLYING_MAX_Y);
        getAttributeInstance(GenericAttributes.maxHealth).setValue(CONFIG.MAX_HEALTH);
        getAttributeInstance(GenericAttributes.c).setValue(CONFIG.AI_KNOCKBACK_RESISTANCE); // KNOCKBACK_RESISTANCE
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(CONFIG.BASE_SPEED);
        getAttributeInstance(GenericAttributes.h).setValue(CONFIG.AI_ARMOR); // ARMOR
        getAttributeInstance(GenericAttributes.i).setValue(CONFIG.AI_ARMOR_TOUGHNESS); // ARMOR_TOUGHNESS
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(CONFIG.AI_FOLLOW_RANGE);
    }

    // canDespawn
    @Override
    public boolean isTypeNotPersistent() {
        return !hasCustomName() && !isLeashed();
    }

    // initAI - override vanilla AI
    @Override
    protected void n() {
        goalSelector.a(0, new AIWitherDoNothing(this));
        goalSelector.a(2, new AIAttackRanged(this, 1.0D, 40, 20.0F));
        goalSelector.a(5, new AIWanderAvoidWater(this, 1.0D));
        goalSelector.a(6, new AIWatchClosest(this, EntityHuman.class, 8.0F));
        goalSelector.a(7, new AILookIdle(this));
        targetSelector.a(1, new AIHurtByTarget(this, false));
        targetSelector.a(2, new AIAttackNearest<>(this, EntityInsentient.class, 0, false, false, NOT_UNDEAD));
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return false;
    }

    // canBeRidden
    @Override
    protected boolean n(Entity entity) {
        return k <= 0; // rideCooldown
    }

    @Override
    protected void mobTick() {
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        int invulTime = getInvulTime();
        if (invulTime > 0) {
            if (--invulTime <= 0) {
                if (CONFIG.AI_SPAWN_EXPLOSION_ENABLED) {
                    ExplosionPrimeEvent event = new ExplosionPrimeEvent(getBukkitEntity(), CONFIG.AI_SPAWN_EXPLOSION_RADIUS, CONFIG.AI_SPAWN_EXPLOSION_FIRE);
                    if (event.callEvent()) {
                        world.createExplosion(this, locX, locY + getHeadHeight(), locZ, event.getRadius(), event.getFire(), CONFIG.AI_SPAWN_EXPLOSION_GRIEF && world.getGameRules().getBoolean("mobGriefing"));
                    }
                }
                int soundRadius = world.spigotConfig.witherSpawnSoundRadius * world.spigotConfig.witherSpawnSoundRadius;
                int x, z;
                int y = (int) locY;
                for (EntityHuman human : world.players) {
                    EntityPlayer player = (EntityPlayer) human;
                    int viewDistance = player.getViewDistance();
                    double deltaX = locX - player.locX;
                    double deltaZ = locZ - player.locZ;
                    double distance = deltaX * deltaX + deltaZ * deltaZ;
                    if (world.spigotConfig.witherSpawnSoundRadius <= 0 || distance <= soundRadius) {
                        if (distance > viewDistance * viewDistance) {
                            double deltaLength = Math.sqrt(distance);
                            x = (int) (player.locX + (deltaX / deltaLength) * viewDistance);
                            z = (int) (player.locZ + (deltaZ / deltaLength) * viewDistance);
                        } else {
                            x = (int) locX;
                            z = (int) locZ;
                        }
                        player.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1023, new BlockPosition(x, y, z), 0, true));
                    }
                }
            }
            setInvulTime(invulTime);
            if (ticksLived % 10 == 0) {
                heal(10.0F, EntityRegainHealthEvent.RegainReason.WITHER_SPAWN);
            }
            return;
        }
        //super.mobTick(); // empty from EntityInsentient
        for (int i = 1; i < 3; ++i) {
            if (ticksLived >= getNextHeadUpdate(i - 1)) {
                setNextHeadUpdate(i - 1, ticksLived + 10 + random.nextInt(10));
                if (world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD) {
                    int idleHeadUpdate = getIdleHeadUpdate(i - 1);
                    setIdleHeadUpdate(i - 1, idleHeadUpdate + 1);
                    if (idleHeadUpdate > 15) {
                        shoot(i + 1,
                                MathHelper.a(random, locX - 10.0D, locX + 10.0D),
                                MathHelper.a(random, locY - 5.0D, locY + 5.0D),
                                MathHelper.a(random, locZ - 10.0D, locZ + 10.0D),
                                true, null);
                        setIdleHeadUpdate(i - 1, 0);
                    }
                }
                int targetId = getWatchedTargetId(i);
                if (targetId > 0) {
                    Entity target = world.getEntity(targetId);
                    if (target != null && target.isAlive() && h(target) <= 900.0D && hasLineOfSight(target)) { // getDistanceSq
                        if (target instanceof EntityHuman && ((EntityHuman) target).abilities.isInvulnerable) {
                            updateWatchedTargetId(i, 0);
                        } else {
                            shoot(i + 1, (EntityLiving) target);
                            setNextHeadUpdate(i - 1, ticksLived + 40 + random.nextInt(20));
                            setIdleHeadUpdate(i - 1, 0);
                        }
                    } else {
                        updateWatchedTargetId(i, 0);
                    }
                } else {
                    List<EntityLiving> list = world.a(EntityLiving.class, getBoundingBox().grow(20.0D, 8.0D, 20.0D), NOT_UNDEAD.and(IEntitySelector.notSpectator())); // getEntitiesWithinAABB
                    for (int i1 = 0; i1 < 10 && !list.isEmpty(); ++i1) {
                        EntityLiving target = list.get(random.nextInt(list.size()));
                        if (target != this && target.isAlive() && hasLineOfSight(target)) {
                            if (target instanceof EntityHuman) {
                                if (!((EntityHuman) target).abilities.isInvulnerable) {
                                    updateWatchedTargetId(i, target.getId());
                                }
                            } else {
                                updateWatchedTargetId(i, target.getId());
                            }
                            break;
                        }
                        list.remove(target);
                    }
                }
            }
        }
        updateWatchedTargetId(0, getGoalTarget() != null ? getGoalTarget().getId() : 0);
        int blockBreakCounter = getBlockBreakCounter();
        if (blockBreakCounter > 0) {
            setBlockBreakCounter(--blockBreakCounter);
            if (blockBreakCounter == 0 && world.getGameRules().getBoolean("mobGriefing")) {
                int startX = MathHelper.floor(locY);
                int startY = MathHelper.floor(locX);
                int startZ = MathHelper.floor(locZ);
                boolean destroyedBlock = false;
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        for (int y = 0; y <= 3; ++y) {
                            BlockPosition pos = new BlockPosition(startX + x, startY + y, startZ + z);
                            IBlockData state = world.getType(pos);
                            if (!state.isAir() && a(state.getBlock())) { // canDestroyBlock
                                if (!CraftEventFactory.callEntityChangeBlockEvent(this, pos, Blocks.AIR.getBlockData()).isCancelled()) {
                                    destroyedBlock = world.setAir(pos, true) || destroyedBlock;
                                }
                            }
                        }
                    }
                }
                if (destroyedBlock) {
                    world.a(null, 1022, new BlockPosition(this), 0); // playEvent
                }
            }
        }
        if (ticksLived % 20 == 0 && CONFIG.AI_REGEN_EVERY_SECOND > 0.0F) {
            heal(CONFIG.AI_REGEN_EVERY_SECOND, EntityRegainHealthEvent.RegainReason.REGEN);
        }
        getBossInfo().setProgress(getHealth() / getMaxHealth()); // bossInfo
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

    public int getInvulTime() {
        return dz(); // getInvulTime
    }

    public void setInvulTime(int invulTime) {
        d(invulTime); // setInvulTime
    }

    public int getWatchedTargetId(int head) {
        return p(head); // getWatchedTargetId
    }

    public void updateWatchedTargetId(int head, int newId) {
        a(head, newId); // updateWatchedTargetId
    }

    // getWatchedTargetId
    @Override
    public int p(int head) {
        return getRider() != null ? 0 : super.p(head);
    }

    // updateWatchedTargetId
    @Override
    public void a(int head, int newId) {
        if (getRider() == null) {
            super.a(head, newId);
        }
    }

    // attackEntityWithRangedAttack
    @Override
    public void a(EntityLiving target, float f) {
        shoot(0, target);
    }

    @Override
    public boolean onSpacebar() {
        return shoot(getRider(), new int[]{0, 1, 2});
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
        return shoot(getRider(), hand == EnumHand.MAIN_HAND ? new int[]{1} : new int[]{2});
    }

    public boolean shoot(EntityPlayer rider, int[] heads) {
        if (shootCooldown > 0) {
            return false;
        }

        shootCooldown = CONFIG.RIDING_SHOOT_COOLDOWN;
        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.wither")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Location loc = player.getTargetBlock(null, 120).getLocation();
        for (int head : heads) {
            shoot(head, loc.getX(), loc.getY(), loc.getZ(), false, rider);
        }

        return true;
    }

    private void shoot(int head, EntityLiving target) {
        shoot(head, target.locX, target.locY + target.getHeadHeight() * 0.5D, target.locZ, head == 0 && random.nextFloat() < 0.001F, null);
    }

    public void shoot(int head, double x, double y, double z, boolean invulnerable, EntityPlayer rider) {
        world.a(null, 1024, new BlockPosition(this), 0); // playEvent
        double headX = getHeadX(head);
        double headY = getHeadY(head);
        double headZ = getHeadZ(head);
        CustomWitherSkull skull = new CustomWitherSkull(world, this, rider, x - headX, y - headY, z - headZ);
        if (invulnerable) {
            // actually should be named setInvulnerable, not charged
            // invulnerable skulls move more slowly and blocks have better resistance against them
            skull.setCharged(true);
        }
        skull.locY = headY;
        skull.locX = headX;
        skull.locZ = headZ;
        world.addEntity(skull);
    }

    public double getHeadX(int head) {
        return head <= 0 ? locX : locX + (double) MathHelper.cos((aQ + (float) (180 * (head - 1))) * Const.DEG2RAD_FLOAT) * 1.3D;
    }

    public double getHeadY(int head) {
        return head <= 0 ? locY + 3.0D : locY + 2.2D;
    }

    public double getHeadZ(int head) {
        return head <= 0 ? locZ : locZ + (double) MathHelper.sin((aQ + (float) (180 * (head - 1))) * Const.DEG2RAD_FLOAT) * 1.3D;
    }

    private static Field bossInfo;
    private static Field blockBreakCounter;
    private static Field nextHeadUpdate;
    private static Field idleHeadUpdates;

    static {
        try {
            bossInfo = EntityWither.class.getDeclaredField("bL");
            bossInfo.setAccessible(true);
            blockBreakCounter = EntityWither.class.getDeclaredField("bK");
            blockBreakCounter.setAccessible(true);
            nextHeadUpdate = EntityWither.class.getDeclaredField("bI");
            nextHeadUpdate.setAccessible(true);
            idleHeadUpdates = EntityWither.class.getDeclaredField("bJ");
            idleHeadUpdates.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    private BossBattleServer getBossInfo() {
        try {
            return (BossBattleServer) bossInfo.get(this);
        } catch (IllegalAccessException ignore) {
        }
        return null;
    }

    private int getBlockBreakCounter() {
        try {
            return blockBreakCounter.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setBlockBreakCounter(int count) {
        try {
            blockBreakCounter.setInt(this, count);
        } catch (IllegalAccessException ignore) {
        }
    }

    private int getNextHeadUpdate(int head) {
        try {
            return ((int[]) nextHeadUpdate.get(this))[head];
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setNextHeadUpdate(int head, int value) {
        try {
            int[] arr = (int[]) nextHeadUpdate.get(this);
            arr[head] = value;
            nextHeadUpdate.set(this, arr);
        } catch (IllegalAccessException ignore) {
        }
    }

    private int getIdleHeadUpdate(int head) {
        try {
            return ((int[]) idleHeadUpdates.get(this))[head];
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private void setIdleHeadUpdate(int head, int value) {
        try {
            int[] arr = (int[]) idleHeadUpdates.get(this);
            arr[head] = value;
            idleHeadUpdates.set(this, arr);
        } catch (IllegalAccessException ignore) {
        }
    }
}
