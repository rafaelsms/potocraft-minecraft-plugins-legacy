package com.rafaelsms.potocraft.serverprofile;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.database.BaseDatabase;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.serverprofile.players.Profile;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Database extends BaseDatabase {

    private final @NotNull ServerProfilePlugin plugin;

    public Database(@NotNull ServerProfilePlugin plugin) {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabaseName());
        this.plugin = plugin;
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) throws DatabaseException {
        plugin.logger().warn("Database exception:", databaseException);
        if (plugin.getConfiguration().isMongoDatabaseExceptionFatal()) {
            plugin.getServer().shutdown();
        }
        throw databaseException;
    }

    private @NotNull MongoCollection<Document> getPlayerProfiles() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getMongoPlayerProfileCollectionName());
    }

    public List<Profile> getProfileRanking() throws DatabaseException {
        return throwingWrapper(() -> {
            List<Profile> profiles = new LinkedList<>();
            FindIterable<Document> documents = getPlayerProfiles().find().sort(Profile.rankingSort()).limit(7);
            for (Document document : documents) {
                profiles.add(new Profile(document));
            }
            return profiles;
        });
    }

    public @NotNull Optional<Profile> loadProfile(@NotNull UUID playerId) throws DatabaseException {
        return throwingWrapper(() -> {
            Document playerProfileDocument = getPlayerProfiles().find(Profile.filterId(playerId)).first();
            if (playerProfileDocument == null) {
                return Optional.empty();
            }
            return Optional.of(new Profile(playerProfileDocument));
        });
    }

    public void saveProfileCatching(@NotNull Profile profile) {
        catchingWrapper(() -> {
            getPlayerProfiles().replaceOne(profile.filterId(), profile.toDocument(), UPSERT);
        });
    }

    private @NotNull MongoCollection<Document> getWarpsCollection() throws DatabaseException {
        return getClient().getCollection(plugin.getConfiguration().getMongoWarpsCollectionName());
    }

    public @NotNull Optional<List<Warp>> getWarps() {
        return catchingWrapper(() -> {
            List<Warp> warps = new ArrayList<>();
            for (Document document : getWarpsCollection().find()) {
                warps.add(new Warp(document));
            }
            return warps;
        });
    }

    public void deleteWarp(@NotNull String warpName) throws DatabaseException {
        throwingWrapper(() -> {
            getWarpsCollection().deleteOne(Warp.filterName(warpName));
        });
    }

    public void replaceWarp(@NotNull Warp warp) throws DatabaseException {
        throwingWrapper(() -> {
            getWarpsCollection().replaceOne(warp.filterName(), warp.toDocument(), UPSERT);
        });
    }

    public @NotNull Optional<Profile> searchOfflineProfile(@NotNull String search) throws DatabaseException {
        // It'll search all players and match the closest name
        // Since there are no more than a few thousand, this should not be too heavy
        return throwingWrapper(() -> TextUtil.closestMatch(getPlayerProfiles().find(),
                                                           document -> document.getString(Profile.getPlayerNameField()),
                                                           search).map(Profile::new));
    }
}
