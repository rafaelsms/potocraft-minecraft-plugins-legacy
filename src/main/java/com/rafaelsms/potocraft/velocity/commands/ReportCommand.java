package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.command.RawCommand;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportCommand implements RawCommand {

    private static final Pattern subCommandPattern = Pattern.compile("^\\s*(kick|ban|history|mute)\\s+.*$", Pattern.CASE_INSENSITIVE);

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

        @Override
        public void execute(Invocation invocation) {

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }
    }

    private class BanCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }
    }

    private class MuteCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }
    }

    private class HistoryCommand implements RawCommand {

        @Override
        public void execute(Invocation invocation) {

        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of();
        }
    }
}
