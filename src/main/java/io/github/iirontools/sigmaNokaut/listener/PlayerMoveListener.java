package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final SigmaNokaut plugin;

    public PlayerMoveListener(SigmaNokaut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getNokautManager().isKnockedOut(event.getPlayer().getUniqueId())) return;
        KnockOut knockOut = plugin.getNokautManager().getNokautByUUID(event.getPlayer().getUniqueId());
        if (knockOut != null) {
            if (knockOut.getLiftingPlayer() != null) {
                System.out.println("nokaut.getLiftingPlayer() != null");
                return;
            }
        }

        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setCancelled(true);
        }
    }
}
