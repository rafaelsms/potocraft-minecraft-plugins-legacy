package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.profile.ReportEntry;
import com.rafaelsms.potocraft.common.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.rafaelsms.potocraft.velocity.util.VelocityUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Commands are difficult to generify because of String parsing, I did this way to be easier to debug and fix
@SuppressWarnings("DuplicatedCode")
// TODO god this is ugly: 1. move subcommands to its own classes, separate ban from mute, move more methods to util
public class ReportCommand implements RawCommand {

    private static final String subCommandRegex = "^\\s*(kick|ban|history|mute)\\s+";
    // We need to match everything (till the end) to ensure the format is correct, so we include a string end
    private static final Pattern subCommandPattern = Pattern.compile(subCommandRegex + ".*$", Pattern.CASE_INSENSITIVE);
    // We need to replace the first command part, so we need just the regex
    private static final Pattern subCommandReplacer = Pattern.compile(subCommandRegex, Pattern.CASE_INSENSITIVE);

    private final HashMap<String, RawCommand> subCommands = new HashMap<>();

    private final @NotNull VelocityPlugin plugin;

    public ReportCommand(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
        subCommands.put("kick", new KickCommand());
        subCommands.put("history", new HistoryCommand());
        TimedReportCommand timedReportCommand = new TimedReportCommand();
        subCommands.put("mute", timedReportCommand);
        subCommands.put("ban", timedReportCommand);
    }

    @Override
    public void execute(Invocation invocation) {
        // Match and execute subcommands
        Matcher subCommandMatcher = subCommandPattern.matcher(invocation.arguments());
        if (subCommandMatcher.matches()) {
            String subCommand = subCommandMatcher.group(1);
            // Test source's permission
            RawCommand command = subCommands.get(subCommand.toLowerCase());
            if (!command.hasPermission(invocation)) {
                invocation.source().sendMessage(plugin.getSettings().getNoPermission());
                return;
            }
            command.execute(invocation);
        } else {
            invocation.source().sendMessage(plugin.getSettings().getCommandReportHelp());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        Matcher subCommandMatcher = subCommandPattern.matcher(invocation.arguments());
        if (subCommandMatcher.matches()) {
            String subCommand = subCommandMatcher.group(1);
            RawCommand command = subCommands.get(subCommand.toLowerCase());
            // Check if sender has permission
            if (command.hasPermission(invocation)) {
                return command.suggest(invocation);
            }
        } else if (subCommandMatcher.hitEnd()) {
            return List.of("kick", "ban", "history", "mute");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true || invocation.source().hasPermission(Permissions.REPORT_COMMAND);
    }

    private Optional<Player> searchPlayer(@NotNull String playerName) {
        for (Player player : plugin.getProxyServer().getAllPlayers()) {
            if (!player.getUsername().equalsIgnoreCase(playerName)) continue;
            return Optional.of(player);
        }
        return Optional.empty();
    }

    private Optional<UUID> getSourcePlayerId(@NotNull CommandSource source) {
        if (source instanceof Player player) {
            return Optional.of(player.getUniqueId());
        }
        return Optional.empty();
    }

    private class KickCommand implements RawCommand {

        private static final Pattern kickPattern = Pattern.compile("^\\s*(\\S+)\\s*(\\S+.*)?$");

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String arguments = subCommandReplacer.matcher(invocation.arguments()).replaceFirst("");
            Matcher matcher = kickPattern.matcher(arguments);
            if (!matcher.matches()) {
                source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp("kick"));
                return;
            }

            UUID reporterId = getSourcePlayerId(source).orElse(null);
            String playerNameString = matcher.group(1);
            String reason = matcher.group(2);

            // Find players against typed name
            Player player = searchPlayer(playerNameString).orElse(null);
            if (player == null) {
                source.sendMessage(plugin.getSettings().getPlayerNotFound());
                return;
            }

            if (player.hasPermission(Permissions.REPORT_COMMAND_KICK_EXEMPT)) {
                source.sendMessage(plugin.getSettings().getCommandReportPlayerExempt());
                return;
            }

            try {
                VelocityProfile profile = plugin.getDatabase().getProfile(player.getUniqueId()).orElseThrow();
                ReportEntry reportEntry = profile.kicked(reporterId, reason);
                // Save the profile
                plugin.getDatabase().saveProfile(profile);
                player.disconnect(reportEntry.getMessage(plugin));
            } catch (Exception ignored) {
                // Just disconnect if player doesn't have profile or failed somewhere
                player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
                Component playerName = Component.text(player.getUsername());
                source.sendMessage(plugin.getSettings().getCommandReportCouldNotSaveReport(playerName));
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return switch (Util.parseArguments(invocation.arguments()).length) {
                case 2 -> List.of("hacking", "spam");
                case 1 -> VelocityUtil.getPlayerNameList(plugin);
                default -> List.of();
            };
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return true || invocation.source().hasPermission(Permissions.REPORT_COMMAND_KICK);
        }
    }

    private class TimedReportCommand implements RawCommand {

        private static final Pattern timedPattern = Pattern.compile("^\\s*(\\S+)\\s*((\\d+)([wdhms]))?(\\s+(\\S+.*))?$");

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            // Get subcommand
            Matcher subCommandMatcher = subCommandPattern.matcher(invocation.arguments());
            if (!subCommandMatcher.matches()) {
                source.sendMessage(plugin.getSettings().getCommandGenericError());
                return;
            }
            String subCommand = subCommandMatcher.group(1).toLowerCase();
            source.sendMessage(Component.text(subCommand));

            String arguments = subCommandReplacer.matcher(invocation.arguments()).replaceFirst("");
            Matcher matcher = timedPattern.matcher(arguments);
            if (!matcher.matches()) {
                source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp("kick"));
                return;
            }

            UUID reporterId = getSourcePlayerId(source).orElse(null);
            String playerNameString = matcher.group(1);
            String timeString = matcher.group(3);
            String timeUnit = matcher.group(4);
            String reason = matcher.group(6);

            // Find players against typed name
            Player player = searchPlayer(playerNameString).orElse(null);
            if (player == null) {
                source.sendMessage(plugin.getSettings().getPlayerNotFound());
                return;
            }

            if (subCommand.equalsIgnoreCase("mute") && player.hasPermission(Permissions.REPORT_COMMAND_MUTE_EXEMPT)) {
                source.sendMessage(plugin.getSettings().getCommandReportPlayerExempt());
                return;
            }

            if (subCommand.equalsIgnoreCase("ban") && player.hasPermission(Permissions.REPORT_COMMAND_BAN_EXEMPT)) {
                source.sendMessage(plugin.getSettings().getCommandReportPlayerExempt());
                return;
            }

            // Parse time
            Duration duration = null;
            if (timeString != null && timeUnit != null) {
                int timeAmount = Integer.parseInt(timeString);
                // Attempt to parse time unit
                duration = switch (timeUnit.toLowerCase()) {
                    case "w" -> Duration.ofDays(timeAmount * 7L);
                    case "d" -> Duration.ofDays(timeAmount);
                    case "h" -> Duration.ofHours(timeAmount);
                    case "m" -> Duration.ofMinutes(timeAmount);
                    case "s" -> Duration.ofSeconds(timeAmount);
                    default -> null;
                };

                if (duration == null) {
                    source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp(subCommand));
                    return;
                }
            }

