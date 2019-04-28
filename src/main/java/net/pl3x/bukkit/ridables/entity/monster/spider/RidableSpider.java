package net.pl3x.bukkit.ridables.entity.monster.spider;

import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityIronGolem;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySpider;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLeapAtTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.SpiderConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.SpiderWeb;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RidableSpider extends EntitySpider implements RidableEntity {
    private static SpiderConfig config;

    private int shootCooldown = 0;

    public RidableSpider(EntityTypes<? extends EntitySpider> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.SPIDER;
    }

    @Override
    public SpiderConfig getConfig() {
        return (SpiderConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(3, new PathfinderGoalLeapAtTarget(this, 0.4F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new RidableSpider.AISpiderAttack(this));
        goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalRandomLookaround(this) {
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
        });
        targetSelector.a(2, new RidableSpider.AISpiderTarget<>(this, EntityHuman.class));
        targetSelector.a(3, new RidableSpider.AISpiderTarget<>(this, EntityIronGolem.class));
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
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        super.mobTick();
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion);
        if (positionChanged && isClimbing() && getRider() != null) {
            Vec3D mot = getMot();
            setMot(mot.z, 0.2D * config.RIDING_CLIMB_SPEED, mot.z);
        }
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
    public boolean isClimbing() {
        return (getRider() == null || config.RIDING_CLIMB_WALLS) && l(); // isBesideClimbableBlock
    }

    @Override
    public boolean onClick() {
        EntityPlayer rider = getRider();
        if (rider == null || !rider.b(EnumHand.MAIN_HAND).isEmpty()) {
            return false; // must have empty hands to shoot
        }
        if (shootCooldown == 0) {
            return shoot(rider);
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        shootCooldown = config.RIDING_SHOOT_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.spider")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection()
                .normalize().multiply(25).add(new Vector(0, 10, 0)).normalize().multiply(25);

        a(SoundEffects.ENTITY_SPIDER_STEP, 1.0F, 1.0F);

        SpiderWeb web = new SpiderWeb(world, this, rider, locX, locY, locZ);
        web.shoot(direction.getX(), direction.getY(), direction.getZ(), config.RIDING_SHOOT_SPEED);
        world.addEntity(web);

        return true;
    }

    static class AISpiderAttack extends PathfinderGoalMeleeAttack {
        private final RidableEntity spider;

        AISpiderAttack(RidableEntity spider) {
            super((EntitySpider) spider, 1.0D, true);
            this.spider = spider;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return spider.getRider() == null && super.a() && !a.isVehicle();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            if (spider.getRider() != null) {
                return false;
            }
            if (a.aE() >= 0.5F && a.getRandom().nextInt(100) == 0) {
                a.setGoalTarget(null);
                return false;
            }
            return super.b();
        }

        // getAttackReach
        @Override
        protected double a(EntityLiving target) {
            return (double) (4.0F + target.getWidth());
        }
    }

    static class AISpiderTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {
        private final RidableEntity spider;

        AISpiderTarget(RidableEntity spider, Class<T> targetClass) {
            super((EntitySpider) spider, targetClass, true);
            this.spider = spider;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return spider.getRider() == null && e.aE() < 0.5F && super.a();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return spider.getRider() == null && super.b();
        }
    }
}
