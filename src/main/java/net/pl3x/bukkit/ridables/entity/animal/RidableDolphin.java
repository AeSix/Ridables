package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ControllerMove;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityDolphin;
import net.minecraft.server.v1_14_R1.EntityGuardian;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityItem;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MobEffect;
import net.minecraft.server.v1_14_R1.MobEffects;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.PathMode;
import net.minecraft.server.v1_14_R1.PathfinderGoal;
import net.minecraft.server.v1_14_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreath;
import net.minecraft.server.v1_14_R1.PathfinderGoalFollowBoat;
import net.minecraft.server.v1_14_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomSwim;
import net.minecraft.server.v1_14_R1.PathfinderGoalWater;
import net.minecraft.server.v1_14_R1.PathfinderGoalWaterJump;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_14_R1.RandomPositionGenerator;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.DolphinConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDWater;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.DolphinSpit;
import net.pl3x.bukkit.ridables.event.DolphinSpitEvent;
import net.pl3x.bukkit.ridables.event.RidableDismountEvent;
import net.pl3x.bukkit.ridables.util.Const;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.List;

public class RidableDolphin extends EntityDolphin implements RidableEntity {
    private static DolphinConfig config;

    private int bounceCounter = 0;
    private boolean bounceUp = false;
    private int spacebarCooldown = 0;
    private boolean dashing = false;
    private int dashCounter = 0;

    public RidableDolphin(EntityTypes<? extends EntityDolphin> entitytypes, World world) {
        super(entitytypes, world);
        moveController = new DolphinWASDController(this);
        lookController = new DolphinLookController(this, 10);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.DOLPHIN;
    }

