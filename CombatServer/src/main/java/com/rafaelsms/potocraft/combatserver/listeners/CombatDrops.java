package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.NotNull;

public class CombatDrops implements Listener {

    private final @NotNull CombatServerPlugin plugin;

    public CombatDrops(@NotNull CombatServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void preventExperiencePickup(PlayerExpChangeEvent event) {
        event.setAmount(0);
        event.getPlayer().setTotalExperience(0);
    }

    @EventHandler(ignoreCancelled = true)
    private void changeDrops(PlayerDeathEvent event) {
        // Do not drop experience and don't keep inventory (we will give it back later)
        event.setKeepLevel(false);
        event.setKeepInventory(false);
        event.setShouldDropExperience(false);

        // Clear drops
        event.getDrops().clear();
    }
}
