package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;

public class HideMessagesListener implements Listener {

    private final HashMap<UUID, ZonedDateTime> playerLastDeathDate = new HashMap<>();

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

    @EventHandler(ignoreCancelled = true)
    private void hideDeathMessage(PlayerDeathEvent event) {
        Duration delayMessages = plugin.getConfiguration().getDelayBetweenDeathMessages();
        ZonedDateTime lastDeathDate = playerLastDeathDate.get(event.getPlayer().getUniqueId());
        ZonedDateTime now = ZonedDateTime.now();
        if (lastDeathDate != null && lastDeathDate.minus(delayMessages).isAfter(now)) {
            event.deathMessage(null);
        }
        playerLastDeathDate.put(event.getPlayer().getUniqueId(), now);
        plugin.getServer()
              .getScheduler()
              .runTaskLater(plugin,
                            () -> playerLastDeathDate.remove(event.getPlayer().getUniqueId()),
                            delayMessages.toSeconds() * 20L);
    }
}
