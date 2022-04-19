package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.LuckPermsUtil;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScoreboardListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    private Scoreboard scoreboard;

    public ScoreboardListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void setUpScoreboard(ServerLoadEvent event) {
        scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        // Register a new show health objective
        if (scoreboard.getObjective("health") == null) {
            Objective objective = scoreboard.registerNewObjective("health",
                                                                  "health",
                                                                  TextUtil.toColorizedString("&c❤"),
                                                                  RenderType.HEARTS);
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setRenderType(RenderType.HEARTS);
        }
        plugin.logger().info("Registered colored scoreboard.");
    }

    @EventHandler
    private void unsetPlayerScoreboard(AsyncPlayerPreLoginEvent event) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Delete player's team
            Team playerTeam = scoreboard.getTeam(event.getName());
            if (playerTeam != null) {
                playerTeam.unregister();
            }
            future.complete(null);
        });
        try {
            future.get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            plugin.logger().warn("Failed to wait Scoreboard removal:", exception);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void setPlayersScoreboard(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerId = player.getUniqueId();
        // Set player's scoreboard
        player.setScoreboard(scoreboard);
        // Remove player's team if existent
        Team existingTeam = scoreboard.getTeam(playerName);
        if (existingTeam != null) {
            existingTeam.unregister();
        }
        // Create a new team
        Team playerTeam = scoreboard.registerNewTeam(playerName);
        // Add player
        playerTeam.addEntry(playerName);
        Optional<ChatColor> permissionColor = getPlayerPermissionColor(player);
        permissionColor.ifPresent(playerTeam::setColor);
        // Set prefix
        playerTeam.setPrefix(LuckPermsUtil.getColorizedPrefix(playerId).orElse("") +
                             permissionColor.orElse(ChatColor.WHITE));
        // Set suffix
        playerTeam.setSuffix(LuckPermsUtil.getColorizedSuffix(playerId).orElse(""));
    }

    @EventHandler
    private void unsetPlayerScoreboard(PlayerQuitEvent event) {
        // Delete player's team
        Team playerTeam = scoreboard.getTeam(event.getPlayer().getName());
        if (playerTeam != null) {
            playerTeam.unregister();
        }
    }

    private @NotNull Optional<ChatColor> getPlayerPermissionColor(@NotNull Player player) {
        for (ChatColor color : ChatColor.values()) {
            if (player.hasPermission(Permissions.COLORED_NAME_PREFIX + color.name().toLowerCase())) {
                return Optional.of(color);
            }
        }
        return Optional.empty();
    }
}
