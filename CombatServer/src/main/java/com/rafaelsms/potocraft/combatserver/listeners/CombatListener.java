package com.rafaelsms.potocraft.combatserver.listeners;

import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CombatListener implements Listener {

    private final @NotNull CombatServerPlugin plugin;

    public CombatListener(@NotNull CombatServerPlugin plugin) {
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

    @EventHandler(ignoreCancelled = true)
    private void healOnKill(PlayerDeathEvent event) {
        Player killer = event.getPlayer().getKiller();
        if (killer != null) {
            killer.setHealth(Objects.requireNonNull(killer.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
        }
    }
}
