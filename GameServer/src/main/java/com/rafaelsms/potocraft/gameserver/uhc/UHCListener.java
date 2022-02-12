package com.rafaelsms.potocraft.gameserver.uhc;

import com.rafaelsms.potocraft.gameserver.GameServerPlugin;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

public class UHCListener implements Listener {

    private final @NotNull GameServerPlugin plugin;
    private final @NotNull UHCGameMode gameMode;

    public UHCListener(@NotNull GameServerPlugin plugin, @NotNull UHCGameMode gameMode) {
        this.plugin = plugin;
        this.gameMode = gameMode;
    }

    private boolean shouldIgnoreWorld(@NotNull World world) {
        return !plugin.getConfiguration().getUhcWorldNames().contains(world.getName());
    }

    @EventHandler(ignoreCancelled = true)
    private void setSpectator(PlayerChangedWorldEvent event) {
        if (shouldIgnoreWorld(event.getPlayer().getWorld())) {
            return;
        }
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler(ignoreCancelled = true)
    private void setSpectator(PlayerRespawnEvent event) {
        if (shouldIgnoreWorld(event.getPlayer().getWorld())) {
            return;
        }
        event.setRespawnLocation(event.getPlayer().getLocation());
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler(ignoreCancelled = true)
    private void preventPortalCreation(PortalCreateEvent event) {
        if (shouldIgnoreWorld(event.getWorld())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void killOnQuit(PlayerQuitEvent event) {
        if (shouldIgnoreWorld(event.getPlayer().getWorld())) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        // TODO
    }
}
