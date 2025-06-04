package io.github.iirontools.sigmaNokaut.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class KnockOutHologram {

    private static final double DEFAULT_LINE_SPACING = 0.25;
    private static final double DEFAULT_BASE_Y_OFFSET = 1.6;

    private final List<ArmorStand> lines = new ArrayList<>();

    public KnockOutHologram(Location baseLocation, List<Component> components) {
        World world = baseLocation.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location must have a valid world");
        }

        for (int i = 0; i < components.size(); i++) {
            double yOffset = DEFAULT_BASE_Y_OFFSET - (i * DEFAULT_LINE_SPACING);
            Location lineLocation = baseLocation.clone().add(0, yOffset, 0);
            lines.add(createHologramLine(world, lineLocation, components.get(i)));
        }
    }

    private ArmorStand createHologramLine(World world, Location location, Component name) {
        ArmorStand stand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.customName(name);
        stand.setMarker(true);
        return stand;
    }

    public void updateHologramLocation(Location newLocation, List<Component> newComponents) {
        if (lines.isEmpty()) return;

        for (int i = 0; i < lines.size(); i++) {
            ArmorStand stand = lines.get(i);
            if (stand == null || stand.isDead()) continue;

            double yOffset = DEFAULT_BASE_Y_OFFSET - (i * DEFAULT_LINE_SPACING);
            stand.teleport(newLocation.clone().add(0, yOffset, 0));

            if (i < newComponents.size()) {
                stand.customName(newComponents.get(i));
            }
        }
    }

    public void destroyHologram() {
        for (ArmorStand stand : lines) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        lines.clear();
    }
}
