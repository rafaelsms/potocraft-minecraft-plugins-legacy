package com.rafaelsms.potocraft.serverutility.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLogging implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public PlayerLogging(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerElytraBoostEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "elytra boosted");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerTeleportEvent event) {
        logPlayer(event.getPlayer(),
                  event.getTo(),
                  "teleported by %s".formatted(event.getCause().name().toLowerCase()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerQuitEvent event) {
        logPlayer(event.getPlayer(), event.getPlayer().getLocation(), "quit");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerPortalEvent event) {
        logPlayer(event.getPlayer(), event.getTo(), "entered portal");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerStartSpectatingEntityEvent event) {
        if (event.getNewSpectatorTarget() instanceof Player player) {
            logPlayer(event.getPlayer(), player.getLocation(), "spectating %s".formatted(player.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void log(PlayerStopSpectatingEntityEvent event) {
        if (event.getSpectatorTarget() instanceof Player player) {
            logPlayer(event.getPlayer(),
                      event.getPlayer().getLocation(),
                      "stopped spectating %s".formatted(player.getName()));
        }
    }

    private void logPlayer(@NotNull Player player, @NotNull Location location, @Nullable String action) {
        if (plugin.getConfiguration().isPlayerLoggingEnabled()) {
            plugin.logger()
                  .info("%s %s at world = %s, %d %d %d".formatted(player.getName(),
                                                                  Util.getOrElse(action, ""),
                                                                  player.getWorld().getName(),
                                                                  location.getBlockX(),
                                                                  location.getBlockY(),
                                                                  location.getBlockZ()));
        }
    }
}