            // Include duration to expiration date
            ZonedDateTime expirationDate = null;
            if (duration != null) {
                expirationDate = ZonedDateTime.now().plus(duration);
            }

            try {
                VelocityProfile profile = plugin.getDatabase().getProfile(player.getUniqueId()).orElseThrow();
                ReportEntry reportEntry = switch (subCommand) {
                    case "ban" -> profile.banned(reporterId, reason, expirationDate);
                    case "mute" -> profile.muted(reporterId, reason, expirationDate);
                    default -> null;
                };
                if (reportEntry == null) {
                    source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp(subCommand));
                    return;
                }
                // Save the profile
                plugin.getDatabase().saveProfile(profile);
                if (subCommand.equalsIgnoreCase("ban")) {
                    player.disconnect(reportEntry.getMessage(plugin));
                } else {
                    player.sendMessage(reportEntry.getMessage(plugin));
                }
            } catch (Exception ignored) {
                // Just disconnect if player doesn't have profile or failed somewhere
                player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
                Component playerName = Component.text(player.getUsername());
                source.sendMessage(plugin.getSettings().getCommandReportCouldNotSaveReport(playerName));
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return switch (Util.parseArguments(invocation.arguments()).length) {
                case 3 -> List.of("hacking", "spam");
                case 2 -> List.of("4d", "30m", "1w");
                case 1 -> VelocityUtil.getPlayerNameList(plugin);
                default -> List.of();
            };
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            Matcher subCommandMatcher = subCommandPattern.matcher(invocation.arguments());
            if (!subCommandMatcher.matches()) {
                return false;
            }
            String subCommand = subCommandMatcher.group(1).toLowerCase();
            if (subCommand.equalsIgnoreCase("mute")) {
                return true || invocation.source().hasPermission(Permissions.REPORT_COMMAND_MUTE);
            }
            if (subCommand.equalsIgnoreCase("ban")) {
                return true || invocation.source().hasPermission(Permissions.REPORT_COMMAND_BAN);
            }
            return false;
        }
    }

    private class HistoryCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            String arguments = subCommandReplacer.matcher(invocation.arguments()).replaceFirst("");

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return true || invocation.source().hasPermission(Permissions.REPORT_COMMAND_HISTORY);
        }
    }
}
