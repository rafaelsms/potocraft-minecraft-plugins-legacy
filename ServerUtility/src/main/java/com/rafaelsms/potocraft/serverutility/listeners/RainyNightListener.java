package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

public class RainyNightListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public RainyNightListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onSpawn(CreatureSpawnEvent event) {
        if (!plugin.getConfiguration().isRainyNightEnabled()) {
            return;
        }
        World world = event.getLocation().getWorld();
        if (!world.isThundering()) {
            return;
        }
        if (world.isDayTime()) {
            return;
        }
        if (!(event.getEntity() instanceof Monster) || event.getEntityType() == EntityType.CREEPER) {
            return;
        }
        event.getEntity().addPotionEffects(plugin.getConfiguration().getRainyNightPotionEffects());
    }
}
