package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

/**
 * We will: - allow offline players to join if their name is not used by Mojang; - allow online and floodgate players to
 * join normally.
 */
public class OfflineCheckerListener {

    private final @NotNull LoginManagerPlugin plugin;

    public OfflineCheckerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    private void denyJavaWithBedrockPrefix(LoginEvent event, Continuation continuation) {
        // Ignore cancelled events already
        if (!event.getResult().isAllowed()) {
            continuation.resume();
            return;
        }

        // If is a floodgate player, allow without checking
        FloodgateApi floodgateApi = FloodgateApi.getInstance();
        FloodgatePlayer floodgatePlayer = floodgateApi.getPlayer(event.getPlayer().getUniqueId());
        if (floodgatePlayer != null) {
            continuation.resume();
            return;
        }

        // If is not a floodgate player and is using Floodgate's prefix, deny login
        if (floodgateApi.getPlayerPrefix().length() > 0 &&
            event.getPlayer().getUsername().startsWith(floodgateApi.getPlayerPrefix())) {
            Component reason = plugin.getConfiguration().getKickMessageInvalidPrefixForJavaPlayer();
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
            continuation.resume();
            return;
        }

        // Validate username against configuration regex for Java players
        if (!plugin.getConfiguration()
                   .getAllowedJavaUsernamesRegex()
                   .matcher(event.getPlayer().getUsername())
                   .matches()) {
            Component reason = plugin.getConfiguration().getKickMessageInvalidJavaUsername();
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
        }
        continuation.resume();
    }

    @Subscribe(order = PostOrder.LATE) // Ensure we are after Floodgate's EARLY
    private void acceptAndVerifyOfflinePlayers(PreLoginEvent event, Continuation continuation) {
        // Ignore cancelled events already
        if (!event.getResult().isAllowed()) {
            continuation.resume();
            return;
        }

        try {
            // FloodgateAPI#getPlayers was throwing under unknown conditions
            // If this is not caught, players will default to online mode and offline players can't join
            for (FloodgatePlayer player : FloodgateApi.getInstance().getPlayers()) {
                // If player has the same name as Floodgate's player, allow join without changing the event
                if (player.getCorrectUsername().equalsIgnoreCase(event.getUsername())) {
                    continuation.resume();
                    return;
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().warn("Failed to check if player is Floodgate player: {}", exception.getMessage());
            exception.printStackTrace();
        }

        // Check if player has existing account
        try {
            if (isMojangUsernameExistent(event.getUsername())) {
                // If Mojang player exists, force online mode so the account is secured against offline players
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            } else {
                // If it doesn't exist, force offline mode, so we can require authentication
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            }
        } catch (IOException exception) {
            // If we failed to request, disconnect player
            plugin.getLogger().warn("Failed to access Mojang's username API: {}", exception.getMessage());
            Component reason = plugin.getConfiguration().getKickMessageCouldNotCheckMojangUsername();
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
            exception.printStackTrace();
        }
        continuation.resume();
    }

    private boolean isMojangUsernameExistent(@NotNull String name) throws IOException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/%s".formatted(name));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        return connection.getResponseCode() == 200;
    }
}
