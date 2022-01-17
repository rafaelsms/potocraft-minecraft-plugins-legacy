package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.Util;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KickCommand implements RawCommand {

    // /kick <name> <reason>

    private final Pattern commandSyntax = Pattern.compile("^\\s*(\\S+)(\\s+(\\S+.*))?$");

    private final @NotNull LoginManagerPlugin plugin;

    public KickCommand(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        Matcher matcher = commandSyntax.matcher(invocation.arguments());
        if (!matcher.matches()) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandKickHelp());
            return;
        }

        String usernameRegex = matcher.group(1);
        Optional<String> reason = Optional.ofNullable(matcher.group(3));

        List<Profile> offlineProfiles;
        try {
            offlineProfiles = plugin.getDatabase().getOfflineProfiles(usernameRegex);
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getKickMessageFailedToRetrieveProfile());
            return;
        }

        Optional<Profile> profileOptional = Util.handleUniqueProfile(plugin, invocation.source(), offlineProfiles);
        if (profileOptional.isEmpty()) {
            return;
        }
        Profile profile = profileOptional.get();
        Optional<Player> optionalPlayer = getOnlinePlayer(profile.getPlayerId());
        if (optionalPlayer.isEmpty()) {
            invocation.source().sendMessage(plugin.getConfiguration().getNoPlayerFound());
            return;
        }
        Player player = optionalPlayer.get();
        if (player.hasPermission(Permissions.COMMAND_KICK_EXEMPT)) {
            invocation.source().sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        try {
            profile.addReportEntry(ReportEntry.Type.KICK, getId(invocation.source()), null, reason.orElse(null));
            plugin.getDatabase().saveProfile(profile);
            Component disconnectReason = plugin
                    .getConfiguration()
                    .getPunishmentMessageKicked(getName(invocation.source()), reason.orElse(null));
            player.disconnect(disconnectReason);
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_KICK);
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

    private @Nullable String getName(CommandSource source) {
        if (source instanceof Player player) {
            return player.getUsername();
        }
        return null;
    }
}
