package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.jetbrains.annotations.NotNull;

public class MendingNerfListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public MendingNerfListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void nerfMending(PlayerItemMendEvent event) {
        if (!plugin.getConfiguration().isMendingNerfed()) {
            return;
        }
        // The default formula: half is going to the repair (which repairs double the amount) and half is going to the player
        // What we will do: half will be consumed to repair 1 point only and half will go to the player
        int halfExperience = event.getExperienceOrb().getExperience() / 2;
        event.getExperienceOrb().setExperience(halfExperience);
        event.setRepairAmount(Math.min(event.getRepairAmount(), 1));
    }
}
