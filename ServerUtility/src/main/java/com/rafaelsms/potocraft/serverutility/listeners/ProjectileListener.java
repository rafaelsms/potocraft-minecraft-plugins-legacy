package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

public class ProjectileListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public ProjectileListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void multiplyArrowSpeed(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        // Apply velocity change
        arrow.setVelocity(arrow.getVelocity().multiply(plugin.getConfiguration().getArrowVelocityMultiplier()));
        // Apply gravity change
        arrow.setGravity(plugin.getConfiguration().isArrowAffectedByGravity());
        if (!plugin.getConfiguration().isArrowAffectedByGravity()) {
            arrow.setPersistent(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void fixArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // Apply speed modifier to reduce damage
        event.setDamage(event.getDamage() / plugin.getConfiguration().getArrowVelocityMultiplier());
    }

    @EventHandler(ignoreCancelled = true)
    private void removeOldArrows(EntityMoveEvent event) {
        if (!plugin.getConfiguration().isArrowAffectedByGravity()) {
            return;
        }
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        if (arrow.getTicksLived() > 20 * 60) { // a minute flying
            arrow.remove();
        }
    }
}
