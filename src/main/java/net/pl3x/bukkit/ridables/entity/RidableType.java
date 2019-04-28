package net.pl3x.bukkit.ridables.entity;

import com.google.common.collect.Maps;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumMobSpawn;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;
import net.pl3x.bukkit.ridables.configuration.mob.BatConfig;
import net.pl3x.bukkit.ridables.configuration.mob.BlazeConfig;
import net.pl3x.bukkit.ridables.configuration.mob.CaveSpiderConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ChickenConfig;
import net.pl3x.bukkit.ridables.configuration.mob.CodConfig;
import net.pl3x.bukkit.ridables.configuration.mob.CowConfig;
import net.pl3x.bukkit.ridables.configuration.mob.CreeperConfig;
import net.pl3x.bukkit.ridables.configuration.mob.DolphinConfig;
import net.pl3x.bukkit.ridables.configuration.mob.DonkeyConfig;
import net.pl3x.bukkit.ridables.configuration.mob.DrownedConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ElderGuardianConfig;
import net.pl3x.bukkit.ridables.configuration.mob.EnderDragonConfig;
import net.pl3x.bukkit.ridables.configuration.mob.EndermanConfig;
import net.pl3x.bukkit.ridables.configuration.mob.EndermiteConfig;
import net.pl3x.bukkit.ridables.configuration.mob.EvokerConfig;
import net.pl3x.bukkit.ridables.configuration.mob.GhastConfig;
import net.pl3x.bukkit.ridables.configuration.mob.GiantConfig;
import net.pl3x.bukkit.ridables.configuration.mob.GuardianConfig;
import net.pl3x.bukkit.ridables.configuration.mob.HorseConfig;
import net.pl3x.bukkit.ridables.configuration.mob.HuskConfig;
import net.pl3x.bukkit.ridables.configuration.mob.IllusionerConfig;
import net.pl3x.bukkit.ridables.configuration.mob.IronGolemConfig;
import net.pl3x.bukkit.ridables.configuration.mob.LlamaConfig;
import net.pl3x.bukkit.ridables.configuration.mob.MagmaCubeConfig;
import net.pl3x.bukkit.ridables.configuration.mob.MooshroomConfig;
import net.pl3x.bukkit.ridables.configuration.mob.MuleConfig;
import net.pl3x.bukkit.ridables.configuration.mob.OcelotConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ParrotConfig;
import net.pl3x.bukkit.ridables.configuration.mob.PhantomConfig;
import net.pl3x.bukkit.ridables.configuration.mob.PigConfig;
import net.pl3x.bukkit.ridables.configuration.mob.PolarBearConfig;
import net.pl3x.bukkit.ridables.configuration.mob.PufferfishConfig;
import net.pl3x.bukkit.ridables.configuration.mob.RabbitConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SalmonConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SheepConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ShulkerConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SilverfishConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SkeletonConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SkeletonHorseConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SlimeConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SnowGolemConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SpiderConfig;
import net.pl3x.bukkit.ridables.configuration.mob.SquidConfig;
import net.pl3x.bukkit.ridables.configuration.mob.StrayConfig;
import net.pl3x.bukkit.ridables.configuration.mob.TropicalFishConfig;
import net.pl3x.bukkit.ridables.configuration.mob.TurtleConfig;
import net.pl3x.bukkit.ridables.configuration.mob.VexConfig;
import net.pl3x.bukkit.ridables.configuration.mob.VillagerConfig;
import net.pl3x.bukkit.ridables.configuration.mob.VindicatorConfig;
import net.pl3x.bukkit.ridables.configuration.mob.WitchConfig;
import net.pl3x.bukkit.ridables.configuration.mob.WitherConfig;
import net.pl3x.bukkit.ridables.configuration.mob.WitherSkeletonConfig;
import net.pl3x.bukkit.ridables.configuration.mob.WolfConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ZombieConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ZombieHorseConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ZombiePigmanConfig;
import net.pl3x.bukkit.ridables.configuration.mob.ZombieVillagerConfig;
import net.pl3x.bukkit.ridables.data.Bucket;
import net.pl3x.bukkit.ridables.entity.ambient.RidableBat;
import net.pl3x.bukkit.ridables.entity.animal.RidableChicken;
import net.pl3x.bukkit.ridables.entity.animal.RidableCow;
import net.pl3x.bukkit.ridables.entity.animal.RidableDolphin;
import net.pl3x.bukkit.ridables.entity.animal.RidableIronGolem;
import net.pl3x.bukkit.ridables.entity.animal.RidableMooshroom;
import net.pl3x.bukkit.ridables.entity.animal.RidableOcelot;
import net.pl3x.bukkit.ridables.entity.animal.RidableParrot;
import net.pl3x.bukkit.ridables.entity.animal.RidablePig;
import net.pl3x.bukkit.ridables.entity.animal.RidablePolarBear;
import net.pl3x.bukkit.ridables.entity.animal.RidableRabbit;
import net.pl3x.bukkit.ridables.entity.animal.RidableSheep;
import net.pl3x.bukkit.ridables.entity.animal.RidableSnowGolem;
import net.pl3x.bukkit.ridables.entity.animal.RidableSquid;
import net.pl3x.bukkit.ridables.entity.animal.RidableTurtle;
import net.pl3x.bukkit.ridables.entity.animal.RidableWolf;
import net.pl3x.bukkit.ridables.entity.animal.fish.RidableCod;
import net.pl3x.bukkit.ridables.entity.animal.fish.RidablePufferFish;
import net.pl3x.bukkit.ridables.entity.animal.fish.RidableSalmon;
import net.pl3x.bukkit.ridables.entity.animal.fish.RidableTropicalFish;
import net.pl3x.bukkit.ridables.entity.animal.horse.RidableDonkey;
import net.pl3x.bukkit.ridables.entity.animal.horse.RidableHorse;
import net.pl3x.bukkit.ridables.entity.animal.horse.RidableLlama;
import net.pl3x.bukkit.ridables.entity.animal.horse.RidableMule;
import net.pl3x.bukkit.ridables.entity.animal.horse.RidableSkeletonHorse;
import net.pl3x.bukkit.ridables.entity.animal.horse.RidableZombieHorse;
import net.pl3x.bukkit.ridables.entity.boss.RidableEnderDragon;
import net.pl3x.bukkit.ridables.entity.boss.RidableWither;
import net.pl3x.bukkit.ridables.entity.monster.RidableBlaze;
import net.pl3x.bukkit.ridables.entity.monster.RidableCreeper;
import net.pl3x.bukkit.ridables.entity.monster.RidableEnderman;
import net.pl3x.bukkit.ridables.entity.monster.RidableEndermite;
import net.pl3x.bukkit.ridables.entity.monster.RidableEvoker;
import net.pl3x.bukkit.ridables.entity.monster.RidableGhast;
import net.pl3x.bukkit.ridables.entity.monster.RidableGiant;
import net.pl3x.bukkit.ridables.entity.monster.RidableIllusioner;
import net.pl3x.bukkit.ridables.entity.monster.RidablePhantom;
import net.pl3x.bukkit.ridables.entity.monster.RidableShulker;
import net.pl3x.bukkit.ridables.entity.monster.RidableSilverfish;
import net.pl3x.bukkit.ridables.entity.monster.RidableVex;
import net.pl3x.bukkit.ridables.entity.monster.RidableVindicator;
import net.pl3x.bukkit.ridables.entity.monster.RidableWitch;
import net.pl3x.bukkit.ridables.entity.monster.guardian.RidableElderGuardian;
import net.pl3x.bukkit.ridables.entity.monster.guardian.RidableGuardian;
import net.pl3x.bukkit.ridables.entity.monster.skeleton.RidableSkeleton;
import net.pl3x.bukkit.ridables.entity.monster.skeleton.RidableStray;
import net.pl3x.bukkit.ridables.entity.monster.skeleton.RidableWitherSkeleton;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableMagmaCube;
import net.pl3x.bukkit.ridables.entity.monster.slime.RidableSlime;
import net.pl3x.bukkit.ridables.entity.monster.spider.RidableCaveSpider;
import net.pl3x.bukkit.ridables.entity.monster.spider.RidableSpider;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableDrowned;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableHusk;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableZombie;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableZombiePigman;
import net.pl3x.bukkit.ridables.entity.monster.zombie.RidableZombieVillager;
import net.pl3x.bukkit.ridables.entity.npc.RidableVillager;
import net.pl3x.bukkit.ridables.util.Logger;
import net.pl3x.bukkit.ridables.util.RegistryHax;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class RidableType {
    private final static Map<EntityType, RidableType> BY_BUKKIT_TYPE = Maps.newHashMap();

    public static final RidableType BAT = inject("bat", EntityTypes.BAT, RidableBat::new, new BatConfig());
    public static final RidableType BLAZE = inject("blaze", EntityTypes.BLAZE, RidableBlaze::new, new BlazeConfig());
    // CAT
    public static final RidableType CAVE_SPIDER = inject("cave_spider", EntityTypes.CAVE_SPIDER, RidableCaveSpider::new, new CaveSpiderConfig());
    public static final RidableType CHICKEN = inject("chicken", EntityTypes.CHICKEN, RidableChicken::new, new ChickenConfig());
    public static final RidableType COD = inject("cod", EntityTypes.COD, RidableCod::new, new CodConfig());
    public static final RidableType COW = inject("cow", EntityTypes.COW, RidableCow::new, new CowConfig());
    public static final RidableType CREEPER = inject("creeper", EntityTypes.CREEPER, RidableCreeper::new, new CreeperConfig());
    public static final RidableType DOLPHIN = inject("dolphin", EntityTypes.DOLPHIN, RidableDolphin::new, new DolphinConfig(), Bucket.DOLPHIN);
    public static final RidableType DONKEY = inject("donkey", EntityTypes.DONKEY, RidableDonkey::new, new DonkeyConfig());
    public static final RidableType DROWNED = inject("drowned", EntityTypes.DROWNED, RidableDrowned::new, new DrownedConfig());
    public static final RidableType ELDER_GUARDIAN = inject("elder_guardian", EntityTypes.ELDER_GUARDIAN, RidableElderGuardian::new, new ElderGuardianConfig(), Bucket.ELDER_GUARDIAN);
    public static final RidableType ENDER_DRAGON = inject("ender_dragon", EntityTypes.ENDER_DRAGON, RidableEnderDragon::new, new EnderDragonConfig());
    public static final RidableType ENDERMAN = inject("enderman", EntityTypes.ENDERMAN, RidableEnderman::new, new EndermanConfig());
    public static final RidableType ENDERMITE = inject("endermite", EntityTypes.ENDERMITE, RidableEndermite::new, new EndermiteConfig());
    public static final RidableType EVOKER = inject("evoker", EntityTypes.EVOKER, RidableEvoker::new, new EvokerConfig());
    // FOX
    public static final RidableType GHAST = inject("ghast", EntityTypes.GHAST, RidableGhast::new, new GhastConfig());
    public static final RidableType GIANT = inject("giant", EntityTypes.GIANT, RidableGiant::new, new GiantConfig());
    public static final RidableType GUARDIAN = inject("guardian", EntityTypes.GUARDIAN, RidableGuardian::new, new GuardianConfig(), Bucket.GUARDIAN);
    public static final RidableType HORSE = inject("horse", EntityTypes.HORSE, RidableHorse::new, new HorseConfig());
    public static final RidableType HUSK = inject("husk", EntityTypes.HUSK, RidableHusk::new, new HuskConfig());
    public static final RidableType ILLUSIONER = inject("illusioner", EntityTypes.ILLUSIONER, RidableIllusioner::new, new IllusionerConfig());
    public static final RidableType IRON_GOLEM = inject("iron_golem", EntityTypes.IRON_GOLEM, RidableIronGolem::new, new IronGolemConfig());
    public static final RidableType LLAMA = inject("llama", EntityTypes.LLAMA, RidableLlama::new, new LlamaConfig());
    // LLAMA_TRADER
    public static final RidableType MAGMA_CUBE = inject("magma_cube", EntityTypes.MAGMA_CUBE, RidableMagmaCube::new, new MagmaCubeConfig());
    public static final RidableType MOOSHROOM = inject("mooshroom", EntityTypes.MOOSHROOM, RidableMooshroom::new, new MooshroomConfig());
    public static final RidableType MULE = inject("mule", EntityTypes.MULE, RidableMule::new, new MuleConfig());
    public static final RidableType OCELOT = inject("ocelot", EntityTypes.OCELOT, RidableOcelot::new, new OcelotConfig());
    // PANDA
    public static final RidableType PARROT = inject("parrot", EntityTypes.PARROT, RidableParrot::new, new ParrotConfig());
    public static final RidableType PHANTOM = inject("phantom", EntityTypes.PHANTOM, RidablePhantom::new, new PhantomConfig());
    public static final RidableType PIG = inject("pig", EntityTypes.PIG, RidablePig::new, new PigConfig());
    // PILLAGER
    public static final RidableType POLAR_BEAR = inject("polar_bear", EntityTypes.POLAR_BEAR, RidablePolarBear::new, new PolarBearConfig());
    public static final RidableType PUFFERFISH = inject("pufferfish", EntityTypes.PUFFERFISH, RidablePufferFish::new, new PufferfishConfig());
    public static final RidableType RABBIT = inject("rabbit", EntityTypes.RABBIT, RidableRabbit::new, new RabbitConfig());
    // RAVAGER
    public static final RidableType SALMON = inject("salmon", EntityTypes.SALMON, RidableSalmon::new, new SalmonConfig());
    public static final RidableType SHEEP = inject("sheep", EntityTypes.SHEEP, RidableSheep::new, new SheepConfig());
    public static final RidableType SHULKER = inject("shulker", EntityTypes.SHULKER, RidableShulker::new, new ShulkerConfig());
    public static final RidableType SILVERFISH = inject("silverfish", EntityTypes.SILVERFISH, RidableSilverfish::new, new SilverfishConfig());
    public static final RidableType SKELETON = inject("skeleton", EntityTypes.SKELETON, RidableSkeleton::new, new SkeletonConfig());
    public static final RidableType SKELETON_HORSE = inject("skeleton_horse", EntityTypes.SKELETON_HORSE, RidableSkeletonHorse::new, new SkeletonHorseConfig());
    public static final RidableType SLIME = inject("slime", EntityTypes.SLIME, RidableSlime::new, new SlimeConfig());
    public static final RidableType SNOW_GOLEM = inject("snow_golem", EntityTypes.SNOW_GOLEM, RidableSnowGolem::new, new SnowGolemConfig());
    public static final RidableType SPIDER = inject("spider", EntityTypes.SPIDER, RidableSpider::new, new SpiderConfig());
    public static final RidableType SQUID = inject("squid", EntityTypes.SQUID, RidableSquid::new, new SquidConfig(), Bucket.SQUID);
    public static final RidableType STRAY = inject("stray", EntityTypes.STRAY, RidableStray::new, new StrayConfig());
    public static final RidableType TROPICAL_FISH = inject("tropical_fish", EntityTypes.TROPICAL_FISH, RidableTropicalFish::new, new TropicalFishConfig());
    public static final RidableType TURTLE = inject("turtle", EntityTypes.TURTLE, RidableTurtle::new, new TurtleConfig(), Bucket.TURTLE);
    public static final RidableType VEX = inject("vex", EntityTypes.VEX, RidableVex::new, new VexConfig());
    public static final RidableType VILLAGER = inject("villager", EntityTypes.VILLAGER, RidableVillager::new, new VillagerConfig());
    // VILLAGER_TRADER
    public static final RidableType VINDICATOR = inject("vindicator", EntityTypes.VINDICATOR, RidableVindicator::new, new VindicatorConfig());
    public static final RidableType WITCH = inject("witch", EntityTypes.WITCH, RidableWitch::new, new WitchConfig());
    public static final RidableType WITHER = inject("wither", EntityTypes.WITHER, RidableWither::new, new WitherConfig());
    public static final RidableType WITHER_SKELETON = inject("wither_skeleton", EntityTypes.WITHER_SKELETON, RidableWitherSkeleton::new, new WitherSkeletonConfig());
    public static final RidableType WOLF = inject("wolf", EntityTypes.WOLF, RidableWolf::new, new WolfConfig());
    public static final RidableType ZOMBIE = inject("zombie", EntityTypes.ZOMBIE, RidableZombie::new, new ZombieConfig());
    public static final RidableType ZOMBIE_HORSE = inject("zombie_horse", EntityTypes.ZOMBIE_HORSE, RidableZombieHorse::new, new ZombieHorseConfig());
    public static final RidableType ZOMBIE_PIGMAN = inject("zombie_pigman", EntityTypes.ZOMBIE_PIGMAN, RidableZombiePigman::new, new ZombiePigmanConfig());
    public static final RidableType ZOMBIE_VILLAGER = inject("zombie_villager", EntityTypes.ZOMBIE_VILLAGER, RidableZombieVillager::new, new ZombieVillagerConfig());

    private static RidableType inject(String name, EntityTypes nmsType, EntityTypes.b entityTypes_b, MobConfig config) {
        return inject(name, nmsType, entityTypes_b, config, null);
    }

    private static RidableType inject(String name, EntityTypes nmsType, EntityTypes.b entityTypes_b, MobConfig config, Bucket waterBucket) {
        if (Config.isEnabled(name)) {
            if (RegistryHax.injectReplacementEntityTypes(nmsType, entityTypes_b)) {
                Logger.info("Successfully injected replacement entity: &a" + name);
                RidableType ridableType = new RidableType(name, nmsType, config, waterBucket);
                //noinspection deprecation
                BY_BUKKIT_TYPE.put(EntityType.fromName(name), ridableType);
                return ridableType;
            }
            Logger.error("Failed to inject replacement entity: &e" + name);
        } else {
            Logger.debug("Skipping disabled entity: &a" + name);
        }
        return null;
    }

    /**
     * Get all ridable types that are loaded
     *
     * @return All loaded ridable types
     */
    public static Collection<RidableType> getAllRidableTypes() {
        return new HashSet<>(BY_BUKKIT_TYPE.values());
    }

    /**
     * Gets a ridable type from a Bukkit type
     *
     * @param bukkitType Entity type
     * @return RidableType
     */
    @Nullable
    public static RidableType getRidableType(EntityType bukkitType) {
        return BY_BUKKIT_TYPE.get(bukkitType);
    }

    /**
     * Get a ridable type from an NMS type
     *
     * @param entity Entity type
     * @return RidableType
     */
    @Nullable
    public static RidableType getRidableType(org.bukkit.entity.Entity entity) {
        return entity == null ? null : BY_BUKKIT_TYPE.get(entity.getType());
    }

    /**
     * Get a ridable entity from a Bukkit entity
     *
     * @param entity Bukkit entity
     * @return RidableEntity
     */
    @Nullable
    public static RidableEntity getRidable(org.bukkit.entity.Entity entity) {
        net.minecraft.server.v1_14_R1.Entity craftEntity = ((CraftEntity) entity).getHandle();
        return craftEntity instanceof RidableEntity ? (RidableEntity) craftEntity : null;
    }

    private final EntityTypes<?> entityTypes;
    private final MobConfig config;
    private final Bucket waterBucket;
    private final String name;

    private RidableType(String name, EntityTypes<?> entityTypes, MobConfig config, Bucket waterBucket) {
        this.name = name;
        this.entityTypes = entityTypes;
        this.config = config;
        this.waterBucket = waterBucket;
    }

    /**
     * Get the mojang name of this entity type
     *
     * @return Entity type name
     */
    public String getName() {
        return name;
    }

    /**
     * Get mob's configuration for this ridable type
     *
     * @return MobConfig
     */
    public MobConfig getConfig() {
        return config;
    }

    /**
     * Get the bucket for this entity that can be used to place it into the world
     * <p>
     * This bucket works similar to Material.COD_BUCKET
     *
     * @return Bucket if one is set, null otherwise
     */
    @Nullable
    public Bucket getWaterBucket() {
        return waterBucket;
    }

    /**
     * Spawn this ridable entity in the world at given location
     *
     * @param loc Location to spawn at
     * @return Entity that was spawned, or null if entity could not be spawned
     */
    @Nullable
    public Entity spawn(Location loc) {
        return spawn(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Spawn this ridable entity in the world at given coordinates
     *
     * @param world World to spawn in
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @return Entity that was spawned, or null if entity could not be spawned
     */
    @Nullable
    public Entity spawn(World world, int x, int y, int z) {
        return spawn(world, null, null, null, new BlockPosition(x, y, z), null, true, false);
    }

    /**
     * Spawns entity
     *
     * @param world  World to spawn in
     * @param nbt    EntityTag NBT
     * @param name   Custom entity name
     * @param player Player reference, used to check OP status for applying EntityTag NBT
     * @param pos    Position to spawn at
     * @param center Center entity on position and correct Y for entity height
     * @param fixY   Not exactly sure yet. Alters the Y position. This is only ever true when using a spawn egg and clicked block face is UP.
     * @return Entity that was spawned, or null if entity could not be spawned
     */
    @Nullable
    public Entity spawn(World world, NBTTagCompound nbt, IChatBaseComponent name, EntityHuman player, BlockPosition pos, EnumMobSpawn mobSpawn, boolean center, boolean fixY) {
        net.minecraft.server.v1_14_R1.Entity nmsEntity = entityTypes.spawnCreature(((CraftWorld) world).getHandle(), nbt, name, player, pos, mobSpawn == null ? EnumMobSpawn.SPAWN_EGG : mobSpawn, center, fixY); // spawn
        return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
    }
}
