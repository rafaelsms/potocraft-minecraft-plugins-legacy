package com.rafaelsms.potocraft.common.profile;

import com.rafaelsms.potocraft.common.database.Converter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.UUID;

public class TimedReportEntry extends ReportEntry {

    private final @Nullable ZonedDateTime expirationDate;
    private boolean active = true;

    public TimedReportEntry(@NotNull ReportType reportType,
                            @Nullable UUID reporterId,
                            @Nullable ZonedDateTime expirationDate,
                            @Nullable String reason) {
        super(reportType, reporterId, reason);
        if (reportType == ReportType.KICK_REPORT) {
            throw new IllegalArgumentException("Kick reports are not timed");
        }
        this.expirationDate = expirationDate;
    }

    public TimedReportEntry(@NotNull ReportType reportType, @NotNull Document document) {
        super(reportType, document);
        if (reportType == ReportType.KICK_REPORT) {
            throw new IllegalArgumentException("Kick reports are not timed");
        }
        this.expirationDate = Converter.toDateTime(document.getString(Constants.EXPIRATION_DATE_KEY));
        this.active = document.getBoolean(Constants.REPORT_ACTIVE_KEY);
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

    @Override
    public boolean isPreventsJoining() {
        return super.isPreventsJoining() && isActive();
    }

    @Override
    public boolean isPreventsChatting() {
        return super.isPreventsChatting() && isActive();
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = super.toDocument();
        document.put(Constants.EXPIRATION_DATE_KEY, Converter.fromDateTime(expirationDate));
        document.put(Constants.REPORT_ACTIVE_KEY, active);
        return document;
    }
}
