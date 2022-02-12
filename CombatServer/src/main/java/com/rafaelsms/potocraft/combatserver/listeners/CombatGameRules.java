package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

public class CombatGameRules implements Listener {

    private final @NotNull CombatServerPlugin plugin;

    public CombatGameRules(@NotNull CombatServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void setGameRules(WorldLoadEvent event) {
        event.getWorld().setGameRule(GameRule.DO_ENTITY_DROPS, false);
        event.getWorld().setGameRule(GameRule.DO_MOB_LOOT, false);
        event.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        event.getWorld().setGameRule(GameRule.DO_TILE_DROPS, false);
        event.getWorld().setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        event.getWorld().setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        event.getWorld().setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
        event.getWorld().setGameRule(GameRule.DISABLE_RAIDS, true);
        event.getWorld().setGameRule(GameRule.KEEP_INVENTORY, false);
        event.getWorld().setGameRule(GameRule.DO_INSOMNIA, false);
        event.getWorld().setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, true);
        event.getWorld().setGameRule(GameRule.MOB_GRIEFING, false);
    }
}
