package com.rafaelsms.potocraft.loginmanager.util;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
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
}
