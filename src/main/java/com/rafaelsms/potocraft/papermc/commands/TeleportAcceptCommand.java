package com.rafaelsms.potocraft.papermc.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportRequest;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TeleportAcceptCommand implements com.rafaelsms.potocraft.common.util.Command {

    private final @NotNull PaperPlugin plugin;

    public TeleportAcceptCommand(@NotNull PaperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] arguments) {
        // Check if console
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSettings().getCommandConsoleCantExecute());
            return true;
        }

        // Check permissions
        if (!sender.hasPermission(Permissions.TELEPORT_COMMAND)) {
            sender.sendMessage(plugin.getSettings().getNoPermission());
            return true;
        }

        // Check arguments size
        if (arguments.length > 1) {
            sender.sendMessage(plugin.getSettings().getTeleportRequestAnswerHelp());
            return true;
        }

        PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
        List<TeleportRequest> teleportRequests = user.getTeleportRequests();

        if (teleportRequests.isEmpty()) {
            sender.sendMessage(plugin.getSettings().getTeleportRequestsListEmpty());
            return true;
        } else if (teleportRequests.size() == 1) {
            handleFoundRequest(user, teleportRequests.get(0));
            return true;
        } else {
            if (arguments.length > 0) {
                Optional<TeleportRequest> teleportRequest = TextUtil.closestStringMatch(teleportRequests,
                                                                                        request -> request
                                                                                                .getRequester()
                                                                                                .getPlayer()
                                                                                                .getName(),
                                                                                        arguments[0]);
                // If found, handle it, otherwise fallback to listing
                if (teleportRequest.isPresent()) {
                    handleFoundRequest(user, teleportRequest.get());
                    return true;
                }
            }
            List<String> playerNames =
                    TextUtil.toStringList(teleportRequests, request -> request.getRequester().getPlayer().getName());
            sender.sendMessage(plugin.getSettings().getTeleportRequestsList(playerNames));
            return true;
        }
    }

    private void handleFoundRequest(@NotNull PaperUser user, @NotNull TeleportRequest teleportRequest) {
        // Check if player can teleport
        PaperUser.TeleportResult teleportStatus = user.getTeleportStatus();
        if (!teleportStatus.isAllowed()) {
            user.getPlayer().sendMessage(plugin.getSettings().getTeleportCanNotTeleportNow());
            return;
        }

        // Accept the request
        TeleportTask teleportTask =
                teleportRequest.accept(plugin, PlayerTeleportEvent.TeleportCause.COMMAND).warnParticipant(true).build();
        user.setTeleportTask(teleportTask);
        String requesterName = teleportRequest.getRequester().getPlayer().getName();
        user.getPlayer().sendMessage(plugin.getSettings().getTeleportRequestAccepted(requesterName));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 0) {
            PaperUser user = plugin.getUserManager().getUser(player.getUniqueId());
            return TextUtil.toStringList(user.getTeleportRequests(),
                                         request -> request.getRequester().getPlayer().getName());
        }
        return List.of();
    }
}
