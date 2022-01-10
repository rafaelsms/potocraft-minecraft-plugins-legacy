package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.papermc.user.PaperUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class UserListener implements Listener {

    private final @NotNull PaperUserManager userManager;

    public UserListener(@NotNull PaperUserManager userManager) {
        this.userManager = userManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event) {
        userManager.userJoinedListener(event.getPlayer().getUniqueId(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        userManager.userQuitListener(event.getPlayer().getUniqueId());
    }
}
