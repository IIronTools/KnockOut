package io.github.iirontools.sigmaNokaut;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.iirontools.sigmaNokaut.command.PlayerLiftingCommand;
import io.github.iirontools.sigmaNokaut.config.MainConfig;
import io.github.iirontools.sigmaNokaut.listener.PlayerDeathListener;
import io.github.iirontools.sigmaNokaut.listener.PlayerMoveListener;
import io.github.iirontools.sigmaNokaut.listener.PlayerSneakListener;
import io.github.iirontools.sigmaNokaut.manager.NokautManager;
import io.github.iirontools.sigmaNokaut.util.Szczurek;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class SigmaNokaut extends JavaPlugin {

    private MainConfig mainConfig;

    private NokautManager nokautManager;

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
        this.nokautManager = new NokautManager(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSneakListener(this), this);
        getServer().getPluginManager().registerEvents(new Szczurek(this), this); // Szczurek
    }

    private void registerCommands() {
        getCommand("podniesgracza").setExecutor(new PlayerLiftingCommand(this));
    }


    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public NokautManager getNokautManager() {
        return nokautManager;
    }
}
