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
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class KnockOut extends BukkitRunnable {

    private final SigmaKnockOut plugin;

    @Getter private final Player knockedOutPlayer;
    @Getter @Setter private Player liftingPlayer;
    @Getter @Setter private Location location;
    private final Set<UUID> healingPlayers = new HashSet<>();
    private double progress;

    private static final BlockData BARRIER_DATA = Material.BARRIER.createBlockData();
    private Location barrierLocation = null;
    private KnockOutHologram hologram = null;
    private boolean firstIteration = true;

    public KnockOut(SigmaKnockOut plugin, Player knockedOutPlayer, Location location) {
        this.plugin = plugin;
        this.knockedOutPlayer = knockedOutPlayer;
        this.liftingPlayer = null;
        this.location = location;
        this.progress = plugin.getMainConfig().getKnockoutThreshold(); // Seconds
    }

    @Override
    public void run() {
        if (knockedOutPlayer == null || !knockedOutPlayer.isOnline()) {
            finishKnockout(false);
            return;
        }

        MainConfig config = plugin.getMainConfig();

        if (firstIteration) {
            knockedOutPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 10, true));
            firstIteration = false;
            knockedOutPlayer.setInvulnerable(true);
            knockedOutPlayer.setCollidable(false);
            hologram = new KnockOutHologram(location, List.of(config.getHologramText(), Component.text(Math.round(progress)).append(Component.text(" ♥", NamedTextColor.RED))));
        }

        if (progress <= 0) {
            finishKnockout(false);
            return;
        }

        if (progress >= config.getReviveThreshold()) {
            finishKnockout(true);
            this.cancel();
            return;
        }

        if (liftingPlayer != null) {
            liftingPlayer.addPassenger(knockedOutPlayer);
            setPose(EntityPose.SWIMMING, false);
            location = liftingPlayer.getLocation().clone().add(0, 1.75, 0);
        } else {
            updateBarrierLocation(location.clone().add(0, 1, 0));
            setPose(EntityPose.SWIMMING, true);
        }

        displayKnockedOutTitle();
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
        setPose(EntityPose.STANDING, true);
        removeKnockedOutTitle();
        knockedOutPlayer.removePotionEffect(PotionEffectType.BLINDNESS);

        if (revived) {
            knockedOutPlayer.sendMessage(plugin.getMainConfig().getMessagePrefix()
                    .append(plugin.getMainConfig().getHealedMessage()));
        } else {
            knockedOutPlayer.setHealth(0.0);
        }

        plugin.getKnockOutManager().removeKnockedOutPlayer(knockedOutPlayer.getUniqueId());
        this.cancel();
    }

    private void updateHealingProgress(MainConfig config) {
        boolean anyHealerInRange = false;
        Iterator<UUID> iterator = healingPlayers.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Player healer = plugin.getServer().getPlayer(uuid);
            if (healer != null && healer.isOnline() &&
                    healer.getLocation().distanceSquared(this.location) <= Math.pow(config.getHealingRange(), 2)) {
                anyHealerInRange = true;
                progress += (double) config.getHealPerPlayerRate() / 20;
            } else {
                iterator.remove();
            }
        }

        if (!anyHealerInRange) {
            if (liftingPlayer == null) {
                progress -= 0.05;
            }
        } else {
//            knockedOutPlayer.getWorld().spawnParticle(Particle.HEART, knockedOutPlayer.getLocation(), 1);

        }
    }

    private void updateBarrierLocation(Location location) {
        destroyBarrier();
        this.barrierLocation = location;
        knockedOutPlayer.sendBlockChange(location, BARRIER_DATA);
    }

    private void destroyBarrier() {
        if (barrierLocation != null) {
            knockedOutPlayer.sendBlockChange(barrierLocation, barrierLocation.getBlock().getBlockData());
            barrierLocation = null;
        }
    }

    private void setPose(EntityPose entityPose, boolean ignoreKnockedOutPlayer) {
        int entityId = knockedOutPlayer.getEntityId();
        List<EntityData<?>> metadataList = new ArrayList<>();
        metadataList.add(new EntityData<>(6, EntityDataTypes.ENTITY_POSE, entityPose));
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(entityId, metadataList);

        for (var onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (ignoreKnockedOutPlayer && onlinePlayer.equals(knockedOutPlayer)) continue;
            PacketEvents.getAPI().getPlayerManager().sendPacket(onlinePlayer, packet);
        }
    }

    private void displayKnockedOutTitle() {
        int secondsRemaining = (int) Math.round(progress);
        Component subtitle = plugin.getMainConfig().getKnockedOutSubtitle().replaceText(builder -> builder
                .matchLiteral("{seconds}")
                .replacement(Component.text(String.valueOf(secondsRemaining), NamedTextColor.YELLOW))
        );

        Title title = Title.title(
                plugin.getMainConfig().getKnockedOutTitle(),
                subtitle,
                Title.Times.times(Duration.ZERO, Duration.ofDays(10000), Duration.ZERO)
        );
        knockedOutPlayer.showTitle(title);
    }

    private void removeKnockedOutTitle() {
        knockedOutPlayer.clearTitle();
    }

    public boolean isHealing(UUID uuid) {
        return healingPlayers.contains(uuid);
    }

    public void addHealingPlayer(UUID uuid) {
        healingPlayers.add(uuid);
    }
}
