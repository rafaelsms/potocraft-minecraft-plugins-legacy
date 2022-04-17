package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ScoreboardListener implements Listener {

    private final @NotNull ServerUtilityPlugin plugin;

    public ScoreboardListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void setUpScoreboard(ServerLoadEvent event) {
        Scoreboard mainScoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        // Remove every objective
        for (Objective objective : new ArrayList<>(mainScoreboard.getObjectives())) {
            objective.unregister();
        }
        // Register a new show health objective
        Objective objective =
                mainScoreboard.registerNewObjective("showHealth", "health", (Component) null, RenderType.HEARTS);
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    @EventHandler
    private void setPlayersScoreboard(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
    }
}
