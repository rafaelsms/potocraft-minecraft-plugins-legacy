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
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

public final class LoginUtil {

    private static final Pattern PASSWORD_REGEX = Pattern.compile("^\\S{4,32}$");

    // Private constructor
    private LoginUtil() {
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
        Optional<InetSocketAddress> inetSocketAddress = getInetAddress(player.getSocketAddress());
        if (inetSocketAddress.isPresent() && player.hasPermission(Permissions.OFFLINE_AUTO_LOGIN)) {
            Duration autoLoginWindow = plugin.getConfiguration().getAutoLoginWindow();
            return profile.isLoggedIn(inetSocketAddress.get(), autoLoginWindow);
        }
        return profile.isLoggedIn(inetSocketAddress.orElse(null), Duration.ZERO);
    }

    public static void sendPlayerToDefaultServer(@NotNull LoginManagerPlugin plugin, @NotNull ProxiedPlayer player) {
        ServerInfo serverInfo = plugin.getProxy().getServerInfo(plugin.getConfiguration().getDefaultServer());
        if (serverInfo == null) {
            player.sendMessage(plugin.getConfiguration().getCommandNoServerAvailable());
            return;
        }
        player.connect(serverInfo, ServerConnectEvent.Reason.PLUGIN);
    }

    public static @NotNull Optional<InetSocketAddress> getInetAddress(@NotNull SocketAddress address) {
        if (address instanceof InetSocketAddress inetAddress) {
            return Optional.of(inetAddress);
        }
        return Optional.empty();
    }

    public static boolean isValidPassword(@NotNull String password) {
        return PASSWORD_REGEX.matcher(password).matches();
    }
}
