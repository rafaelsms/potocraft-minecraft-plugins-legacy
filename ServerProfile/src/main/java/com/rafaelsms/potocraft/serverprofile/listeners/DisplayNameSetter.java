package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

public class DisplayNameSetter implements Listener {

    private final @NotNull ServerProfilePlugin plugin;

    public DisplayNameSetter(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void createTabNameUpdateTask(ServerLoadEvent event) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, new UpdateTabNameTask(), 20 * 30, 20 * 30);
    }

    @EventHandler(ignoreCancelled = true)
    private void setTabName(PlayerJoinEvent event) {
        setTabName(event.getPlayer());
    }

    private void setTabName(@NotNull Player player) {
        player.playerListName(plugin.getConfiguration().getServerTabName(player));
    }

    private class UpdateTabNameTask implements Runnable {

        @Override
        public void run() {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                setTabName(player);
            }
        }
    }
}
