package com.rafaelsms.potocraft.papermc.database;

import com.mongodb.client.MongoCollection;
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

    private MongoCollection<Document> getServerProfilesCollection() throws DatabaseException {
        String collectionName = plugin.getSettings().getMongoServerProfilesCollection();
        assert collectionName != null;
        return super.getDatabase().getCollection(collectionName);
    }

    public @NotNull Optional<ServerProfile> getServerProfile(@NotNull UUID playerId) throws DatabaseException {
        MongoCollection<Document> serverProfiles = getServerProfilesCollection();
        Document serverProfileDocument = serverProfiles.find(ServerProfile.filterId(playerId)).first();
        if (serverProfileDocument == null) {
            return Optional.empty();
        }
        return Optional.of(ServerProfile.fromDocument(serverProfileDocument));
    }

    public void saveServerProfile(@NotNull ServerProfile profile) throws DatabaseException {
        MongoCollection<Document> serverProfiles = getServerProfilesCollection();
        serverProfiles.replaceOne(profile.filterId(), profile.toDocument());
    }
}
