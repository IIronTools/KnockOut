package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SigmaKnockOut plugin;

    public PlayerQuitListener(SigmaKnockOut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getCombatLogX().getCombatManager().isInCombat(event.getPlayer())) {
            plugin.getLogger().severe("Was in combat");
        } else {
            plugin.getLogger().severe("Was NOT in combat");
        }

        if (!plugin.getKnockOutManager().isKnockedOut(event.getPlayer().getUniqueId())) return;

        KnockOut knockOut = plugin.getKnockOutManager().getNokautByUUID(event.getPlayer().getUniqueId());
        if (knockOut == null) return;
        knockOut.finishKnockout(false);
    }
}
