package com.rafaelsms.potocraft.plugin;

import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class BasePermissions {

    private final @NotNull JavaPlugin plugin;

    protected BasePermissions(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerPermissions() {
        registerPermissions(string -> {
            Permission permission = new Permission(string);
            plugin.getServer().getPluginManager().addPermission(permission);
            return permission;
        });
    }

    protected abstract void registerPermissions(@NotNull Function<String, Permission> permissionConsumer);
}
