package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final SigmaKnockOut plugin;

    public PlayerDeathListener(SigmaKnockOut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getKnockOutManager().isKnockedOut(uuid)) {
            return;
        }

        Location location = player.getLocation();

        event.setCancelled(true);
        plugin.getKnockOutManager().addKnockedOutPlayer(uuid, location);
    }
}
