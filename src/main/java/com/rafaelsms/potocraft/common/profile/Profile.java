package com.rafaelsms.potocraft.common.profile;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.common.Plugin;
import com.rafaelsms.potocraft.common.util.Converter;
import com.rafaelsms.potocraft.common.util.DatabaseObject;
import com.rafaelsms.potocraft.common.util.Location;
import com.rafaelsms.potocraft.common.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class Profile extends DatabaseObject {

    private final @NotNull Plugin plugin;
    private final @NotNull UUID playerId;

    protected @Nullable ZonedDateTime lastLoginDate = null;
    protected @Nullable String lastLoginAddress = null;
    private @Nullable Integer pin = null;

    private @NotNull String lastPlayerName;
    private final @NotNull ZonedDateTime firstJoinDate;
    private @NotNull ZonedDateTime lastJoinDate;
    private @Nullable ZonedDateTime lastQuitDate = null;
    private long playTime = 0;

    private final List<ReportEntry> reportEntries = new LinkedList<>();
    private final Set<UUID> ignoredPlayers = new HashSet<>();

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
        this.playerId = Converter.toUUID(document.getString(Constants.PLAYER_ID_KEY));

        setPIN(document.getInteger(Constants.PIN_KEY));

        this.lastPlayerName = document.getString(Constants.LAST_PLAYER_NAME_KEY);
        this.firstJoinDate =
                Objects.requireNonNull(Converter.toDateTime(document.getString(Constants.FIRST_JOIN_DATE_KEY)));
        this.lastJoinDate =
                Objects.requireNonNull(Converter.toDateTime(document.getString(Constants.LAST_JOIN_DATE_KEY)));
        this.lastQuitDate = Converter.toDateTime(document.getString(Constants.LAST_QUIT_DATE_KEY));
        this.playTime = document.getLong(Constants.PLAY_TIME_MILLIS_KEY);

        List<Document> reportEntries = document.getList(Constants.REPORT_ENTRIES_KEY, Document.class);
        this.reportEntries.addAll(Converter.fromList(reportEntries, ReportEntry::fromDocument));
        List<UUID> ignoredPlayers =
                Converter.fromDocumentList(document, Constants.IGNORED_PLAYERS_KEY, Converter::toUUID, String.class);
        this.ignoredPlayers.addAll(ignoredPlayers);

        this.lastLocation = Converter.toLocation((Document) document.get(Constants.LAST_LOCATION_KEY));
    }

    public @NotNull UUID getUniqueId() {
        return playerId;
    }

    public @NotNull String getLastPlayerName() {
        return lastPlayerName;
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
        if (pin != null && (pin < 0 || pin > 999_999)) {
            return false;
        }
        this.pin = pin;
        return true;
    }

    protected void setLoggedIn(@Nullable InetSocketAddress address) {
        ZonedDateTime now = ZonedDateTime.now();
        this.lastLoginDate = now;
        this.lastJoinDate = now;
        this.lastLoginAddress = address == null ? null : Util.getIpAddress(address);
    }

    public List<ReportEntry> getReportEntries() {
        return Collections.unmodifiableList(this.reportEntries);
    }

    public Optional<ReportEntry> getJoinPreventingReport() {
        for (ReportEntry reportEntry : reportEntries) {
            if (!reportEntry.isPreventsJoining()) {
                continue;
            }
            return Optional.of(reportEntry);
        }
        return Optional.empty();
    }

    public Optional<ReportEntry> getChatPreventingReport() {
        for (ReportEntry reportEntry : reportEntries) {
            if (!reportEntry.isPreventsChatting()) {
                continue;
            }
            return Optional.of(reportEntry);
        }
        return Optional.empty();
    }

    public ReportEntry kicked(@Nullable UUID reporterId, @Nullable String reason) {
        ReportEntry reportEntry = ReportEntry.kicked(reporterId, reason);
        this.reportEntries.add(reportEntry);
        return reportEntry;
    }

    public TimedReportEntry muted(@Nullable UUID reporterId,
                                  @Nullable String reason,
                                  @Nullable ZonedDateTime expirationDate) {
        TimedReportEntry timedReportEntry = ReportEntry.muted(reporterId, reason, expirationDate);
        this.reportEntries.add(timedReportEntry);
        return timedReportEntry;
    }

    public TimedReportEntry banned(@Nullable UUID reporterId,
                                   @Nullable String reason,
                                   @Nullable ZonedDateTime expirationDate) {
        TimedReportEntry timedReportEntry = ReportEntry.banned(reporterId, reason, expirationDate);
        this.reportEntries.add(timedReportEntry);
        return timedReportEntry;
    }

    public Set<UUID> getIgnoredPlayers() {
        return Collections.unmodifiableSet(this.ignoredPlayers);
    }

    public boolean ignorePlayer(@NotNull UUID ignoredPlayer) {
        return this.ignoredPlayers.add(ignoredPlayer);
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

        document.put(Constants.PLAYER_ID_KEY, Converter.fromUUID(playerId));

        document.put(Constants.PIN_KEY, pin);

        document.put(Constants.LAST_PLAYER_NAME_KEY, lastPlayerName);
        document.put(Constants.FIRST_JOIN_DATE_KEY, Converter.fromDateTime(firstJoinDate));
        document.put(Constants.LAST_JOIN_DATE_KEY, Converter.fromDateTime(lastJoinDate));
        document.put(Constants.LAST_QUIT_DATE_KEY, Converter.fromDateTime(lastQuitDate));
        document.put(Constants.PLAY_TIME_MILLIS_KEY, playTime);

        document.put(Constants.REPORT_ENTRIES_KEY, Converter.toList(this.reportEntries, ReportEntry::toDocument));
        Converter.toDocumentList(document, Constants.IGNORED_PLAYERS_KEY, this.ignoredPlayers, Converter::fromUUID);

        document.put(Constants.LAST_LOCATION_KEY, Converter.fromLocation(lastLocation));

        return document;
    }

    public Bson filterId() {
        return Profile.filterId(this.playerId);
    }

    public static Bson filterId(@NotNull UUID playerId) {
        return Filters.eq(Constants.PLAYER_ID_KEY, Converter.fromUUID(playerId));
    }

    public static String lastNameField() {
        return Constants.LAST_PLAYER_NAME_KEY;
    }

    private static class Constants {

        public static final String PLAYER_ID_KEY = "_id";

        public static final String PIN_KEY = "pin";

        public static final String LAST_PLAYER_NAME_KEY = "lastPlayerName";
        public static final String FIRST_JOIN_DATE_KEY = "firstJoinDate";
        public static final String LAST_JOIN_DATE_KEY = "lastJoinDate";
        public static final String LAST_QUIT_DATE_KEY = "lastQuitDate";
        public static final String PLAY_TIME_MILLIS_KEY = "playTimeMillis";

        public static final String REPORT_ENTRIES_KEY = "reportEntries";
        public static final String IGNORED_PLAYERS_KEY = "ignoredPlayers";

        public static final String LAST_LOCATION_KEY = "lastLocation";

    }
}
