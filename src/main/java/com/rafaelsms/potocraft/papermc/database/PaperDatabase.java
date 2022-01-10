package com.rafaelsms.potocraft.papermc.database;

import com.rafaelsms.potocraft.common.database.Database;
import com.rafaelsms.potocraft.common.database.DatabaseException;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.profile.PaperProfile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PaperDatabase extends Database<PaperProfile> {

    private final @NotNull PaperPlugin plugin;

    public PaperDatabase(@NotNull PaperPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public @NotNull Optional<PaperProfile> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return super.getProfile(playerId);
    }

    public void saveProfile(@NotNull PaperProfile profile) throws DatabaseException {
        super.saveProfile(profile);
    }

    @Override
    protected @NotNull PaperProfile makeProfile(@NotNull Document document) {
        return new PaperProfile(plugin, document);
    }
}
