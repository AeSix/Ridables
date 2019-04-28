package net.pl3x.bukkit.ridables.configuration;

import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_14_R1.command.VanillaCommandWrapper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Config {
    public static String LANGUAGE_FILE = "lang-en.yml";

    public static boolean LOGGER_INFO = true;
    public static boolean LOGGER_WARN = true;
    public static boolean LOGGER_ERROR = true;
    public static boolean LOGGER_DEBUG = false;

    public static boolean COMMANDS_LIST_IS_WHITELIST = true;
    public static Set<String> COMMANDS_LIST = new HashSet<>();

    public static boolean RIDING_ENABLE_MOVE_EVENT = false;
    public static boolean RIDING_SADDLE_REQUIRE = false;
    public static boolean RIDING_SADDLE_CONSUME = false;

    private static File mobsDir;

    public static File getMobsDirectory() {
        if (mobsDir == null) {
            mobsDir = new File(Ridables.getInstance().getDataFolder(), "mobs");
            if (mobsDir.mkdirs()) {
                Logger.debug("Created new mob config directory");
            }
        }
        return mobsDir;
    }

    /**
     * Reload the config.yml from disk
     */
    public static void reload() {
        Ridables plugin = Ridables.getInstance();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        LANGUAGE_FILE = config.getString("language-file", "lang-en.yml");

        LOGGER_INFO = config.getBoolean("logger.info", true);
        LOGGER_WARN = config.getBoolean("logger.warn", true);
        LOGGER_ERROR = config.getBoolean("logger.error", true);
        LOGGER_DEBUG = config.getBoolean("logger.debug", false);

        RIDING_ENABLE_MOVE_EVENT = config.getBoolean("riding.enable-move-event", false);
        RIDING_SADDLE_REQUIRE = config.getBoolean("riding.saddle.require", false);
        RIDING_SADDLE_CONSUME = config.getBoolean("riding.saddle.consume", false);

        reloadCommandsList(config);
    }

    public static void reloadCommandsList(FileConfiguration config) {
        COMMANDS_LIST_IS_WHITELIST = config.getBoolean("commands-list-is-whitelist", true);
        COMMANDS_LIST.clear();

        Set<String> toAdd = new HashSet<>();                   // use Set to prevent duplicates
        for (String command : config.getStringList("commands-list")) {
            toAdd.clear();                                     // clear previous command's entries
            toAdd.add(command);                                // add raw command from config
            Command bukkitCmd = Bukkit.getCommandMap().getCommand(command);
            if (bukkitCmd != null) {
                toAdd.add(bukkitCmd.getLabel());               // add bukkit's command label (includes vanilla's label)
                toAdd.addAll(bukkitCmd.getAliases());          // add bukkit's command aliases (vanilla has no concept of aliases)
                if (bukkitCmd instanceof PluginCommand) {
                    String pluginName = ((PluginCommand) bukkitCmd).getPlugin().getName();
                    toAdd.addAll(toAdd.stream()                // add bukkit's fallback commands and aliases
                            .map(cmd -> pluginName + ":" + cmd)
                            .collect(Collectors.toList()));
                } else if (bukkitCmd instanceof VanillaCommandWrapper) {
                    toAdd.addAll(toAdd.stream()                // add vanilla's fallback commands
                            .map(cmd -> "minecraft:" + cmd)
                            .collect(Collectors.toList()));
                }
            }
            COMMANDS_LIST.addAll(toAdd.stream()
                    .map(String::toLowerCase)                  // lowercase _everything_
                    .collect(Collectors.toSet()));
        }
    }

    public static boolean isEnabled(String name) {
        return Ridables.getInstance().getConfig().getBoolean("mobs." + name, false);
    }
}
