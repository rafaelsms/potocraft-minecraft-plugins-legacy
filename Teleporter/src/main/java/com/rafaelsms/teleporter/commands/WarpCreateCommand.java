package com.rafaelsms.teleporter.commands;

import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.player.User;
import org.jetbrains.annotations.NotNull;

public class WarpCreateCommand extends BaseTeleportCommand {

    public WarpCreateCommand(@NotNull TeleporterPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onTeleportCommand(@NotNull User user, @NotNull String label, @NotNull String[] arguments) {
    }
}
