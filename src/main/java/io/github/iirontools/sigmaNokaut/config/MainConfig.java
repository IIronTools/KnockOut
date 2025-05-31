package io.github.iirontools.sigmaNokaut.config;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MainConfig {

    private final SigmaNokaut plugin;
    private final File file;
    private YamlConfiguration config;


    @Getter private int knockoutThreshold;
    @Getter private int passiveDecayRate;
    @Getter private int healPerPlayerRate;
    @Getter private int reviveThreshold;
    @Getter private int deathThreshold;
    @Getter private double healingRange;
    @Getter private Component messagePrefix;

    public MainConfig(SigmaNokaut plugin) {
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
        messagePrefix = config.getRichMessage("message-prefix");
    }
}
