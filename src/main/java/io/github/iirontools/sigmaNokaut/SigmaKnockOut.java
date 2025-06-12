package io.github.iirontools.sigmaNokaut;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import io.github.iirontools.sigmaNokaut.command.GiveUpCommand;
import io.github.iirontools.sigmaNokaut.command.PlayerLiftingCommand;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import io.github.iirontools.sigmaNokaut.listener.*;
import io.github.iirontools.sigmaNokaut.manager.KnockOutManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SigmaKnockOut extends JavaPlugin {

    private MainConfig mainConfig;
    private KnockOutManager knockOutManager;
    private ICombatLogX combatLogX;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        loadConfigs();
        registerManagers();
        registerListeners();
        registerCommands();
        registerAPI();

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    private void loadConfigs() {
        this.mainConfig = new MainConfig(this);
    }

    private void registerManagers() {
        this.knockOutManager = new KnockOutManager(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSneakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(mainConfig, knockOutManager), this);
    }

    private void registerCommands() {
        getCommand("liftplayer").setExecutor(new PlayerLiftingCommand(this));
        getCommand("surrender").setExecutor(new GiveUpCommand(this));
    }

    private void registerAPI() {
        Plugin combatLogXPlugin = getServer().getPluginManager().getPlugin("CombatLogX");
        if (combatLogXPlugin instanceof ICombatLogX) {
            this.combatLogX = (ICombatLogX) combatLogXPlugin;
        } else {
            this.combatLogX = null;
        }
    }
}
