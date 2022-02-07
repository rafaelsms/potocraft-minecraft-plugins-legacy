package com.rafaelsms.potocraft.loginmanager.util;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

public final class Util {

    // Private constructor
    private Util() {
    }

    public static Optional<ProxiedPlayer> getPlayer(@NotNull Connection connection) {
        if (connection instanceof ProxiedPlayer player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    public static boolean isPlayerLoggedIn(@NotNull LoginManagerPlugin plugin,
                                           @NotNull Profile profile,
                                           @NotNull ProxiedPlayer player) {
        if (player.getSocketAddress() instanceof InetSocketAddress inetSocketAddress) {
            if (player.hasPermission(Permissions.OFFLINE_AUTO_LOGIN)) {
                Duration autoLoginWindow = plugin.getConfiguration().getAutoLoginWindow();
                return profile.isLoggedIn(inetSocketAddress, autoLoginWindow);
            }
            return profile.isLoggedIn(inetSocketAddress, Duration.ZERO);
        }
        return profile.isLoggedIn(null, Duration.ZERO);
    }

    public static void sendPlayerToDefaultServer(@NotNull LoginManagerPlugin plugin, @NotNull ProxiedPlayer player) {
        ServerInfo serverInfo = plugin.getProxy().getServerInfo(plugin.getConfiguration().getDefaultServer());
        player.connect(serverInfo, ServerConnectEvent.Reason.PLUGIN);
    }
}
