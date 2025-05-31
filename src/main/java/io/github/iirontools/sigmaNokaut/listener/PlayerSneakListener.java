package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.github.iirontools.sigmaNokaut.model.Nokaut;
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

        Nokaut nokaut = plugin.getNokautManager().getNokautByUUID(knockedOutUUID);
        if (nokaut == null) return;


        // Przestawanie podnoszenia
        if (nokaut.getLiftingPlayer() != null && nokaut.getLiftingPlayer().equals(player)) {
            nokaut.setLiftingPlayer(null);
            player.removePassenger(nokaut.getKnockedOutPlayer());
            nokaut.setLocation(event.getPlayer().getLocation());
            nokaut.getKnockedOutPlayer().teleport(event.getPlayer().getLocation());
            return;
        }

        // Leczenie
        if (nokaut.isHealing(player.getUniqueId())) return;

        nokaut.addHealingPlayer(player.getUniqueId());

        Component message = Component.text("Leczysz gracza ", NamedTextColor.GREEN).append(knockedOutPlayer.displayName());
        player.sendMessage(plugin.getMainConfig().getMessagePrefix().append(message));
    }
}
