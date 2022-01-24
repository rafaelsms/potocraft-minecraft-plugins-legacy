package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListListener {

    private final @NotNull LoginManagerPlugin plugin;

    public PlayerListListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            for (RegisteredServer server : plugin.getServer().getAllServers()) {
                for (Player player : server.getPlayersConnected()) {
                    setPlayerTabList(player, server.getServerInfo().getName());
                }
            }
        }).repeat(500, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    private void updatePlayerList(ServerConnectedEvent event) {
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            setPlayerTabList(event.getPlayer(), event.getServer().getServerInfo().getName());
            updatePlayersTabList(event.getPlayer(), event.getServer().getServerInfo().getName());
        }).delay(100, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    private void updatePlayerList(DisconnectEvent event) {
        updatePlayersTabList(event.getPlayer(), null);
    }

    private void updatePlayersTabList(@NotNull Player player, @Nullable String playerServer) {
        // Update other's tab list
        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            String serverName = server.getServerInfo().getName();
            if (playerServer != null && playerServer.equalsIgnoreCase(serverName)) {
                continue;
            }
            for (Player otherPlayer : server.getPlayersConnected()) {
                if (playerServer != null) {
                    addEntryToList(otherPlayer.getTabList(), player, playerServer);
                } else {
                    removeEntry(otherPlayer.getTabList(), player.getUniqueId());
                }
            }
        }
    }

    private void setPlayerTabList(@NotNull Player player, @NotNull String playerServer) {
        TabList tabList = player.getTabList();
        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            String serverName = server.getServerInfo().getName();
            if (playerServer.equalsIgnoreCase(serverName)) {
                continue;
            }
            for (Player otherPlayer : server.getPlayersConnected()) {
                addEntryToList(tabList, otherPlayer, serverName);
            }
        }
    }

    private void addEntryToList(@NotNull TabList tabList, @NotNull Player player, @NotNull String playerServer) {
        TabListEntry entry = getEntry(tabList, player, playerServer);
        // We will always update the only changing player
        tabList.removeEntry(player.getUniqueId());
        tabList.addEntry(entry);
    }

    private TabListEntry getEntry(@NotNull TabList tabList, @NotNull Player player, @NotNull String playerServer) {
        return TabListEntry
                .builder()
                .tabList(tabList)
                .latency((int) Math.min(player.getPing(), Integer.MAX_VALUE))
                .profile(player.getGameProfile())
                .displayName(plugin.getConfiguration().getTabDisplayName(player, playerServer))
                .build();
    }

    private void removeEntry(@NotNull TabList tabList, @NotNull UUID playerId) {
        tabList.removeEntry(playerId);
    }
}
