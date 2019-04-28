package net.pl3x.bukkit.ridables.command;

import net.minecraft.server.v1_14_R1.Entity;
import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdRidables implements TabExecutor {
    private final Ridables plugin;

    public CmdRidables(Ridables plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ridables.command.ridables")) {
            Logger.debug("Perm Check: " + sender.getName() + " does NOT have permission for /ridables command");
            Lang.send(sender, Lang.COMMAND_NO_PERMISSION);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            Lang.send(sender, Lang.RELOADING_CONFIG);
            Config.reload();

            Lang.send(sender, Lang.RELOADING_LANG);
            Lang.reload();

            Lang.send(sender, Lang.RELOADING_MOB_CONFIGS);
            RidableType.getAllRidableTypes().forEach(ridable -> {
                if (ridable.getConfig() != null) {
                    ridable.getConfig().reload();
                }
            });

            Lang.send(sender, Lang.RELOADING_MOB_ATTRIBUTES);
            Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
                Entity nmsEntity = ((CraftEntity) entity).getHandle();
                if (nmsEntity instanceof RidableEntity) {
                    ((RidableEntity) nmsEntity).reloadAttributes(); // TODO
                }
            }));

            Lang.send(sender, Lang.RELOAD_COMPLETE);
            return true;
        }

        Lang.send(sender, "&e[&3Ridables&e]&a v" + plugin.getDescription().getVersion());
        return true;
    }
}
