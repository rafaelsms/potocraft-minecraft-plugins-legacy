package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.jetbrains.annotations.NotNull;

public class TotemLimiter implements Listener {

    private final @NotNull ServerProfilePlugin plugin;

    public TotemLimiter(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void setTotemUsage(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        plugin.getUserManager().getUser(player).getProfile().setTotemUsage();
        player.sendMessage(plugin.getConfiguration().getTotemEnteredCooldown());
        player.sendActionBar(plugin.getConfiguration().getTotemEnteredCooldown());
    }

    @EventHandler(ignoreCancelled = true)
    private void preventTotemUsage(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.hasPermission(Permissions.UNLIMITED_TOTEM_USAGE)) {
            return;
        }
        if (plugin.getUserManager().getUser(player).isTotemInCooldown()) {
            player.sendMessage(plugin.getConfiguration().getTotemInCooldown());
            event.setCancelled(true);
        }
    }
}
