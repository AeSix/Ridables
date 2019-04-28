package net.pl3x.bukkit.ridables.util;

import com.mojang.datafixers.types.Type;
import net.minecraft.server.v1_14_R1.BiomeBase;
import net.minecraft.server.v1_14_R1.DataConverterRegistry;
import net.minecraft.server.v1_14_R1.DataConverterTypes;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumCreatureType;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.mob.GiantConfig;
import net.pl3x.bukkit.ridables.configuration.mob.IllusionerConfig;
import net.pl3x.bukkit.ridables.data.BiomeData;
import net.pl3x.bukkit.ridables.entity.RidableType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RegistryHax {
    private static Field entityTypes_b_field;
    private static Method biomeBase_addSpawn;

    static {
        try {
            entityTypes_b_field = EntityTypes.class.getDeclaredField("aZ");
            entityTypes_b_field.setAccessible(true);
            biomeBase_addSpawn = BiomeBase.class.getDeclaredMethod("a", EnumCreatureType.class, BiomeBase.BiomeMeta.class);
            biomeBase_addSpawn.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException ignore) {
        }
    }

    public static void injectNewEntityTypes(String name, String extend_from, EntityTypes.b entityTypes_b, EnumCreatureType creatureType, float width, float height) {
        Map<Object, Type<?>> dataTypes = (Map<Object, Type<?>>) DataConverterRegistry.a().getSchema(15190).findChoiceType(DataConverterTypes.o).types(); // entity_tree
        dataTypes.put("minecraft:" + name, dataTypes.get("minecraft:" + extend_from));
        IRegistry.a(IRegistry.ENTITY_TYPE, name, EntityTypes.a.a(entityTypes_b, creatureType).a(width, height).a(name));
        Logger.info("Successfully injected new entity: &a" + name);
    }

    public static boolean injectReplacementEntityTypes(EntityTypes<?> entityTypes, EntityTypes.b entityTypes_b) {
        MinecraftKey key = IRegistry.ENTITY_TYPE.getKey(entityTypes);
        try {
            entityTypes_b_field.set(entityTypes, entityTypes_b);
        } catch (IllegalAccessException ignore) {
            return false;
        }
        IRegistry.ENTITY_TYPE.a(IRegistry.ENTITY_TYPE.a(entityTypes), key, entityTypes);
        return true;
    }

    public static void addMobsToBiomes() {
        GiantConfig giantConfig = (GiantConfig) RidableType.GIANT.getConfig();
        if (Config.isEnabled("giant") && giantConfig.SPAWN_NATURALLY) {
            if (giantConfig.SPAWN_BIOMES.isEmpty()) {
                Logger.warn("Giant is configured to spawn naturally in biomes, but no biomes are set!");
            } else {
                Logger.info("Adding Giant to spawn naturally in biomes");
                giantConfig.SPAWN_BIOMES.forEach(data -> injectSpawn(data, EntityTypes.GIANT));
            }
        }

        IllusionerConfig illusionerConfig = (IllusionerConfig) RidableType.ILLUSIONER.getConfig();
        if (Config.isEnabled("illusioner") && illusionerConfig.SPAWN_NATURALLY) {
            if (illusionerConfig.SPAWN_BIOMES.isEmpty()) {
                Logger.warn("Illusioner is configured to spawn naturally in biomes, but no biomes are set!");
            } else {
                Logger.info("Adding Illusioner to spawn naturally in biomes");
                illusionerConfig.SPAWN_BIOMES.forEach(data -> injectSpawn(data, EntityTypes.ILLUSIONER));
            }
        }
    }

    private static void injectSpawn(BiomeData data, EntityTypes<? extends EntityInsentient> type) {
        Logger.debug("- " + data.getBiome());
        BiomeBase biome = IRegistry.BIOME.get(new MinecraftKey(data.getBiome()));
        if (biome != null) {
            try {
                biomeBase_addSpawn.invoke(biome, EnumCreatureType.MONSTER, new BiomeBase.BiomeMeta(type, data.getWeight(), data.getMin(), data.getMax()));
            } catch (IllegalAccessException | InvocationTargetException ignore) {
            }
        } else {
            Logger.error("Could not find biome named &e" + data.getBiome());
        }
    }
}
