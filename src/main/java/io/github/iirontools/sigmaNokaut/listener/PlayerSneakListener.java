package io.github.iirontools.sigmaNokaut.listener;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

public class PlayerSneakListener implements Listener {

    private final SigmaKnockOut plugin;
    private final MainConfig mainConfig;

    public PlayerSneakListener(SigmaKnockOut plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getMainConfig();
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {

        if (!event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        UUID knockedOutUUID = plugin.getKnockOutManager().getKnockedOutPlayerWithinDistance(player.getLocation(), plugin.getMainConfig().getHealingRange());

        if (knockedOutUUID == null || knockedOutUUID.equals(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        Player knockedOutPlayer = plugin.getServer().getPlayer(knockedOutUUID);
        if (knockedOutPlayer == null || !knockedOutPlayer.isOnline()) return;

        KnockOut knockOut = plugin.getKnockOutManager().getNokautByUUID(knockedOutUUID);
        if (knockOut == null) return;
        
        // Stopping Lifting
        if (knockOut.getLiftingPlayer() != null && knockOut.getLiftingPlayer().equals(player)) {
            knockOut.setLiftingPlayer(null);
            player.removePassenger(knockOut.getKnockedOutPlayer());
            knockOut.setLocation(event.getPlayer().getLocation());
            knockOut.getKnockedOutPlayer().teleport(event.getPlayer().getLocation());
            return;
        }

        // Healing
        if (knockOut.isHealing(player.getUniqueId())) return;

        knockOut.addHealingPlayer(player.getUniqueId());
        player.sendMessage(mainConfig.getMessagePrefix().append(mainConfig.getHealingPlayerMessage()).append(knockedOutPlayer.displayName()));
    }
}
