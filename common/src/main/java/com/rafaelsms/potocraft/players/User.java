package com.rafaelsms.potocraft.players;

import com.rafaelsms.potocraft.database.pojo.PlayerObject;

import java.util.Objects;
import java.util.UUID;

public abstract class User<T, R extends BasePlayer<T>, S extends PlayerObject> {

    private final R player;
    private final S playerObject;

    public User(R player, S playerObject) {
        this.player = player;
        this.playerObject = playerObject;
        // Update player object with up-to-date data
        this.playerObject.setPlayerId(player.getPlayerId());
        this.playerObject.setPlayerName(player.getPlayerName());
    }

    public UUID getPlayerId() {
        return player.getPlayerId();
    }

    public T getPlayer() {
        return player.getInstance();
    }

    public S getPlayerObject() {
        return playerObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User<?, ?, ?> user = (User<?, ?, ?>) o;
        return Objects.equals(player, user.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
