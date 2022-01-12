package com.rafaelsms.potocraft.papermc.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportRequest;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportTask;
import com.rafaelsms.potocraft.papermc.util.PaperUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TeleportHereCommand implements com.rafaelsms.potocraft.common.util.Command {

    // /teleporteaqui <nome>

    private final @NotNull PaperPlugin plugin;

    public TeleportHereCommand(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String alias,
                             @NotNull String[] arguments) {
        // Check if player has permission
        if (!sender.hasPermission(Permissions.TELEPORT_COMMAND) ||
            !sender.hasPermission(Permissions.TELEPORT_COMMAND_HERE)) {
            sender.sendMessage(plugin.getSettings().getNoPermission());
            return true;
        }

        // Ignore console
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return true;
        }

        // Check if argument length is correct
        if (arguments.length != 1) {
            sender.sendMessage(plugin.getSettings().getTeleportHereHelp());
            return true;
        }

        // Find player
        String playerName = arguments[0];
        Optional<Player> teleportingOptional = PaperUtil.searchPlayerName(plugin, playerName);
        if (teleportingOptional.isEmpty()) {
            sender.sendMessage(plugin.getSettings().getPlayerNotFound());
            return true;
        }

        Player teleportingPlayer = teleportingOptional.get();
        PaperUser teleportingUser = plugin.getUserManager().getUser(teleportingPlayer.getUniqueId());
        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());

        // Check teleport status
        PaperUser.TeleportResult teleportResult = user.getTeleportStatus();
        if (!teleportResult.isAllowed()) {
            switch (teleportResult) {
                case IN_COMBAT -> player.sendMessage(plugin.getSettings().getCombatBlockedCommand());
                case PLAYER_UNAVAILABLE -> player.sendMessage(plugin.getSettings().getTeleportDestinationUnavailable());
                case IN_COOLDOWN -> player.sendMessage(plugin
                                                               .getSettings()
                                                               .getTeleportInCooldown(user.getTeleportCooldown()));
                case ALREADY_TELEPORTING -> player.sendMessage(plugin.getSettings().getTeleportCanNotTeleportNow());
            }
            return true;
        }

        // Check if player doesn't require requests
        if (player.hasPermission(Permissions.TELEPORT_COMMAND_NO_REQUEST)) {
            TeleportTask teleportTask = TeleportTask.Builder
                    .builder(plugin, teleportingUser, user, PlayerTeleportEvent.TeleportCause.COMMAND)
                    .withParticipant(user)
                    .build();
            // The teleport task will be shown to the user who requested
            // because the other one haven't had a chance to accept it
            user.setTeleportTask(teleportTask);
            return true;
        }

        // Send request to player
        TeleportRequest teleportRequest = new TeleportRequest(plugin, user, teleportingUser, user);
        if (teleportingUser.addTeleportRequest(teleportRequest)) {
            // Just warn requested player if new request
            teleportingPlayer.sendMessage(plugin.getSettings().getTeleportHereRequestReceived(player.getName()));
        }
        player.sendMessage(plugin.getSettings().getTeleportRequestSent(teleportingPlayer.getName()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] arguments) {
        if (arguments.length == 0) {
            return PaperUtil.getPlayerNameList(plugin);
        }
        return List.of();
    }
}
