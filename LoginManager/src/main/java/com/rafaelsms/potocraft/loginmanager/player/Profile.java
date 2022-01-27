package com.rafaelsms.potocraft.loginmanager.player;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.Util;
import com.velocitypowered.api.proxy.Player;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Profile extends DatabaseObject {

    private final @NotNull UUID playerId;

    /* Login information for offline players */
    private @Nullable Boolean loggedIn = null;
    private @Nullable ZonedDateTime lastLoginDate = null;
    private @Nullable String lastLoginAddress = null;
    private @Nullable Integer pin = null;

    /* Play time and useful information */
    private @NotNull String lastPlayerName;
    private @Nullable String lastServerName = null;
    private @Nullable ZonedDateTime firstJoinDate = null;
    private @Nullable ZonedDateTime lastJoinDate = null;
    private @Nullable ZonedDateTime lastQuitDate = null;
    private @Nullable Long playTime = null;

    /* Report incident history of player */
    private final @NotNull List<ReportEntry> reportEntries = new LinkedList<>();

    public Profile(@NotNull UUID playerId, @NotNull String playerName) {
        super();
        this.playerId = playerId;
        this.lastPlayerName = playerName;
    }

    public Profile(@NotNull Document document) {
        super(document);
        this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);

        this.loggedIn = document.getBoolean(Keys.LOGGED_IN);
        this.lastLoginDate = Util.convert(document.getString(Keys.LAST_LOGIN_DATE), Util::toDateTime);
        this.lastLoginAddress = document.getString(Keys.LAST_LOGIN_ADDRESS);
        this.pin = document.getInteger(Keys.PIN);

        this.lastPlayerName = document.getString(Keys.LAST_PLAYER_NAME);
        this.lastServerName = document.getString(Keys.LAST_SERVER_NAME);
        this.firstJoinDate = Util.convert(document.getString(Keys.FIRST_JOIN_DATE), Util::toDateTime);
        this.lastJoinDate = Util.convert(document.getString(Keys.LAST_JOIN_DATE), Util::toDateTime);
        this.lastQuitDate = Util.convert(document.getString(Keys.LAST_QUIT_DATE), Util::toDateTime);
        this.playTime = document.getLong(Keys.PLAY_TIME);

        List<Document> reportEntriesDocuments = document.getList(Keys.REPORT_ENTRIES, Document.class);
        this.reportEntries.addAll(Util.convertList(reportEntriesDocuments, ReportEntry::fromDocument));
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getLastPlayerName() {
        return lastPlayerName;
    }

    public @NotNull Optional<String> getLastServerName() {
        return Optional.ofNullable(lastServerName);
    }

    public Optional<ZonedDateTime> getLastJoinDate() {
        return Optional.ofNullable(lastJoinDate);
    }

    public Optional<ZonedDateTime> getLastQuitDate() {
        return Optional.ofNullable(lastQuitDate);
    }

    public Optional<Long> getPlayTime() {
        return Optional.ofNullable(playTime);
    }

    /**
     * Checks if player is logged in and sets logged in flag when auto login detected.
     *
     * @param address         player's current address
     * @param autoLoginWindow time window to auto login to work
     * @return true if player logged in using the same address or if player joined within auto login time using the same
     * address
     * @see com.rafaelsms.potocraft.loginmanager.util.Util#isPlayerLoggedIn(LoginManagerPlugin, Profile, Player) helper
     */
    public boolean isLoggedIn(@NotNull InetSocketAddress address, @NotNull Duration autoLoginWindow) {
        String ipAddress = TextUtil.getIpAddress(address);
        if (pin == null || loggedIn == null || !ipAddress.equalsIgnoreCase(lastLoginAddress)) {
            return false;
        }
        // We asserted that player has a pin and its IP is the same, so we continue the session if logged in
        if (loggedIn) {
            setLoggedIn(address);
            return true;
        }
        // If it isn't logged in, we check if last quit date is within auto login window
        ZonedDateTime loginExpiration = ZonedDateTime.now().minus(autoLoginWindow);
        if (lastQuitDate != null && this.lastQuitDate.isAfter(loginExpiration)) {
            // If it is, log in
            setLoggedIn(address);
            return true;
        }
        return false;
    }

    public void setLoggedIn(@NotNull InetSocketAddress address) {
        // Update logged in date and join date only if player is logged off
        if (this.loggedIn == null || !this.loggedIn) {
            this.loggedIn = true;
            this.lastLoginDate = ZonedDateTime.now();
            setJoinDate(lastPlayerName); // offline mode can't change names
        }
        this.lastLoginAddress = TextUtil.getIpAddress(address);
    }

    public boolean isPinValid(int pin) {
        return this.pin != null && pin == this.pin;
    }

    public boolean hasPin() {
        return pin != null && pin >= 0 && pin <= 999_999;
    }

    public boolean setPin(int pin) {
        if (pin < 0 || pin > 999_999) {
            return false;
        }
        this.pin = pin;
        return true;
    }

    public void setJoinDate(@NotNull String playerName) {
        ZonedDateTime now = ZonedDateTime.now();
        if (this.firstJoinDate == null) {
            this.firstJoinDate = now;
        }
        this.lastPlayerName = playerName;
        this.lastJoinDate = now;
    }

    public void setQuitDate(@Nullable String lastServerName) {
        if (this.loggedIn != null) {
            this.loggedIn = false;
        }
        this.lastQuitDate = ZonedDateTime.now();
        this.lastServerName = lastServerName;
        this.playTime = Util.getOrElse(playTime, 0L) +
                        Duration.between(Util.getOrElse(lastJoinDate, lastQuitDate), lastQuitDate).toMillis();
    }

    /**
     * Adds the report entry to the player profile without taking any action.
     *
     * @param type           type of report
     * @param reporterId     null if Console, reporter player Id otherwise
     * @param expirationDate nullable expiration date of the punishment
     * @param reason         nullable reason for report
     */
    public void addReportEntry(@NotNull ReportEntry.Type type,
                               @Nullable UUID reporterId,
                               @Nullable ZonedDateTime expirationDate,
                               @Nullable String reason) {
        this.reportEntries.add(new ReportEntry(type, reporterId, expirationDate, reason));
    }

    /**
     * List every report entry for the player without any filtering.
     *
     * @return a unmodifiable list
     */
    public List<ReportEntry> getReportEntries() {
        return Collections.unmodifiableList(reportEntries);
    }

    public static String getPlayerNameField() {
        return Keys.LAST_PLAYER_NAME;
    }

    public static String getIdField() {
        return Keys.PLAYER_ID;
    }

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
    }

    public static Bson filterAddress(@NotNull InetSocketAddress address) {
        return Filters.eq(Keys.LAST_LOGIN_ADDRESS, TextUtil.getIpAddress(address));
    }

    public Bson filterId() {
        return filterId(playerId);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));

        document.put(Keys.LOGGED_IN, loggedIn);
        document.put(Keys.LAST_LOGIN_DATE, Util.convert(lastLoginDate, Util::fromDateTime));
        document.put(Keys.LAST_LOGIN_ADDRESS, lastLoginAddress);
        document.put(Keys.PIN, pin);

        document.put(Keys.LAST_PLAYER_NAME, lastPlayerName);
        document.put(Keys.LAST_SERVER_NAME, lastServerName);
        document.put(Keys.FIRST_JOIN_DATE, Util.convert(firstJoinDate, Util::fromDateTime));
        document.put(Keys.LAST_JOIN_DATE, Util.convert(lastJoinDate, Util::fromDateTime));
        document.put(Keys.LAST_QUIT_DATE, Util.convert(lastQuitDate, Util::fromDateTime));
        document.put(Keys.PLAY_TIME, playTime);

        document.put(Keys.REPORT_ENTRIES, Util.convertList(reportEntries, ReportEntry::toDocument));
        return document;
    }

    private static final class Keys {

        public static final String PLAYER_ID = "_id";
        public static final String LOGGED_IN = "isLoggedIn";
        public static final String LAST_LOGIN_DATE = "lastLoginDate";
        public static final String LAST_LOGIN_ADDRESS = "lastLoginAddress";
        public static final String PIN = "pin";

        public static final String LAST_PLAYER_NAME = "lastPlayerName";
        public static final String LAST_SERVER_NAME = "lastServerName";
        public static final String FIRST_JOIN_DATE = "firstJoinDate";
        public static final String LAST_JOIN_DATE = "lastJoinDate";
        public static final String LAST_QUIT_DATE = "lastQuitDate";
        public static final String PLAY_TIME = "playTimeMillis";

        public static final String REPORT_ENTRIES = "reportEntries";

        // Private constructor
        private Keys() {
        }
    }
}
