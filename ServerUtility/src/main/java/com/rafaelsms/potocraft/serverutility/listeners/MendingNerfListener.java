package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class MendingNerfListener implements Listener {

    private final HashSet<Integer> experienceOrbId = new HashSet<>();
    private final @NotNull ServerUtilityPlugin plugin;

    public MendingNerfListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void nerfMending(PlayerItemMendEvent event) {
        if (plugin.getConfiguration().getMendingMaxRepairAmount() < 0) {
            return;
        }
        // The default formula: half is going to the repair (which repairs double the amount) and half is going to the player
        // What we will do: half will be consumed to repair 1 point only and half will go to the player
        // TODO: remove workaround for https://github.com/PaperMC/Paper/issues/7449
        if (experienceOrbId.contains(event.getExperienceOrb().getEntityId())) {
            return;
        }
        experienceOrbId.add(event.getExperienceOrb().getEntityId());
        int halfExperience = event.getExperienceOrb().getExperience() / 2;
        event.getExperienceOrb().setExperience(halfExperience);
        event.setRepairAmount(Math.min(event.getRepairAmount(), plugin.getConfiguration().getMendingMaxRepairAmount()));
        plugin.getServer()
              .getScheduler()
              .runTaskLater(plugin, () -> experienceOrbId.remove(event.getExperienceOrb().getEntityId()), 20);
    }
}
