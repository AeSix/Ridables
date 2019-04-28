package net.pl3x.bukkit.ridables.configuration.mob;

import net.minecraft.server.v1_14_R1.PotionRegistry;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;
import net.pl3x.bukkit.ridables.util.Logger;

public class WitchConfig extends MobConfig {
    public double BASE_SPEED = 0.25D;
    public double MAX_HEALTH = 8.0D;
    public float AI_JUMP_POWER = 0.42F;
    public float AI_STEP_HEIGHT = 0.6F;
    public double AI_FOLLOW_RANGE = 16.0D;
    public double RIDING_SPEED = 0.25D;
    public float RIDING_JUMP_POWER = 0.5F;
    public float RIDING_STEP_HEIGHT = 0.6F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public boolean RIDING_ENABLE_MOVE_EVENT = false;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;
    public int RIDING_SHOOT_COOLDOWN = 40;
    public float RIDING_SHOOT_SPEED = 1.0F;
    public PotionRegistry RIDING_SHOOT_POTION_TYPE = PotionRegistry.a("harming");

    public WitchConfig() {
        super("witch.yml");
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
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.jump-power", RIDING_JUMP_POWER);
            addDefault("riding.step-height", RIDING_STEP_HEIGHT);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("shoot.cooldown", RIDING_SHOOT_COOLDOWN);
            addDefault("shoot.speed", RIDING_SHOOT_SPEED);
            addDefault("shoot.potion", RIDING_SHOOT_POTION_TYPE.b("")); // getNamePrefixed
            save();
        }

        BASE_SPEED = getDouble("base-speed");
        MAX_HEALTH = getDouble("max-health");
        AI_JUMP_POWER = (float) getDouble("ai.jump-power");
        AI_STEP_HEIGHT = (float) getDouble("ai.step-height");
        AI_FOLLOW_RANGE = getDouble("ai.follow-range");
        RIDING_SPEED = getDouble("riding.speed");
        RIDING_JUMP_POWER = (float) getDouble("riding.jump-power");
        RIDING_STEP_HEIGHT = (float) getDouble("riding.step-height");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_SHOOT_COOLDOWN = (int) getDouble("shoot.cooldown", 40);
        RIDING_SHOOT_SPEED = (float) getDouble("shoot.speed", 1.0D);
        RIDING_ENABLE_MOVE_EVENT = isSet("riding.enable-move-event") ? getBoolean("riding.enable-move-event") : Config.RIDING_ENABLE_MOVE_EVENT;
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;

        String potion = getString("shoot.potion", "harming").toLowerCase();
        PotionRegistry potionType = PotionRegistry.a(potion);
        if (potionType != null) {
            RIDING_SHOOT_POTION_TYPE = potionType;
        } else {
            Logger.error("Unknown potion type for witch.shoot.potion: " + potion);
            Logger.error("Using potion of harming as default");
            RIDING_SHOOT_POTION_TYPE = PotionRegistry.a("harming");
        }
    }
}
