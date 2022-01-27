package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.jetbrains.annotations.NotNull;

public class LavaListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public LavaListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void preventLavaFlow(BlockFromToEvent event) {
        if (event.getBlock().getBlockData().getMaterial() != Material.LAVA) {
            return;
        }
        if (plugin.getConfiguration().isAllowLavaFlow()) {
            return;
        }
        if (plugin.getConfiguration().getLavaFlowWorlds().contains(event.getBlock().getWorld())) {
            return;
        }
        event.setCancelled(true);
    }
}
