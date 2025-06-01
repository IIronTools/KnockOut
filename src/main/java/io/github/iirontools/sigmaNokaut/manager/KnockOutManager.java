package io.github.iirontools.sigmaNokaut.manager;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.model.KnockOut;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class KnockOutManager {

    private final SigmaKnockOut plugin;
    private final Map<UUID, KnockOut> knockedOutPlayers;


    public KnockOutManager(SigmaKnockOut plugin) {
        this.plugin = plugin;
        this.knockedOutPlayers = new HashMap<>();
    }

    public boolean isKnockedOut(UUID uuid) {
        return knockedOutPlayers.containsKey(uuid);
    }

    public void addKnockedOutPlayer(UUID uuid, Location location) {
        Player player = plugin.getServer().getPlayer(uuid);
        KnockOut knockOut = new KnockOut(plugin, player, location);
        knockOut.runTaskTimer(plugin, 0, 1);
        knockedOutPlayers.put(uuid, knockOut);
        plugin.getLogger().info(knockedOutPlayers.keySet().toString());
    }

    public void removeKnockedOutPlayer(UUID uuid) {
        knockedOutPlayers.remove(uuid);
    }

    public UUID getKnockedOutPlayerWithinDistance(Location location, double distance) {
        for (var entry : knockedOutPlayers.entrySet()) {
            Location knockedOutLocation = entry.getValue().getLocation();
            System.out.println(knockedOutLocation);

            if (location.distanceSquared(knockedOutLocation) <= Math.pow(distance, 2)) {
                System.out.println(entry.getKey());
                return entry.getKey();
            }
        }
        return null;
    }

    public KnockOut getNokautByUUID(UUID uuid) {
        return knockedOutPlayers.get(uuid);
    }
}
