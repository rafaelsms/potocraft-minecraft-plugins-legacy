package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.Util;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We will: - prevent chat if player trying to chat is muted; - prevent join if player is banned.
 */
public class ReportsCheckerListener {

    private final Pattern commandPattern = Pattern.compile("^\\s*(\\S+)(\\s+.*)?$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public ReportsCheckerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void checkJoinReports(LoginEvent event, Continuation continuation) {
        // If player isn't allowed already, just return
        if (!event.getResult().isAllowed()) {
            continuation.resume();
            return;
        }

        Player player = event.getPlayer();
        Optional<Profile> profileOptional;
        try {
            profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
        } catch (Database.DatabaseException ignored) {
            Component reason = plugin.getConfiguration().getKickMessageFailedToRetrieveProfile();
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
            continuation.resume();
            return;
        }

        // Continue if player doesn't have a profile yet
        if (profileOptional.isEmpty()) {
            continuation.resume();
            return;
        }

        // Check if there is a report that prevents joining
        Profile profile = profileOptional.get();
        for (ReportEntry entry : profile.getReportEntries()) {
            if (entry.isPreventingJoin()) {
                String reporterName = Util.convert(entry.getReporterId(), this::getReporterName);
                Component reason = plugin.getConfiguration()
                                         .getPunishmentMessageBanned(reporterName,
                                                                     entry.getExpirationDate(),
                                                                     entry.getReason());
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                continuation.resume();
                return;
            }
        }

        // Allow if player wasn't kicked
        continuation.resume();
    }

    @Subscribe
    private void checkChatReports(PlayerChatEvent event, Continuation continuation) {
        // If player isn't allowed already, just return
        if (!event.getResult().isAllowed()) {
            continuation.resume();
            return;
        }

        Player player = event.getPlayer();
        Optional<Profile> profileOptional;
        try {
            profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
        } catch (Database.DatabaseException ignored) {
            Component reason = plugin.getConfiguration().getKickMessageFailedToRetrieveProfile();
            player.sendMessage(reason);
            event.setResult(PlayerChatEvent.ChatResult.denied());
            continuation.resume();
            return;
        }

        // Continue if player doesn't have a profile yet
        if (profileOptional.isEmpty()) {
            continuation.resume();
            return;
        }

        // Check if there is a report that prevents joining
        Profile profile = profileOptional.get();
        for (ReportEntry entry : profile.getReportEntries()) {
            if (entry.isPreventingChat()) {
                String reporterName = Util.convert(entry.getReporterId(), this::getReporterName);
                Component reason = plugin.getConfiguration()
                                         .getPunishmentMessageMuted(reporterName,
                                                                    entry.getExpirationDate(),
                                                                    entry.getReason());
                player.sendMessage(reason);
                event.setResult(PlayerChatEvent.ChatResult.denied());
                continuation.resume();
                return;
            }
        }

        // Allow if player wasn't kicked
        continuation.resume();
    }

    @Subscribe
    private void preventMutedFromUsingCommands(CommandExecuteEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed()) {
            continuation.resume();
            return;
        }
        if (!(event.getCommandSource() instanceof Player player) || !isPlayerMuted(player)) {
            continuation.resume();
            return;
        }

        // Attempt to fit into the regex
        Matcher matcher = commandPattern.matcher(event.getCommand());
        if (!matcher.matches()) {
            plugin.getLogger()
                  .warn("Didn't expected to not find a command match: \"%s\"".formatted(event.getCommand()));
            continuation.resume();
            return;
        }

        // Filter out any commands that aren't on the allowed list
        String command = matcher.group(1).toLowerCase();
        if (plugin.getConfiguration().getBlockedCommandsMuted().contains(command)) {
            player.sendMessage(plugin.getConfiguration().getPunishmentMessageBlockedCommandMuted());
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }
        continuation.resume();
    }


    private @Nullable String getReporterName(@NotNull UUID reporterId) {
        Optional<Player> playerOptional = plugin.getServer().getPlayer(reporterId);
        if (playerOptional.isPresent()) {
            return playerOptional.get().getUsername();
        }
        Optional<Profile> profileOptional = plugin.getDatabase().getProfileCatching(reporterId);
        return profileOptional.map(Profile::getLastPlayerName).orElse(null);
    }

    private boolean isPlayerMuted(@NotNull Player player) {
        try {
            Optional<Profile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
            if (profileOptional.isEmpty()) {
                return false;
            }
            Profile profile = profileOptional.get();
            for (ReportEntry reportEntry : profile.getReportEntries()) {
                if (reportEntry.isPreventingChat()) {
                    return true;
                }
            }
            return false;
        } catch (Database.DatabaseException ignored) {
            return true;
        }
    }
}
