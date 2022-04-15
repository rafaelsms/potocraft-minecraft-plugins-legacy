package com.rafaelsms.teleporter.commands;

import com.rafaelsms.potocraft.plugin.BaseCommand;
import com.rafaelsms.potocraft.util.Util;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import com.rafaelsms.teleporter.teleports.TeleportDestination;
import com.rafaelsms.teleporter.teleports.TeleportResult;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public abstract class BaseTeleportCommand extends BaseCommand<User> {

    protected final @NotNull TeleporterPlugin plugin;

    public BaseTeleportCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin, plugin.getPermissions().getTeleportCommandPermission());
        this.plugin = plugin;
    }

    @Override
    public void onPlayerCommand(@NotNull User user, String label, String[] arguments) {
        // Check if player can teleport
        TeleportResult teleportStatus = user.getTeleportStatus(true);
        if (teleportStatus.isNegative()) {
            teleportStatus.getTeleportStatusMessage(plugin).ifPresent(user.getPlayer()::sendMessage);
            return;
        }

        onTeleportCommand(user, label, arguments);
    }

    protected abstract void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments);

    protected void executeTeleportToUser(@NotNull User requesting,
                                         @NotNull User teleporting,
                                         @NotNull User destination) {
        executeTeleport(requesting, teleporting, TeleportDestination.ofPlayer(destination));
    }

    protected void executeTeleportToLocation(@NotNull User requesting,
                                             @NotNull User teleporting,
                                             @NotNull Location destination) {
        executeTeleport(requesting, teleporting, TeleportDestination.ofLocation(destination));
    }

    protected void executeTeleport(@NotNull User requesting,
                                   @NotNull User teleporting,
                                   @NotNull TeleportDestination destination) {
        BiConsumer<TeleportResult, Throwable> teleportResultConsumer =
                Util.biConsumer(teleportResult -> teleportResult.getTeleportStatusMessage(plugin)
                                                                .ifPresent(requesting.getPlayer()::sendMessage),
                                throwable -> requesting.getPlayer()
                                                       .sendMessage(plugin.getConfiguration().getTeleportFailed()));
        teleporting.teleport(destination, true).whenComplete(teleportResultConsumer);
    }
}
