package com.rafaelsms.potocraft.loginmanager.util;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public final class Util {

    // Private constructor
    private Util() {
    }

    public static boolean isPlayerLoggedIn(@NotNull LoginManagerPlugin plugin,
                                           @NotNull Profile profile,
                                           @NotNull Player player) {
        if (player.hasPermission(Permissions.OFFLINE_AUTO_LOGIN)) {
            Duration autoLoginWindow = plugin.getConfiguration().getAutoLoginWindow();
            return profile.isLoggedIn(player.getRemoteAddress(), autoLoginWindow);
        }
        return profile.isLoggedIn(player.getRemoteAddress(), Duration.ZERO);
    }

    public static @NotNull Optional<Profile> handleUniqueProfile(@NotNull LoginManagerPlugin plugin,
                                                                 @NotNull CommandSource source,
                                                                 @NotNull List<Profile> profiles) {
        if (profiles.isEmpty()) {
            source.sendMessage(plugin.getConfiguration().getCommandNoProfileFound());
            return Optional.empty();
        } else if (profiles.size() > 1) {
            source.sendMessage(plugin.getConfiguration().getCommandMultipleProfilesFound(profiles));
            return Optional.empty();
        } else {
            return Optional.of(profiles.get(0));
        }
    }

    public static AsyncEventExecutor getExecutor(@NotNull LoginManagerPlugin plugin, @Nullable Continuation continuation) {
        return new AsyncEventExecutor(plugin, continuation);
    }

    public static void sendPlayerToDefault(@NotNull LoginManagerPlugin plugin, @NotNull Player player) {
        for (String serverName : plugin.getServer().getConfiguration().getAttemptConnectionOrder()) {
            Optional<RegisteredServer> serverOptional = plugin.getServer().getServer(serverName);
            if (serverOptional.isPresent()) {
                player.createConnectionRequest(serverOptional.get()).fireAndForget();
                return;
            }
        }
        player.sendMessage(plugin.getConfiguration().getCommandLoginNoServerAvailable());
    }

    public static class AsyncEventExecutor implements Executor {

        private final @NotNull LoginManagerPlugin plugin;
        private final @Nullable Continuation continuation;

        private AsyncEventExecutor(@NotNull LoginManagerPlugin plugin, @Nullable Continuation continuation) {
            this.plugin = plugin;
            this.continuation = continuation;
        }

        @Override
        public void execute(@NotNull Runnable command) {
            try {
                command.run();
            } catch (Exception exception) {
                plugin.getLogger().error("Caught exception on async task:", exception);
                if (continuation != null) {
                    continuation.resumeWithException(exception);
                }
            }
        }
    }
}
