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
import java.util.Objects;
import java.util.Optional;

public class TeleportCommand implements com.rafaelsms.potocraft.common.util.Command {

    // 1 /teleport <player>
    // 2 /teleport <player_src> <player_dest> [ console & player ]
    // 3 /teleport <x> <y> <z>
    // 4 /teleport <world> <x> <y> <z>
    // 5 /teleport <player_src> <world> <x> <y> <z> [ console & player ]

    private final @NotNull PaperPlugin plugin;

    public TeleportCommand(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String alias,
                             @NotNull String[] arguments) {
        // Check permissions
        if (!sender.hasPermission(Permissions.TELEPORT_COMMAND)) {
            sender.sendMessage(plugin.getSettings().getNoPermission());
            return true;
        }

        switch (arguments.length) {
            case 5: { // /teleport <player_src> <world> <x> <y> <z> [ console & player ]

            }
            case 4: { // /teleport <world> <x> <y> <z>
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
                    return true;
                }

            }
            case 3: { // /teleport <x> <y> <z>
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
                    return true;
                }

            }
            case 2: { // /teleport <player_src> <player_dest> [ console & player ]

            }
            case 1: { // /teleport <player>
                // Ignore console
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
                    return true;
                }

                // Find player
                String playerName = arguments[0];
                Optional<Player> destinationOptional = PaperUtil.searchPlayerName(plugin, playerName);
                if (destinationOptional.isEmpty()) {
                    sender.sendMessage(plugin.getSettings().getPlayerNotFound());
                    return true;
                }

                Player destinationPlayer = destinationOptional.get();
                PaperUser playerUser = plugin.getUserManager().getUser(player.getUniqueId());
                PaperUser destinationUser = plugin.getUserManager().getUser(destinationPlayer.getUniqueId());

                // Check if destination is same player
                if (Objects.equals(destinationPlayer.getUniqueId(), player.getUniqueId())) {
                    player.sendMessage(plugin.getSettings().getTeleportToItself());
                    return true;
                }

                // Return if player can't teleport right now
                if (!PaperUtil.handleTeleportStatus(plugin, playerUser)) {
                    return true;
                }

                // Check if player doesn't require requests
                if (player.hasPermission(Permissions.TELEPORT_COMMAND_NO_REQUEST)) {
                    TeleportTask teleportTask = TeleportTask.Builder
                            .builder(plugin, playerUser, destinationUser, PlayerTeleportEvent.TeleportCause.COMMAND)
                            .withParticipant(destinationUser)
                            .warnParticipant(false)
                            .build();
                    playerUser.setTeleportTask(teleportTask);
                    return true;
                }

                // Send request to player
                TeleportRequest teleportRequest = new TeleportRequest(plugin, playerUser, playerUser, destinationUser);
                if (destinationUser.addTeleportRequest(teleportRequest)) {
                    // Just warn requested player if new request
                    destinationPlayer.sendMessage(plugin.getSettings().getTeleportRequestReceived(player.getName()));
                }
                player.sendMessage(plugin.getSettings().getTeleportRequestSent(destinationPlayer.getName()));
                return true;
            }
            default: {
                sender.sendMessage(plugin.getSettings().getTeleportHelp());
                return true;
            }
        }
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
