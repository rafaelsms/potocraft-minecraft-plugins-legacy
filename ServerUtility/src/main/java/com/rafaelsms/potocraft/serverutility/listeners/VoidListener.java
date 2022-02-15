package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class VoidListener implements Listener {

    private final HashMap<UUID, Location> lastSafeLocation = new HashMap<>();

    private final @NotNull ServerUtilityPlugin plugin;

    public VoidListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void storeSafeLocation(PlayerMoveEvent event) {
        Location newLocation = event.getTo().toBlockLocation();
        Location lastLocation = this.lastSafeLocation.getOrDefault(event.getPlayer().getUniqueId(), newLocation);
        if (Objects.equals(lastLocation, newLocation)) {
            return;
        }
        Block highestBlock = newLocation.getWorld().getHighestBlockAt(newLocation);
        if (highestBlock.getType().isEmpty()) {
            return;
        }
        this.lastSafeLocation.put(event.getPlayer().getUniqueId(), newLocation);
    }

    @EventHandler
    private void storeSafeLocation(PlayerQuitEvent event) {
        this.lastSafeLocation.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void preventVoidDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Location safeLocation = this.lastSafeLocation.get(player.getUniqueId());
        if (safeLocation == null) {
            return;
        }
        // Teleport player to safe location and kill it
        player.teleport(safeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setHealth(0.0);
    }
}
