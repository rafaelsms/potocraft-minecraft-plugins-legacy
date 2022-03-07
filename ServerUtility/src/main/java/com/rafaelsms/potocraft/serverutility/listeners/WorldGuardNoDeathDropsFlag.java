package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldGuardNoDeathDropsFlag extends WorldGuardStateFlagRegister {

    public static final String FLAG_NAME = "no-death-drops";

    public WorldGuardNoDeathDropsFlag(@NotNull ServerUtilityPlugin plugin) {
        // Default flag value is false, so every player will have its item damaged by default
        super(plugin, FLAG_NAME, false, RegionGroup.ALL);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void preventItemDroppingOnDeath(PlayerDeathEvent event) {
        Optional<Boolean> flagOptional = testFlag(event.getPlayer().getLocation(), event.getPlayer());
        if (flagOptional.isEmpty()) {
            return;
        }
        if (flagOptional.get()) {
            // Keep level and inventory
            event.setKeepLevel(true);
            event.setKeepInventory(true);
            // Clear drops and experience
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }
}
