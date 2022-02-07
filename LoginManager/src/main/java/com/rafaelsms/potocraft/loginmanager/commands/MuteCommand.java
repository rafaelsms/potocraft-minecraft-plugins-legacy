package com.rafaelsms.potocraft.loginmanager.commands;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.Permissions;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.loginmanager.player.ReportEntry;
import com.rafaelsms.potocraft.loginmanager.util.CommandUtil;
import com.rafaelsms.potocraft.util.TextUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class MuteCommand extends Command {

    // /mute <name> [time amount] <reason>

    private final Pattern commandSyntax =
            Pattern.compile("^\\s*(\\S+)(\\s+([0-9wdhms]+))?(\\s+(\\S+.*))?$", Pattern.CASE_INSENSITIVE);

    private final @NotNull LoginManagerPlugin plugin;

    public MuteCommand(@NotNull LoginManagerPlugin plugin) {
        super("mute", Permissions.COMMAND_MUTE, "silenciar", "silencio");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfiguration().getCommandMuteHelp());
            return;
        }

        Optional<Duration> duration = Optional.empty();
        Optional<String> reason = Optional.empty();
        if (args.length > 1) {
            duration = TextUtil.parseTime(args[1]);
            if (duration.isEmpty()) {
                reason = TextUtil.joinStrings(args, 1);
            } else {
                reason = TextUtil.joinStrings(args, 2);
            }
        }

        if (duration.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getCommandMuteHelp());
            return;
        }
        ZonedDateTime expirationDate = ZonedDateTime.now().plus(duration.get());

        Optional<Profile> profileOptional = CommandUtil.handlePlayerSearch(plugin, sender, args[0]);
        if (profileOptional.isEmpty()) {
            return;
        }

        Profile profile = profileOptional.get();
        Optional<ProxiedPlayer> optionalPlayer = getOnlinePlayer(profile.getPlayerId());
        if (!sender.hasPermission(Permissions.COMMAND_MUTE_OFFLINE) && optionalPlayer.isEmpty()) {
            sender.sendMessage(plugin.getConfiguration().getCommandMutePlayerOffline());
            return;
        }
        if (optionalPlayer.isPresent() && optionalPlayer.get().hasPermission(Permissions.COMMAND_MUTE_EXEMPT)) {
            sender.sendMessage(plugin.getConfiguration().getNoPermission());
            return;
        }

        try {
            profile.addReportEntry(ReportEntry.Type.MUTE, getId(sender), expirationDate, reason.orElse(null));
            plugin.getDatabase().saveProfile(profile);
            sender.sendMessage(plugin.getConfiguration().getPlayerPunished(profile.getLastPlayerName()));
        } catch (Database.DatabaseException ignored) {
            sender.sendMessage(plugin.getConfiguration().getKickMessageFailedToSaveProfile());
        }
    }

    private Optional<ProxiedPlayer> getOnlinePlayer(@NotNull UUID playerId) {
        return Optional.ofNullable(plugin.getProxy().getPlayer(playerId));
    }

    private @Nullable UUID getId(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return player.getUniqueId();
        }
        return null;
    }
}
