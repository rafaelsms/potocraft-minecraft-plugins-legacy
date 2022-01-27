package com.rafaelsms.potocraft.serverprofile.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class StatisticsListener implements Listener {

    private final @NotNull ServerProfilePlugin plugin;

    public StatisticsListener(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    private Profile getProfile(@NotNull Player player) {
        return plugin.getUserManager().getUser(player).getProfile();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void increaseStatistics(EntityDeathEvent event) {
        // Ignore simple entities
        if (!(event.getEntity() instanceof Creature)) {
            return;
        }

        // Increase mob kill count if killer exists
        Player killerPlayer = event.getEntity().getKiller();
        if (killerPlayer != null) {
            getProfile(killerPlayer).incrementMobKill();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void increaseStatistics(PlayerDeathEvent event) {
        // Increase dead player's death count
        getProfile(event.getPlayer()).incrementDeathCount();

        // Increase killer's kill count
        Player killerPlayer = event.getPlayer().getKiller();
        if (killerPlayer != null) {
            getProfile(killerPlayer).incrementPlayerKill();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void increaseStatistics(BlockBreakEvent event) {
        getProfile(event.getPlayer()).incrementBlocksBroken();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void increaseStatistics(BlockPlaceEvent event) {
        getProfile(event.getPlayer()).incrementBlocksPlaced();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void increaseStatistics(PlayerPickupExperienceEvent event) {
        getProfile(event.getPlayer()).incrementExperience(event.getExperienceOrb().getExperience());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void increaseStatistics(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        getProfile(player).incrementTotemUsage();
    }
}
