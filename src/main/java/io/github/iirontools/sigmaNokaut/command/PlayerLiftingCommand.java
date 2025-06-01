package io.github.iirontools.sigmaNokaut.command;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLiftingCommand implements CommandExecutor {

    private final SigmaKnockOut plugin;
    private final MainConfig mainConfig;

    public PlayerLiftingCommand(SigmaKnockOut plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getMainConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mainConfig.getConsoleUsageMessage());
            return true;
        }

        Location location = player.getLocation();
        Component prefix = plugin.getMainConfig().getMessagePrefix();

        UUID knockedOutUUID = plugin.getKnockOutManager().getKnockedOutPlayerWithinDistance(location, plugin.getMainConfig().getHealingRange()); // TODO trzeba dodać nowy RANGE do configu
        if (knockedOutUUID == null) {
            player.sendMessage(prefix.append(mainConfig.getNoKnockedOutNearbyMessage()));
            return true;
        }

        KnockOut knockOut = plugin.getKnockOutManager().getNokautByUUID(knockedOutUUID);
        if (knockOut == null) {
            player.sendMessage(prefix.append(mainConfig.getNoKnockedOutNearbyMessage()));
            return true;
        }

        if (knockOut.getKnockedOutPlayer().equals(player)) {
            player.sendMessage(prefix.append(mainConfig.getYouAreKnockedOutMessage()));
            return true;
        }

        if (knockOut.getLiftingPlayer() != null && knockOut.getLiftingPlayer().equals(player)) {
            player.sendMessage(prefix.append(mainConfig.getAlreadyLiftingMessage()));
            return true;
        }

        knockOut.setLiftingPlayer(player);
        player.sendMessage(prefix.append(mainConfig.getPlayerLiftedMessage()));
        return true;
    }
}
