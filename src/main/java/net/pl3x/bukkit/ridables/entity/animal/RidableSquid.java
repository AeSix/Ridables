package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntitySquid;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.pl3x.bukkit.ridables.configuration.mob.SquidConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDWater;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RidableSquid extends EntitySquid implements RidableEntity {
    private static SquidConfig config;

    private int spacebarCooldown = 0;

    public RidableSquid(EntityTypes<? extends EntitySquid> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new ControllerWASDWater(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.SQUID;
    }

    @Override
    public SquidConfig getConfig() {
        return (SquidConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoal() {
            public boolean a() { // shouldExecute
                return true;
            }

            public void e() { // tick
                EntityPlayer rider = getRider();
                if (rider == null) {
                    if (cv() > 100) { // getIdleTime
                        RidableSquid.this.a(0.0F, 0.0F, 0.0F); // setMovementVector
                    } else if (random.nextInt(50) == 0 || !inWater || !l()) { // hasMovementVector
                        float randYaw = random.nextFloat() * Const.TWOPI_FLOAT;
                        RidableSquid.this.a(MathHelper.cos(randYaw) * 0.2F, -0.1F + random.nextFloat() * 0.2F, MathHelper.sin(randYaw) * 0.2F); // setMovementVector
                    }
                } else {
                    if (ControllerWASD.isJumping(rider)) {
                        RidableSpacebarEvent event = new RidableSpacebarEvent(RidableSquid.this);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled() && !event.isHandled()) {
                            onSpacebar();
                        }
                    }
                    float forward = ControllerWASD.getForward(rider);
                    float strafe = ControllerWASD.getStrafe(rider);
                    float speed = (float) getRidingSpeed() * 5;
                    if (forward < 0) {
                        speed *= -0.5;
                    }
                    Vector target = ((CraftPlayer) ((Entity) rider).getBukkitEntity()).getEyeLocation()
                            .subtract(new Vector(0, 2, 0)).getDirection().normalize().multiply(speed);
                    if (strafe != 0) {
                        if (forward == 0) {
                            rotateVectorAroundY(target, strafe > 0 ? -90 : 90);
                            target.setY(0);
                        } else {
                            if (forward < 0) {
                                rotateVectorAroundY(target, strafe > 0 ? 45 : -45);
                            } else {
                                rotateVectorAroundY(target, strafe > 0 ? -45 : 45);
                            }
                        }
                    }
                    if (forward != 0 || strafe != 0) {
                        Vec3D vec = new Vec3D(target.getX(), target.getY(), target.getZ());
                        RidableSquid.this.a((float) vec.x / 20.0F, (float) vec.y / 20.0F, (float) vec.z / 20.0F); // setMovementVector
                    } else {
                        RidableSquid.this.a(0.0F, 0.0F, 0.0F); // setMovementVector
                    }
                }
            }

            private void rotateVectorAroundY(Vector vector, double degrees) {
                double rad = Math.toRadians(degrees);
                double cos = Math.cos(rad);
                double sine = Math.sin(rad);
                double x = vector.getX();
                double z = vector.getZ();
                vector.setX(cos * x - sine * z);
                vector.setZ(sine * x + cos * z);
            }
        });
        goalSelector.a(1, new PathfinderGoal() {
            private EntityLiving target;
            private int fleeTicks;

            public boolean a() { // shouldExecute
                if (getRider() != null) {
                    return false;
                }
                if (!isInWater()) {
                    return false;
                }
                target = getLastDamager();
                if (target == null) {
                    return false;
                }
                return h(target) < 100.0D; // getDistanceSq
            }

            public void c() { // startExecuting
                fleeTicks = 0;
            }

            public void e() { // tick
                ++fleeTicks;
                Vec3D dir = new Vec3D(locX - target.locX, locY - target.locY, locZ - target.locZ);
                BlockPosition pos = new BlockPosition(locX + dir.x, locY + dir.y, locZ + dir.z);
                IBlockData state = world.getType(pos);
                if (world.getFluid(pos).a(TagsFluid.WATER) || state.isAir()) { // isTagged
                    double length = dir.f(); // length
                    if (length > 0.0D) {
                        dir.d(); // normalize
                        float scale = 3.0F;
                        if (length > 5.0D) {
                            scale = (float) ((double) scale - (length - 5.0D) / 5.0D);
                        }
                        if (scale > 0.0F) {
                            dir = dir.a((double) scale); // scale
                        }
                    }
                    if (state.isAir()) {
                        dir = dir.a(0.0D, dir.y, 0.0D); // subtract
                    }
                    RidableSquid.this.a((float) dir.x / 20.0F, (float) dir.y / 20.0F, (float) dir.z / 20.0F); // setMovementVector
                }
                if (fleeTicks % 10 == 5) {
                    world.addParticle(Particles.BUBBLE, locX, locY, locZ, 0.0D, 0.0D, 0.0D);
                }
            }
        });
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return true;
    }

    @Override
    protected void mobTick() {
        if (spacebarCooldown > 0) {
            spacebarCooldown--;
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
        if (super.a(entityhuman, hand)) {
            return true; // handled by vanilla action
        }
        if (collectInWaterBucket(entityhuman, hand)) {
            return true; // handled
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
        if (spacebarCooldown == 0 && getRider().getBukkitEntity().hasPermission("ridables.special.squid")) {
            spacebarCooldown = config.RIDING_INK_COOLDOWN;
            squirtInk();
        }
        return false;
    }

    public void squirtInk() {
        a(SoundEffects.ENTITY_SQUID_SQUIRT, getSoundVolume(), cU()); // playSound getSoundPitch
        Vec3D pos = applyCurrentRotation(new Vec3D(0.0D, -1.0D, 0.0D)).add(locX, locY, locZ);
        for (int i = 0; i < 30; ++i) {
            Vec3D offset = applyCurrentRotation(new Vec3D(random.nextDouble() * 0.6D - 0.3D, -1.0D, random.nextDouble() * 0.6D - 0.3D)).a(random.nextDouble() * 2.0D + 0.3D);
            ((WorldServer) world).a(Particles.SQUID_INK, pos.x, pos.y + 0.5D, pos.z, 0, offset.x, offset.y, offset.z, (double) 0.1F); // spawnParticle SQUID_INK
        }
    }

    private Vec3D applyCurrentRotation(Vec3D vec) {
        return vec.a(c * Const.DEG2RAD_FLOAT).b(-aL * Const.DEG2RAD_FLOAT); // rotatePitch prevSquidPitch rotateYaw prevRenderYawOffset
    }
}
