package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.rafaelsms.potocraft.util.TextUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Pattern;

public class ChangePinCommand extends Command {

    // /<command> <old pin> <new pin> <new pin>

    private final Pattern pinFormat = Pattern.compile("^(\\d{6})$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public ChangePinCommand(@NotNull LoginManagerPlugin plugin) {
        super("mudarsenha", Permissions.COMMAND_CHANGE_PIN, "changepin", "changepassword", "mudarpin");
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
        if (!PlayerType.get(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        // Check arguments
        if (args.length != 3 ||
            !pinFormat.matcher(args[0]).matches() ||
            !pinFormat.matcher(args[1]).matches() ||
            !pinFormat.matcher(args[2]).matches()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinHelp());
            return;
        }

        // Parse old pin
        String oldPinString = args[0];
        Optional<Integer> oldPinOptional = TextUtil.parsePin(oldPinString);
        if (oldPinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinHelp());
            return;
        }
        int oldPin = oldPinOptional.get();

        // Get new pin and verify they are equal
        String newPinString = args[1];
        String newPinConfirmationString = args[2];
        Optional<Integer> pinOptional = TextUtil.parseMatchingPins(newPinString, newPinConfirmationString);
        if (pinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinInvalidPins());
            return;
        }
        int newPin = pinOptional.get();

        // Retrieve player profile
        Profile profile;
        try {
            Optional<Profile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            if (profileOptional.isEmpty()) {
                player.sendMessage(plugin.getConfiguration().getCommandChangePinRegisterFirst());
                return;
            }
            profile = profileOptional.get();

            // Check if player is online
            if (!Util.isPlayerLoggedIn(plugin, profile, player)) {
                player.sendMessage(plugin.getConfiguration().getCommandLoggedInOnly());
                return;
            }
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Check if old pin is valid
        if (!profile.isPinValid(oldPin)) {
            player.disconnect(plugin.getConfiguration().getCommandIncorrectPin());
            return;
        }

        // Check if couldn't set pin
        if (!profile.setPin(newPin)) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPinFormat());
            return;
        }

        // Update player's pin
        try {
            plugin.getDatabase().saveProfile(profile);
            player.sendMessage(plugin.getConfiguration().getCommandChangedPinSuccessful());
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }
    }
}
