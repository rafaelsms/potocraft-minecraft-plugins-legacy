package com.rafaelsms.potocraft.profile;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.Plugin;
import com.rafaelsms.potocraft.util.Converter;
import com.rafaelsms.potocraft.util.DatabaseObject;
import com.rafaelsms.potocraft.util.Location;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull Plugin plugin;
    private final @NotNull UUID playerId;

    private @NotNull String lastPlayerName;
    protected @Nullable ZonedDateTime lastLoginDate = null;
    protected @Nullable String lastLoginAddress = null;
    private @Nullable Integer pin = null;

    private final @NotNull ZonedDateTime firstJoinDate;
    private @NotNull ZonedDateTime lastJoinDate;
    private @Nullable ZonedDateTime lastQuitDate = null;
    private long playTime = 0;

    private @Nullable Location lastLocation = null;

    protected Profile(@NotNull Plugin plugin, @NotNull UUID playerId, @NotNull String playerName) {
        super(new Document());
        this.plugin = plugin;
        this.playerId = playerId;

        this.lastPlayerName = playerName;
        this.firstJoinDate = ZonedDateTime.now();
        this.lastJoinDate = ZonedDateTime.now();
    }

    public Profile(@NotNull Plugin plugin, @NotNull Document document) {
        super(document);
        this.plugin = plugin;
        this.playerId = Converter.toUUID(document.getString(Constants.PLAYER_KEY));

        setPIN(document.getInteger(Constants.PIN_KEY));

        this.lastPlayerName = document.getString(Constants.LAST_PLAYER_NAME_KEY);
        this.firstJoinDate = Objects.requireNonNull(Converter.toDateTime(document.getString(Constants.FIRST_JOIN_DATE_KEY)));
        this.lastJoinDate = Objects.requireNonNull(Converter.toDateTime(document.getString(Constants.LAST_JOIN_DATE_KEY)));
        this.lastQuitDate = Converter.toDateTime(document.getString(Constants.LAST_QUIT_DATE_KEY));
        this.playTime = document.getLong(Constants.PLAY_TIME_MILLIS_KEY);

        this.lastLocation = Converter.toLocation((Document) document.get(Constants.LAST_LOCATION_KEY));
    }

    public UUID getUniqueId() {
        return playerId;
    }

    public void updateLastPlayerName(@NotNull String username) {
        this.lastPlayerName = username;
    }

    /**
     * Update online mode player's join date
     */
    protected void updateJoinDate() {
        this.lastJoinDate = ZonedDateTime.now();
    }

    /**
     * Update online mode player's quit date
     */
    protected void updateQuitDate() {
        this.lastQuitDate = ZonedDateTime.now();
        this.playTime += Duration.between(this.lastJoinDate, this.lastQuitDate).toMillis();
    }

    public boolean hasPin() {
        return this.pin != null;
    }

    public boolean isValidPIN(int pin) {
        return this.pin != null && this.pin == pin;
    }

    public boolean setPIN(@Nullable Integer pin) {
        if (pin != null && (pin < 0 || pin > 999_999))
            return false;
        this.pin = pin;
        return true;
    }

    protected void setLoggedIn(@Nullable InetSocketAddress address) {
        ZonedDateTime now = ZonedDateTime.now();
        this.lastLoginDate = now;
        this.lastJoinDate = now;
        this.lastLoginAddress = address == null ? null : Util.getIpAddress(address);
    }

    public @NotNull Optional<Location> getLastLocation() {
        return Optional.ofNullable(lastLocation);
    }

    protected void setLastLocation(@Nullable Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Override
    public Document toDocument() {
        Document document = new Document();

        document.put(Constants.PLAYER_KEY, Converter.fromUUID(playerId));

        document.put(Constants.PIN_KEY, pin);

        document.put(Constants.FIRST_JOIN_DATE_KEY, Converter.fromDateTime(firstJoinDate));
        document.put(Constants.LAST_JOIN_DATE_KEY, Converter.fromDateTime(lastJoinDate));
        document.put(Constants.LAST_QUIT_DATE_KEY, Converter.fromDateTime(lastQuitDate));
        document.put(Constants.PLAY_TIME_MILLIS_KEY, playTime);

        document.put(Constants.LAST_LOCATION_KEY, Converter.fromLocation(lastLocation));

        return document;
    }

    public static Bson filterId(UUID playerId) {
        return Filters.eq(Constants.PLAYER_KEY, Converter.fromUUID(playerId));
    }

    public Bson filterId() {
        return Profile.filterId(this.playerId);
    }

    private static class Constants {

        public static final String PLAYER_KEY = "_id";

        public static final String PIN_KEY = "pin";

        public static final String LAST_PLAYER_NAME_KEY = "lastPlayerName";
        public static final String FIRST_JOIN_DATE_KEY = "firstJoinDate";
        public static final String LAST_JOIN_DATE_KEY = "lastJoinDate";
        public static final String LAST_QUIT_DATE_KEY = "lastQuitDate";
        public static final String PLAY_TIME_MILLIS_KEY = "playTimeMillis";

        public static final String LAST_LOCATION_KEY = "lastLocation";

    }
}
