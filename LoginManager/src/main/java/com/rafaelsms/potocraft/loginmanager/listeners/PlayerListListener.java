package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListListener {

    private final @NotNull LoginManagerPlugin plugin;

    public PlayerListListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            for (Player player : plugin.getServer().getAllPlayers()) {
                updatePlayerTabList(player);
            }
        }).repeat(500, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    private void updatePlayerList(ServerConnectedEvent event) {
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            initPlayerList(event.getPlayer(), event.getServer().getServerInfo().getName());
            updateChangingPlayerList(event.getPlayer(), event.getServer().getServerInfo().getName());
        }).delay(100, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe
    private void updatePlayerList(DisconnectEvent event) {
        updateChangingPlayerList(event.getPlayer(), null);
    }

    private void updateChangingPlayerList(@NotNull Player player, @Nullable String playerServer) {
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

    private void updatePlayerTabList(Player player) {
        TabList tabList = player.getTabList();
        for (TabListEntry listEntry : tabList.getEntries()) {
            // Update everyone's display name and latency
            Optional<Player> optionalPlayer = plugin.getServer().getPlayer(listEntry.getProfile().getId());
            if (optionalPlayer.isPresent()) {
                Component displayName = plugin
                        .getConfiguration()
                        .getTabDisplayName(optionalPlayer.get(), getServerName(optionalPlayer.get()));
                listEntry.setDisplayName(displayName);
                listEntry.setLatency((int) Math.min(player.getPing(), Integer.MAX_VALUE));
            }
        }
    }

    private void initPlayerList(@NotNull Player player, @NotNull String playerServer) {
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
        TabListEntry entry = getNewEntry(tabList, player, playerServer);
        // We will always update the only changing player
        tabList.removeEntry(player.getUniqueId());
        tabList.addEntry(entry);
    }

    private TabListEntry getNewEntry(@NotNull TabList tabList, @NotNull Player player, @NotNull String playerServer) {
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

    private String getServerName(Player player) {
        return player.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("limbo?");
    }
}
