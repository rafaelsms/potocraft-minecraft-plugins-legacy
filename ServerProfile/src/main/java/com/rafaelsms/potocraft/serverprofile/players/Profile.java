package com.rafaelsms.potocraft.serverprofile.players;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.rafaelsms.potocraft.database.IdentifiedDatabaseObject;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Profile extends IdentifiedDatabaseObject {

    private @Nullable ZonedDateTime lastTotemUsage = null;

    public List<Home> getHomesSortedByDate() {
        List<Home> sortedHomes = new LinkedList<>(homes.values());
        sortedHomes.sort(Comparator.comparing(Home::getCreationDate));
        return Collections.unmodifiableList(sortedHomes);
    }

    public Optional<Home> getHome(@NotNull String name) {
        return Optional.ofNullable(homes.get(name.toLowerCase()));
    }

    public int getHomesSize() {
        return homes.size();
    }

    public boolean addHome(@NotNull String name, @NotNull Location location) {
        return homes.putIfAbsent(name.toLowerCase(), new Home(name, location)) == null;
    }

    public boolean removeHome(@NotNull String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public @NotNull Optional<ZonedDateTime> getLastTeleportDate() {
        return Optional.ofNullable(lastTeleportDate);
    }

    public void setLastTeleportDate() {
        this.lastTeleportDate = ZonedDateTime.now();
    }

    public @NotNull Optional<Location> getBackLocation(@NotNull ServerProfilePlugin plugin) {
        return Optional.ofNullable(backLocation).flatMap(locationField -> locationField.toLocation(plugin));
    }

    public void setBackLocation(@Nullable Location location) {
        this.backLocation = Util.convert(location, CachedLocationField::new);
    }

    public Optional<ZonedDateTime> getDeathDateTime() {
        return Optional.ofNullable(deathDateTime);
    }

    public @NotNull Optional<Location> getDeathLocation(@NotNull ServerProfilePlugin plugin) {
        return Optional.ofNullable(deathLocation).flatMap(locationField -> locationField.toLocation(plugin));
    }

    public void setDeathLocation(@Nullable Location location) {
        this.deathLocation = Util.convert(location, CachedLocationField::new);
        this.deathDateTime = ZonedDateTime.now();
    }

    public void setQuitTime() {
        ZonedDateTime now = ZonedDateTime.now();
        this.playTimeMillis += Duration.between(lastJoinDateTime, now).toMillis();
    }

    public void incrementMobKill() {
        this.mobKills += 1;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void incrementPlayerKill() {
        this.playerKills += 1;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void incrementDeathCount() {
        this.deathCount += 1;
    }

    public void incrementBlocksPlaced() {
        this.blocksPlaced += 1;
    }

    public void incrementBlocksBroken() {
        this.blocksBroken += 1;
    }

    public void incrementExperience(int experience) {
        this.experiencePickedUp += experience;
    }

    public void incrementTotemUsage() {
        this.totemUsages += 1;
    }

    public Optional<ZonedDateTime> getLastTotemUsedDate() {
        return Optional.ofNullable(lastTotemUsage);
    }

    public void setTotemUsage() {
        this.lastTotemUsage = ZonedDateTime.now();
    }

    public boolean isImportedEssentials() {
        return importedEssentials;
    }

    public void setImportedEssentials() {
        this.importedEssentials = true;
    }

    public static Bson rankingSort() {
        return Sorts.orderBy(Sorts.descending(Keys.PLAYER_KILLS), Sorts.ascending(Keys.DEATH_COUNT));
    }

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
    }

    public static String getPlayerNameField() {
        return Keys.PLAYER_NAME;
    }

    public Bson filterId() {
        return filterId(playerId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Profile profile = (Profile) o;
        return playerId.equals(profile.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(this.playerId, UUID::toString));
        document.put(Keys.PLAYER_NAME, this.playerName);

        document.put(Keys.HOMES, Util.convertList(this.homes.values(), Home::toDocument));

        document.put(Keys.LAST_TELEPORT_DATE, Util.fromDateTime(this.lastTeleportDate));
        document.put(Keys.BACK_LOCATION, Util.convert(backLocation, CachedLocationField::toDocument));
        document.put(Keys.DEATH_DATE_TIME, Util.convert(deathDateTime, Util::fromDateTime));
        document.put(Keys.DEATH_LOCATION, Util.convert(deathLocation, CachedLocationField::toDocument));
        document.put(Keys.TOTEM_USED_DATE, Util.convert(lastTotemUsage, Util::fromDateTime));

        document.put(Keys.LAST_JOIN_DATE, Util.convertNonNull(lastJoinDateTime, Util::fromDateTime));
        document.put(Keys.PLAY_TIME_MILLIS, playTimeMillis);

        document.put(Keys.MOB_KILLS, mobKills);
        document.put(Keys.PLAYER_KILLS, playerKills);
        document.put(Keys.DEATH_COUNT, deathCount);
        document.put(Keys.BLOCKS_PLACED, blocksPlaced);
        document.put(Keys.BLOCKS_BROKEN, blocksBroken);
        document.put(Keys.EXPERIENCE_PICKED_UP, experiencePickedUp);
        document.put(Keys.TOTEM_USAGES, totemUsages);

        document.put(Keys.IMPORTED_ESSENTIALS, importedEssentials);
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String PLAYER_NAME = "playerName";

        public static final String HOMES = "homeList";

        public static final String LAST_TELEPORT_DATE = "lastTeleportDate";
        public static final String BACK_LOCATION = "backLocation";
        public static final String DEATH_DATE_TIME = "deathDateTime";
        public static final String DEATH_LOCATION = "deathLocation";
        public static final String TOTEM_USED_DATE = "lastTotemUsedDate";

        public static final String LAST_JOIN_DATE = "lastJoinDate";
        public static final String PLAY_TIME_MILLIS = "playTimeMillis";

        public static final String MOB_KILLS = "mobKillCount";
        public static final String PLAYER_KILLS = "playerKillCount";
        public static final String DEATH_COUNT = "deathCount";
        public static final String BLOCKS_PLACED = "placedBlocks";
        public static final String BLOCKS_BROKEN = "brokenBlocks";
        public static final String EXPERIENCE_PICKED_UP = "experiencePickedUp";
        public static final String TOTEM_USAGES = "totemUsages";

        public static final String IMPORTED_ESSENTIALS = "importedEssentials";

        // Private constructor
        private Keys() {
        }
    }
}
