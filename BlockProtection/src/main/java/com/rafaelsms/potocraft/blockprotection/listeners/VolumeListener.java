package com.rafaelsms.potocraft.blockprotection.listeners;

import com.rafaelsms.potocraft.blockprotection.BlockProtectionPlugin;
import com.rafaelsms.potocraft.blockprotection.protection.Region;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VolumeListener implements Listener {

    private final @NotNull Map<UUID, Region> regions = new HashMap<>();

    private final @NotNull BlockProtectionPlugin plugin;

    public VolumeListener(@NotNull BlockProtectionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void incrementVolume(BlockBreakEvent event) {
        plugin.getUserManager().getUser(event.getPlayer()).incrementVolume();
    }
}
