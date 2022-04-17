package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    @SuppressWarnings("deprecation")
    @EventHandler
    private void setPlayersScoreboard(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(scoreboard);
        // Remove player's team if existent
        Team existingTeam = scoreboard.getTeam(event.getPlayer().getUniqueId().toString());
        if (existingTeam != null) {
            existingTeam.unregister();
        }
        // Create a new team
        Team playerTeam = scoreboard.registerNewTeam(event.getPlayer().getUniqueId().toString());
        // Add player
        playerTeam.addEntry(event.getPlayer().getName());
        Optional<ChatColor> permissionColor = getPlayerPermissionColor(event.getPlayer());
        permissionColor.ifPresent(playerTeam::setColor);
        // Set prefix
        String prefix = TextUtil.getPrefix(event.getPlayer().getUniqueId());
        playerTeam.setPrefix(TextUtil.toColorizedString(prefix) + permissionColor.orElse(ChatColor.WHITE));
        // Set suffix
        String suffix = TextUtil.getSuffix(event.getPlayer().getUniqueId());
        playerTeam.setSuffix(TextUtil.toColorizedString(suffix));
    }

    @EventHandler
    private void unsetPlayerScoreboard(PlayerQuitEvent event) {
        // Delete player's team
        Team playerTeam = scoreboard.getTeam(event.getPlayer().getUniqueId().toString());
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
