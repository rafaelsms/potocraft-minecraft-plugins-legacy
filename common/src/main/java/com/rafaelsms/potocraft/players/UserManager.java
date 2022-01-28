package com.rafaelsms.potocraft.players;

import com.rafaelsms.potocraft.database.pojo.PlayerObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class UserManager<T, R extends BasePlayer<T>, S extends PlayerObject, U extends User<T, R, S>> {

    private final Map<UUID, U> userMap = Collections.synchronizedMap(new HashMap<>());

    public U getUser(@NotNull UUID playerId) {
        return userMap.get(playerId);
    }

    public abstract U getUser(@NotNull T player);

    public S getProfile(@NotNull UUID playerId) {
        return getUser(playerId).getPlayerObject();
    }

    public S getProfile(@NotNull T player) {
        return getUser(player).getPlayerObject();
    }

    protected void addUser(@NotNull U user) {
        this.userMap.put(user.getPlayerId(), user);
    }

    protected void removeUser(@NotNull U user) {
        this.userMap.remove(user.getPlayerId());
    }
}
