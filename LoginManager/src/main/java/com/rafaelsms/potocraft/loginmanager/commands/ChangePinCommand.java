package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePinCommand implements RawCommand {

    // /<command> <old pin> <new pin> <new pin>

    private final Pattern pinExtractor =
            Pattern.compile("^\\s*(\\d{6})\\s*(\\d{6})\\s*(\\d{6})(\\s+.*)?$", Pattern.CASE_INSENSITIVE);
    private final Pattern tabCompletable =
            Pattern.compile("^\\s*(\\S+)?\\s*(\\S+)?\\s*(\\S+)?\\s*$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public ChangePinCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Check if source is a player
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Check player type
        if (!PlayerType.get(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        // Check arguments
        Matcher matcher = pinExtractor.matcher(invocation.arguments());
        if (!matcher.matches()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinHelp());
            return;
        }

        // Parse old pin
        String oldPinString = matcher.group(1);
        Optional<Integer> oldPinOptional = TextUtil.parsePin(oldPinString);
        if (oldPinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandChangePinHelp());
            return;
        }
        int oldPin = oldPinOptional.get();

        // Get new pin and verify they are equal
        String newPinString = matcher.group(2);
        String newPinConfirmationString = matcher.group(3);
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

    @Override
    public List<String> suggest(Invocation invocation) {
        Matcher matcher = tabCompletable.matcher(invocation.arguments());
        if (matcher.matches()) {
            return List.of("123456", "834712");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_CHANGE_PIN);
    }
}
