package io.github.iirontools.sigmaNokaut.manager;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.github.iirontools.sigmaNokaut.model.Nokaut;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class NokautManager {

    private final SigmaNokaut plugin;
    private final Map<UUID, Nokaut> knockedOutPlayers;


    public NokautManager(SigmaNokaut plugin) {
        this.plugin = plugin;
        this.knockedOutPlayers = new HashMap<>();
    }

    public boolean isKnockedOut(UUID uuid) {
        return knockedOutPlayers.containsKey(uuid);
    }

    public void addKnockedOutPlayer(UUID uuid, Location location) {
        Player player = plugin.getServer().getPlayer(uuid);
        Nokaut nokaut = new Nokaut(plugin, player, location);
        nokaut.runTaskTimer(plugin, 0, 2);
        knockedOutPlayers.put(uuid, nokaut);
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

    public Nokaut getNokautByUUID(UUID uuid) {
        return knockedOutPlayers.get(uuid);
    }
}
