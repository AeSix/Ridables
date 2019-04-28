package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class BatConfig extends MobConfig {
    public double RIDING_SPEED = 0.7D;
    public double RIDING_VERTICAL = 1.0F;
    public double RIDING_GRAVITY = 0.04F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public int RIDING_FLYING_MAX_Y = 256;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;

    public BatConfig() {
        super("bat.yml");
        reload();
    }

    @Override
    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.vertical", RIDING_VERTICAL);
            addDefault("riding.gravity", RIDING_GRAVITY);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("riding.flying-max-y", RIDING_FLYING_MAX_Y);
            save();
        }

        RIDING_SPEED = getDouble("riding.speed");
        RIDING_VERTICAL = getDouble("riding.vertical");
        RIDING_GRAVITY = getDouble("riding.gravity");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_FLYING_MAX_Y = (int) getDouble("riding.flying-max-y");
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
    }
}
