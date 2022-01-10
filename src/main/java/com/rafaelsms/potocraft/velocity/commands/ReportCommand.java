package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.profile.ReportEntry;
import com.rafaelsms.potocraft.common.profile.TimedReportEntry;
import com.rafaelsms.potocraft.common.util.DatabaseException;
import com.rafaelsms.potocraft.common.util.Util;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.rafaelsms.potocraft.velocity.util.VelocityUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportCommand implements RawCommand {

    private static final String subCommandRegex = "^\\s*(kick|ban|history|mute|unreport)\\s+";
    // We need to match everything (till the end) to ensure the format is correct, so we include a string end
    private static final Pattern subCommandPattern = Pattern.compile(subCommandRegex + ".*$", Pattern.CASE_INSENSITIVE);
    // We need to replace the first command part, so we need just the regex
    private static final Pattern subCommandReplacer = Pattern.compile(subCommandRegex, Pattern.CASE_INSENSITIVE);

    private final HashMap<String, RawCommand> subCommands = new HashMap<>();

    private final @NotNull VelocityPlugin plugin;

    public ReportCommand(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
        subCommands.put("kick", new KickSubCommand());
        subCommands.put("history", new HistorySubCommand());
        subCommands.put("mute", new MuteSubCommand());
        subCommands.put("ban", new BanSubCommand());
        subCommands.put("unreport", new UnreportSubCommand());
    }

    private Optional<VelocityProfile> handlePlayerSearch(@NotNull CommandSource source,
                                                         @Nullable String playerNamePattern) throws DatabaseException {
        if (playerNamePattern == null) {
            return Optional.empty();
        }
        List<VelocityProfile> profiles = plugin.getDatabase().searchOfflineProfile(playerNamePattern);
        if (profiles.isEmpty()) {
            source.sendMessage(plugin.getSettings().getPlayerNotFound());
            return Optional.empty();
        } else if (profiles.size() > 1) {
            source.sendMessage(plugin.getSettings().getManyPlayersFound(profiles));
            return Optional.empty();
        }
        return Optional.of(profiles.get(0));
    }

    private Optional<UUID> getSourcePlayerId(@NotNull CommandSource source) {
        if (source instanceof Player player) {
            return Optional.of(player.getUniqueId());
        }
        return Optional.empty();
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
            return List.of("kick", "ban", "history", "mute", "unreport");
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.REPORT_COMMAND);
    }

    private class KickSubCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] arguments =
                    Util.parseArguments(subCommandReplacer.matcher(invocation.arguments()).replaceFirst(""));

            // Check argument length
            if (arguments.length == 0) {
                source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp("kick"));
                return;
            }

            UUID reporterId = getSourcePlayerId(source).orElse(null);
            String playerNameString = Util.getArgument(arguments, 0).orElse(null);
            String reason = Util.joinStrings(arguments, 1);

            Optional<Player> optionalPlayer = plugin.getProxyServer().getPlayer(playerNameString);
            if (optionalPlayer.isEmpty()) {
                source.sendMessage(plugin.getSettings().getPlayerNotFound());
                return;
            }
            Player player = optionalPlayer.get();

            // Check if player can be kicked
            if (player.hasPermission(Permissions.REPORT_COMMAND_KICK_EXEMPT)) {
                source.sendMessage(plugin.getSettings().getCommandReportPlayerExempt());
                return;
            }

            try {
                // Find players against typed name
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
            return invocation.source().hasPermission(Permissions.REPORT_COMMAND_KICK);
        }
    }

    private class BanSubCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] arguments =
                    Util.parseArguments(subCommandReplacer.matcher(invocation.arguments()).replaceFirst(""));

            // Check if no argument was given
            if (arguments.length == 0) {
                source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp("ban"));
                return;
            }

            // Get data from strings
            UUID reporterId = getSourcePlayerId(source).orElse(null);
            String playerNameString = Util.getArgument(arguments, 0).orElse(null);
            Duration duration = Util.parseTime(Util.getArgument(arguments, 1).orElse(null)).orElse(null);
            String reason;
            if (duration == null) {
                reason = Util.joinStrings(arguments, 1);
            } else {
                reason = Util.joinStrings(arguments, 2);
            }

            // Append duration to expiration date
            ZonedDateTime expirationDate = null;
            if (duration != null) {
                expirationDate = ZonedDateTime.now().plus(duration);
            }

            try {
                Optional<VelocityProfile> profileOptional = handlePlayerSearch(source, playerNameString);
                if (profileOptional.isEmpty()) {
                    return;
                }
                VelocityProfile profile = profileOptional.get();

                // Find player by id
                Optional<Player> player = plugin.getProxyServer().getPlayer(profile.getUniqueId());
                if (player.isEmpty() && !source.hasPermission(Permissions.REPORT_COMMAND_BAN_OFFLINE)) {
                    source.sendMessage(plugin.getSettings().getPlayerNotFound());
                    return;
                }
                if (player.isPresent() && player.get().hasPermission(Permissions.REPORT_COMMAND_BAN_EXEMPT)) {
                    source.sendMessage(plugin.getSettings().getCommandReportPlayerExempt());
                    return;
                }

                ReportEntry reportEntry = profile.banned(reporterId, reason, expirationDate);
                // Save the profile
                plugin.getDatabase().saveProfile(profile);
                player.ifPresent(value -> value.disconnect(reportEntry.getMessage(plugin)));
            } catch (Exception ignored) {
                source.sendMessage(plugin.getSettings().getCommandGenericError());
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return switch (Util.parseArguments(invocation.arguments()).length) {
                case 3 -> List.of("hacking", "spam");
                case 2 -> List.of("4d", "30m", "12h", "1w", "hacking");
                case 1 -> VelocityUtil.getPlayerNameList(plugin);
                default -> List.of();
            };
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission(Permissions.REPORT_COMMAND_BAN);
        }
    }

    private class MuteSubCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] arguments =
                    Util.parseArguments(subCommandReplacer.matcher(invocation.arguments()).replaceFirst(""));

            // Check if no argument was given
            if (arguments.length == 0) {
                source.sendMessage(plugin.getSettings().getCommandReportSubCommandHelp("mute"));
                return;
            }

            // Get data from strings
            UUID reporterId = getSourcePlayerId(source).orElse(null);
            String playerNameString = Util.getArgument(arguments, 0).orElse(null);
            Duration duration = Util.parseTime(Util.getArgument(arguments, 1).orElse(null)).orElse(null);
            String reason;
            if (duration == null) {
                reason = Util.joinStrings(arguments, 1);
            } else {
                reason = Util.joinStrings(arguments, 2);
            }

            // Include duration to expiration date
            ZonedDateTime expirationDate = null;
            if (duration != null) {
                expirationDate = ZonedDateTime.now().plus(duration);
            }

            try {
                Optional<VelocityProfile> profileOptional = handlePlayerSearch(source, playerNameString);
                if (profileOptional.isEmpty()) {
                    return;
                }
                VelocityProfile profile = profileOptional.get();

                // Find player by id
                Optional<Player> player = plugin.getProxyServer().getPlayer(profile.getUniqueId());
                if (player.isEmpty() && !source.hasPermission(Permissions.REPORT_COMMAND_MUTE_OFFLINE)) {
                    source.sendMessage(plugin.getSettings().getPlayerNotFound());
                    return;
                }
                if (player.isPresent() && player.get().hasPermission(Permissions.REPORT_COMMAND_MUTE_EXEMPT)) {
                    source.sendMessage(plugin.getSettings().getCommandReportPlayerExempt());
                    return;
                }

                ReportEntry reportEntry = profile.muted(reporterId, reason, expirationDate);
                // Save the profile
                plugin.getDatabase().saveProfile(profile);
                player.ifPresent(value -> value.sendMessage(reportEntry.getMessage(plugin)));
            } catch (Exception ignored) {
                source.sendMessage(plugin.getSettings().getCommandGenericError());
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return switch (Util.parseArguments(invocation.arguments()).length) {
                case 3 -> List.of("propaganda", "spam");
                case 2 -> List.of("1d", "18h", "12h", "3d", "spam");
                case 1 -> VelocityUtil.getPlayerNameList(plugin);
                default -> List.of();
            };
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission(Permissions.REPORT_COMMAND_MUTE);
        }
    }

    private class HistorySubCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] arguments =
                    Util.parseArguments(subCommandReplacer.matcher(invocation.arguments()).replaceFirst(""));

            // Check if no argument was given
            String playerNameString = Util.getArgument(arguments, 0).orElse(null);
            if (playerNameString == null) {
                source.sendMessage(plugin.getSettings().getCommandReportHistoryHelp());
                return;
            }

            try {
                List<VelocityProfile> profiles = plugin.getDatabase().searchOfflineProfile(playerNameString);
                if (profiles.isEmpty()) {
                    source.sendMessage(plugin.getSettings().getPlayerNotFound());
                } else if (profiles.size() > 1) {
                    source.sendMessage(plugin.getSettings().getManyPlayersFound(profiles));
                } else {
                    VelocityProfile profile = profiles.get(0);
                    source.sendMessage(plugin
                                               .getSettings()
                                               .getCommandReportHistory(profile.getLastPlayerName(),
                                                                        profile.getReportEntries()));
                }
            } catch (Exception ignored) {
                source.sendMessage(plugin.getSettings().getCommandGenericError());
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            if (Util.parseArguments(invocation.arguments()).length == 1) {
                return VelocityUtil.getPlayerNameList(plugin);
            }
            return List.of();
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission(Permissions.REPORT_COMMAND_HISTORY);
        }
    }

    private class UnreportSubCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] arguments =
                    Util.parseArguments(subCommandReplacer.matcher(invocation.arguments()).replaceFirst(""));

            // Check if no argument was given
            String playerNameString = Util.getArgument(arguments, 0).orElse(null);
            if (playerNameString == null) {
                source.sendMessage(plugin.getSettings().getCommandReportUnreportHelp());
                return;
            }

            try {
                List<VelocityProfile> profiles = plugin.getDatabase().searchOfflineProfile(playerNameString);
                if (profiles.size() == 0) {
                    source.sendMessage(plugin.getSettings().getPlayerNotFound());
                } else if (profiles.size() == 1) {
                    VelocityProfile profile = profiles.get(0);
                    Optional<ReportEntry> joinPreventingOptional = profile.getJoinPreventingReport();
                    if (joinPreventingOptional.isPresent() &&
                            joinPreventingOptional.get() instanceof TimedReportEntry reportEntry) {
                        reportEntry.setActive(false);
                        plugin.getDatabase().saveProfile(profile);
                        source.sendMessage(plugin
                                                   .getSettings()
                                                   .getCommandReportUnreportSuccessfully(profile.getLastPlayerName()));
                        return;
                    }
                    Optional<ReportEntry> chatPreventingOptional = profile.getChatPreventingReport();
                    if (chatPreventingOptional.isPresent() &&
                            chatPreventingOptional.get() instanceof TimedReportEntry reportEntry) {
                        reportEntry.setActive(false);
                        plugin.getDatabase().saveProfile(profile);
                        source.sendMessage(plugin
                                                   .getSettings()
                                                   .getCommandReportUnreportSuccessfully(profile.getLastPlayerName()));
                        return;
                    }
                    source.sendMessage(plugin.getSettings().getCommandReportUnreportNoEntry());
                } else {
                    source.sendMessage(plugin.getSettings().getManyPlayersFound(profiles));
                }
            } catch (Exception ignored) {
                source.sendMessage(plugin.getSettings().getCommandGenericError());
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            if (Util.parseArguments(invocation.arguments()).length == 1) {
                return VelocityUtil.getPlayerNameList(plugin);
            }
            return List.of();
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission(Permissions.REPORT_COMMAND_UNREPORT);
        }
    }
}
