package com.rafaelsms.potocraft.serverutility.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExperienceModifier implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public ExperienceModifier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void modifyExperienceValue(PlayerPickupExperienceEvent event) {
        if (!plugin.getConfiguration().isExperienceModifierEnabled()) {
            return;
        }

        double modifier = plugin.getConfiguration().getExperienceModifierDefault();
        for (Map.Entry<String, Double> entry : plugin.getConfiguration().getExperienceModifierGroups().entrySet()) {
            if (event.getPlayer().hasPermission(entry.getKey())) {
                modifier = Math.max(entry.getValue(), modifier);
            }
        }
        event.getExperienceOrb().setExperience((int) Math.floor(event.getExperienceOrb().getExperience() * modifier));
    }
}
