package com.rafaelsms.potocraft.serverprofile.players;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;

    private final Map<String, Home> homes = Collections.synchronizedMap(new HashMap<>());

    private @Nullable ZonedDateTime lastTeleportDate = null;
    private @Nullable StoredLocation backLocation = null;
    private @Nullable ZonedDateTime deathDateTime = null;
    private @Nullable StoredLocation deathLocation = null;
    private @Nullable ZonedDateTime lastTotemUsage = null;

    private final @NotNull ZonedDateTime lastJoinDateTime = ZonedDateTime.now();
    private long playTimeMillis = 0;

    private int mobKills = 0;
    private int playerKills = 0;
    private int deathCount = 0;
    private int blocksPlaced = 0;
    private int blocksBroken = 0;
    private long experiencePickedUp = 0;
    private int totemUsages = 0;

    public Profile(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    public Profile(@NotNull Document document) {
        super(document);
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);

        List<Home> homes = Util.convertList(document.getList(Keys.HOMES, Document.class), Home::new);
        for (Home home : homes) {
            this.homes.put(home.getName().toLowerCase(), home);
        }

        this.lastTeleportDate = Util.toDateTime(document.getString(Keys.LAST_TELEPORT_DATE));
        this.backLocation = Util.convert(document.get(Keys.BACK_LOCATION, Document.class), StoredLocation::new);
        this.deathDateTime = Util.convert(document.getString(Keys.DEATH_DATE_TIME), Util::toDateTime);
        this.deathLocation = Util.convert(document.get(Keys.DEATH_LOCATION, Document.class), StoredLocation::new);
        this.lastTotemUsage = Util.convert(document.getString(Keys.TOTEM_USED_DATE), Util::toDateTime);

        this.playTimeMillis = Util.getCatchingOrElse(() -> document.getLong(Keys.PLAY_TIME_MILLIS), 0L);

        this.mobKills = Util.getCatchingOrElse(() -> document.getInteger(Keys.MOB_KILLS), 0);
        this.playerKills = Util.getCatchingOrElse(() -> document.getInteger(Keys.PLAYER_KILLS), 0);
        this.deathCount = Util.getCatchingOrElse(() -> document.getInteger(Keys.DEATH_COUNT), 0);
        this.blocksPlaced = Util.getCatchingOrElse(() -> document.getInteger(Keys.BLOCKS_PLACED), 0);
        this.blocksBroken = Util.getCatchingOrElse(() -> document.getInteger(Keys.BLOCKS_BROKEN), 0);
        this.experiencePickedUp = Util.getCatchingOrElse(() -> document.getLong(Keys.EXPERIENCE_PICKED_UP), 0L);
        this.totemUsages = Util.getCatchingOrElse(() -> document.getInteger(Keys.TOTEM_USAGES), 0);
    }

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
        return Optional.ofNullable(Util.convert(backLocation, storedLocation -> storedLocation.toLocation(plugin)));
    }

    public void setBackLocation(@Nullable Location location) {
        this.backLocation = Util.convert(location, StoredLocation::new);
    }

    public @NotNull Optional<Location> getDeathLocation(@NotNull ServerProfilePlugin plugin) {
        return Optional.ofNullable(Util.convert(deathLocation, storedLocation -> storedLocation.toLocation(plugin)));
    }

    public void setDeathLocation(@Nullable Location location) {
        this.deathLocation = Util.convert(location, StoredLocation::new);
        this.deathDateTime = ZonedDateTime.now();
    }

    public void setQuitTime() {
        ZonedDateTime now = ZonedDateTime.now();
        this.playTimeMillis += Duration.between(lastJoinDateTime, now).toMillis();
    }

    public void incrementMobKill() {
        this.mobKills += 1;
    }

    public void incrementPlayerKill() {
        this.playerKills += 1;
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

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
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

        document.put(Keys.HOMES, Util.convertList(this.homes.values(), Home::toDocument));

        document.put(Keys.LAST_TELEPORT_DATE, Util.fromDateTime(this.lastTeleportDate));
        document.put(Keys.BACK_LOCATION, Util.convert(backLocation, StoredLocation::toDocument));
        document.put(Keys.DEATH_DATE_TIME, Util.convert(deathDateTime, Util::fromDateTime));
        document.put(Keys.DEATH_LOCATION, Util.convert(deathLocation, StoredLocation::toDocument));
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
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";

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

        // Private constructor
        private Keys() {
        }
    }
}
