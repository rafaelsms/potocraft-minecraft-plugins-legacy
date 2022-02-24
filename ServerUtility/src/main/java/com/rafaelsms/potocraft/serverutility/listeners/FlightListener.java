package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class FlightListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public FlightListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void preventPlayerVersusPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged)) {
            return;
        }
        // Prevent damage if player has flight mode enabled
        if (damaged.isFlying() && damaged.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }

        Player damager = null;
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        } else if (event.getDamager() instanceof Player player) {
            damager = player;
        }
        if (damager == null) {
            return;
        }
        // Prevent damage if player has flight mode enabled
        if (damager.isFlying() && damager.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
}
