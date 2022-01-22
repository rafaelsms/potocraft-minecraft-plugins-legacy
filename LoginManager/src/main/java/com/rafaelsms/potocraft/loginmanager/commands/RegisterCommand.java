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

public class RegisterCommand implements RawCommand {

    private final Pattern pinExtractor = Pattern.compile("^\\s*(\\d{6})\\s*(\\d{6})(\\s+.*)?$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public RegisterCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Prevent console from executing
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandPlayersOnly());
            return;
        }

        // Offline players only
        if (!PlayerType.get(player).requiresLogin()) {
            player.sendMessage(plugin.getConfiguration().getCommandOfflinePlayersOnly());
            return;
        }

        // Retrieve profile
        Profile profile;
        try {
            profile = plugin
                    .getDatabase()
                    .getProfile(player.getUniqueId())
                    .orElse(new Profile(player.getUniqueId(), player.getUsername()));
        } catch (Exception ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        // Check if profile already has a PIN
        if (profile.hasPin()) {
            if (Util.isPlayerLoggedIn(plugin, profile, player) &&
                invocation.source().hasPermission(Permissions.COMMAND_CHANGE_PIN)) {
                player.sendMessage(plugin.getConfiguration().getCommandRegisterShouldChangePinInstead());
            } else {
                player.sendMessage(plugin.getConfiguration().getCommandRegisterShouldLoginInstead());
            }
            return;
        }

        // Retrieve arguments
        Matcher matcher = pinExtractor.matcher(invocation.arguments());
        if (!matcher.matches()) {
            player.sendMessage(plugin.getConfiguration().getCommandRegisterHelp());
            return;
        }

        // Parse to PIN
        String pinString = matcher.group(1);
        String pinConfirmationString = matcher.group(2);
        Optional<Integer> pinOptional = TextUtil.parseMatchingPins(pinString, pinConfirmationString);
        if (pinOptional.isEmpty()) {
            player.sendMessage(plugin.getConfiguration().getCommandRegisterInvalidPin());
            return;
        }

        // Check if couldn't set pin
        if (!profile.setPin(pinOptional.get())) {
            player.sendMessage(plugin.getConfiguration().getCommandIncorrectPinFormat());
            return;
        }

        // Save status
        try {
            profile.setLoggedIn(player.getRemoteAddress());
            player.sendMessage(plugin.getConfiguration().getCommandLoggedIn());
            plugin.getDatabase().saveProfile(profile);
            invocation.source().sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (Database.DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }

        // Send player to default server
        Util.sendPlayerToDefault(plugin, player);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of("123456", "000000");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_REGISTER);
    }
}
