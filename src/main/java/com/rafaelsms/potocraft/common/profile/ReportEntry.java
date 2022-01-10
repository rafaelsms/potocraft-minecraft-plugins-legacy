package com.rafaelsms.potocraft.common.profile;

import com.rafaelsms.potocraft.common.database.Converter;
import com.rafaelsms.potocraft.common.database.DatabaseObject;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class ReportEntry extends DatabaseObject {

    private final @NotNull ReportType reportType;

    protected final @Nullable UUID reporterId;
    protected final @NotNull ZonedDateTime reportDate;
    protected final @Nullable String reason;

    protected ReportEntry(@NotNull ReportType reportType, @Nullable UUID reporterId, @Nullable String reason) {
        super(new Document());
        this.reportType = reportType;
        this.reporterId = reporterId;
        this.reportDate = ZonedDateTime.now();
        this.reason = reason;
    }

    protected ReportEntry(@NotNull ReportType reportType, @NotNull Document document) {
        super(document);
        this.reportType = reportType;
        this.reporterId = Converter.toUUID(document.getString(Constants.REPORTER_KEY));
        this.reportDate = Objects.requireNonNull(Converter.toDateTime(document.getString(Constants.REPORT_DATE_KEY)));
        this.reason = document.getString(Constants.REPORT_REASON_KEY);
    }

    public static @NotNull ReportEntry fromDocument(@NotNull Document document) {
        return ReportType.fromString(document.getString(Constants.REPORT_TYPE_KEY)).getConverter().apply(document);
    }

    public static @NotNull ReportEntry kicked(@Nullable UUID reporterId, @Nullable String reason) {
        return new ReportEntry(ReportType.KICK_REPORT, reporterId, reason);
    }

    public static @NotNull TimedReportEntry banned(@Nullable UUID reporterId,
                                                   @Nullable String reason,
                                                   @Nullable ZonedDateTime expirationDate) {
        return new TimedReportEntry(ReportType.BAN_REPORT, reporterId, expirationDate, reason);
    }

    public static @NotNull TimedReportEntry muted(@Nullable UUID reporterId,
                                                  @Nullable String reason,
                                                  @Nullable ZonedDateTime expirationDate) {
        return new TimedReportEntry(ReportType.MUTE_REPORT, reporterId, expirationDate, reason);
    }

    public @Nullable String getReason() {
        return reason;
    }

    public boolean isPreventsJoining() {
        return reportType.isPreventsJoining();
    }

    public boolean isPreventsChatting() {
        return reportType.isPreventsChatting();
    }

    public Component getMessage(@NotNull VelocityPlugin plugin) {
        return ReportType.getMessage(plugin, this);
    }

    public Component getHistoryMessage(@NotNull VelocityPlugin plugin) {
        return ReportType.getHistoryMessage(plugin, this);
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        document.put(Constants.REPORT_TYPE_KEY, reportType.toString());
        document.put(Constants.REPORTER_KEY, Converter.fromUUID(reporterId));
        document.put(Constants.REPORT_DATE_KEY, Converter.fromDateTime(reportDate));
        document.put(Constants.REPORT_REASON_KEY, reason);
        return document;
    }

    protected static class Constants {

        public static final String REPORT_TYPE_KEY = "reportType";
        public static final String REPORTER_KEY = "reporterId";
        public static final String REPORT_REASON_KEY = "reason";
        public static final String REPORT_DATE_KEY = "reportDate";
        public static final String EXPIRATION_DATE_KEY = "expirationDate";
        public static final String REPORT_ACTIVE_KEY = "isActive";

    }

    protected enum ReportType {

        BAN_REPORT(true, true),
        MUTE_REPORT(false, true),
        KICK_REPORT(false, false),
        ;

        private final boolean preventsJoining;
        private final boolean preventsChatting;

        ReportType(boolean preventsJoining, boolean preventsChatting) {
            this.preventsJoining = preventsJoining;
            this.preventsChatting = preventsChatting;
        }

        private @NotNull Function<Document, ? extends ReportEntry> getConverter() {
            return switch (this) {
                case BAN_REPORT -> document -> new TimedReportEntry(BAN_REPORT, document);
                case MUTE_REPORT -> document -> new TimedReportEntry(MUTE_REPORT, document);
                case KICK_REPORT -> document -> new ReportEntry(KICK_REPORT, document);
            };
        }

        public boolean isPreventsChatting() {
            return preventsChatting;
        }

        public boolean isPreventsJoining() {
            return preventsJoining;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public static ReportType fromString(@NotNull String string) {
            for (ReportType reportType : values()) {
                if (reportType.name().equalsIgnoreCase(string)) {
                    return reportType;
                }
            }
            throw new IllegalStateException("Cannot find report type for \"%s\"".formatted(string));
        }

        public static Component getMessage(@NotNull VelocityPlugin plugin, @NotNull ReportEntry reportEntry) {
            Component reporter = getReporterComponent(plugin, reportEntry);
            Component reason = getReasonComponent(plugin, reportEntry);
            Component expirationDate = getExpirationDateComponent(plugin, reportEntry);

            return switch (reportEntry.reportType) {
                case BAN_REPORT -> plugin.getSettings().getKickMessageBanned(reporter, reason, expirationDate);
                case KICK_REPORT -> plugin.getSettings().getKickMessageKicked(reporter, reason);
                case MUTE_REPORT -> plugin
                        .getSettings()
                        .getCommandReportYouHaveBeenMuted(reporter, reason, expirationDate);
            };
        }

        public static Component getHistoryMessage(@NotNull VelocityPlugin plugin, @NotNull ReportEntry reportEntry) {
            Component reporter = getReporterComponent(plugin, reportEntry);
            Component reason = getReasonComponent(plugin, reportEntry);
            Component expirationDate = getExpirationDateComponent(plugin, reportEntry);

            return switch (reportEntry.reportType) {
                case BAN_REPORT -> plugin.getSettings().getReportHistoryEntryBanned(reporter, reason, expirationDate);
                case KICK_REPORT -> plugin.getSettings().getReportHistoryEntryKicked(reporter, reason);
                case MUTE_REPORT -> plugin.getSettings().getReportHistoryEntryMuted(reporter, reason, expirationDate);
            };
        }

        private static Component getReasonComponent(@NotNull VelocityPlugin plugin, @NotNull ReportEntry reportEntry) {
            Component reason = plugin.getSettings().getCommandReportUnknownReason();
            if (reportEntry.getReason() != null) {
                reason = Component.text(reportEntry.getReason());
            }
            return reason;
        }

        private static Component getReporterComponent(@NotNull VelocityPlugin plugin,
                                                      @NotNull ReportEntry reportEntry) {
            Component reporter = plugin.getSettings().getUnknownPlayerName();
            if (reportEntry.reporterId == null) {
                reporter = plugin.getSettings().getConsoleName();
            } else {
                try {
                    VelocityProfile profile = plugin.getDatabase().getProfile(reportEntry.reporterId).orElseThrow();
                    reporter = Component.text(profile.getLastPlayerName());
                } catch (Exception ignored) {
                }
            }
            return reporter;
        }

        private static Component getExpirationDateComponent(@NotNull VelocityPlugin plugin,
                                                            @NotNull ReportEntry reportEntry) {
            Component expirationDate = plugin.getSettings().getCommandReportNoExpirationDate();
            if (reportEntry instanceof TimedReportEntry timedReportEntry &&
                timedReportEntry.getExpirationDate() != null) {
                String dateTime =
                        plugin.getSettings().getDateTimeFormatter().format(timedReportEntry.getExpirationDate());
                expirationDate = Component.text(dateTime);
            }
            return expirationDate;
        }
    }
}
