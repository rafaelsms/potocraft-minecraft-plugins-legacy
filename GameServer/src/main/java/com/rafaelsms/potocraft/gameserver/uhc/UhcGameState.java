package com.rafaelsms.potocraft.gameserver.uhc;

import com.rafaelsms.potocraft.gameserver.GameServerPlugin;
import com.rafaelsms.potocraft.gameserver.util.GameUtil;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class UhcGameState implements Runnable {

    private final @NotNull GameServerPlugin plugin;
    private final @NotNull World world;

    private GameState oldGameState = GameState.WAITING_PLAYERS;
    private GameState gameState = GameState.WAITING_PLAYERS;
    private int timeCurrentState = 0;

    // Waiting players variables
    private final BossBar waitingPlayersBar;
    private int oldPlayerCount = 0;

    public UhcGameState(@NotNull GameServerPlugin plugin, @NotNull World world) {
        this.plugin = plugin;
        this.world = world;
        this.waitingPlayersBar =
                BossBar.bossBar(null, BossBar.MIN_PROGRESS, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
    }

    @Override
    public void run() {
        // Detect game state change automatically
        if (gameState != oldGameState) {
            timeCurrentState = 0;
            oldGameState = gameState;
        }
        timeCurrentState++;
        // Handle game state tick
        switch (gameState) {
            case WAITING_PLAYERS -> handleWaitingPlayers();
            case STARTING -> handleStarting();
            case PLAYING_PROTECTED -> handlePlayingProtected();
            case PLAYING_PVP -> handlePlayingPvp();
            case INTERMISSION -> handleIntermission();
        }
    }

    private void handleWaitingPlayers() {
        int playerCount = GameUtil.countGameMode(world, GameMode.SPECTATOR);
        if (playerCount >= plugin.getConfiguration().getUhcMinPlayers()) {
            // Set as starting
            gameState = GameState.STARTING;
            // If player count changed, reset timer
            if (playerCount != oldPlayerCount) {
                timeCurrentState = 0;
                oldPlayerCount = 0;
            }
        } else {
            // Show boss bar
        }
    }

    private void handleStarting() {
    }

    private void handlePlayingProtected() {
    }

    private void handlePlayingPvp() {
    }

    private void handleIntermission() {
    }
}
