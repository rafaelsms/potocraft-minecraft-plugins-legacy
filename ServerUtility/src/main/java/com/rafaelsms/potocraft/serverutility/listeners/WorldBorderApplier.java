package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

public class WorldBorderApplier implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public WorldBorderApplier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void applyGameRules(WorldLoadEvent event) {
        World world = event.getWorld();
        world.getWorldBorder().setSize(plugin.getConfiguration().getWorldSizeDiameter(world.getName()));
        world.getWorldBorder().setCenter(0, 0);
    }
}
