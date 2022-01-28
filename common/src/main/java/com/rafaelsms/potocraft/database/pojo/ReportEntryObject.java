package com.rafaelsms.potocraft.database.pojo;

import org.bson.types.ObjectId;

import java.time.ZonedDateTime;
import java.util.Set;

public class ReportEntryObject {

    private String type;
    private ZonedDateTime reportDate;
    private ObjectId reporterId;
    private ZonedDateTime expirationDate;
    private String reason;
    private boolean active;

    public ReportEntryObject() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (!Type.values().contains(type)) {
            throw new IllegalArgumentException("Invalid type");
        }
        this.type = type;
    }

    public ZonedDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(ZonedDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public ObjectId getReporterId() {
        return reporterId;
    }

    public void setReporterId(ObjectId reporterId) {
        this.reporterId = reporterId;
    }

    public ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(ZonedDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static final class Type {

        public static final String BAN = "BAN";
        public static final String KICK = "KICK";
        public static final String MUTE = "MUTE";

        // Private constructor
        private Type() {
        }

        public static Set<String> values() {
            return Set.of(BAN, KICK, MUTE);
        }
    }
}
