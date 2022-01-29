package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.User;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class HardcoreListener implements Listener {

    private final @NotNull ServerProfilePlugin plugin;

    public HardcoreListener(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void preventJoin(PlayerLoginEvent event) {
        if (!plugin.getConfiguration().isHardcoreModeEnabled()) {
            return;
        }
        plugin.getUserManager().getUser(event.getPlayer()).getHardcoreExpirationDate().ifPresent(expirationDate -> {
            // Ignore if expiration date is in the past
            if (expirationDate.isBefore(ZonedDateTime.now())) {
                return;
            }

            // Kick banned player
            Component message = plugin.getConfiguration().getHardcoreBanMessage(expirationDate);
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void kickOnDeath(PlayerDeathEvent event) {
        if (!plugin.getConfiguration().isHardcoreModeEnabled()) {
            return;
        }

        // Do in the end of tick or items will duplicate
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Kick player if expiration date is in the future
            User user = plugin.getUserManager().getUser(event.getPlayer());
            user.getHardcoreExpirationDate().ifPresent(expirationDate -> {
                if (expirationDate.isAfter(ZonedDateTime.now())) {
                    event.getPlayer().kick(plugin.getConfiguration().getHardcoreBanMessage(expirationDate));
                }
            });
        });
    }
}
