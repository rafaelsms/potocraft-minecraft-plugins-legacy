package com.rafaelsms.potocraft.loginmanager.util;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class CommandUtil {

    // Private constructor
    private CommandUtil() {
    }

    public static Optional<Profile> handlePlayerSearch(@NotNull LoginManagerPlugin plugin,
                                                       @NotNull CommandSender sender,
                                                       @NotNull String usernameSearch) {
        Optional<Profile> optionalProfile;
        try {
            optionalProfile = plugin.getDatabase().searchOfflineProfile(usernameSearch);
        } catch (DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getCommandFailedToSearchProfile());
            return Optional.empty();
        }
        if (optionalProfile.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getCommandNoProfileFound());
        }
        return optionalProfile;
    }
}
