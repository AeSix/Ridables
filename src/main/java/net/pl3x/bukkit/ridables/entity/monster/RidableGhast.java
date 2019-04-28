package net.pl3x.bukkit.ridables.entity.monster;

import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityGhast;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLargeFireball;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.GhastConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.CustomFireball;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.EnumSet;

public class RidableGhast extends EntityGhast implements RidableEntity {
    private static GhastConfig config;

    private int spacebarCooldown = 0;

    public RidableGhast(EntityTypes<? extends EntityGhast> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new GhastWASDController(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.GHAST;
    }

    @Override
    public GhastConfig getConfig() {
        return (GhastConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        PathfinderGoal ghastIdleMove = new PathfinderGoal() { // EntityGhast.PathfinderGoalGhastIdleMove
            public boolean a() { // shouldExecute
                if (getRider() != null) {
                    return false;
                }
                ControllerMove controllermove = getControllerMove();
                if (!controllermove.b()) {
                    return true;
                } else {
                    double x = controllermove.d() - locX;
                    double y = controllermove.e() - locY;
                    double z = controllermove.f() - locZ;
                    double distance = x * x + y * y + z * z;
                    return distance < 1.0D || distance > 3600.0D;
                }
            }

            public boolean b() { // shouldContinueExecuting
                return false;
            }

            public void c() { // startExecuting
                double d0 = locX + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                double d1 = locY + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                double d2 = locZ + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                getControllerMove().a(d0, d1, d2, 1.0D);
            }
        };
        ghastIdleMove.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        goalSelector.a(5, ghastIdleMove);
        goalSelector.a(7, new PathfinderGoal() { // EntityGhast.PathfinderGoalGhastMoveTowardsTarget
            public boolean a() { // shouldExecute
                return getRider() == null;
            }

            public void e() { // tick
                EntityLiving target = getGoalTarget();
                if (target == null) {
                    Vec3D mot = getMot();
                    aK = yaw = -((float) MathHelper.d(mot.x, mot.z)) * Const.RAD2DEG_FLOAT;
                } else if (target.h(RidableGhast.this) < 4096.0D) { // getDistanceSq
                    aK = yaw = -((float) MathHelper.d(target.locX - locX, target.locZ - locZ)) * Const.RAD2DEG_FLOAT;
                }
            }
        });
        goalSelector.a(7, new PathfinderGoal() { // EntityGhast.PathfinderGoalGhastAttackTarget
            private int attackTimer;

            public boolean a() { // shouldExecute
                return getRider() == null && getGoalTarget() != null;
            }

            public void c() { // startExecuting
                attackTimer = 0;
            }

            public void d() { // resetTask
                r(false); // setAttacking
            }

            public void e() { // tick
                EntityLiving target = getGoalTarget();
                if (target != null && target.h(RidableGhast.this) < 4096.0D && hasLineOfSight(target)) { // getDistanceSq
                    ++attackTimer;
                    if (attackTimer == 10) {
                        world.a(null, 1015, new BlockPosition(RidableGhast.this), 0); // playEvent
                    }
                    if (attackTimer == 20) {
                        Vec3D vec3d = f(1.0F); // getLook
                        world.a(null, 1016, new BlockPosition(RidableGhast.this), 0); // playEvent
                        EntityLargeFireball fireball = new EntityLargeFireball(world, RidableGhast.this,
                                target.locX - (locX + vec3d.x * 4.0D),
                                target.getBoundingBox().minY + (double) (target.getHeight() / 2.0F) - (0.5D + locY + (double) (getHeight() / 2.0F)),
                                target.locZ - (locZ + vec3d.z * 4.0D));
                        fireball.bukkitYield = fireball.yield = getPower();
                        fireball.locX = locX + vec3d.x * 4.0D;
                        fireball.locY = locY + (double) (getHeight() / 2.0F) + 0.5D;
                        fireball.locZ = locZ + vec3d.z * 4.0D;
                        world.addEntity(fireball);
                        attackTimer = -40;
                    }
                } else if (attackTimer > 0) {
                    --attackTimer;
                }
                r(attackTimer > 10); // setAttacking
            }
        });
        targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<EntityHuman>(this, EntityHuman.class, 10, true, false,
                (entityliving) -> Math.abs(entityliving.locY - this.locY) <= 4.0D) {
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
        if (spacebarCooldown > 0) {
            spacebarCooldown--;
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
    public boolean onSpacebar() {
        if (spacebarCooldown == 0) {
            return shoot(getRider());
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        spacebarCooldown = config.RIDING_FIREBALL_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.ghast")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection()
                .normalize().multiply(25).add(new Vector(0, 2.5, 0)).normalize().multiply(25);

        a(SoundEffects.ENTITY_GHAST_WARN, 1.0F, 1.0F);

        new BukkitRunnable() {
            @Override
            public void run() {
                CustomFireball fireball = new CustomFireball(world, RidableGhast.this, rider,
                        direction.getX(), direction.getY(), direction.getZ(),
                        config.RIDING_FIREBALL_SPEED, config.RIDING_FIREBALL_DAMAGE, config.RIDING_FIREBALL_EXPLOSION_GRIEF);
                fireball.isIncendiary = config.RIDING_FIREBALL_EXPLOSION_FIRE;
                world.addEntity(fireball);

                a(SoundEffects.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
            }
        }.runTaskLater(Ridables.getInstance(), 10);

        return true;
    }

    class GhastWASDController extends ControllerWASDFlying {
        private RidableGhast ghast;
        private int cooldown;

        GhastWASDController(RidableGhast ghast) {
            super(ghast);
            this.ghast = ghast;
        }

        @Override
        public void tick() {
            if (h == Operation.MOVE_TO && cooldown-- <= 0) {
                cooldown += ghast.getRandom().nextInt(5) + 2;
                Vec3D velocity = new Vec3D(b - ghast.locX, c - ghast.locY, d - ghast.locZ);
                double distance = velocity.f();
                velocity = velocity.d();
                if (isNotColliding(velocity, MathHelper.f(distance))) {
                    ghast.setMot(ghast.getMot().e(velocity.a(0.1D)));
                } else {
                    h = Operation.WAIT;
                }
            }
        }

        private boolean isNotColliding(Vec3D vec3d, int i) {
            AxisAlignedBB aabb = ghast.getBoundingBox();
            for (int j = 1; j < i; ++j) {
                aabb = aabb.b(vec3d);
                if (!ghast.world.getCubes(ghast, aabb)) {
                    return false;
                }
            }
            return true;
        }
    }
}
