package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

public class ProjectileListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public ProjectileListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void multiplyArrowSpeed(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        arrow.setVelocity(arrow.getVelocity().multiply(plugin.getConfiguration().getArrowVelocityMultiplier()));
    }
}
