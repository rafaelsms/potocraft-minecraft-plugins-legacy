package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        subCommands.put("mute", new MuteCommand());
        subCommands.put("history", new HistoryCommand());
        subCommands.put("ban", new BanCommand());
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
        return invocation.source().hasPermission(Permissions.REPORT_COMMAND);
    }

    private class KickCommand implements RawCommand {

        private static final Pattern kickPattern = Pattern.compile("\\s*(\\S+)");

        @Override
        public void execute(Invocation invocation) {
            String arguments = subCommandReplacer.matcher(invocation.arguments()).replaceFirst("");
            Matcher playerMatcher = kickPattern.matcher(arguments);

            // Search in the command for possible names
            Set<String> typedNames = new HashSet<>();
            while (playerMatcher.find()) {
                String playerName = playerMatcher.group(1).toLowerCase();
                typedNames.add(playerName);
            }

            // Find players against typed name
            List<Player> foundPlayers = new ArrayList<>(4);
            for (Player onlinePlayer : plugin.getProxyServer().getAllPlayers()) {
                if (!typedNames.contains(onlinePlayer.getUsername().toLowerCase())) continue;
                foundPlayers.add(onlinePlayer);
            }

            // If no player found, warn user
            if (foundPlayers.size() == 0) {
                // TODO no matches
                return;
            }

            for (Player player : foundPlayers) {
                try {
                    VelocityProfile profile = plugin.getDatabase().getProfile(player.getUniqueId()).orElseThrow();
                    // TODO create report
                    // Save the profile
                    plugin.getDatabase().saveProfile(profile);
                    // TODO kick with report message
                    //player.disconnect();
                } catch (Exception exception) {
                    // Just disconnect if player doesn't have profile or failed somewhere
                    player.disconnect(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
                    // TODO failed to save report for player
                    //invocation.source().sendMessage();
                }
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }
    }

    private class BanCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            String arguments = subCommandReplacer.matcher(invocation.arguments()).replaceFirst("");

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }
    }

    private class MuteCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {
            String arguments = subCommandReplacer.matcher(invocation.arguments()).replaceFirst("");

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
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
    }
}
