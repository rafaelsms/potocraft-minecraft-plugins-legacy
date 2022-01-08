package com.rafaelsms.potocraft.velocity.database;

import com.rafaelsms.potocraft.common.util.Database;
import com.rafaelsms.potocraft.common.util.DatabaseException;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class VelocityDatabase extends Database<VelocityProfile> {

    private final @NotNull VelocityPlugin plugin;

    public VelocityDatabase(@NotNull VelocityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public @NotNull Optional<VelocityProfile> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return super.getProfile(playerId);
    }

    public void saveProfile(@NotNull VelocityProfile profile) throws DatabaseException {
        super.saveProfile(profile);
    }

    @Override
    protected @NotNull VelocityProfile makeProfile(@NotNull Document document) {
        return new VelocityProfile(plugin, document);
    }
}
