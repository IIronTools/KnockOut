package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

public class PlayerSneakListener implements Listener {

    private final SigmaNokaut plugin;

    public PlayerSneakListener(SigmaNokaut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {

        if (!event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        UUID knockedOutUUID = plugin.getNokautManager().getKnockedOutPlayerWithinDistance(player.getLocation(), plugin.getMainConfig().getHealingRange());

        if (knockedOutUUID == null || knockedOutUUID.equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        Player knockedOutPlayer = plugin.getServer().getPlayer(knockedOutUUID);
        if (knockedOutPlayer == null || !knockedOutPlayer.isOnline()) return;

        KnockOut knockOut = plugin.getNokautManager().getNokautByUUID(knockedOutUUID);
        if (knockOut == null) return;


        // Przestawanie podnoszenia
        if (knockOut.getLiftingPlayer() != null && knockOut.getLiftingPlayer().equals(player)) {
            knockOut.setLiftingPlayer(null);
            player.removePassenger(knockOut.getKnockedOutPlayer());
            knockOut.setLocation(event.getPlayer().getLocation());
            knockOut.getKnockedOutPlayer().teleport(event.getPlayer().getLocation());
            return;
        }

        // Leczenie
        if (knockOut.isHealing(player.getUniqueId())) return;

        knockOut.addHealingPlayer(player.getUniqueId());

        Component message = Component.text("Leczysz gracza ", NamedTextColor.GREEN).append(knockedOutPlayer.displayName());
        player.sendMessage(plugin.getMainConfig().getMessagePrefix().append(message));
    }
}
