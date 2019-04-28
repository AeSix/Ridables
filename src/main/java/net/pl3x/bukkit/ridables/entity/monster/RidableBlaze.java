package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.AttributeInstance;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityBlaze;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.BlazeConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableFlyingEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlyingWithSpacebar;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.CustomFireball;
import net.pl3x.bukkit.ridables.event.BlazeShootFireballEvent;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.EnumSet;

public class RidableBlaze extends EntityBlaze implements RidableEntity, RidableFlyingEntity {
    private static BlazeConfig config;

    private int shootCooldown = 0;

    public RidableBlaze(EntityTypes<? extends EntityBlaze> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASDFlyingWithSpacebar(this, 0.5D);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.BLAZE;
    }

    @Override
    public BlazeConfig getConfig() {
        return (BlazeConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    public int getMaxY() {
        return config.RIDING_FLYING_MAX_Y;
    }

    @Override
    protected void initPathfinder() {
        PathfinderGoal blazeFireballAttack = new PathfinderGoal() {
            private EntityLiving target;
            private int attackStep;
            private int attackTime;

            public boolean a() { // shouldExecute
                target = getGoalTarget();
                return target != null && target.isAlive() && getRider() != null;
            }

            public boolean b() { // shouldContinueExecuting
                return a();
            }

            public void c() { // startExecuting
                attackStep = 0;
            }

            public void d() { // resetTask
                r(false); // setOnFire
            }

            public void e() { // tick
                --attackTime;
                double distance = h(target);
                if (distance < 4.0D) {
                    if (attackTime <= 0) {
                        attackTime = 20;
                        C(target); // attackEnemyAsMob
                    }
                    getControllerMove().a(target.locX, target.locY, target.locZ, 1.0D);
                } else if (distance < g() * g()) {
                    double x = target.locX - locX;
                    double y = target.getBoundingBox().minY + (double) (target.getHeight() / 2.0F) - (locY + (double) (getHeight() / 2.0F));
                    double z = target.locZ - locZ;
                    if (attackTime <= 0) {
                        ++attackStep;
                        if (attackStep == 1) {
                            attackTime = 60;
                            r(true); // setOnFire
                        } else if (attackStep <= 4) {
                            attackTime = 6;
                        } else {
                            attackTime = 100;
                            attackStep = 0;
                            r(false); // setOnFire
                        }
                        if (attackStep > 1) {
                            float f = MathHelper.c(MathHelper.sqrt(distance)) * 0.5F;
                            world.a(null, 1018, new BlockPosition((int) locX, (int) locY, (int) locZ), 0);
                            for (int i = 0; i < 1; ++i) {
                                CustomFireball fireball = new CustomFireball(world, RidableBlaze.this, null, x + random.nextGaussian() * (double) f, y, z + random.nextGaussian() * (double) f, 1, 0, false);
                                fireball.locY = locY + (double) (getHeight() / 2.0F) + 0.5D;
                                BlazeShootFireballEvent event = new BlazeShootFireballEvent(RidableBlaze.this, fireball);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    world.addEntity(fireball);
                                }
                            }
                        }
                    }
                    getControllerLook().a(target, 10.0F, 10.0F);
                } else {
                    getNavigation().o(); // clearPath
                    getControllerMove().a(target.locX, target.locY, target.locZ, 1.0D);
                }
                super.e();
            }

            private double g() { // getFollowDistance
                AttributeInstance range = getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
                return range == null ? 16.0D : range.getValue();
            }
        };
        blazeFireballAttack.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));

        goalSelector.a(4, blazeFireballAttack);
        goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D, 0.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new PathfinderGoalRandomLookaround(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalHurtByTarget(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        }.a(new Class[0]));
        targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, true) {
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

    @Override
    protected void mobTick() {
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        if (getRider() != null) {
            setMot(getMot().add(0.0D, bi > 0 ? 0.07D * config.RIDING_VERTICAL : 0.04704D - config.RIDING_GRAVITY, 0.0D));
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
        return handleClick();
    }

    @Override
    public boolean onClick(Block block, BlockFace blockFace, EnumHand hand) {
        return handleClick();
    }

    @Override
    public boolean onClick(EnumHand hand) {
        return handleClick();
    }

    private boolean handleClick() {
        if (shootCooldown == 0) {
            EntityPlayer rider = getRider();
            if (rider != null) {
                return shoot(rider);
            }
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        shootCooldown = config.RIDING_SHOOT_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = rider.getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.blaze")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(25).add(new Vector(0, 1, 0));

        CustomFireball fireball = new CustomFireball(world, this, rider, direction.getX(), direction.getY(), direction.getZ(),
                config.RIDING_SHOOT_SPEED, config.RIDING_SHOOT_IMPACT_DAMAGE, config.RIDING_SHOOT_GRIEF);
        fireball.isIncendiary = config.RIDING_SHOOT_EXPLOSION_FIRE;

        BlazeShootFireballEvent event = new BlazeShootFireballEvent(this, fireball);
        if (event.isCancelled()) {
            return false; // cancelled
        }

        world.addEntity(fireball);

        world.triggerEffect(1018, new BlockPosition(locX, locY, locZ), 0);
        a(SoundEffects.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F); // playSound

        return true;
    }
}
