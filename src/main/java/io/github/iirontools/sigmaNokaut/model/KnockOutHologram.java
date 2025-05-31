package io.github.iirontools.sigmaNokaut.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class KnockOutHologram {

    private final ArmorStand hologramStand;

    public KnockOutHologram(Location location) {
        hologramStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        hologramStand.setInvisible(true);
        hologramStand.setInvulnerable(true);
        hologramStand.setGravity(false);
        hologramStand.setCustomNameVisible(true);
        hologramStand.customName(Component.text("✚ Nokautowany ✚").color(NamedTextColor.RED));
        hologramStand.setMarker(true);
    }

    public void updateHologramLocation(Location newLocation) {
        if (hologramStand != null && !hologramStand.isDead()) {
            hologramStand.teleport(newLocation.clone().add(0, 1.5, 0));
        }
    }

    public void destroyHologram() {
        if (hologramStand != null && !hologramStand.isDead()) {
            hologramStand.remove();
        }
    }
}
