package com.rafaelsms.potocraft.players;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ServerPlayer implements BasePlayer<Player> {

    private final @NotNull Player player;

    public ServerPlayer(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Player getInstance() {
        return player;
    }

    @Override
    public @NotNull UUID getPlayerId() {
        return player.getUniqueId();
    }

    @Override
    public @NotNull String getPlayerName() {
        return player.getName();
    }
}
