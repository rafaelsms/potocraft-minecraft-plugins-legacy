package com.rafaelsms.potocraft.players;

import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ProxyPlayer implements BasePlayer<Player> {

    private final @NotNull Player player;

    public ProxyPlayer(@NotNull Player player) {
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
        return player.getUsername();
    }
}
