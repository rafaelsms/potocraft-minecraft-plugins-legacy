package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class HideMessagesListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public HideMessagesListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void hideJoinMessage(PlayerJoinEvent event) {
        if (plugin.getConfiguration().isHideJoinQuitMessages()) {
            event.joinMessage(Component.empty());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void hideQuitMessage(PlayerQuitEvent event) {
        if (plugin.getConfiguration().isHideJoinQuitMessages()) {
            event.quitMessage(Component.empty());
        }
    }
}
