package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.util.TextUtil;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        String username = matcher.group(1);
        Optional<String> reason = Optional.ofNullable(matcher.group(3));

        Optional<Player> playerOptional =
                TextUtil.closestMatch(plugin.getServer().getAllPlayers(), Player::getUsername, username);
        if (playerOptional.isEmpty()) {
            invocation.source().sendMessage(plugin.getConfiguration().getNoPlayerFound());
            return;
        }
        Player player = playerOptional.get();
        if (player.hasPermission(Permissions.COMMAND_KICK_EXEMPT)) {
            invocation.source().sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        Profile profile;
        try {
            profile = plugin.getDatabase().getProfile(player.getUniqueId()).orElse(null);
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandFailedToSearchProfile());
            return;
        }
        if (profile == null) {
            player.disconnect(Component.empty());
            invocation.source().sendMessage(plugin.getConfiguration().getPlayerPunished(player.getUsername()));
            return;
        }

        try {
            profile.addReportEntry(ReportEntry.Type.KICK, getId(invocation.source()), null, reason.orElse(null));
            plugin.getDatabase().saveProfile(profile);
            Component kickedMessage = plugin
                    .getConfiguration()
                    .getPunishmentMessageKicked(getName(invocation.source()), reason.orElse(null));
            player.disconnect(kickedMessage);
            invocation.source().sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (Database.DatabaseException ignored) {
            invocation.source().sendMessage(plugin.getConfiguration().getCommandFailedToSaveProfile());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.COMMAND_KICK);
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
