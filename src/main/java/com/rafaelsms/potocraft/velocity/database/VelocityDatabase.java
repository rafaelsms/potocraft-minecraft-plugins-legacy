package com.rafaelsms.potocraft.velocity.database;

import com.rafaelsms.potocraft.util.Database;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityDatabase extends Database {

    private final VelocityPlugin plugin;

    public VelocityDatabase(@NotNull VelocityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public @NotNull CompletableFuture<VelocityUser> getProfile(@NotNull UUID playerUniqueId) {
        return super.getProfile(playerUniqueId, (document) -> new VelocityUser(plugin, document));
    }

    public @NotNull CompletableFuture<Void> saveProfile(@NotNull VelocityUser velocityUser) {
        return super.saveProfile(velocityUser);
    }
}
