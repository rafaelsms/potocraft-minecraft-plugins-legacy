package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.util.PlayerType;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ProfileUpdater {

    private final VelocityPlugin plugin;

    public ProfileUpdater(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    private void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        CompletableFuture.runAsync(() -> plugin.getDatabase().retrieveProfile(player.getUniqueId(), (profile) -> {
            // There is a profile, so we update login time and save it
            plugin.retrievePlayerType(player.getUniqueId(), playerType -> {
                // Offline players have their login date adjusted on /login
                if (playerType != PlayerType.OFFLINE_PLAYER) {
                    profile.updateJoinDate();
                    plugin.getDatabase().saveProfile(profile, continuation::resume, (exception) -> {
                        plugin.logger().warn("Failed to save profile for player %s (uuid = %s)"
                                .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString()));
                        Component reason = plugin.getSettings().getKickMessageCouldNotSaveProfile();
                        event.setResult(ResultedEvent.ComponentResult.denied(reason));
                        continuation.resume();
                    });
                }
            }, () -> {
                Component reason = plugin.getSettings().getKickMessageCouldNotCheckPlayerType();
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                continuation.resume();
            }, exception -> {
                Component reason = plugin.getSettings().getKickMessageCouldNotCheckPlayerType();
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
            });
        }, () -> {
            // There is no profile, so we create one
            VelocityUser profile = new VelocityUser(plugin, player.getUniqueId());
            plugin.getDatabase().saveProfile(profile, continuation::resume, (exception) -> {
                plugin.logger().warn("Failed to create profile on login for player %s (uuid = %s)"
                        .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString()));
                Component reason = plugin.getSettings().getKickMessageCouldNotSaveProfile();
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                continuation.resume();
            });
        }, (exception) -> {
            // Exception thrown in the database
            plugin.logger().warn("Failed to retrieve profile on login for player %s (uuid = %s): %s"
                    .formatted(player.getUsername(), player.getUniqueId().toString(), exception.getLocalizedMessage()));
            Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
            continuation.resume();
        }));
    }

    @Subscribe
    private void onDisconnect(DisconnectEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        if (player == null) return;
        CompletableFuture.runAsync(() -> plugin.getDatabase().retrieveProfile(player.getUniqueId(), (profile) -> {
            // Update quit time
            plugin.retrievePlayerType(player.getUniqueId(), playerType -> {
                // Offline players must be logged in to set quit date
                if (playerType != PlayerType.OFFLINE_PLAYER ||
                            profile.isLoggedIn(event.getPlayer().getRemoteAddress())) {
                    profile.updateQuitDate();
                    plugin.getDatabase().saveProfile(profile, continuation::resume, (exception) -> {
                        plugin.logger().warn("Failed to save profile on disconnect for player %s (uuid = %s)"
                                .formatted(event.getPlayer().getUsername(), event.getPlayer().getUniqueId().toString()));
                        continuation.resume();
                    });
                }
            }, () -> {
                plugin.logger().warn("Failed to retrieve player type on disconnect for player %s (uuid = %s)"
                        .formatted(player.getUsername(), player.getUniqueId().toString()));
                continuation.resume();
            }, exception -> {
                plugin.logger().warn("Failed to retrieve player type on disconnect for player %s (uuid = %s)"
                        .formatted(player.getUsername(), player.getUniqueId().toString()));
                continuation.resume();
            });
        }, continuation::resume, (exception -> {
            plugin.logger().warn("Failed to retrieve profile on disconnect for player %s (uuid = %s)"
                    .formatted(player.getUsername(), player.getUniqueId().toString()));
            continuation.resume();
        })));
    }
}
