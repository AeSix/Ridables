package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class CreeperConfig extends MobConfig {
    public double RIDING_SPEED = 0.25D;
    public float RIDING_JUMP_POWER = 0.5F;
    public float RIDING_STEP_HEIGHT = 0.6F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;
    public double RIDING_EXPLOSION_DAMAGE = 5.0D;
    public int RIDING_EXPLOSION_RADIUS = 3;
    public boolean RIDING_EXPLOSION_GRIEF = true;
    public boolean RIDING_EXPLOSION_FIRE = false;
    public boolean RIDING_EXPLOSION_LINGERING_CLOUD = false;

    public CreeperConfig() {
        super("creeper.yml");
        reload();
    }

    @Override
    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.jump-power", RIDING_JUMP_POWER);
            addDefault("riding.step-height", RIDING_STEP_HEIGHT);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("riding.explosion.damage", RIDING_EXPLOSION_DAMAGE);
            addDefault("riding.explosion.radius", RIDING_EXPLOSION_RADIUS);
            addDefault("riding.explosion.grief", RIDING_EXPLOSION_GRIEF);
            addDefault("riding.explosion.fire", RIDING_EXPLOSION_FIRE);
            addDefault("riding.explosion.lingering-cloud", RIDING_EXPLOSION_LINGERING_CLOUD);
            save();
        }

        RIDING_SPEED = getDouble("riding.speed");
        RIDING_JUMP_POWER = (float) getDouble("riding.jump-power");
        RIDING_STEP_HEIGHT = (float) getDouble("riding.step-height");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
        RIDING_EXPLOSION_DAMAGE = getDouble("riding.explosion.damage");
        RIDING_EXPLOSION_RADIUS = (int) getDouble("riding.explosion.radius");
        RIDING_EXPLOSION_GRIEF = getBoolean("riding.explosion.grief");
        RIDING_EXPLOSION_FIRE = getBoolean("riding.explosion.fire");
        RIDING_EXPLOSION_LINGERING_CLOUD = getBoolean("riding.explosion.lingering-cloud");
    }
}
