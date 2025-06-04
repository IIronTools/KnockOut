package io.github.iirontools.sigmaNokaut;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.iirontools.sigmaNokaut.command.GiveUpCommand;
import io.github.iirontools.sigmaNokaut.command.PlayerLiftingCommand;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import io.github.iirontools.sigmaNokaut.listener.*;
import io.github.iirontools.sigmaNokaut.manager.KnockOutManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class SigmaKnockOut extends JavaPlugin {

    @Getter private MainConfig mainConfig;
    @Getter private KnockOutManager knockOutManager;

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
        getCommand("podniesgracza").setExecutor(new PlayerLiftingCommand(this));
        getCommand("poddajsie").setExecutor(new GiveUpCommand(this));
    }
}
