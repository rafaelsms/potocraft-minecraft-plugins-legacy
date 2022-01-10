package com.rafaelsms.potocraft.velocity.database;

import com.rafaelsms.potocraft.common.database.Database;
import com.rafaelsms.potocraft.common.database.DatabaseException;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VelocityDatabase extends Database<VelocityProfile> {

    private final @NotNull VelocityPlugin plugin;

    public VelocityDatabase(@NotNull VelocityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public @NotNull Optional<VelocityProfile> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return super.getProfile(playerId);
    }

    @Override
    public List<VelocityProfile> searchOfflineProfile(@NotNull String namePattern) throws DatabaseException {
        return super.searchOfflineProfile(namePattern);
    }

    @Override
    public void saveProfile(@NotNull VelocityProfile profile) throws DatabaseException {
        super.saveProfile(profile);
    }

    @Override
    protected @NotNull VelocityProfile makeProfile(@NotNull Document document) {
        return new VelocityProfile(plugin, document);
    }
}
