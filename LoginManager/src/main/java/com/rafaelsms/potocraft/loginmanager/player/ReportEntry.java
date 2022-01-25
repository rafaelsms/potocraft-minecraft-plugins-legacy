package com.rafaelsms.potocraft.loginmanager.player;

import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ReportEntry extends DatabaseObject {

    private final @NotNull Type type;

    private final @NotNull ZonedDateTime date;
    private final @Nullable UUID reporterId;
    private final @Nullable ZonedDateTime expirationDate;
    private final @Nullable String reason;
    private boolean active = true;

    public ReportEntry(@NotNull Type type,
                       @Nullable UUID reporterId,
                       @Nullable ZonedDateTime expirationDate,
                       @Nullable String reason) {
        super();
        this.type = type;
        this.date = ZonedDateTime.now();
        this.reporterId = reporterId;
        this.expirationDate = expirationDate;
        this.reason = reason;
    }

    private ReportEntry(@NotNull Document document) {
        super(document);
        this.type = Util.convertNonNull(document.getString(Keys.TYPE), Type::valueOf);
        this.date = Util.convertNonNull(document.getString(Keys.DATE), Util::toDateTime);
        this.reporterId = Util.convert(document.getString(Keys.REPORTER_ID), UUID::fromString);
        this.expirationDate = Util.convert(document.getString(Keys.EXPIRATION_DATE), Util::toDateTime);
        this.reason = document.getString(Keys.REASON);
        this.active = document.getBoolean(Keys.IS_ACTIVE);
    }

    public @Nullable UUID getReporterId() {
        return reporterId;
    }

    public @Nullable String getReason() {
        return reason;
    }

    public @Nullable ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    public boolean isActive() {
        return active && (this.expirationDate == null || ZonedDateTime.now().isBefore(this.expirationDate));
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public @NotNull String getType() {
        return type.name().toLowerCase();
    }

    public boolean isPreventingJoin() {
        return type.isPreventingJoin() && isActive();
    }

    public boolean isPreventingChat() {
        return type.isPreventingChat() && isActive();
    }

    public static @NotNull ReportEntry fromDocument(@NotNull Document document) {
        return new ReportEntry(document);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.TYPE, Util.convertNonNull(type, Type::name));
        document.put(Keys.DATE, Util.convertNonNull(date, Util::fromDateTime));
        document.put(Keys.REPORTER_ID, Util.convert(reporterId, UUID::toString));
        document.put(Keys.EXPIRATION_DATE, Util.convert(expirationDate, Util::fromDateTime));
        document.put(Keys.REASON, reason);
        document.put(Keys.IS_ACTIVE, active);
        return document;
    }

    private static final class Keys {

        public static final String TYPE = "entryType";
        public static final String DATE = "incidentDate";
        public static final String REPORTER_ID = "reporterPlayerId";
        public static final String EXPIRATION_DATE = "expirationDate";
        public static final String REASON = "reportReason";
        public static final String IS_ACTIVE = "isActive";

        private Keys() {
        }
    }

    public enum Type {
        KICK(false, false),
        MUTE(false, true),
        BAN(true, true),
        ;

        private final boolean preventsJoin;
        private final boolean preventsChat;

        Type(boolean preventsJoin, boolean preventsChat) {
            this.preventsJoin = preventsJoin;
            this.preventsChat = preventsChat;
        }

        public boolean isPreventingJoin() {
            return preventsJoin;
        }

        public boolean isPreventingChat() {
            return preventsChat;
        }
    }
}
