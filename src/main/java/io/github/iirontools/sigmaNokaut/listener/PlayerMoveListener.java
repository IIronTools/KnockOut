package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final SigmaKnockOut plugin;

    public PlayerMoveListener(SigmaKnockOut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getKnockOutManager().isKnockedOut(event.getPlayer().getUniqueId())) return;
        KnockOut knockOut = plugin.getKnockOutManager().getNokautByUUID(event.getPlayer().getUniqueId());
        if (knockOut != null) {
            if (knockOut.getLiftingPlayer() != null) {
                return;
            }
        }

        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setCancelled(true);
        }
    }
}
