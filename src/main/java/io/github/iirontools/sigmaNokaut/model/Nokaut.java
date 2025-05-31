package io.github.iirontools.sigmaNokaut.model;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.iirontools.sigmaNokaut.SigmaNokaut;
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

public class Nokaut extends BukkitRunnable {

    private final SigmaNokaut plugin;

    @Getter private final Player knockedOutPlayer;
    @Getter @Setter private Player liftingPlayer;
    @Getter @Setter private Location location;
    private final Set<UUID> healingPlayers = new HashSet<>();
    private int progress;

    private static final BlockData BARRIER_DATA = Material.BARRIER.createBlockData();
    private Location barrierLocation = null;
    private boolean firstIteration = true;

    public Nokaut(SigmaNokaut plugin, Player knockedOutPlayer, Location location) {
        this.plugin = plugin;
        this.knockedOutPlayer = knockedOutPlayer;
        this.liftingPlayer = null;
        this.location = location;
        this.progress = plugin.getMainConfig().getKnockoutThreshold();
    }

    @Override
    public void run() {
        if (knockedOutPlayer == null || !knockedOutPlayer.isOnline()) {
            this.cancel();
            return;
        }

        MainConfig config = plugin.getMainConfig();

        if (firstIteration) {
            knockedOutPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 10, true));
            firstIteration = false;
        }

        if (progress <= config.getDeathThreshold()) {
            finishKnockout(false);
            this.cancel();
            return;
        }

        if (progress >= config.getReviveThreshold()) {
            finishKnockout(true);
            this.cancel();
            return;
        }

        if (liftingPlayer != null) {
            liftingPlayer.addPassenger(knockedOutPlayer);
            setPose(EntityPose.SWIMMING, true);
            location = liftingPlayer.getLocation().clone().add(0, 1.75, 0);
//            knockedOutPlayer.teleport(location);
        } else {
            updateBarrierLocation(location.clone().add(0, 1, 0));
            setPose(EntityPose.SWIMMING, true);
        }
        displayKnockedOutTitle();
        updateHealingProgress(config);
    }

    private void finishKnockout(boolean revived) {
        destroyBarrier();
        if (liftingPlayer != null) {
            liftingPlayer.removePassenger(knockedOutPlayer);
        }

        setPose(EntityPose.STANDING, true);
        removeKnockedOutTitle();
        knockedOutPlayer.removePotionEffect(PotionEffectType.BLINDNESS);

        if (revived) {
            knockedOutPlayer.sendMessage(plugin.getMainConfig().getMessagePrefix()
                    .append(Component.text("Zostałeś uleczony", NamedTextColor.GREEN)));
        } else {
            knockedOutPlayer.setHealth(0.0);
        }

        plugin.getNokautManager().removeKnockedOutPlayer(knockedOutPlayer.getUniqueId());
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
                progress += config.getHealPerPlayerRate();
            } else {
                iterator.remove();
            }
        }

        if (!anyHealerInRange) {
            progress -= config.getPassiveDecayRate();
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
        int secondsRemaining = (progress - plugin.getMainConfig().getDeathThreshold()) / 20;
        Component subtitle = Component.text("Zostało ci ", NamedTextColor.GOLD)
                .append(Component.text(secondsRemaining + " sekund", NamedTextColor.YELLOW));

        Title title = Title.title(
                Component.text("Jesteś znokautowany", NamedTextColor.DARK_RED),
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
