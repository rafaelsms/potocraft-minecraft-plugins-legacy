package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.jetbrains.annotations.NotNull;

public class SpecialEnchantmentsListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public SpecialEnchantmentsListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void removeSpecialEnchantments(VillagerAcquireTradeEvent event) {
        if (plugin.getConfiguration().isRemoveSpecialEnchantmentsFromVillagers()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void removeSpecialEnchantments(VillagerAcquireTradeEvent event) {
        if (plugin.getConfiguration().isRemoveSpecialEnchantmentsFromVillagers()) {
            return;
        }
        event.setCancelled(true);
    }
}
