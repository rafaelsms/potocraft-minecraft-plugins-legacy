package com.rafaelsms.potocraft.common.user;

import com.rafaelsms.potocraft.common.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class UserManager<U extends User, P> {

    protected final Plugin plugin;
    protected final Map<UUID, U> users = Collections.synchronizedMap(new HashMap<>());

    public UserManager(Plugin plugin) {
        this.plugin = plugin;
    }

    protected abstract U makeUser(P playerInstance);

    public void userJoinedListener(@NotNull UUID playerId, P playerInstance) {
        users.put(playerId, makeUser(playerInstance));
    }

    public void userQuitListener(@NotNull UUID playerId) {
        users.remove(playerId);
    }

    public U getUser(@NotNull UUID uuid) {
        return users.get(uuid);
    }
}
