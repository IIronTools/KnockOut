package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.github.iirontools.sigmaNokaut.model.Nokaut;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final SigmaNokaut plugin;

    public PlayerDeathListener(SigmaNokaut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getNokautManager().isKnockedOut(uuid)) {
            System.out.println("isalready");
            return;
        }

        Location location = player.getLocation();

        event.setCancelled(true);
        System.out.println("canceled");
        plugin.getNokautManager().addKnockedOutPlayer(uuid, location);
    }
}
