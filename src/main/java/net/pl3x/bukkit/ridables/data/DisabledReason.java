package net.pl3x.bukkit.ridables.data;

import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.util.Logger;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public enum DisabledReason {
    UNSUPPORTED_SERVER_TYPE(
            "#       This server is unsupported!        #",
            "#     Spigot is a minimum requirement!     #",
            "#   Paper is the recommended requirement!  #",
            "#  Visit http://papermc.io/ for more info  #"
    ),
    UNSUPPORTED_SERVER_VERSION(
            "#       This server is unsupported!        #",
            "#    Only 1.13.2 servers are supported!    #",
            "#   See downloads page for version info    #"
    ),
    SERVER_VERSION_OUTDATED(
            "#      Your server build is outdated!      #",
            "#     Please update your Paper software    #",
            "#    See downloads page for version info   #"
    ),
    NMS_MISMATCH(
            "#        Incorrect server version!         #",
            "#   Check the graph for compatibilities    #",
            "#     See downloads page for more info     #"
    ),
    ALL_ENTITIES_DISABLED(
            "#        All entities are disabled!        #",
            "#  Please follow the instructions on wiki  #",
            "#          http://git.io/ridables          #"
    ),
    PLUGIN_DETECTED_RELOAD(
            "#                 Uh Oh!!                  #",
            "#                                          #",
            "#     Plugin reload has been detected      #",
            "#   Reloading Ridables is NOT supported!   #"
    ),
    SERVER_SHUTTING_DOWN(
            "#     Plugin reload has been detected      #",
            "#   Reloading Ridables is NOT supported!   #",
            "#                                          #",
            "#    Server will be forced to shut down    #",
            "#    now to prevent any possible damage!   #",
            "#                                          #",
            "#     You were warned not to do that..     #"
    );

    private List<String> reason;

    DisabledReason(String... reason) {
        this.reason = Arrays.asList(reason);
    }

    public void printError() {
        printError(false);
    }

    public void printError(boolean showDisablingMessage) {
        Logger.error("############################################");
        Logger.error("#                                          #");
        if (showDisablingMessage) {
            Logger.error("#     Plugin is now disabling itself!      #");
            Logger.error("#                                          #");
        }
        reason.forEach(Logger::error);
        Logger.error("#                                          #");
        Logger.error("############################################");
    }

    public void printError(CommandSender sender) {
        Lang.send(sender, "&c############################################");
        Lang.send(sender, "&c#                                          #");
        Lang.send(sender, "&c#  Ridables plugin is currently disabled!  #");
        Lang.send(sender, "&c#                                          #");
        reason.forEach(reason -> Lang.send(sender, "&c" + reason));
        Lang.send(sender, "&c#                                          #");
        Lang.send(sender, "&c############################################");
    }
}
