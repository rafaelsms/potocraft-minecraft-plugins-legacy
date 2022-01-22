package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * We will:
 * - check if offline profiles are still logged in;
 * - update offline profiles' login date if needed;
 * - update offline profiles' logged in status if needed;
 * - validate profiles' logged in status on startup;
 * - update join/quit date for online players.
 */
public class ProfileUpdater {

    private final @NotNull LoginManagerPlugin plugin;

    public ProfileUpdater(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    private void updateJoinDate(PostLoginEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            Player player = event.getPlayer();
            boolean playerTypeRequiresLogin = PlayerType.get(player).requiresLogin();
            Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
            // Don't create profile for offline players, just return
            if (optionalProfile.isEmpty() && playerTypeRequiresLogin) {
                continuation.resume();
                return;
            }
            // The new profile will only be created for online players
            Profile profile = optionalProfile.orElse(new Profile(player.getUniqueId(), player.getUsername()));

            // Check if profile is logged in and set its join date
            if (!playerTypeRequiresLogin || Util.isPlayerLoggedIn(plugin, profile, player)) {
                profile.setJoinDate(player.getUsername());
                plugin.getDatabase().saveProfileCatching(profile);
            }
            continuation.resume();
        }, Util.getExecutor(plugin, continuation));
    }

    @Subscribe(order = PostOrder.LAST)
    private void updateQuitDate(DisconnectEvent event, Continuation continuation) {
        CompletableFuture.runAsync(() -> {
            Player player = event.getPlayer();
            Optional<Profile> optionalProfile = plugin.getDatabase().getProfileCatching(player.getUniqueId());
            if (optionalProfile.isEmpty()) {
                continuation.resume();
                return;
            }
            Profile profile = optionalProfile.get();

            // Check if profile is logged in and set its join date
            if (isLoggedIn(event, profile, player)) {
                String serverName = event
                        .getPlayer()
                        .getCurrentServer()
                        .map(ServerConnection::getServerInfo)
                        .map(ServerInfo::getName)
                        .orElse(null);
                profile.setQuitDate(serverName);
                plugin.getDatabase().saveProfileCatching(profile);
            }
            continuation.resume();
        }, Util.getExecutor(plugin, continuation));
    }

    private boolean isLoggedIn(@NotNull DisconnectEvent event, @NotNull Profile profile, @NotNull Player player) {
        String playerPrefix = FloodgateApi.getInstance().getPlayerPrefix();
        // If player logged in successfully and have Floodgate's prefix, it was surely a Floodgate player
        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN &&
            !playerPrefix.isBlank() &&
            player.getUsername().startsWith(playerPrefix)) {
            return true;
        }
        // Otherwise, check if player is online mode or logged in
        return player.isOnlineMode() || Util.isPlayerLoggedIn(plugin, profile, player);
    }
}
