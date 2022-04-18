package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.LoginUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ChangePasswordCommand extends Command implements TabExecutor {

    // /<command> <old password> <new password> <new password>

    private final @NotNull LoginManagerPlugin plugin;

    public ChangePasswordCommand(@NotNull LoginManagerPlugin plugin) {
        super("mudarsenha", Permissions.COMMAND_CHANGE_PASSWORD, "changepin", "changepassword", "mudarpin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check if source is a player
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Check player type
        if (!plugin.getPlayerTypeManager().getPlayerType(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        // Check arguments
        if (args.length != 3 ||
            !LoginUtil.isValidPassword(args[0]) ||
            !LoginUtil.isValidPassword(args[1]) ||
            !LoginUtil.isValidPassword(args[2])) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePasswordHelp());
            return;
        }

        // Get new password and verify they are equal
        String oldPasswordString = args[0];
        String newPasswordString = args[1];
        String newPasswordConfirmationString = args[2];
        if (!newPasswordString.equalsIgnoreCase(newPasswordConfirmationString)) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePasswordDoNotMatch());
            return;
        }

        // Retrieve player profile
        Profile profile;
        try {
            Optional<Profile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            if (profileOptional.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getCommandChangePasswordRegisterFirst());
                return;
            }
            profile = profileOptional.get();

            // Check if player is online
            if (!LoginUtil.isPlayerLoggedIn(plugin, profile, player)) {
                player.sendMessage(plugin.getConfiguration().getCommandLoggedInOnly());
                return;
            }
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Check if the old password is valid
        if (!profile.isPasswordValid(oldPasswordString)) {
            player.disconnect(plugin.getConfiguration().getCommandIncorrectPassword());
            return;
        }

        // Check if we couldn't set new password
        if (!profile.setPassword(newPasswordString)) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPasswordFormat());
            return;
        }

        // Update player's password
        try {
            plugin.getDatabase().saveProfile(profile);
            player.sendMessage(plugin.getConfiguration().getCommandChangedPasswordSuccessful());
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 3) {
            return List.of("senhaSegura", "098765");
        }
        return List.of();
    }
}
