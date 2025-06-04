package io.github.iirontools.sigmaNokaut.model;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class KnockOut extends BukkitRunnable {

    private final SigmaKnockOut plugin;
    private final MainConfig config;

    @Getter private final Player knockedOutPlayer;
    @Getter @Setter private Player liftingPlayer;
    @Getter @Setter private Location location;
    private final Set<UUID> healingPlayers = new HashSet<>();
    private double progress;

    private static final BlockData BARRIER_DATA = Material.BARRIER.createBlockData();
    private Location barrierLocation = null;
    private final KnockOutHologram hologram;

    private EntityPose currentPose = null;
    private int lastDisplayedHearts = -1;
    private Location lastHoloLocation;
    private int lastHoloHearts = -1;

    public KnockOut(SigmaKnockOut plugin, Player knockedOutPlayer, Location location) {
        this.plugin = plugin;
        this.config = plugin.getMainConfig();
        this.knockedOutPlayer = knockedOutPlayer;
        this.liftingPlayer = null;
        this.location = location;
        this.progress = plugin.getMainConfig().getKnockoutThreshold(); // Seconds

        // Moved from fistIteration
        knockedOutPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 10, true));
        knockedOutPlayer.setInvulnerable(true);
        knockedOutPlayer.setCollidable(false);
        hologram = new KnockOutHologram(location, List.of(config.getHologramText(), Component.text(Math.round(progress)).append(Component.text(" ♥", NamedTextColor.RED))));
    }

    @Override
    public void run() {
        if (knockedOutPlayer == null || !knockedOutPlayer.isOnline()) {
            finishKnockout(false);
            return;
        }

        if (progress <= 0) {
            finishKnockout(false);
            return;
        }

        if (progress >= config.getReviveThreshold()) {
            finishKnockout(true);
            return;
        }

        if (liftingPlayer != null) {
            destroyBarrier();
            liftingPlayer.addPassenger(knockedOutPlayer);
            updatePose(EntityPose.SWIMMING, false);
            location = liftingPlayer.getLocation().clone().add(0, 1.75, 0);
        } else {
            updateBarrierLocation(location.clone().add(0, 1, 0));
            updatePose(EntityPose.SWIMMING, true);
        }

        int currentHearts = (int) Math.round(progress);
        Location currentLoc = liftingPlayer != null ? liftingPlayer.getLocation().clone().add(0, 1.75, 0) : location.clone().add(0, 1, 0);

        if (!currentLoc.equals(lastHoloLocation) || currentHearts != lastHoloHearts) {
            hologram.updateHologramLocation(
                    currentLoc,
                    List.of(
                            config.getHologramText(),
                            Component.text(currentHearts).append(Component.text(" ♥", NamedTextColor.RED))
                    )
            );
            lastHoloLocation = currentLoc.clone();
            lastHoloHearts = currentHearts;
        }
        if (currentHearts != lastDisplayedHearts) {
            updateKnockedOutTitle(currentHearts);
            lastDisplayedHearts = currentHearts;
        }

        updateHealingProgress(config);
        hologram.updateHologramLocation(location, List.of(config.getHologramText(),Component.text(Math.round(progress)).append(Component.text(" ♥", NamedTextColor.RED))));
    }

    public void finishKnockout(boolean revived) {
        destroyBarrier();
        hologram.destroyHologram();
        if (liftingPlayer != null) {
            liftingPlayer.removePassenger(knockedOutPlayer);
        }

        knockedOutPlayer.setInvulnerable(false);
        knockedOutPlayer.setCollidable(true);
        updatePose(EntityPose.STANDING, true);
        removeKnockedOutTitle();
        knockedOutPlayer.removePotionEffect(PotionEffectType.BLINDNESS);

        if (revived) {
            knockedOutPlayer.sendMessage(config.getMessagePrefix()
                    .append(config.getHealedMessage()));
        } else {
            knockedOutPlayer.setHealth(0.0);
        }
        plugin.getKnockOutManager().removeKnockedOutPlayer(knockedOutPlayer.getUniqueId());
        this.cancel();
    }

    private void updateKnockedOutTitle(int hearts) {
        Component subtitle = config.getKnockedOutSubtitle()
                .replaceText(builder -> builder
                        .matchLiteral("{seconds}")
                        .replacement(Component.text(hearts, NamedTextColor.YELLOW))
                );

        Title title = Title.title(
                config.getKnockedOutTitle(),
                subtitle,
                Title.Times.times(Duration.ZERO, Duration.ofDays(10000), Duration.ZERO)
        );
        knockedOutPlayer.showTitle(title);
    }

    private void updateHealingProgress(MainConfig config) {
        boolean anyHealerInRange = false;

        Iterator<UUID> iterator = healingPlayers.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Player healer = plugin.getServer().getPlayer(uuid);
            if (healer == null || !healer.isOnline()) {
                iterator.remove();
                continue;
            }
            if (healer.getLocation().distanceSquared(this.location) <= Math.pow(config.getHealingRange(), 2)) {
                anyHealerInRange = true;
                progress += (double) config.getHealPerPlayerRate() / 20;
            }
        }
        if (!anyHealerInRange) {
            if (liftingPlayer == null) {
                progress -= 0.05;
            }
        }
    }

    private void updateBarrierLocation(Location location) {
        destroyBarrier();
        this.barrierLocation = location;
        knockedOutPlayer.sendBlockChange(location, BARRIER_DATA);
    }

    private void destroyBarrier() {
        if (barrierLocation != null && barrierLocation.getWorld() != null) {
            knockedOutPlayer.sendBlockChange(barrierLocation, barrierLocation.getBlock().getBlockData());
            barrierLocation = null;
        }
    }

    private void removeKnockedOutTitle() {
        knockedOutPlayer.clearTitle();
    }

    private void updatePose(EntityPose newPose, boolean ignoreKnockedOutPlayer) {
        currentPose = newPose;
        List<EntityData<?>> metadataList = List.of(
                new EntityData<>(6, EntityDataTypes.ENTITY_POSE, newPose)
        );
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                knockedOutPlayer.getEntityId(), metadataList
        );

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (ignoreKnockedOutPlayer && online.equals(knockedOutPlayer)) continue;
            PacketEvents.getAPI().getPlayerManager().sendPacket(online, packet);
        }
    }

    public boolean isHealing(UUID uuid) {
        return healingPlayers.contains(uuid);
    }

    public void addHealingPlayer(UUID uuid) {
        healingPlayers.add(uuid);
    }
}
