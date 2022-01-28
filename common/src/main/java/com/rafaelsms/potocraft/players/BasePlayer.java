package com.rafaelsms.potocraft.players;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface BasePlayer<T> {

    @NotNull T getInstance();

    @NotNull UUID getPlayerId();

    @NotNull String getPlayerName();

}
