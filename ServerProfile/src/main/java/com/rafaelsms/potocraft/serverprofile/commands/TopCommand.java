package com.rafaelsms.potocraft.serverprofile.commands;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.players.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TopCommand implements CommandExecutor {

    private final @NotNull ServerProfilePlugin plugin;

    public TopCommand(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.TOP_COMMAND)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        try {
            UUID senderId = null;
            boolean senderOnTop = false;
            if (sender instanceof Player player) {
                senderId = player.getUniqueId();
            }

            List<Profile> rankedProfiles = new LinkedList<>();
            // Get all profiles in order
            List<Profile> ranking = plugin.getDatabase().getProfileRanking();
            for (Profile profile : ranking) {
                if (Objects.equals(senderId, profile.getPlayerId())) {
                    senderOnTop = true;
                }
                rankedProfiles.add(profile);
            }
            // If sender doesn't show, get its profile too
            if (!senderOnTop && sender instanceof Player player) {
                User user = plugin.getUserManager().getUser(player);
                rankedProfiles.add(user.getProfile());
            }

            sender.sendMessage(plugin.getConfiguration().getTopRanking(rankedProfiles));
        } catch (DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getCouldNotLoadProfile());
        }
        return true;
    }
}
