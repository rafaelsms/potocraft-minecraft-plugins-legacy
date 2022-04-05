package com.rafaelsms.teleporter;

import com.rafaelsms.potocraft.plugin.BasePermissions;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Permissions extends BasePermissions {

    private Permission bypassTeleportTimer;

    public Permissions(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerPermissions(@NotNull Function<String, Permission> permissionConsumer) {
        bypassTeleportTimer = permissionConsumer.apply("teleporter.bypass_timer");
    }

    public Permission getBypassTeleportTimer() {
        return bypassTeleportTimer;
    }
}
