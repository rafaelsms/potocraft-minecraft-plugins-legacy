package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldGuardNoEquipmentDamageFlag extends WorldGuardStateFlagRegister {

    public static final String FLAG_NAME = "no-equipment-damage";

    private final @NotNull ServerUtilityPlugin plugin;

    public WorldGuardNoEquipmentDamageFlag(@NotNull ServerUtilityPlugin plugin) {
        // Default flag value is false, so every player will have its item damaged by default
        super(plugin, FLAG_NAME, false, RegionGroup.ALL);
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void denyEquipmentDamageFlag(PlayerItemDamageEvent event) {
        Optional<Boolean> flagOptional = testFlag(event.getPlayer().getLocation(), event.getPlayer());
        // Empty when WorldGuard failed to search the database
        boolean optionalEmpty = flagOptional.isEmpty();
        if (optionalEmpty || flagOptional.get()) {
            if (optionalEmpty) {
                plugin.logger().error("No region manager found! Denying equipment damage as a precaution.");
            }
            event.setCancelled(true);
        }
    }
}
