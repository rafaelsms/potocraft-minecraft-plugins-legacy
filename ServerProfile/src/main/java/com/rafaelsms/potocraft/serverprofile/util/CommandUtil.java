package com.rafaelsms.potocraft.serverprofile.util;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class CommandUtil {

    // Private constructor
    private CommandUtil() {
    }

    public static Optional<Profile> handlePlayerSearch(@NotNull ServerProfilePlugin plugin,
                                                       @NotNull CommandSender sender,
                                                       @NotNull String usernameSearch) {
        Optional<Profile> optionalProfile;
        try {
            optionalProfile = plugin.getDatabase().searchOfflineProfile(usernameSearch);
        } catch (Database.DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getCouldNotLoadProfile());
            return Optional.empty();
        }
        if (optionalProfile.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getTeleportPlayerNotFound());
        }
        return optionalProfile;
    }

}
