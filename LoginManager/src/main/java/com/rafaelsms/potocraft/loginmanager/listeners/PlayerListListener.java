package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PlayerListListener {

    private final @NotNull LoginManagerPlugin plugin;

    public PlayerListListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void setPlayerList(ServerConnectedEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            // Update everyone's tab list
            createTabList(event.getPlayer(), event.getServer());
            updateTabList(event.getPlayer(), event.getServer());
            // Continue with the event
            continuation.resume();
        });
    }

    @Subscribe
    private void setPlayerList(DisconnectEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            // Update everyone's tab list
            updateTabList(event.getPlayer(), null);
            // Continue with the event
            continuation.resume();
        });
    }

    private void createTabList(@NotNull Player player, @NotNull RegisteredServer server) {
        // For each server, we will update every player's tab list with other servers
        for (RegisteredServer otherServer : plugin.getServer().getAllServers()) {
            boolean sameServer =
                    otherServer.getServerInfo().getName().equalsIgnoreCase(server.getServerInfo().getName());
            for (Player otherPlayer : otherServer.getPlayersConnected()) {
                setPlayerEntry(player.getTabList(), otherPlayer, otherServer, sameServer);
            }
        }
    }

    private void setPlayerEntry(TabList tabList, Player otherPlayer, RegisteredServer otherServer, boolean sameServer) {
        if (tabList.containsEntry(otherPlayer.getUniqueId())) {
            return;
        }
        Component displayName;
        if (sameServer) {
            displayName = plugin.getConfiguration().getTabSameServerDisplayName(otherPlayer);
        } else {
            displayName = plugin
                    .getConfiguration()
                    .getTabOtherServerDisplayName(otherPlayer, otherServer.getServerInfo().getName());
        }
        tabList.addEntry(TabListEntry
                                 .builder()
                                 .profile(otherPlayer.getGameProfile())
                                 .latency((int) Math.min(otherPlayer.getPing(), Integer.MAX_VALUE))
                                 .displayName(displayName)
                                 .build());
    }

    private void updateTabList(@NotNull Player updatingPlayer, @Nullable RegisteredServer server) {
        for (RegisteredServer otherServer : plugin.getServer().getAllServers()) {
            boolean sameServer =
                    server != null && server.getServerInfo().getName().equals(otherServer.getServerInfo().getName());
            for (Player otherPlayer : otherServer.getPlayersConnected()) {
                // If updating player is leaving, just remove it
                TabList otherTabList = otherPlayer.getTabList();
                if (otherTabList.containsEntry(updatingPlayer.getUniqueId())) {
                    otherTabList.removeEntry(updatingPlayer.getUniqueId());
                }
                // Otherwise, insert it with the new server value
                if (server != null) {
                    setPlayerEntry(otherTabList, updatingPlayer, server, sameServer);
                }
            }
        }
    }
}
