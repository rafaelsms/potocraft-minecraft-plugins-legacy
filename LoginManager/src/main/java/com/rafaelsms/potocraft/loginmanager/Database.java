package com.rafaelsms.potocraft.loginmanager;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Database extends com.rafaelsms.potocraft.database.Database {

    private static final int SEARCH_LIMIT = 6;

    private final @NotNull LoginManagerPlugin plugin;

    protected Database(@NotNull LoginManagerPlugin plugin) throws DatabaseException {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabase());
        this.plugin = plugin;
        getPlayerCollection().createIndex(Indexes.text(Profile.getLastPlayerNameField()),
                                          new IndexOptions().background(false));
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) throws DatabaseException {
        plugin.getLogger().warn("Database exception:", databaseException);
        if (plugin.getConfiguration().isMongoDatabaseFailureFatal()) {
            plugin.getServer().shutdown();
        }
        throw databaseException;
    }

    private MongoCollection<Document> getPlayerCollection() throws DatabaseException {
        return getDatabase().getCollection(plugin.getConfiguration().getMongoPlayerProfileCollection());
    }

    public @NotNull Optional<Profile> getProfile(@NotNull UUID playerId) throws DatabaseException {
        return throwingWrapper(() -> {
            Document profileDocument = getPlayerCollection().find(Profile.filterId(playerId)).first();
            if (profileDocument == null) {
                return Optional.empty();
            }
            return Optional.of(new Profile(profileDocument));
        });
    }

    public void saveProfile(@NotNull Profile profile) throws DatabaseException {
        throwingWrapper(() -> {
            getPlayerCollection().replaceOne(profile.filterId(), profile.toDocument(), UPSERT);
            return null;
        });
    }

    public @NotNull Optional<Profile> getProfileCatching(@NotNull UUID playerId) {
        return catchingWrapper(() -> {
            Document profileDocument = getPlayerCollection().find(Profile.filterId(playerId)).first();
            if (profileDocument == null) {
                return null;
            }
            return new Profile(profileDocument);
        });
    }

    public void saveProfileCatching(@NotNull Profile profile) {
        catchingWrapper(() -> {
            getPlayerCollection().replaceOne(profile.filterId(), profile.toDocument(), UPSERT);
            return null;
        });
    }

    public @NotNull List<Profile> getOfflineProfiles(@NotNull String usernameRegex) throws DatabaseException {
        return throwingWrapper(() -> {
            ArrayList<Profile> profiles = new ArrayList<>(SEARCH_LIMIT);
            FindIterable<Document> documents = getPlayerCollection()
                    .find(Filters.regex(Profile.getLastPlayerNameField(), usernameRegex, "i"))
                    .limit(SEARCH_LIMIT);
            for (Document document : documents) {
                Profile profile = new Profile(document);
                // If it is the exact name, return it
                if (profile.getLastPlayerName().equalsIgnoreCase(usernameRegex)) {
                    return List.of(profile);
                }
                profiles.add(profile);
            }
            return profiles;
        });
    }

    public @NotNull Long getAddressUsageCount(InetSocketAddress remoteAddress) throws DatabaseException {
        return throwingWrapper(() -> getPlayerCollection().countDocuments(Profile.filterAddress(remoteAddress)));
    }
}
