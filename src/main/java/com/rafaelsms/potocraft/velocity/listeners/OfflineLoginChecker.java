package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.util.PlayerType;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record OfflineLoginChecker(@NotNull VelocityPlugin plugin) {

    @Subscribe
    private void onPlayerPreLogin(PreLoginEvent event, Continuation continuation) {
        // Ignore cancelled events already
        if (!event.getResult().isAllowed()) {
            plugin.debug("player %s was already denied".formatted(event.getUsername()));
            continuation.resume();
            return;
        }

        // Check if is floodgate player
        if (FloodgateApi.getInstance() != null) {
            for (FloodgatePlayer player : FloodgateApi.getInstance().getPlayers()) {
                if (player.getJavaUsername().equalsIgnoreCase(event.getUsername())) {
                    plugin.debug("player %s is floodgate, allowed".formatted(event.getUsername()));
                    continuation.resume();
                    return;
                }
            }
            if (FloodgateApi.getInstance().getPlayerPrefix().length() > 0 &&
                        event.getUsername().startsWith(FloodgateApi.getInstance().getPlayerPrefix())) {
                plugin.debug("player %s is not floodgate but using prefix, denied".formatted(event.getUsername()));
                Component reason = plugin.getSettings().getKickMessageInvalidPrefixForJavaPlayer();
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
                continuation.resume();
                return;
            }
        } else {
            plugin.logger().warn("Failed to access Floodgate API: disconnecting player %s"
                    .formatted(event.getUsername()));
            Component reason = plugin.getSettings().getKickMessageCouldNotCheckPlayerType();
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
            continuation.resume();
            return;
        }

        // Check if player has existing account
        CompletableFuture.runAsync(() -> {
            try {
                if (mojangUsernameExists(event.getUsername())) {
                    // If Mojang player exists, force online mode so the account is secured against offline players
                    plugin.debug("player name %s is mojang's, forcing online mode".formatted(event.getUsername()));
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                    continuation.resume();
                } else {
                    // If it doesn't exist, force offline mode, so we can require authentication
                    plugin.debug("player name %s isn't mojang's, forcing offline mode".formatted(event.getUsername()));
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
                    continuation.resume();
                }
            } catch (IOException e) {
                // If we failed to request, disconnect player
                plugin.logger().warn("Failed to access Mojang's username API");
                Component reason = plugin.getSettings().getKickMessageCouldNotCheckMojangUsername();
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
                continuation.resume();
            }
        });
    }

    // only denying the connection will make the player go to a limbo and timing out when first joining
    @Subscribe
    private void onPreConnect(ServerPreConnectEvent event, Continuation continuation) {
        Player player = event.getPlayer();

        // Get player type
        Optional<PlayerType> playerTypeOptional = plugin.getPlayerType(player);
        if (playerTypeOptional.isEmpty()) {
            plugin.logger().warn("Couldn't check player connection type for player %s (uuid = %s)"
                    .formatted(player.getUsername(), player.getUniqueId().toString()));
            // Couldn't retrieve player type, just cancel
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType());
            continuation.resume();
            return;
        }
        PlayerType playerType = playerTypeOptional.get();
        plugin.logger().info("Player %s (uuid = %s) connection type is %s"
                .formatted(player.getUsername(), player.getUniqueId(), playerType.toString()));

        // Ignore online/floodgate players
        if (playerType != PlayerType.OFFLINE_PLAYER) {
            plugin.debug("player %s passed through (online/floodgate)".formatted(player.getUsername()));
            continuation.resume();
            return;
        }

        // If it is an offline player, handle
        CompletableFuture.runAsync(() -> plugin.getDatabase().getProfile(player.getUniqueId()).whenComplete((profile, retrievalThrowable) -> {
            // Get login server
            String loginServerName = plugin.getSettings().getLoginServer();
            Optional<RegisteredServer> loginServerOptional = plugin.getProxyServer().getServer(loginServerName);
            Runnable moveToLoginServer = () -> {
                if (loginServerOptional.isEmpty()) {
                    // No login server, cancel join
                    plugin.debug("player %s: disconnected: no login server".formatted(player.getUsername()));
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    player.disconnect(plugin.getSettings().getKickMessageNoLoginServer());
                    continuation.resume();
                } else {
                    plugin.debug("player %s: redirected to login server".formatted(player.getUsername()));
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(loginServerOptional.get()));
                    continuation.resume();
                }
            };

            if (retrievalThrowable != null) {
                plugin.logger().warn("Couldn't retrieve profile before server connect for offline player %s (uuid = %s): %s"
                        .formatted(player.getUsername(), player.getUniqueId().toString(), retrievalThrowable.getLocalizedMessage()));
                // Couldn't retrieve profile, just cancel
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
                continuation.resume();
            } else if (profile == null) {
                // If player profile is null, redirect to login server
                plugin.debug("player %s is offline and does not have a profile".formatted(player.getUsername()));
                moveToLoginServer.run();
            } else {
                // If player is not authenticated, redirect to login server
                if (!profile.isLoggedIn(event.getPlayer().getRemoteAddress())) {
                    plugin.debug("player %s is offline and not authenticated".formatted(player.getUsername()));
                    moveToLoginServer.run();
                }
                // Otherwise, allow connection player as it is authenticated
                plugin.debug("player %s is offline but authenticated".formatted(player.getUsername()));
                continuation.resume();
            }
        }));
    }

    private boolean mojangUsernameExists(@NotNull String name) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/%s".formatted(name));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection.getResponseCode() == 200;
    }

}
