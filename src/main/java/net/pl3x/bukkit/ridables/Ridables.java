package net.pl3x.bukkit.ridables;

import net.minecraft.server.v1_14_R1.EnumCreatureType;
import net.pl3x.bukkit.ridables.bstats.Metrics;
import net.pl3x.bukkit.ridables.command.CmdRidables;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.data.DisabledReason;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.item.CustomEnderCrystal;
import net.pl3x.bukkit.ridables.entity.projectile.CustomEvokerFangs;
import net.pl3x.bukkit.ridables.entity.projectile.CustomFireball;
import net.pl3x.bukkit.ridables.entity.projectile.CustomShulkerBullet;
import net.pl3x.bukkit.ridables.entity.projectile.CustomThrownTrident;
import net.pl3x.bukkit.ridables.entity.projectile.CustomWitherSkull;
import net.pl3x.bukkit.ridables.entity.projectile.DolphinSpit;
import net.pl3x.bukkit.ridables.entity.projectile.PhantomFlames;
import net.pl3x.bukkit.ridables.listener.ClickListener;
import net.pl3x.bukkit.ridables.listener.RidableListener;
import net.pl3x.bukkit.ridables.listener.WaterBucketListener;
import net.pl3x.bukkit.ridables.util.Logger;
import net.pl3x.bukkit.ridables.util.RegistryHax;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static net.pl3x.bukkit.ridables.data.DisabledReason.SERVER_SHUTTING_DOWN;

public class Ridables extends JavaPlugin implements Listener {
    private static Ridables instance;
    private static boolean isPaper;

    private DisabledReason disabledReason = null;

    public Ridables() {
        instance = this;
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            isPaper = true;
        } catch (ClassNotFoundException e) {
            isPaper = false;
        }
    }

    @Override
    public void onLoad() {
        Config.reload();
        Lang.reload();

        // Spigot minimum requirement!
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException e) {
            disabledReason = DisabledReason.UNSUPPORTED_SERVER_TYPE;
            disabledReason.printError();
            return;
        }

        // 1.14 only!
        try {
            Class.forName("net.minecraft.server.v1_14_R1.Entity");
        } catch (ClassNotFoundException e) {
            disabledReason = DisabledReason.UNSUPPORTED_SERVER_VERSION;
            disabledReason.printError();
            return;
        }

        // check for recent build of Paper
        /* not needed yet
        try {
            org.spigotmc.event.entity.EntityDismountEvent.class.getDeclaredMethod("isCancellable");
        } catch (NoSuchMethodException e) {
            disabledReason = DisabledReason.SERVER_VERSION_OUTDATED;
            disabledReason.printError();
            return;
        }
        */

        // check NMS mappings version
        String nmsVersion = ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion();
        if (!nmsVersion.equals("30f0a3bd4ceb5c03fe41ac0cfea4ffe3")) {
            disabledReason = DisabledReason.NMS_MISMATCH;
            disabledReason.printError();
            return;
        }

        // ensure plugin has not been reloaded
        if (System.getProperty("RidablesAlreadyLoaded") != null && System.getProperty("RidablesAlreadyLoaded").equals("true")) {
            disabledReason = DisabledReason.PLUGIN_DETECTED_RELOAD;
            disabledReason.printError();
            return;
        }
        System.setProperty("RidablesAlreadyLoaded", "true");

        // setup creatures by calling something in the class
        RidableType.getRidableType(EntityType.DOLPHIN);

        // check if any entities are enabled
        if (RidableType.getAllRidableTypes().isEmpty()) {
            disabledReason = DisabledReason.ALL_ENTITIES_DISABLED;
            disabledReason.printError();
            return;
        }

        // inject new custom entities
        RegistryHax.injectNewEntityTypes("custom_ender_crystal", "ender_crystal", CustomEnderCrystal::new, EnumCreatureType.MISC, 2.0F, 2.0F);
        RegistryHax.injectNewEntityTypes("custom_evoker_fangs", "evoker_fangs", CustomEvokerFangs::new, EnumCreatureType.MISC, 0.5F, 0.8F);
        RegistryHax.injectNewEntityTypes("custom_fireball", "large_fireball", CustomFireball::new, EnumCreatureType.MISC, 1.0F, 1.0F);
        RegistryHax.injectNewEntityTypes("custom_shulker_bullet", "shulker_bullet", CustomShulkerBullet::new, EnumCreatureType.MISC, 0.3125F, 0.3125F);
        RegistryHax.injectNewEntityTypes("custom_trident", "trident", CustomThrownTrident::new, EnumCreatureType.MISC, 0.5F, 0.5F);
        RegistryHax.injectNewEntityTypes("custom_wither_skull", "wither_skull", CustomWitherSkull::new, EnumCreatureType.MISC, 0.3125F, 0.3125F);
        RegistryHax.injectNewEntityTypes("dolphin_spit", "llama_spit", DolphinSpit::new, EnumCreatureType.MISC, 0.25F, 0.25F);
        RegistryHax.injectNewEntityTypes("phantom_flames", "llama_spit", PhantomFlames::new, EnumCreatureType.MISC, 0.25F, 0.25F);

        // inject new mob spawns into biomes
        RegistryHax.addMobsToBiomes();
    }

    @Override
    public void onEnable() {
        // start bstats with custom graphs
        new Metrics(this);
        // TODO add custom graphs

        // check if plugin is disabled
        if (disabledReason != null) {
            if (disabledReason == DisabledReason.PLUGIN_DETECTED_RELOAD) {
                SERVER_SHUTTING_DOWN.printError();
                Bukkit.shutdown();
                return;
            }
            disabledReason.printError(true);
            return;
        }


        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onServerLoaded(ServerLoadEvent event) {
                // ensure all blacklisted commands are loaded
                Config.reloadCommandsList(getConfig());

                if (!isPaper) {
                    Logger.warn("====================================================");
                    Logger.warn(" " + getDescription().getName() + " works better if you use Paper ");
                    Logger.warn(" as your server software. ");
                    Logger.warn("  ");
                    Logger.warn(" Paper offers significant performance improvements,");
                    Logger.warn(" bug fixes, security enhancements and optional");
                    Logger.warn(" features for server owners to enhance their server.");
                    Logger.warn("  ");
                    Logger.warn(" Paper includes Timings v2, which is significantly");
                    Logger.warn(" better at diagnosing lag problems over v1.");
                    Logger.warn("  ");
                    Logger.warn(" All of your plugins should still work, and the");
                    Logger.warn(" Paper community will gladly help you fix any issues.");
                    Logger.warn("  ");
                    Logger.warn(" Join the Paper Community @ https://papermc.io");
                }
            }
        }, this);

        getServer().getPluginManager().registerEvents(new ClickListener(), this);
        getServer().getPluginManager().registerEvents(new RidableListener(), this);
        getServer().getPluginManager().registerEvents(new WaterBucketListener(), this);

        //noinspection ConstantConditions
        getCommand("ridables").setExecutor(new CmdRidables(this));

        Logger.info("Finished enabling");
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(Entity::leaveVehicle);

        Logger.info("Finished disabling");
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        disabledReason.printError(sender);
        return true;
    }

    /**
     * Get the instance of this plugin
     *
     * @return Ridables instance
     */
    public static Ridables getInstance() {
        return instance;
    }

    /**
     * Check if running on a Paper server
     *
     * @return True if on Paper server
     */
    public static boolean isPaper() {
        return isPaper;
    }
}