    @Override
    public DolphinConfig getConfig() {
        return (DolphinConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        if (dashing) {
            return config.RIDING_SPEED * config.RIDING_DASH_BOOST;
        }
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoalBreath(this));
        goalSelector.a(0, new PathfinderGoalWater(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(1, new AIDolphinSwimToTreasure(this));
        goalSelector.a(2, new AIDolphinSwimWithPlayer(this, 4.0D));
        goalSelector.a(4, new PathfinderGoalRandomSwim(this, 1.0D, 10) {
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
        goalSelector.a(5, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new PathfinderGoalWaterJump(this, 10) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalMeleeAttack(this, (double) 1.2F, true) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new AIDolphinPlayWithItems(this));
        goalSelector.a(8, new PathfinderGoalFollowBoat(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(9, new PathfinderGoalAvoidTarget<EntityGuardian>(this, EntityGuardian.class, 8.0F, 1.0D, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        targetSelector.a(1, new PathfinderGoalHurtByTarget(this, EntityGuardian.class) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        }.a(new Class[0]));
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return true;
    }

    // canBeRidden
    @Override
    protected boolean n(Entity entity) {
        return j <= 0; // rideCooldown
    }

    public boolean nearTargetPos() {
        return dX();
    }

    @Override
    protected void mobTick() {
        if (++bounceCounter > 10) {
            bounceCounter = 0;
            bounceUp = !bounceUp;
        }
        if (spacebarCooldown > 0) {
            spacebarCooldown--;
        }
        if (dashing) {
            if (++dashCounter > config.RIDING_DASH_DURATION) {
                dashCounter = 0;
                dashing = false;
            }
        }

        EntityPlayer rider = getRider();
        if (rider != null && getAirTicks() > 150 && isInWater()) {
            Vec3D mot = getMot();
            if (config.RIDING_BUBBLES) {
                double velocity = mot.x * mot.x + mot.y * mot.y + mot.z * mot.z;
                if (velocity > 0.2 || velocity < -0.2) {
                    int i = (int) (velocity * 5);
                    for (int j = 0; j < i; j++) {
                        ((WorldServer) world).sendParticles(null, Particles.BUBBLE,
                                lastX + random.nextFloat() / 2 - 0.25F,
                                lastY + random.nextFloat() / 2 - 0.25F,
                                lastZ + random.nextFloat() / 2 - 0.25F,
                                1, 0, 0, 0, 0, true);
                    }
                }
            }

            double y = mot.y + 0.005D;
            if (config.RIDING_BOUNCE && ControllerWASDWater.getForward(rider) == 0) {
                y += bounceUp ? 0.005D : -0.005D;
            }
            setMot(mot.x, y, mot.z);
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
        if (spacebarCooldown == 0 && config.RIDING_SPACEBAR_MODE != null) {
            EntityPlayer rider = getRider();
            if (rider != null) {
                if (config.RIDING_SPACEBAR_MODE.equalsIgnoreCase("shoot")) {
                    return shoot(rider);
                } else if (config.RIDING_SPACEBAR_MODE.equalsIgnoreCase("dash")) {
                    return dash(rider);
                }
            }
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        spacebarCooldown = config.RIDING_SHOOT_COOLDOWN;
        if (rider == null) {
            return false;
        }

        CraftPlayer player = rider.getBukkitEntity();
        if (!player.hasPermission("ridables.shoot.dolphin")) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Location loc = player.getEyeLocation();
        loc.setPitch(loc.getPitch() - 10);
        Vector target = loc.getDirection().normalize().multiply(10).add(loc.toVector());

        DolphinSpit spit = new DolphinSpit(world, this, rider);
        spit.shoot(target.getX() - locX, target.getY() - locY, target.getZ() - locZ, config.RIDING_SHOOT_SPEED, 5.0F);

        DolphinSpitEvent event = new DolphinSpitEvent(this, spit);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false; // cancelled
        }

        world.addEntity(spit);
        a(SoundEffects.ENTITY_DOLPHIN_ATTACK, 1.0F, 1.0F); // playSound
        return true;
    }

    public boolean dash(EntityPlayer rider) {
        spacebarCooldown = config.RIDING_DASH_COOLDOWN;
        if (!dashing) {
            if (rider != null && !rider.getBukkitEntity().hasPermission("ridables.special.dolphin")) {
                return false;
            }

            dashing = true;
            dashCounter = 0;
            a(SoundEffects.ENTITY_DOLPHIN_JUMP, 1.0F, 1.0F); // playSound
            return true;
        }
        return false;
    }

    class DolphinLookController extends LookController {
        private final int h;

        DolphinLookController(RidableDolphin dolphin, int h) {
            super(dolphin);
            this.h = h;
        }

        @Override
        public void tick() {
            if (d) { // isLooking
                d = false; // isLooking
                a.aM = a(a.aM, h() + 20.0F, b);
                a.pitch = a(a.pitch, g() + 10F, c);
            } else {
                if (a.getNavigation().n()) { // noPath
                    a.pitch = a(a.pitch, 0.0F, 5.0F);
                }
                a.aM = a(a.aM, a.aK, b); // rotationYawHead
            }
            float yawDiff = MathHelper.g(a.aM - a.aK);
            if (yawDiff < (float) -h) {
                a.aK -= 4.0F; // renderYawOffset
            } else if (yawDiff > (float) h) {
                a.aK += 4.0F; // renderYawOffset
            }
        }
    }

    static class DolphinWASDController extends ControllerWASDWater {
        private final RidableDolphin dolphin;

        DolphinWASDController(RidableDolphin dolphin) {
            super(dolphin);
            this.dolphin = dolphin;
        }

        @Override
        public void tick() {
            if (dolphin.isInWater()) {
                dolphin.setMot(dolphin.getMot().add(0.0D, 0.005D, 0.0D));
            }

            if (h == ControllerMove.Operation.MOVE_TO && !dolphin.getNavigation().n()) {
                double x = b - dolphin.locX;
                double y = c - dolphin.locY;
                double z = d - dolphin.locZ;
                double distance = x * x + y * y + z * z;

                if (distance < (double) 0.00000025F) {
                    a.r(0.0F);
                } else {
                    float f = (float) (MathHelper.d(z, x) * Const.RAD2DEG) - 90.0F;

                    dolphin.aM = dolphin.aK = dolphin.yaw = a(dolphin.yaw, f, 10.0F);
                    float speed = (float) (e * dolphin.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue());

                    if (dolphin.isInWater()) {
                        dolphin.o(speed * 0.02F);
                        float f2 = -((float) (MathHelper.d(y, (double) MathHelper.sqrt(x * x + z * z)) * Const.RAD2DEG));
                        f2 = MathHelper.a(MathHelper.g(f2), -85.0F, 85.0F);
                        dolphin.pitch = a(dolphin.pitch, f2, 5.0F);
                        dolphin.bd = MathHelper.cos(dolphin.pitch * Const.DEG2RAD_FLOAT) * speed;
                        dolphin.bc = -MathHelper.sin(dolphin.pitch * Const.DEG2RAD_FLOAT) * speed;
                    } else {
                        dolphin.o(speed * 0.1F);
                    }
                }
            } else {
                dolphin.o(0.0F);
                dolphin.t(0.0F);
                dolphin.s(0.0F);
                dolphin.r(0.0F);
            }
        }
    }

    static class AIDolphinPlayWithItems extends PathfinderGoal {
        private final RidableDolphin dolphin;
        private int delay;

        AIDolphinPlayWithItems(RidableDolphin dolphin) {
            this.dolphin = dolphin;
        }

        // shouldExecute
        @Override
        public boolean a() {
            if (delay > dolphin.ticksLived) {
                return false;
            }
            if (dolphin.getRider() != null) {
                return false;
            }
            List items = dolphin.world.a(EntityItem.class, dolphin.getBoundingBox().grow(8.0D, 8.0D, 8.0D), EntityDolphin.b);
            return !items.isEmpty() || !dolphin.getEquipment(EnumItemSlot.MAINHAND).isEmpty();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return a();
        }

        // startExecuting
        @Override
        public void c() {
            List items = dolphin.world.a(EntityItem.class, dolphin.getBoundingBox().grow(8.0D, 8.0D, 8.0D), EntityDolphin.b);
            if (!items.isEmpty()) {
                dolphin.getNavigation().a((Entity) items.get(0), (double) 1.2F);
                dolphin.a(SoundEffects.ENTITY_DOLPHIN_PLAY, 1.0F, 1.0F);
            }
            delay = 0;
        }

        // resetTask
        @Override
        public void d() {
            ItemStack stack = dolphin.getEquipment(EnumItemSlot.MAINHAND);
            if (!stack.isEmpty()) {
                throwItem(stack);
                dolphin.setSlot(EnumItemSlot.MAINHAND, ItemStack.a);
                delay = dolphin.ticksLived + dolphin.getRandom().nextInt(100);
            }
        }

        // tick
        @Override
        public void e() {
            List items = dolphin.world.a(EntityItem.class, dolphin.getBoundingBox().grow(8.0D, 8.0D, 8.0D), EntityDolphin.b);
            ItemStack stack = dolphin.getEquipment(EnumItemSlot.MAINHAND);
            if (!stack.isEmpty()) {
                throwItem(stack);
                dolphin.setSlot(EnumItemSlot.MAINHAND, ItemStack.a);
            } else if (!items.isEmpty()) {
                dolphin.getNavigation().a((Entity) items.get(0), (double) 1.2F);
            }
        }

        private void throwItem(ItemStack stack) {
            if (!stack.isEmpty()) {
                double d0 = dolphin.locY - (double) 0.3F + (double) dolphin.getHeadHeight();
                EntityItem entityitem = new EntityItem(dolphin.world, dolphin.locX, d0, dolphin.locZ, stack);

                entityitem.setPickupDelay(40);
                entityitem.setThrower(dolphin.getUniqueID());

                float f1 = dolphin.getRandom().nextFloat() * Const.TWOPI_FLOAT;
                float f2 = 0.02F * dolphin.getRandom().nextFloat();

                entityitem.setMot(
                        (double) (0.3F * -MathHelper.sin(dolphin.yaw * Const.DEG2RAD_FLOAT) * MathHelper.cos(dolphin.pitch * Const.DEG2RAD_FLOAT) + MathHelper.cos(f1) * f2),
                        (double) (0.3F * MathHelper.sin(dolphin.pitch * Const.DEG2RAD_FLOAT) * 1.5F),
                        (double) (0.3F * MathHelper.cos(dolphin.yaw * Const.DEG2RAD_FLOAT) * MathHelper.cos(dolphin.pitch * Const.DEG2RAD_FLOAT) + MathHelper.sin(f1) * f2));

                dolphin.world.addEntity(entityitem);
            }
        }
    }

    static class AIDolphinSwimToTreasure extends PathfinderGoal {
        private final RidableDolphin dolphin;
        private boolean notFound;

        AIDolphinSwimToTreasure(RidableDolphin dolphin) {
            this.dolphin = dolphin;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK)); // setMutexBits
        }

        // isInterruptible
        @Override
        public boolean C_() {
            return dolphin.getRider() != null;
        }

        // shouldExecute
        @Override
        public boolean a() {
            return dolphin.getRider() == null && dolphin.dV() && dolphin.getAirTicks() >= 100;
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            if (dolphin.getRider() != null) {
                return false;
            }
            BlockPosition treasure = dolphin.l(); // getTreasurePos
            return !notFound && dolphin.getAirTicks() >= 100 && !(new BlockPosition(treasure.getX(), dolphin.locY, treasure.getZ())).a(dolphin.ch(), 4.0D);
        }

        // startExecuting
        @Override
        public void c() {
            notFound = false;
            dolphin.getNavigation().o();
            World world = dolphin.world;
            BlockPosition pos = new BlockPosition(dolphin);
            String structureName = (double) world.random.nextFloat() >= 0.5D ? "Ocean_Ruin" : "Shipwreck";
            BlockPosition pos1 = world.a(structureName, pos, 50, false); // findNearestStructure
            if (pos1 == null) {
                BlockPosition pos2 = world.a(structureName.equals("Ocean_Ruin") ? "Shipwreck" : "Ocean_Ruin", pos, 50, false); // findNearestStructure
                if (pos2 == null) {
                    notFound = true;
                    return;
                }
                dolphin.g(pos2); // setTreasurePos
            } else {
                dolphin.g(pos1); // setTreasurePos
            }
            world.broadcastEntityEffect(dolphin, (byte) 38);
        }

        // resetTask
        @Override
        public void d() {
            BlockPosition pos = dolphin.l(); // getTreasurePos
            if ((new BlockPosition((double) pos.getX(), dolphin.locY, (double) pos.getZ())).a(dolphin.ch(), 4.0D) || notFound) {
                dolphin.r(false); // setGotFish
            }
        }

        // tick
        @Override
        public void e() {
            BlockPosition pos = dolphin.l(); // getTreasurePos
            if (dolphin.nearTargetPos() || dolphin.getNavigation().n()) {
                Vec3D vec3d = RandomPositionGenerator.a(dolphin, 16, 1, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), (double) (Const.PI_FLOAT / 8F));
                if (vec3d == null) {
                    vec3d = RandomPositionGenerator.a(dolphin, 8, 4, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()));
                }
                if (vec3d != null) {
                    BlockPosition pos1 = new BlockPosition(vec3d);
                    if (!dolphin.world.getFluid(pos1).a(TagsFluid.WATER) || !dolphin.world.getType(pos1).a(dolphin.world, pos1, PathMode.WATER)) {
                        vec3d = RandomPositionGenerator.a(dolphin, 8, 5, new Vec3D((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()));
                    }
                }
                if (vec3d == null) {
                    notFound = true;
                    return;
                }
                dolphin.getControllerLook().a(vec3d.x, vec3d.y, vec3d.z, (float) (dolphin.dA() + 20), (float) dolphin.M());
                dolphin.getNavigation().a(vec3d.x, vec3d.y, vec3d.z, 1.3D);
                if (dolphin.world.random.nextInt(80) == 0) {
                    dolphin.world.broadcastEntityEffect(dolphin, (byte) 38);
                }
            }
        }
    }

    static class AIDolphinSwimWithPlayer extends PathfinderGoal {
        private final RidableDolphin dolphin;
        private final double speed;
        private EntityHuman targetPlayer;

        AIDolphinSwimWithPlayer(RidableDolphin dolphin, double speed) {
            this.dolphin = dolphin;
            this.speed = speed;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK)); // setMutexBits
        }

        // from EntityDolphin
        private static final PathfinderTargetCondition CONDITION = (new PathfinderTargetCondition()).a(10.0D).b().a();

        // shouldExecute
        @Override
        public boolean a() {
            if (dolphin.getRider() != null) {
                return false;
            }
            targetPlayer = dolphin.world.a(CONDITION, dolphin);
            return targetPlayer != null && targetPlayer.isSwimming();
        }

        // shouldContinueExecuting
        @Override
        public boolean b() {
            return dolphin.getRider() == null && targetPlayer != null && targetPlayer.isSwimming() && dolphin.h(targetPlayer) < 256.0D;
        }

        // startExecuting
        @Override
        public void c() {
            targetPlayer.addEffect(new MobEffect(MobEffects.DOLPHINS_GRACE, 100), EntityPotionEffectEvent.Cause.DOLPHIN);
        }

        // resetTask
        @Override
        public void d() {
            targetPlayer = null;
            dolphin.getNavigation().o(); // clearPath
        }

        // tick
        @Override
        public void e() {
            dolphin.getControllerLook().a(targetPlayer, (float) (dolphin.dA() + 20), (float) dolphin.M()); // setLookPositionWithEntity
            if (dolphin.h(targetPlayer) < 6.25D) {
                dolphin.getNavigation().o(); // clearPath
            } else {
                dolphin.getNavigation().a(targetPlayer, speed); // tryMovingToEntityLiving
            }
            if (targetPlayer.isSwimming() && targetPlayer.world.random.nextInt(6) == 0) {
                targetPlayer.addEffect(new MobEffect(MobEffects.DOLPHINS_GRACE, 100), EntityPotionEffectEvent.Cause.DOLPHIN);
            }
        }
    }
}
