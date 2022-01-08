package com.rafaelsms.potocraft.velocity.commands;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.command.RawCommand;

import java.util.List;

public class ReportCommand implements RawCommand {

    private final VelocityPlugin plugin;

    public ReportCommand(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true || invocation.source().hasPermission(Permissions.CHANGE_PIN_COMMAND);
    }
}
