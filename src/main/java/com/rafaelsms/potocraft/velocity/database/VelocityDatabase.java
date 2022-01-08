package com.rafaelsms.potocraft.velocity.database;

import com.rafaelsms.potocraft.common.util.Database;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityDatabase extends Database {

    private final @NotNull VelocityPlugin plugin;

    public VelocityDatabase(@NotNull VelocityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public @NotNull CompletableFuture<VelocityProfile> getProfile(@NotNull UUID playerUniqueId) {
        return super.getProfile(playerUniqueId, (document) -> new VelocityProfile(plugin, document));
    }

    public @NotNull CompletableFuture<Void> saveProfile(@NotNull VelocityProfile velocityProfile) {
        return super.saveProfile(velocityProfile);
    }
}
