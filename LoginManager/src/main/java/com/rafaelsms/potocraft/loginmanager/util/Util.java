package com.rafaelsms.potocraft.loginmanager.util;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

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
}
