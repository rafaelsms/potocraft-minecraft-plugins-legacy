package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WorldGameRuleApplier implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public WorldGameRuleApplier(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("rawtypes")
    private void applyGameRules(WorldLoadEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        plugin.getConfiguration().getGameDifficulty().ifPresent(world::setDifficulty);
        // Apply default game rules
        for (Map.Entry<GameRule, Object> entry : plugin.getConfiguration().getDefaultGameRules().entrySet()) {
            applyGameRule(world, entry.getKey(), entry.getValue());
        }
        // Override with world-specific game rules
        for (Map.Entry<GameRule, Object> entry : plugin.getConfiguration().getWorldGameRule(worldName).entrySet()) {
            applyGameRule(world, entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void applyGameRule(@NotNull World world, @NotNull GameRule gamerule, @NotNull Object value) {
        if (world.setGameRule(gamerule, value)) {
            plugin.logger().info("Applied {} = {} to world {}", gamerule.getName(), value, world.getName());
        } else {
            plugin.logger().warn("Couldn't apply {} = {} to world {}", gamerule.getName(), value, world.getName());
        }
    }
}
