package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityItem;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySnowman;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IMonster;
import net.minecraft.server.v1_14_R1.Item;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.SnowGolemConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.CustomSnowball;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.util.Vector;

public class RidableSnowGolem extends EntitySnowman implements RidableEntity {
    private static SnowGolemConfig config;

    private int shootCooldown;

    public RidableSnowGolem(EntityTypes<? extends EntitySnowman> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.SNOW_GOLEM;
    }

    @Override
    public SnowGolemConfig getConfig() {
        return (SnowGolemConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(1, new PathfinderGoalArrowAttack(this, 1.25D, 20, 10.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(2, new PathfinderGoalRandomStrollLand(this, 1.0D, 1.0000001E-5F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalRandomLookaround(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityInsentient>(this, EntityInsentient.class, 10, true, false,
                (entityliving) -> entityliving instanceof IMonster) {
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

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        return getRider() == null ? super.cW() : config.RIDING_JUMP_POWER;
    }

    @Override
    protected void mobTick() {
        K = getRider() == null ? 0.6F : config.RIDING_STEP_HEIGHT;
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        super.mobTick();
    }

    // onLivingUpdate
    @Override
    public void movementTick() {
        super.movementTick();
        boolean hasRider = getRider() != null;
        if (at()) { // isWet
            if (hasRider) {
                if (config.RIDING_DAMAGE_WHEN_WET > 0.0F) {
                    damageEntity(DamageSource.DROWN, config.RIDING_DAMAGE_WHEN_WET);
                }
            } else {
                damageEntity(DamageSource.DROWN, 1.0F);
            }
        }
        int x = MathHelper.floor(locX);
        int y = MathHelper.floor(locY);
        int z = MathHelper.floor(locZ);
        if (world.getBiome(new BlockPosition(x, 0, z)).getAdjustedTemperature(new BlockPosition(x, y, z)) > 1.0F) {
            if (hasRider) {
                if (config.RIDING_DAMAGE_WHEN_HOT > 0.0F) {
                    damageEntity(CraftEventFactory.MELTING, config.RIDING_DAMAGE_WHEN_HOT);
                }
            } else {
                damageEntity(CraftEventFactory.MELTING, 1.0F);
            }
        }
        if (!world.getGameRules().getBoolean("mobGriefing")) {
            return; // not allowed to grief world (gamerule trumps all)
        }
        if (hasRider && !config.RIDING_SNOW_TRAIL_ENABLED) {
            return; // not allowed to leave trail while riding
        }
        IBlockData block = Blocks.SNOW.getBlockData();
        for (int l = 0; l < 4; ++l) {
            BlockPosition pos = new BlockPosition(
                    MathHelper.floor(locX + (double) ((float) (l % 2 * 2 - 1) * 0.25F)),
                    MathHelper.floor(locY),
                    MathHelper.floor(locZ + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F)));
            if (world.getType(pos).isAir() && block.canPlace(world, pos)) {
                float temp = world.getBiome(pos).getAdjustedTemperature(pos);
                if (temp < (hasRider ? config.RIDING_SNOW_TRAIL_MAX_TEMP : 0.8F)) {
                    CraftEventFactory.handleBlockFormEvent(world, pos, block, this);
                }
            }
        }
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
        if (passengers.isEmpty()) {
            ItemStack itemstack = entityhuman.b(hand);
            if (hasPumpkin()) {
                if (itemstack.getItem() == Items.SHEARS) {
                    PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player) entityhuman.getBukkitEntity(), getBukkitEntity());
                    world.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return false; // plugin cancelled
                    }
                    setHasPumpkin(false);
                    itemstack.damage(1, entityhuman, entityhuman1 -> entityhuman1.d(hand));
                    if (config.SHEARS_DROPS_PUMPKIN) {
                        EntityItem pumpkin = new EntityItem(world, locX, locY, locZ, new ItemStack(Blocks.PUMPKIN.getItem()));
                        pumpkin.pickupDelay = 10;
                        world.addEntity(pumpkin, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    }
                    return true; // handled
                }
            } else {
                if (config.ADD_PUMPKIN_TO_HEAD && isPumpkin(itemstack)) {
                    setHasPumpkin(true);
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        itemstack.subtract(1);
                    }
                    return true; // handled
                }
            }
            if (hand == EnumHand.MAIN_HAND && !entityhuman.isSneaking() && !entityhuman.isPassenger()) {
                return tryRide(entityhuman, config.RIDING_SADDLE_REQUIRE, config.RIDING_SADDLE_CONSUME);
            }
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

    private boolean isPumpkin(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item == Items.cF // Items.PUMPKIN
                || item == Items.cG // Items.CARVED_PUMPKIN
                || item == Items.cK; // Items.JACK_O_LANTERN
    }

    @Override
    public boolean onClick() {
        if (shootCooldown == 0) {
            return shoot(getRider());
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        shootCooldown = config.RIDING_SHOOT_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.snow_golem")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection()
                .normalize().multiply(25).add(new Vector(0, 2.5, 0)).normalize().multiply(25);

        a(SoundEffects.ENTITY_SNOW_GOLEM_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));

        CustomSnowball snowball = new CustomSnowball(world, this, rider, locX, locY + (double) getHeadHeight(), locZ);
        snowball.shoot(direction.getX(), direction.getY(), direction.getZ(), config.RIDING_SHOOT_SPEED, config.RIDING_SHOOT_INACCURACY);
        world.addEntity(snowball);

        return true;
    }
}
