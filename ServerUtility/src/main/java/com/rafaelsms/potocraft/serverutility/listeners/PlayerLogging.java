package com.rafaelsms.potocraft.serverutility.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler(ignoreCancelled = true)
    private void log(PlayerElytraBoostEvent event) {
        logPlayer(event.getPlayer(), "elytra boosted");
    }

    @EventHandler(ignoreCancelled = true)
    private void log(PlayerTeleportEvent event) {
        logPlayer(event.getPlayer(), "teleported by %s".formatted(event.getCause().name().toLowerCase()));
    }

    @EventHandler(ignoreCancelled = true)
    private void log(PlayerQuitEvent event) {
        logPlayer(event.getPlayer(), "quit");
    }

    @EventHandler(ignoreCancelled = true)
    private void log(PlayerPortalEvent event) {
        logPlayer(event.getPlayer(), "entered portal");
    }

    @EventHandler(ignoreCancelled = true)
    private void log(PlayerStartSpectatingEntityEvent event) {
        if (event.getNewSpectatorTarget() instanceof Player player) {
            logPlayer(event.getPlayer(), "spectating %s".formatted(player.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void log(PlayerStopSpectatingEntityEvent event) {
        if (event.getSpectatorTarget() instanceof Player player) {
            logPlayer(event.getPlayer(), "stopped spectating %s".formatted(player.getName()));
        }
    }

    private void logPlayer(@NotNull Player player, @Nullable String action) {
        if (plugin.getConfiguration().isPlayerLoggingEnabled()) {
            plugin.logger()
                  .info("%s %s at world = %s, %d %d %d".formatted(player.getName(),
                                                                  Util.getOrElse(action, ""),
                                                                  player.getWorld().getName(),
                                                                  player.getLocation().getBlockX(),
                                                                  player.getLocation().getBlockY(),
                                                                  player.getLocation().getBlockZ()));
        }
    }
}
