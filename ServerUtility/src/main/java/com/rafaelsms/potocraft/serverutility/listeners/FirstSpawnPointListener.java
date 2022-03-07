package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class FirstSpawnPointListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public FirstSpawnPointListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void changeFirstSpawnLocation(PlayerSpawnLocationEvent event) {
        if (!plugin.getConfiguration().isForceFirstSpawnLocation()) {
            return;
        }
        if (!event.getPlayer().hasPlayedBefore()) {
            event.setSpawnLocation(event.getSpawnLocation().getWorld().getSpawnLocation());
        }
    }
}
