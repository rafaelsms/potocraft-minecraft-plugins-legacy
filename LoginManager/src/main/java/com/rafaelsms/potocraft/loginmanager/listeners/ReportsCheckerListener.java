package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.Util;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * We will: - prevent chat if player trying to chat is muted; - prevent join if player is banned.
 */
public class ReportsCheckerListener implements Listener {

    private final Pattern commandPattern = Pattern.compile("^\\s*(\\S+)", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public ReportsCheckerListener(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void checkJoinReports(LoginEvent event) {
        // If player isn't allowed already, just return
        if (event.isCancelled()) {
            return;
        }

        event.registerIntent(plugin);
        plugin.runAsync(() -> {
            PendingConnection connection = event.getConnection();
            Optional<Profile> profileOptional;
            try {
                profileOptional = plugin.getDatabase().getProfile(connection.getUniqueId());
            } catch (DatabaseException ignored) {
                event.setCancelReason(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
                event.setCancelled(true);
                event.completeIntent(plugin);
                return;
            }

            // Continue if player doesn't have a profile yet
            if (profileOptional.isEmpty()) {
                event.completeIntent(plugin);
                return;
            }

            // Check if there is a report that prevents joining
            Profile profile = profileOptional.get();
            for (ReportEntry entry : profile.getReportEntries()) {
                if (entry.isPreventingJoin()) {
                    String reporterName = Util.convert(entry.getReporterId(), this::getReporterName);
                    event.setCancelReason(plugin.getConfiguration()
                                                .getPunishmentMessageBanned(reporterName,
                                                                            entry.getExpirationDate(),
                                                                            entry.getReason()));
                    event.setCancelled(true);
                    event.completeIntent(plugin);
                    return;
                }
            }

            event.completeIntent(plugin);
        });
    }

    @EventHandler
    public void preventMutedFromChatting(ChatEvent event) {
        // If player isn't allowed already, just return
        if (event.isCancelled() || event.isCommand()) {
            return;
        }

        // Check if is a player
        Optional<ProxiedPlayer> playerOptional =
                com.rafaelsms.potocraft.loginmanager.util.Util.getPlayer(event.getSender());
        if (playerOptional.isEmpty()) {
            return;
        }

        ProxiedPlayer player = playerOptional.get();
        Optional<Profile> profileOptional;
        try {
            profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
        } catch (DatabaseException ignored) {
            player.disconnect(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            event.setCancelled(true);
            return;
        }

        // Continue if player doesn't have a profile yet
        if (profileOptional.isEmpty()) {
            return;
        }

        // Check if there is a report that prevents joining
        Profile profile = profileOptional.get();
        for (ReportEntry entry : profile.getReportEntries()) {
            if (entry.isPreventingChat()) {
                String reporterName = Util.convert(entry.getReporterId(), this::getReporterName);
                player.sendMessage(plugin.getConfiguration()
                                         .getPunishmentMessageMuted(reporterName,
                                                                    entry.getExpirationDate(),
                                                                    entry.getReason()));
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void preventMutedFromUsingCommands(ChatEvent event) {
        if (event.isCancelled() || !event.isCommand()) {
            return;
        }

        // Check if is a player
        Optional<ProxiedPlayer> playerOptional =
                com.rafaelsms.potocraft.loginmanager.util.Util.getPlayer(event.getSender());
        if (playerOptional.isEmpty()) {
            return;
        }

        // Check if player is muted
        ProxiedPlayer player = playerOptional.get();
        if (!isPlayerMuted(player)) {
            return;
        }

        // Split commands
        String[] words = event.getMessage().split(" ");
        if (words.length == 0) {
            event.setCancelled(true);
            return;
        }

        // Supposed to be a command
        String command = words[0];
        if (command.length() < 1) {
            event.setCancelled(true);
            return;
        }
        command = command.substring(1);

        // Filter out any commands that aren't on the allowed list
        if (plugin.getConfiguration().getBlockedCommandsMuted().contains(command)) {
            player.sendMessage(plugin.getConfiguration().getPunishmentMessageBlockedCommandMuted());
            event.setCancelled(true);
        }
    }


    private @Nullable String getReporterName(@NotNull UUID reporterId) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(reporterId);
        if (player != null) {
            return player.getName();
        }
        Optional<Profile> profileOptional = plugin.getDatabase().getProfileCatching(reporterId);
        return profileOptional.map(Profile::getLastPlayerName).orElse(null);
    }

    private boolean isPlayerMuted(@NotNull ProxiedPlayer player) {
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
        } catch (DatabaseException ignored) {
            return true;
        }
    }
}
