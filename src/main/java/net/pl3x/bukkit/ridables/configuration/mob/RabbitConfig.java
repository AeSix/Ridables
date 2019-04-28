package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class RabbitConfig extends MobConfig {
    public double BASE_SPEED = 0.3D;
    public double MAX_HEALTH = 3.0D;
    public float AI_JUMP_POWER = 0.5F;
    public double AI_FOLLOW_RANGE = 16.0D;
    public float AI_MELEE_DAMAGE = 3.0F;
    public double TOAST_CHANCE = 0.0D;
    public double AI_KILLER_CHANCE = 0.0D;
    public double AI_KILLER_ARMOR = 8.0D;
    public float AI_KILLER_DAMAGE = 8.0F;
    public double RIDING_SPEED = 0.3D;
    public float RIDING_JUMP_POWER = 0.5F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public boolean RIDING_RIDE_KILLER_BUNNY = true;
    public boolean RIDING_BABIES = false;
    public boolean RIDING_ENABLE_MOVE_EVENT = false;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;

    public RabbitConfig() {
        super("rabbit.yml");
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
            addDefault("ai.follow-range", AI_FOLLOW_RANGE);
            addDefault("ai.melee-damage", AI_MELEE_DAMAGE);
            addDefault("ai.toast.chance", TOAST_CHANCE);
            addDefault("ai.killer.chance", AI_KILLER_CHANCE);
            addDefault("ai.killer.armor", AI_KILLER_ARMOR);
            addDefault("ai.killer.damage", AI_KILLER_DAMAGE);
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.jump-power", RIDING_JUMP_POWER);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("riding.ride-killer-bunny", RIDING_RIDE_KILLER_BUNNY);
            addDefault("riding.ride-babies", RIDING_BABIES);
            save();
        }

        BASE_SPEED = getDouble("base-speed");
        MAX_HEALTH = getDouble("max-health");
        AI_JUMP_POWER = (float) getDouble("ai.jump-power");
        AI_FOLLOW_RANGE = getDouble("ai.follow-range");
        AI_MELEE_DAMAGE = (float) getDouble("ai.melee-damage");
        TOAST_CHANCE = getDouble("ai.toast.chance");
        AI_KILLER_CHANCE = getDouble("ai.killer.chance");
        AI_KILLER_ARMOR = getDouble("ai.killer.armor");
        AI_KILLER_DAMAGE = (float) getDouble("ai.killer.damage");
        RIDING_SPEED = getDouble("riding.speed");
        RIDING_JUMP_POWER = (float) getDouble("riding.jump-power");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_RIDE_KILLER_BUNNY = getBoolean("riding.ride-killer-bunny");
        RIDING_BABIES = getBoolean("riding.ride-babies");
        RIDING_ENABLE_MOVE_EVENT = isSet("riding.enable-move-event") ? getBoolean("riding.enable-move-event") : Config.RIDING_ENABLE_MOVE_EVENT;
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
    }
}
