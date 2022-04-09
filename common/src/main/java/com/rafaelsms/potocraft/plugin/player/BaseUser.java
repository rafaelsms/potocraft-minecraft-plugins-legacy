package com.rafaelsms.potocraft.plugin.player;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.util.TickableTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class BaseUser<Profile> implements TickableTask {

    private final @NotNull Profile profile;
    private final @NotNull Player player;

    public BaseUser(@Nullable Profile profile, @Nullable Player player) throws DatabaseException {
        if (profile == null || player == null) {
            throw new DatabaseException(new NullPointerException("Player and profile can't be null!"));
        }
        this.profile = profile;
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull UUID getUniqueId() {
        return getPlayer().getUniqueId();
    }

    public @NotNull Profile getProfile() {
        return profile;
    }
}
