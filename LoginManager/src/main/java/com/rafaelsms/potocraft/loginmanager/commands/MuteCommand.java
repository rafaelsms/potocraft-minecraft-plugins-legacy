package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteCommand implements RawCommand {

    // /mute <name> <reason>

    private final Pattern commandSyntax =
            Pattern.compile("^\\s*(\\S+)(\\s+([0-9wdhms]+))?(\\s+(\\S+.*))?$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public MuteCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandMuteHelp());
            return;
        }

        Optional<Duration> duration = TextUtil.parseTime(matcher.group(3));
        Optional<String> reason = Optional.ofNullable(matcher.group(5));

        if (duration.isEmpty()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandMuteHelp());
            return;
        }
        ZonedDateTime expirationDate = ZonedDateTime.now().plus(duration.get());

        Optional<Profile> profileOptional =
                CommandUtil.handlePlayerSearch(plugin, invocation.source(), matcher.group(1));
        if (profileOptional.isEmpty()) {
            return;
        }

        Profile profile = profileOptional.get();
        Optional<Player> optionalPlayer = getOnlinePlayer(profile.getPlayerId());
        if (!invocation.source().hasPermission(Permissions.COMMAND_MUTE_OFFLINE) && optionalPlayer.isEmpty()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandMutePlayerOffline());
            return;
        }
        if (optionalPlayer.isPresent() && optionalPlayer.get().hasPermission(Permissions.COMMAND_MUTE_EXEMPT)) {
            invocation.source().sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        try {
            profile.addReportEntry(ReportEntry.Type.MUTE,
                                   getId(invocation.source()),
                                   expirationDate,
                                   reason.orElse(null));
            plugin.getDatabase().saveProfile(profile);
            invocation.source().sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_MUTE);
    }

    private Optional<Player> getOnlinePlayer(@NotNull UUID playerId) {
        return plugin.getServer().getPlayer(playerId);
    }

    private @Nullable UUID getId(CommandSource source) {
        if (source instanceof Player player) {
            return player.getUniqueId();
        }
        return null;
    }
}
