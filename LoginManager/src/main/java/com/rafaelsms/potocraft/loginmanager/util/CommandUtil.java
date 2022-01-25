package com.rafaelsms.potocraft.loginmanager.util;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class CommandUtil {

    // Private constructor
    private CommandUtil() {
    }

    public static Optional<Profile> handlePlayerSearch(@NotNull LoginManagerPlugin plugin,
                                                       @NotNull CommandSource source,
                                                       @NotNull String usernameSearch) {
        Optional<Profile> optionalProfile;
        try {
            optionalProfile = plugin.getDatabase().searchOfflineProfile(usernameSearch);
        } catch (Database.DatabaseException ignored) {
            source.sendMessage(plugin.getConfiguration().getCommandFailedToSearchProfile());
            return Optional.empty();
        }
        if (optionalProfile.isEmpty()) {
            source.sendMessage(plugin.getConfiguration().getCommandNoProfileFound());
        }
        return optionalProfile;
    }
}
