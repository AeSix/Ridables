package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class CaveSpiderConfig extends MobConfig {
    public double RIDING_SPEED = 0.3D;
    public float RIDING_JUMP_POWER = 0.5F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public boolean RIDING_CLIMB_WALLS = true;
    public double RIDING_CLIMB_SPEED = 1.0D;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;

    public CaveSpiderConfig() {
        super("cave_spider.yml");
        reload();
    }

    @Override
    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.jump-power", RIDING_JUMP_POWER);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("riding.climb-walls", RIDING_CLIMB_WALLS);
            addDefault("riding.climb-speed", RIDING_CLIMB_SPEED);
            save();
        }

        RIDING_SPEED = getDouble("riding.speed");
        RIDING_JUMP_POWER = (float) getDouble("riding.jump-power");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_CLIMB_WALLS = getBoolean("riding.climb-walls");
        RIDING_CLIMB_SPEED = getDouble("riding.climb-speed");
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
    }
}
