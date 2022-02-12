package com.rafaelsms.potocraft.gameserver.uhc;

import com.rafaelsms.potocraft.gameserver.GameServerPlugin;
import com.rafaelsms.potocraft.gameserver.util.GameMode;
import org.jetbrains.annotations.NotNull;

public class UHCGameMode implements GameMode {

    private GameState gameState = GameState.WAITING_PLAYERS;

    public UHCGameMode(@NotNull GameServerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new UHCListener(plugin, this), plugin);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(@NotNull GameState gameState) {
        this.gameState = gameState;
    }
}
