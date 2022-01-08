package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ProfileUpdater {

    private final @NotNull VelocityPlugin plugin;

    public ProfileUpdater(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    private void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        // Retrieve player profile
        Optional<PlayerType> playerTypeOptional = plugin.getPlayerType(player);
        if (playerTypeOptional.isEmpty()) {
            plugin.logger().warn("Failed to check player type on proxy login for player %s (uuid = %s)"
                    .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString()));
            Component reason = plugin.getSettings().getKickMessageCouldNotCheckPlayerType();
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
            continuation.resume();
            return;
        }
        PlayerType playerType = playerTypeOptional.get();

        // Asynchronously handle database
        CompletableFuture.runAsync(() -> {
            CompletableFuture<VelocityProfile> profileFuture = plugin.getDatabase().getProfile(player.getUniqueId());
            profileFuture.whenComplete((profile, retrievalException) -> {
                if (retrievalException != null) {
                    // Exception thrown in the database
                    plugin.logger().warn("Failed to retrieve profile on proxy login for player %s (uuid = %s): %s"
                            .formatted(player.getUsername(), player.getUniqueId().toString(),
                                    retrievalException.getLocalizedMessage()));
                    Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
                    event.setResult(ResultedEvent.ComponentResult.denied(reason));
                    continuation.resume();
                } else if (profile == null) {
                    // Offline players have their profile created on /login or /register commands
                    if (playerType != PlayerType.OFFLINE_PLAYER) {
                        VelocityProfile newProfile = new VelocityProfile(plugin, player.getUniqueId(), player.getUsername());
                        plugin.getDatabase().saveProfile(newProfile).whenComplete((unused, saveException) -> {
                            if (saveException != null) {
                                plugin.logger().warn("Failed to create profile on proxy login for player %s (uuid = %s): %s"
                                        .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString(),
                                                saveException.getLocalizedMessage()));
                                Component reason = plugin.getSettings().getKickMessageCouldNotSaveProfile();
                                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                            }
                            continuation.resume();
                        });
                        return;
                    }
                    // offline players just continue
                    continuation.resume();
                } else {
                    // Offline players have their profile updated on /login or /register commands
                    if (playerType != PlayerType.OFFLINE_PLAYER) {
                        profile.updateLastPlayerName(event.getPlayer().getUsername());
                        profile.updateJoinDate();
                        plugin.getDatabase().saveProfile(profile).whenComplete((unused, saveException) -> {
                            if (saveException != null) {
                                plugin.logger().warn("Failed to save profile for player %s (uuid = %s) on proxy login: %s"
                                        .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString(),
                                                saveException.getLocalizedMessage()));
                                Component reason = plugin.getSettings().getKickMessageCouldNotSaveProfile();
                                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                            }
                            continuation.resume();
                        });
                        return;
                    }
                    // offline players just continue
                    continuation.resume();
                }
            });
        });
    }

    @Subscribe
    private void onDisconnect(DisconnectEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        if (player == null) return;
        // Asynchronously handle database
        CompletableFuture.runAsync(() -> {
            Optional<PlayerType> playerTypeOptional = plugin.getPlayerType(player);
            if (playerTypeOptional.isEmpty()) {
                plugin.logger().warn("Failed to retrieve player type on disconnect for player %s (uuid = %s)"
                        .formatted(player.getUsername(), player.getUniqueId().toString()));
                continuation.resume();
                return;
            }

            // Retrieve player type
            PlayerType playerType = playerTypeOptional.get();
            plugin.getDatabase().getProfile(player.getUniqueId()).whenComplete((profile, retrievalThrowable) -> {
                if (retrievalThrowable != null) {
                    plugin.logger().warn("Failed to retrieve profile on disconnect for player %s (uuid = %s)"
                            .formatted(player.getUsername(), player.getUniqueId().toString()));
                    continuation.resume();
                } else if (profile != null) {
                    // Offline players must be logged in to set quit date
                    if (playerType != PlayerType.OFFLINE_PLAYER ||
                                profile.isLoggedIn(event.getPlayer().getRemoteAddress())) {
                        profile.updateQuitDate();
                        plugin.getDatabase().saveProfile(profile).whenComplete((unused, saveThrowable) -> {
                            if (saveThrowable != null) {
                                plugin.logger().warn("Failed to save profile on disconnect for player %s (uuid = %s): %s"
                                        .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString(),
                                                saveThrowable.getLocalizedMessage()));
                            }
                            continuation.resume();
                        });
                        return;
                    }
                    continuation.resume();
                }
            });
        });
    }
}
