package com.rafaelsms.potocraft.common.user;

import com.rafaelsms.potocraft.common.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class UserManager<U extends User, P> {

    protected final Plugin<?, ?, ?> plugin;
    protected final Map<UUID, U> users = Collections.synchronizedMap(new HashMap<>());

    public UserManager(Plugin<?, ?, ?> plugin) {
        this.plugin = plugin;
    }

    protected abstract U makeUser(P playerInstance);

    public U userJoinedListener(@NotNull UUID playerId, P playerInstance) {
        U user = makeUser(playerInstance);
        users.put(playerId, user);
        return user;
    }

    public Optional<U> userQuitListener(@NotNull UUID playerId) {
        return Optional.ofNullable(users.remove(playerId));
    }

    public U getUser(@NotNull UUID uuid) {
        return users.get(uuid);
    }
}
