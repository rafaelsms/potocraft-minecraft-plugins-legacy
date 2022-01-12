package com.rafaelsms.potocraft.papermc.listeners;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.database.ServerProfile;
import com.rafaelsms.potocraft.papermc.user.PaperUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserListener implements Listener {

    private final @NotNull PaperPlugin plugin;
    private final @NotNull PaperUserManager userManager;

    public UserListener(@NotNull PaperPlugin plugin, @NotNull PaperUserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
        try {
            UUID playerId = event.getUniqueId();
            ServerProfile serverProfile =
                    plugin.getDatabase().getServerProfile(playerId).orElse(ServerProfile.create(playerId));
            userManager.insertServerProfile(serverProfile);
        } catch (Exception ignored) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                           plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
        }
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
