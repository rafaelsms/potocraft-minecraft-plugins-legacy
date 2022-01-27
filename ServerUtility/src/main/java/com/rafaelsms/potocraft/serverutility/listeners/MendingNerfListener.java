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
        event.setRepairAmount(1);
    }
}
