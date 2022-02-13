package com.rafaelsms.potocraft.gameserver.uhc;

import com.rafaelsms.potocraft.gameserver.GameServerPlugin;
import com.rafaelsms.potocraft.gameserver.util.GameMode;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UHCGameMode implements GameMode {

    private final Map<UUID, GameState> gameStateMap = new HashMap<>();
    private GameState gameState = GameState.WAITING_PLAYERS;

    public UHCGameMode(@NotNull GameServerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new UHCListener(plugin, this), plugin);
    }

    public @Nullable GameState getGameState(@NotNull World world) {
        return gameStateMap.get(world.getUID());
    }

    public void setGameState(@NotNull World world, @NotNull GameState gameState) {
        this.gameStateMap.put(world.getUID(), gameState);
    }
}
