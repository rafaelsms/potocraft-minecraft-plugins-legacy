package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldGuardNoEquipmentDamageFlag extends WorldGuardStateFlagRegister {

    public static final String FLAG_NAME = "no-equipment-damage";

    public WorldGuardNoEquipmentDamageFlag(@NotNull ServerUtilityPlugin plugin) {
        // Default flag value is false, so every player will have its item damaged by default
        super(plugin, FLAG_NAME, false, RegionGroup.ALL);
    }

    @EventHandler(ignoreCancelled = true)
    private void denyEquipmentDamageFlag(PlayerItemDamageEvent event) {
        Optional<Boolean> flagOptional = testFlag(event.getPlayer().getLocation(), event.getPlayer());
        if (flagOptional.isEmpty()) {
            return;
        }
        if (flagOptional.get()) {
            event.setCancelled(true);
        }
    }
}
