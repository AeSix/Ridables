package net.pl3x.bukkit.ridables.entity.projectile;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.DamageSource;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EntityShulkerBullet;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.GameProfileSerializer;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MobEffect;
import net.minecraft.server.v1_14_R1.MobEffects;
import net.minecraft.server.v1_14_R1.MovingObjectPosition;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.Particles;
import net.minecraft.server.v1_14_R1.ProjectileHelper;
import net.minecraft.server.v1_14_R1.SoundCategory;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.pl3x.bukkit.ridables.entity.monster.RidableShulker;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CustomShulkerBulletFromAI extends EntityShulkerBullet {
    private EntityLiving shooter;
    private Entity target;
    @Nullable
    private EnumDirection c;
    private int d;
    private double e;
    private double f;
    private double g;
    @Nullable
    private UUID h;
    private BlockPosition aw;
    @Nullable
    private UUID ax;
    private BlockPosition ay;

    public CustomShulkerBulletFromAI(World world) {
        super(world);
        this.setSize(0.3125F, 0.3125F);
        this.noclip = true;
    }

    public CustomShulkerBulletFromAI(World world, RidableShulker shulker, Entity target, EnumDirection.EnumAxis direction) {
        this(world);
        this.shooter = shulker;
        BlockPosition blockposition = new BlockPosition(shulker);
        double d0 = (double) blockposition.getX() + 0.5D;
        double d1 = (double) blockposition.getY() + 0.5D;
        double d2 = (double) blockposition.getZ() + 0.5D;
        this.setPositionRotation(d0, d1, d2, this.yaw, this.pitch);
        this.target = target;
        this.c = EnumDirection.UP;
        this.a(direction);
        this.projectileSource = (LivingEntity) shulker.getBukkitEntity();
    }

    @Override
    public EntityLiving getShooter() {
        return this.shooter;
    }

    @Override
    public void setShooter(EntityLiving e) {
        this.shooter = e;
    }

    @Override
    public Entity getTarget() {
        return this.target;
    }

    @Override
    public void setTarget(Entity e) {
        this.target = e;
        this.c = EnumDirection.UP;
        this.a(EnumDirection.EnumAxis.X);
    }

    @Override
    public SoundCategory bV() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected void b(NBTTagCompound nbt) {
        BlockPosition blockposition;
        NBTTagCompound nbt1;
        if (this.shooter != null) {
            blockposition = new BlockPosition(this.shooter);
            nbt1 = GameProfileSerializer.a(this.shooter.getUniqueID());
            nbt1.setInt("X", blockposition.getX());
            nbt1.setInt("Y", blockposition.getY());
            nbt1.setInt("Z", blockposition.getZ());
            nbt.set("Owner", nbt1);
        }

        if (this.target != null) {
            blockposition = new BlockPosition(this.target);
            nbt1 = GameProfileSerializer.a(this.target.getUniqueID());
            nbt1.setInt("X", blockposition.getX());
            nbt1.setInt("Y", blockposition.getY());
            nbt1.setInt("Z", blockposition.getZ());
            nbt.set("Target", nbt1);
        }

        if (this.c != null) {
            nbt.setInt("Dir", this.c.a());
        }

        nbt.setInt("Steps", this.d);
        nbt.setDouble("TXD", this.e);
        nbt.setDouble("TYD", this.f);
        nbt.setDouble("TZD", this.g);
    }

    @Override
    protected void a(NBTTagCompound nbt) {
        this.d = nbt.getInt("Steps");
        this.e = nbt.getDouble("TXD");
        this.f = nbt.getDouble("TYD");
        this.g = nbt.getDouble("TZD");
        if (nbt.hasKeyOfType("Dir", 99)) {
            this.c = EnumDirection.fromType1(nbt.getInt("Dir"));
        }

        NBTTagCompound nbt1;
        if (nbt.hasKeyOfType("Owner", 10)) {
            nbt1 = nbt.getCompound("Owner");
            this.h = GameProfileSerializer.b(nbt1);
            this.aw = new BlockPosition(nbt1.getInt("X"), nbt1.getInt("Y"), nbt1.getInt("Z"));
        }

        if (nbt.hasKeyOfType("Target", 10)) {
            nbt1 = nbt.getCompound("Target");
            this.ax = GameProfileSerializer.b(nbt1);
            this.ay = new BlockPosition(nbt1.getInt("X"), nbt1.getInt("Y"), nbt1.getInt("Z"));
        }

    }

    @Override
    protected void x_() {
    }

    private void a(@Nullable EnumDirection direction) {
        this.c = direction;
    }

    private void a(@Nullable EnumDirection.EnumAxis axis) {
        double d0 = 0.5D;
        BlockPosition blockposition;
        if (this.target == null) {
            blockposition = (new BlockPosition(this)).down();
        } else {
            d0 = (double) this.target.length * 0.5D;
            blockposition = new BlockPosition(this.target.locX, this.target.locY + d0, this.target.locZ);
        }

        double d1 = (double) blockposition.getX() + 0.5D;
        double d2 = (double) blockposition.getY() + d0;
        double d3 = (double) blockposition.getZ() + 0.5D;
        EnumDirection enumdirection = null;
        if (blockposition.g(this.locX, this.locY, this.locZ) >= 4.0D) {
            BlockPosition blockposition1 = new BlockPosition(this);
            ArrayList<EnumDirection> list = Lists.newArrayList();
            if (axis != EnumDirection.EnumAxis.X) {
                if (blockposition1.getX() < blockposition.getX() && this.world.isEmpty(blockposition1.east())) {
                    list.add(EnumDirection.EAST);
                } else if (blockposition1.getX() > blockposition.getX() && this.world.isEmpty(blockposition1.west())) {
                    list.add(EnumDirection.WEST);
                }
            }

            if (axis != EnumDirection.EnumAxis.Y) {
                if (blockposition1.getY() < blockposition.getY() && this.world.isEmpty(blockposition1.up())) {
                    list.add(EnumDirection.UP);
                } else if (blockposition1.getY() > blockposition.getY() && this.world.isEmpty(blockposition1.down())) {
                    list.add(EnumDirection.DOWN);
                }
            }

            if (axis != EnumDirection.EnumAxis.Z) {
                if (blockposition1.getZ() < blockposition.getZ() && this.world.isEmpty(blockposition1.south())) {
                    list.add(EnumDirection.SOUTH);
                } else if (blockposition1.getZ() > blockposition.getZ() && this.world.isEmpty(blockposition1.north())) {
                    list.add(EnumDirection.NORTH);
                }
            }

            enumdirection = EnumDirection.a(this.random);
            if (list.isEmpty()) {
                for (int i = 5; !this.world.isEmpty(blockposition1.shift(enumdirection)) && i > 0; --i) {
                    enumdirection = EnumDirection.a(this.random);
                }
            } else {
                enumdirection = list.get(this.random.nextInt(list.size()));
            }

            d1 = this.locX + (double) enumdirection.getAdjacentX();
            d2 = this.locY + (double) enumdirection.getAdjacentY();
            d3 = this.locZ + (double) enumdirection.getAdjacentZ();
        }

        this.a(enumdirection);
        double d4 = d1 - this.locX;
        double d5 = d2 - this.locY;
        double d6 = d3 - this.locZ;
        double d7 = (double) MathHelper.sqrt(d4 * d4 + d5 * d5 + d6 * d6);
        if (d7 == 0.0D) {
            this.e = 0.0D;
            this.f = 0.0D;
            this.g = 0.0D;
        } else {
            this.e = d4 / d7 * 0.15D;
            this.f = d5 / d7 * 0.15D;
            this.g = d6 / d7 * 0.15D;
        }

        this.impulse = true;
        this.d = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void tick() {
        if (!this.world.isClientSide && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.die();
        } else {
            super.tick();
            if (!this.world.isClientSide) {
                List list;
                Iterator iterator;
                EntityLiving entityliving;
                if (this.target == null && this.ax != null) {
                    list = this.world.a(EntityLiving.class, new AxisAlignedBB(this.ay.a(-2, -2, -2), this.ay.a(2, 2, 2)));
                    iterator = list.iterator();

                    while (iterator.hasNext()) {
                        entityliving = (EntityLiving) iterator.next();
                        if (entityliving.getUniqueID().equals(this.ax)) {
                            this.target = entityliving;
                            break;
                        }
                    }

                    this.ax = null;
                }

                if (this.shooter == null && this.h != null) {
                    list = this.world.a(EntityLiving.class, new AxisAlignedBB(this.aw.a(-2, -2, -2), this.aw.a(2, 2, 2)));
                    iterator = list.iterator();

                    while (iterator.hasNext()) {
                        entityliving = (EntityLiving) iterator.next();
                        if (entityliving.getUniqueID().equals(this.h)) {
                            this.shooter = entityliving;
                            break;
                        }
                    }

                    this.h = null;
                }

                if (this.target == null || !this.target.isAlive() || this.target instanceof EntityHuman && ((EntityHuman) this.target).isSpectator()) {
                    if (!this.isNoGravity()) {
                        this.motY -= 0.04D;
                    }
                } else {
                    this.e = MathHelper.a(this.e * 1.025D, -1.0D, 1.0D);
                    this.f = MathHelper.a(this.f * 1.025D, -1.0D, 1.0D);
                    this.g = MathHelper.a(this.g * 1.025D, -1.0D, 1.0D);
                    this.motX += (this.e - this.motX) * RidableShulker.CONFIG.AI_SHOOT_SPEED;
                    this.motY += (this.f - this.motY) * RidableShulker.CONFIG.AI_SHOOT_SPEED;
                    this.motZ += (this.g - this.motZ) * RidableShulker.CONFIG.AI_SHOOT_SPEED;
                }

                MovingObjectPosition movingobjectposition = ProjectileHelper.a(this, true, false, this.shooter);
                if (movingobjectposition != null) {
                    this.a(movingobjectposition);
                }
            }

            this.setPosition(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
            ProjectileHelper.a(this, 0.5F);
            if (this.world.isClientSide) {
                this.world.addParticle(Particles.r, this.locX - this.motX, this.locY - this.motY + 0.15D, this.locZ - this.motZ, 0.0D, 0.0D, 0.0D);
            } else if (this.target != null && !this.target.dead) {
                if (this.d > 0) {
                    --this.d;
                    if (this.d == 0) {
                        this.a(this.c == null ? null : this.c.k());
                    }
                }

                if (this.c != null) {
                    BlockPosition blockposition = new BlockPosition(this);
                    EnumDirection.EnumAxis enumdirection_enumaxis = this.c.k();
                    if (this.world.q(blockposition.shift(this.c))) {
                        this.a(enumdirection_enumaxis);
                    } else {
                        BlockPosition blockposition1 = new BlockPosition(this.target);
                        if (enumdirection_enumaxis == EnumDirection.EnumAxis.X && blockposition.getX() == blockposition1.getX() || enumdirection_enumaxis == EnumDirection.EnumAxis.Z && blockposition.getZ() == blockposition1.getZ() || enumdirection_enumaxis == EnumDirection.EnumAxis.Y && blockposition.getY() == blockposition1.getY()) {
                            this.a(enumdirection_enumaxis);
                        }
                    }
                }
            }
        }

    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @Override
    public float az() {
        return 1.0F;
    }

    // bulletHit
    @Override
    protected void a(MovingObjectPosition mop) {
        CraftEventFactory.callProjectileHitEvent(this, mop);
        if (mop.entity == null) {
            ((WorldServer) world).a(Particles.u, locX, locY, locZ, 2, 0.2D, 0.2D, 0.2D, 0.0D); // spawnParticle EXPLOSION
            a(SoundEffects.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F); // playSound
        } else if (RidableShulker.CONFIG.AI_SHOOT_DAMAGE > 0.0F) {
            if (mop.entity.damageEntity(DamageSource.a(this, getShooter()).c(), RidableShulker.CONFIG.AI_SHOOT_DAMAGE)) { // causeIndirect setProjectile
                a(getShooter(), mop.entity); // applyEnchantments
                if (mop.entity instanceof EntityLiving) {
                    ((EntityLiving) mop.entity).addEffect(new MobEffect(MobEffects.LEVITATION, 200), EntityPotionEffectEvent.Cause.ATTACK);
                }
            }
        }
        die();
    }

    @Override
    public boolean isInteractable() {
        return true;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (!this.world.isClientSide) {
            this.a(SoundEffects.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
            ((WorldServer) this.world).a(Particles.h, this.locX, this.locY, this.locZ, 15, 0.2D, 0.2D, 0.2D, 0.0D);
            this.die();
        }

        return true;
    }
}
