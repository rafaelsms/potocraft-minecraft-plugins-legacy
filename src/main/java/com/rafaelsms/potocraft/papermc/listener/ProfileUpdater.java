package com.rafaelsms.potocraft.papermc.listener;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfileUpdater implements Listener {

    private final PaperPlugin plugin;

    public ProfileUpdater(PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        plugin.getDatabase().retrieveProfile(event.getPlayer(), paperUser -> {
            paperUser.setQuitLocation();
            plugin.getDatabase().saveProfile(paperUser, () -> {

            }, exception -> {

            });
        }, () -> {

        }, exception -> {

        });
    }

}
