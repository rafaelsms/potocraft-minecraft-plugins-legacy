package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.util.PlayerType;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ChangePinCommand implements RawCommand {

    // /<command> <old pin> <new pin> <new pin>
    // /<command> <username> <new pin> <new pin>

    private final @NotNull VelocityPlugin plugin;

    public ChangePinCommand(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    private Optional<Integer> getMatchingPins(@NotNull CommandSource source,
                                              @NotNull String firstPin,
                                              @NotNull String secondPin) {
        try {
            int newPin = TextUtil.parsePin(firstPin).orElseThrow();
            int newPinConfirmation = TextUtil.parsePin(secondPin).orElseThrow();
            if (newPin != newPinConfirmation) {
                source.sendMessage(plugin.getSettings().getCommandChangePinPinsDoNotMatch());
                return Optional.empty();
            }
            return Optional.of(newPin);
        } catch (Exception ignored) {
            // New pin is not numeric
            source.sendMessage(plugin.getSettings().getCommandChangePinHelp());
            return Optional.empty();
        }
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] arguments = TextUtil.parseArguments(invocation.arguments());
        if (arguments.length < 3) {
            source.sendMessage(plugin.getSettings().getCommandChangePinHelp());
            return;
        }

        Optional<Integer> oldPinOptional = TextUtil.parsePin(arguments[0]);
        if (oldPinOptional.isEmpty() || !(source instanceof Player player)) {
            if (!source.hasPermission(Permissions.CHANGE_PIN_COMMAND_OTHERS)) {
                source.sendMessage(plugin.getSettings().getCommandChangePinHelp());
                return;
            }

            Optional<Integer> pinOptional = getMatchingPins(source, arguments[1], arguments[2]);
            if (pinOptional.isEmpty()) {
                return;
            }
            int newPin = pinOptional.get();

            try {
                List<VelocityProfile> profiles = plugin.getDatabase().searchOfflineProfile(arguments[0]);
                if (profiles.isEmpty()) {
                    source.sendMessage(plugin.getSettings().getPlayerNotFound());
                } else if (profiles.size() > 1) {
                    source.sendMessage(plugin.getSettings().getManyPlayersFound(profiles));
                } else {
                    VelocityProfile profile = profiles.get(0);
                    profile.setPIN(newPin);
                    plugin.getDatabase().saveProfile(profile);
                }
            } catch (Exception ignored) {
                source.sendMessage(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
            }
        } else {
            // Get new pin and verify they are equal
            Optional<Integer> pinOptional = getMatchingPins(source, arguments[1], arguments[2]);
            if (pinOptional.isEmpty()) {
                return;
            }
            int newPin = pinOptional.get();

            // Check if player is online mode
            try {
                PlayerType playerType = plugin.getPlayerType(player);
                if (playerType != PlayerType.OFFLINE_PLAYER) {
                    player.sendMessage(plugin.getSettings().getCommandOfflinePlayersOnly());
                    return;
                }
            } catch (Exception ignored) {
                player.disconnect(plugin.getSettings().getKickMessageCouldNotCheckPlayerType());
                return;
            }

            // Retrieve player profile
            VelocityProfile profile;
            try {
                Optional<VelocityProfile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
                if (profileOptional.isEmpty()) {
                    source.sendMessage(plugin.getSettings().getCommandMustRegisterFirst());
                    return;
                }
                profile = profileOptional.get();
            } catch (Exception ignored) {
                player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
                return;
            }

            // Check if profile has same pin
            int oldPin = oldPinOptional.get();
            if (!profile.isValidPIN(oldPin)) {
                player.sendMessage(plugin.getSettings().getCommandIncorrectPIN());
                return;
            }

            // Update pin
            try {
                profile.setPIN(newPin);
                plugin.getDatabase().saveProfile(profile);
            } catch (Exception ignored) {
                player.disconnect(plugin.getSettings().getKickMessageCouldNotSaveProfile());
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (TextUtil.parseArguments(invocation.arguments()).length <= 2) {
            return List.of("123456", "000000");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.CHANGE_PIN_COMMAND);
    }
}
