package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class SnowGolemConfig extends MobConfig {
    public double BASE_SPEED = 0.2D;
    public double MAX_HEALTH = 4.0D;
    public float AI_JUMP_POWER = 0.42F;
    public float AI_STEP_HEIGHT = 0.6F;
    public double AI_FOLLOW_RANGE = 16.0D;
    public boolean SHEARS_DROPS_PUMPKIN = true;
    public boolean ADD_PUMPKIN_TO_HEAD = true;
    public float AI_DAMAGE_WHEN_WET = 1.0F;
    public float AI_DAMAGE_WHEN_HOT = 1.0F;
    public boolean AI_SNOW_TRAIL_ENABLED = true;
    public float AI_SNOW_TRAIL_MAX_TEMP = 0.8F;
    public float AI_SHOOT_SPEED = 1.6F;
    public float AI_SHOOT_INACCURACY = 12.0F;
    public float AI_SHOOT_DAMAGE = 0.0F;
    public double RIDING_SPEED = 0.2D;
    public float RIDING_JUMP_POWER = 0.5F;
    public float RIDING_STEP_HEIGHT = 0.6F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public float RIDING_DAMAGE_WHEN_WET = 1.0F;
    public float RIDING_DAMAGE_WHEN_HOT = 1.0F;
    public boolean RIDING_SNOW_TRAIL_ENABLED = true;
    public float RIDING_SNOW_TRAIL_MAX_TEMP = 0.8F;
    public int RIDING_SHOOT_COOLDOWN = 20;
    public float RIDING_SHOOT_SPEED = 1.6F;
    public float RIDING_SHOOT_INACCURACY = 0.0F;
    public float RIDING_SHOOT_DAMAGE = 0.0F;
    public boolean RIDING_ENABLE_MOVE_EVENT = false;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;


    public SnowGolemConfig() {
        super("snow_golem.yml");
        reload();
    }

    @Override
    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("base-speed", BASE_SPEED);
            addDefault("max-health", MAX_HEALTH);
            addDefault("ai.jump-power", AI_JUMP_POWER);
            addDefault("ai.step-height", AI_STEP_HEIGHT);
            addDefault("ai.follow-range", AI_FOLLOW_RANGE);
            addDefault("ai.shears-drop-pumpkin", SHEARS_DROPS_PUMPKIN);
            addDefault("ai.add-pumpkin-to-head", ADD_PUMPKIN_TO_HEAD);
            addDefault("ai.damage.when-wet", AI_DAMAGE_WHEN_WET);
            addDefault("ai.damage.when-hot", AI_DAMAGE_WHEN_HOT);
            addDefault("ai.snow-trail.enabled", AI_SNOW_TRAIL_ENABLED);
            addDefault("ai.snow-trail.max-temp", AI_SNOW_TRAIL_MAX_TEMP);
            addDefault("ai.shoot.speed", AI_SHOOT_SPEED);
            addDefault("ai.shoot.inaccuracy", AI_SHOOT_INACCURACY);
            addDefault("ai.shoot.damage", AI_SHOOT_DAMAGE);
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.jump-power", RIDING_JUMP_POWER);
            addDefault("riding.step-height", RIDING_STEP_HEIGHT);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("riding.damage.when-wet", RIDING_DAMAGE_WHEN_WET);
            addDefault("riding.damage.when-hot", RIDING_DAMAGE_WHEN_HOT);
            addDefault("riding.snow-trail.enabled", RIDING_SNOW_TRAIL_ENABLED);
            addDefault("riding.snow-trail.max-temp", RIDING_SNOW_TRAIL_MAX_TEMP);
            addDefault("riding.shoot.cooldown", RIDING_SHOOT_COOLDOWN);
            addDefault("riding.shoot.speed", RIDING_SHOOT_SPEED);
            addDefault("riding.shoot.inaccuracy", RIDING_SHOOT_INACCURACY);
            addDefault("riding.shoot.damage", RIDING_SHOOT_DAMAGE);
            save();
        }

        BASE_SPEED = getDouble("base-speed");
        MAX_HEALTH = getDouble("max-health");
        AI_JUMP_POWER = (float) getDouble("ai.jump-power");
        AI_STEP_HEIGHT = (float) getDouble("ai.step-height");
        AI_FOLLOW_RANGE = getDouble("ai.follow-range");
        SHEARS_DROPS_PUMPKIN = getBoolean("ai.shears-drop-pumpkin");
        ADD_PUMPKIN_TO_HEAD = getBoolean("ai.add-pumpkin-to-head");
        AI_DAMAGE_WHEN_WET = (float) getDouble("ai.damage.when-wet");
        AI_DAMAGE_WHEN_HOT = (float) getDouble("ai.damage.when-hot");
        AI_SNOW_TRAIL_ENABLED = getBoolean("ai.snow-trail.enabled");
        AI_SNOW_TRAIL_MAX_TEMP = (float) getDouble("ai.snow-trail.max-temp");
        AI_SHOOT_SPEED = (float) getDouble("ai.shoot.speed");
        AI_SHOOT_INACCURACY = (float) getDouble("ai.shoot.inaccuracy");
        AI_SHOOT_DAMAGE = (float) getDouble("ai.shoot.damage");
        RIDING_SPEED = getDouble("riding.speed");
        RIDING_JUMP_POWER = (float) getDouble("riding.jump-power");
        RIDING_STEP_HEIGHT = (float) getDouble("riding.step-height");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_DAMAGE_WHEN_WET = (float) getDouble("riding.damage.when-wet");
        RIDING_DAMAGE_WHEN_HOT = (float) getDouble("riding.damage.when-hot");
        RIDING_SNOW_TRAIL_ENABLED = getBoolean("riding.snow-trail.enabled");
        RIDING_SNOW_TRAIL_MAX_TEMP = (float) getDouble("riding.snow-trail.max-temp");
        RIDING_SHOOT_COOLDOWN = (int) getDouble("riding.shoot.cooldown");
        RIDING_SHOOT_SPEED = (float) getDouble("riding.shoot.speed");
        RIDING_SHOOT_INACCURACY = (float) getDouble("riding.shoot.inaccuracy");
        RIDING_SHOOT_DAMAGE = (float) getDouble("riding.shoot.damage");
        RIDING_ENABLE_MOVE_EVENT = isSet("riding.enable-move-event") ? getBoolean("riding.enable-move-event") : Config.RIDING_ENABLE_MOVE_EVENT;
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
    }
}
