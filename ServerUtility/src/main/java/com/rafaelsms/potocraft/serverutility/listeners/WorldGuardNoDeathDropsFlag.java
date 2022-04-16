package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldGuardNoDeathDropsFlag extends WorldGuardStateFlagRegister {

    public static final String FLAG_NAME = "no-death-drops";

    private final @NotNull ServerUtilityPlugin plugin;

    public WorldGuardNoDeathDropsFlag(@NotNull ServerUtilityPlugin plugin) {
        // Default flag value is false, so every player will have its item damaged by default
        super(plugin, FLAG_NAME, false, RegionGroup.ALL);
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void preventItemDroppingOnDeath(PlayerDeathEvent event) {
        Optional<Boolean> flagOptional = testFlag(event.getPlayer().getLocation(), event.getPlayer());
        // Empty when WorldGuard failed to search the database
        boolean optionalEmpty = flagOptional.isEmpty();
        if (optionalEmpty || flagOptional.get()) {
            if (optionalEmpty) {
                plugin.logger().error("No region manager found! Preventing item drop as a precaution.");
            }
            // Keep level and inventory
            event.setKeepLevel(true);
            event.setKeepInventory(true);
            // Clear drops and experience
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void preventTotemConsumption(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Optional<Boolean> flagOptional = testFlag(player.getLocation(), player);
        // Empty when WorldGuard failed to search the database
        if (flagOptional.isEmpty()) {
            // Do nothing (it is safer to let totem be spent than to deny resurrections)
            plugin.logger().error("No region manager found! Allowing totem consumption as a precaution.");
            return;
        }
        if (flagOptional.get()) {
            event.setCancelled(true);
        }
    }
}
