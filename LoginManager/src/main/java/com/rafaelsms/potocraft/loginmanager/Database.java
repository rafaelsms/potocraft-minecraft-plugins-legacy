package com.rafaelsms.potocraft.loginmanager;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.loginmanager.player.Profile;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class Database extends BaseDatabase {

    private static final int SEARCH_LIMIT = 6;

    private final @NotNull LoginManagerPlugin plugin;

    protected Database(@NotNull LoginManagerPlugin plugin) throws DatabaseException {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabase());
        this.plugin = plugin;
        getPlayerCollection().createIndex(Indexes.text(Profile.getPlayerNameField()),
                                          new IndexOptions().background(false));
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) throws DatabaseException {
        plugin.logger().warn("Database exception:", databaseException);
        throw databaseException;
    }

    private MongoCollection<Document> getPlayerCollection() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getMongoPlayerProfileCollection());
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
        });
    }

    public @NotNull Optional<Profile> getProfileCatching(@NotNull UUID playerId) {
        return catchingWrapper(() -> Util.convert(getPlayerCollection().find(Profile.filterId(playerId)).first(),
                                                  Profile::new));
    }

    public void saveProfileCatching(@NotNull Profile profile) {
        catchingWrapper(() -> {
            getPlayerCollection().replaceOne(profile.filterId(), profile.toDocument(), UPSERT);
        });
    }

    public @NotNull Optional<Profile> searchOfflineProfile(@NotNull String search) throws DatabaseException {
        // It'll search all players and match the closest name
        // Since there are no more than a few thousand, this should not be too heavy
        return throwingWrapper(() -> TextUtil.closestMatch(getPlayerCollection().find(),
                                                           document -> document.getString(Profile.getPlayerNameField()),
                                                           search).map(Profile::new));
    }

    public @NotNull Long getAddressUsageCount(InetSocketAddress remoteAddress) throws DatabaseException {
        return throwingWrapper(() -> getPlayerCollection().countDocuments(Profile.filterAddress(remoteAddress)));
    }
}
