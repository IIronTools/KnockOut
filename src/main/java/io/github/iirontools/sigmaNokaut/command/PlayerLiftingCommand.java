package io.github.iirontools.sigmaNokaut.command;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLiftingCommand implements CommandExecutor {

    private final SigmaNokaut plugin;

    public PlayerLiftingCommand(SigmaNokaut plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tylko gracze mogą używać tej komendy");
            return true;
        }

        Location location = player.getLocation();
        Component prefix = plugin.getMainConfig().getMessagePrefix();

        UUID knockedOutUUID = plugin.getNokautManager().getKnockedOutPlayerWithinDistance(location, plugin.getMainConfig().getHealingRange()); // TODO trzeba dodać nowy RANGE do configu
        if (knockedOutUUID == null) {
            player.sendMessage(prefix.append(Component.text("W pobliżu nie znajduje się żadnen znokautowany gracz!", NamedTextColor.RED)));
            return true;
        }

        KnockOut knockOut = plugin.getNokautManager().getNokautByUUID(knockedOutUUID);
        if (knockOut == null) {
            player.sendMessage(prefix.append(Component.text("W pobliżu nie znajduje się żadnen znokautowany gracz!", NamedTextColor.RED)));
            return true;
        }

        if (knockOut.getKnockedOutPlayer().equals(player)) {
            player.sendMessage(prefix.append(Component.text("Jesteś znokautowany. Nie możesz podnosić innych graczy!", NamedTextColor.RED)));
            return true;
        }

        if (knockOut.getLiftingPlayer() != null && knockOut.getLiftingPlayer().equals(player)) {
            player.sendMessage(prefix.append(Component.text("Już podnosisz gracza!", NamedTextColor.RED)));
            return true;
        }

        knockOut.setLiftingPlayer(player);
        player.sendMessage(prefix.append(Component.text("Podniesiono gracza!", NamedTextColor.GREEN)));
        return true;
    }
}
