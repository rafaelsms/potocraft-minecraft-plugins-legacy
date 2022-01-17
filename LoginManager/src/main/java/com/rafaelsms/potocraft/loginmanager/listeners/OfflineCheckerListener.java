package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import net.kyori.adventure.text.Component;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * We will:
 * - allow offline players to join if their name is not used by Mojang;
 * - allow online and floodgate players to join normally.
 */
public class OfflineCheckerListener {

    private final @NotNull LoginManagerPlugin plugin;

    public OfflineCheckerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    private void acceptAndVerifyOfflinePlayers(PreLoginEvent event, Continuation continuation) {
        // Ignore cancelled events already
        if (!event.getResult().isAllowed()) {
            continuation.resume();
            return;
        }

        // Validate username against configuration regex
        if (!plugin.getConfiguration().getAllowedUsernamesRegex().matcher(event.getUsername()).matches()) {
            Component reason = plugin.getConfiguration().getKickMessageInvalidUsername();
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
            continuation.resume();
            return;
        }

        // Check if is floodgate player
        for (FloodgatePlayer player : FloodgateApi.getInstance().getPlayers()) {
            if (player.isLinked() && player.getLinkedPlayer().getJavaUsername().equalsIgnoreCase(event.getUsername())) {
                continuation.resume();
                return;
            }
            if (player.getJavaUsername().equalsIgnoreCase(event.getUsername())) {
                continuation.resume();
                return;
            }
        }
        if (FloodgateApi.getInstance().getPlayerPrefix().length() > 0 &&
            event.getUsername().startsWith(FloodgateApi.getInstance().getPlayerPrefix())) {
            Component reason = plugin.getConfiguration().getKickMessageInvalidPrefixForJavaPlayer();
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
            continuation.resume();
            return;
        }

        // Check if player has existing account
        CompletableFuture.runAsync(() -> {
            try {
                if (isMojangUsernameExistent(event.getUsername())) {
                    // If Mojang player exists, force online mode so the account is secured against offline players
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                    continuation.resume();
                } else {
                    // If it doesn't exist, force offline mode, so we can require authentication
                    event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
                    continuation.resume();
                }
            } catch (IOException e) {
                // If we failed to request, disconnect player
                plugin.getLogger().warn("Failed to access Mojang's username API");
                Component reason = plugin.getConfiguration().getKickMessageCouldNotCheckMojangUsername();
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(reason));
                continuation.resume();
            }
        }, Util.getExecutor(plugin, continuation));
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
