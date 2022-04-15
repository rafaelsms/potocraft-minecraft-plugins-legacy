package com.rafaelsms.teleporter.commands;

import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import com.rafaelsms.teleporter.teleports.TeleportRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public class TeleportAcceptCommand extends BaseTeleportCommand {

    public TeleportAcceptCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments) {
        Collection<TeleportRequest> teleportRequests = user.getTeleportRequests();

        // Check if there is no teleport request available
        if (teleportRequests.isEmpty()) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportRequestNoRequestAvailable());
            return;
        }

        // Check if it is ambiguous which teleport to accept
        if (arguments.length == 0 && teleportRequests.size() > 1) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportRequestManyRequests(teleportRequests));
            return;
        }

        Optional<TeleportRequest> closestMatch = Optional.empty();
        if (arguments.length > 0) {
            // Attempt to parse as text
            closestMatch = TextUtil.closestMatch(teleportRequests,
                                                 request -> request.getRequester().getPlayer().getName(),
                                                 arguments[0]);
        } else {
            // Should be only one teleport request here
            for (TeleportRequest teleportRequest : teleportRequests) {
                closestMatch = Optional.of(teleportRequest);
            }
        }
        if (closestMatch.isEmpty()) {
            user.getPlayer().sendMessage(plugin.getConfiguration().getTeleportRequestSpecificRequestNotFound());
            return;
        }

        // Cancel the request and execute teleport
        TeleportRequest teleportRequest = closestMatch.get();
        teleportRequest.cancelTask();
        executeTeleportToUser(teleportRequest.getRequester(),
                              teleportRequest.getTeleporting(),
                              teleportRequest.getDestination());
    }
}
