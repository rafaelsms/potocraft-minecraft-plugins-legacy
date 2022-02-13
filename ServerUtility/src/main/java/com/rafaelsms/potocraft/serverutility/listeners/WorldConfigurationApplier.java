package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.WorldCombatConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

public class WorldConfigurationApplier implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public WorldConfigurationApplier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void applyWorldConfiguration(WorldLoadEvent event) {
        WorldCombatConfig combatConfig = plugin.getConfiguration().getCombatConfiguration(event.getWorld().getName());
        if (!combatConfig.isConstantCombat()) {
            return;
        }
        event.getWorld().setPVP(combatConfig.getConstantCombatSetting());
    }

    @EventHandler(ignoreCancelled = true)
    private void preventSkipNight(TimeSkipEvent event) {
        if (event.getSkipReason() != TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            return;
        }
        if (plugin.getConfiguration().getCombatConfiguration(event.getWorld().getName()).isPreventSkipNight()) {
            event.setCancelled(true);
        }
    }
}
