package io.github.iirontools.sigmaNokaut.config;

import io.github.iirontools.sigmaNokaut.SigmaKnockOut;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MainConfig {

    private final SigmaKnockOut plugin;
    private final File file;
    private YamlConfiguration config;


    @Getter private int knockoutThreshold;
    @Getter private int passiveDecayRate;
    @Getter private int healPerPlayerRate;
    @Getter private int reviveThreshold;
    @Getter private int deathThreshold;
    @Getter private double healingRange;

    @Getter private Component messagePrefix;
    @Getter private String consoleUsageMessage;
    @Getter private Component notKnockedOutMessage;
    @Getter private Component youSurrenderedMessage;
    @Getter private Component noKnockedOutNearbyMessage;
    @Getter private Component youAreKnockedOutMessage;
    @Getter private Component alreadyLiftingMessage;
    @Getter private Component playerLiftedMessage;
    @Getter private Component healingPlayerMessage;
    @Getter private Component chatPrefixKnockedOutMessage;
    @Getter private Component hologramText;
    @Getter private Component healedMessage;

    public MainConfig(SigmaKnockOut plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);

        knockoutThreshold = config.getInt("knockout-threshold", 1000);
        passiveDecayRate = config.getInt("passive-decay-rate", 1);
        healPerPlayerRate = config.getInt("heal-per-player-rate", 2);
        reviveThreshold = config.getInt("revive-threshold", 1010);
        deathThreshold = config.getInt("death-threshold", 0);
        healingRange = config.getDouble("healing-range", 3.0);

        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages != null) {
            messagePrefix = config.getRichMessage("message-prefix", Component.text("[", NamedTextColor.DARK_RED)
                    .append(Component.text("SYSTEM", NamedTextColor.RED))
                    .append(Component.text("] ", NamedTextColor.DARK_RED)));
            consoleUsageMessage = messages.getString("console-usage", "Only player can use this command!");
            notKnockedOutMessage = messages.getRichMessage("not-knocked-out", Component.text("You are not knocked out", NamedTextColor.RED));
            youSurrenderedMessage = messages.getRichMessage("you-surrendered", Component.text("You have surrendered", NamedTextColor.RED));
            noKnockedOutNearbyMessage = messages.getRichMessage("no-knocked-out-nearby", Component.text("There is no knocked out player nearby!", NamedTextColor.RED));
            youAreKnockedOutMessage = messages.getRichMessage("you-are-knocked-out", Component.text("You are knocked out and cannot lift other players!", NamedTextColor.RED));
            alreadyLiftingMessage = messages.getRichMessage("already-lifting", Component.text("You are already lifting a player!", NamedTextColor.RED));
            playerLiftedMessage = messages.getRichMessage("player-lifted", Component.text("You have lifted the player!", NamedTextColor.GREEN));
            healingPlayerMessage = messages.getRichMessage("healing-player", Component.text("You are healing!", NamedTextColor.GREEN));
            chatPrefixKnockedOutMessage = messages.getRichMessage("chat-prefix-knocked-out", Component.text("☠", NamedTextColor.RED)
                    .append(Component.text("[", NamedTextColor.GRAY))
                    .append(Component.text("Knocked Out", NamedTextColor.DARK_RED))
                    .append(Component.text("]", NamedTextColor.GRAY))
                    .append(Component.text("☠", NamedTextColor.RED)));
            hologramText = messages.getRichMessage( "holograms.knocked-out-name", Component.text("✚ Knocked Out ✚", NamedTextColor.RED));
            healedMessage = messages.getRichMessage("healed", Component.text("You have been healed", NamedTextColor.GREEN));

        }
    }
}
