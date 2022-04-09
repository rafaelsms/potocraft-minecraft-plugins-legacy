package com.rafaelsms.teleporter;

import com.rafaelsms.potocraft.plugin.BasePermissions;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Permissions extends BasePermissions {

    private Permission bypassTeleportTimer;
    private Permission bypassTeleportRequest;
    private Permission teleportCommandPermission;

    public Permissions(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerPermissions(@NotNull Function<String, Permission> permissionConsumer) {
        bypassTeleportTimer = permissionConsumer.apply("teleporter.bypass_timer");
        bypassTeleportRequest = permissionConsumer.apply("teleporter.bypass_request");
        teleportCommandPermission = permissionConsumer.apply("teleporter.command.teleport");
    }

    public @NotNull Permission getBypassTeleportTimer() {
        return bypassTeleportTimer;
    }

    public @NotNull Permission getBypassTeleportRequest() {
        return bypassTeleportRequest;
    }

    public @NotNull Permission getTeleportCommandPermission() {
        return teleportCommandPermission;
    }
}
