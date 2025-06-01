package io.github.iirontools.sigmaNokaut.command;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveUpCommand implements CommandExecutor {

    private final SigmaKnockOut plugin;
    private final MainConfig mainConfig;

    public GiveUpCommand(SigmaKnockOut plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getMainConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mainConfig.getConsoleUsageMessage());
            return true;
        }

        var prefix = plugin.getMainConfig().getMessagePrefix();
        var knockout = plugin.getKnockOutManager().getNokautByUUID(player.getUniqueId());
        if (knockout == null) {
            player.sendMessage(prefix.append(mainConfig.getNotKnockedOutMessage()));
            return true;
        }

        knockout.finishKnockout(false);
        player.sendMessage(prefix.append(mainConfig.getYouSurrenderedMessage()));
        return true;
    }
}
