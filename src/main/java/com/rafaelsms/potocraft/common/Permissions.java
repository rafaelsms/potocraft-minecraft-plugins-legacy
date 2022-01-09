package com.rafaelsms.potocraft.common;

import org.jetbrains.annotations.NotNull;

public final class Permissions {

    public static final @NotNull String LOGIN_COMMAND = "potocraft.proxy_command.login";
    public static final @NotNull String CHANGE_PIN_COMMAND = "potocraft.proxy_command.change_pin";
    public static final @NotNull String CHANGE_PIN_COMMAND_OTHERS = "potocraft.proxy_command.change_pin.others"; // TODO
    public static final @NotNull String REGISTER_COMMAND = "potocraft.proxy_command.register";
    public static final @NotNull String REGISTER_COMMAND_OTHERS = "potocraft.proxy_command.register.others"; // TODO
    public static final @NotNull String REPORT_COMMAND = "potocraft.proxy_command.report";
    public static final @NotNull String REPORT_COMMAND_KICK = "potocraft.proxy_command.report.kick";
    public static final @NotNull String REPORT_COMMAND_KICK_EXEMPT = "potocraft.proxy_command.report.kick.exempt";
    public static final @NotNull String REPORT_COMMAND_HISTORY = "potocraft.proxy_command.report.history";
    public static final @NotNull String REPORT_COMMAND_MUTE = "potocraft.proxy_command.report.mute";
    public static final @NotNull String REPORT_COMMAND_MUTE_EXEMPT = "potocraft.proxy_command.report.mute.exempt";
    public static final @NotNull String REPORT_COMMAND_MUTE_OFFLINE = "potocraft.proxy_command.report.mute.offline";
    public static final @NotNull String REPORT_COMMAND_BAN = "potocraft.proxy_command.report.ban";
    public static final @NotNull String REPORT_COMMAND_BAN_EXEMPT = "potocraft.proxy_command.report.ban.offline";
    public static final @NotNull String REPORT_COMMAND_UNREPORT = "potocraft.proxy_command.report.unreport";
    public static final @NotNull String REPORT_COMMAND_BAN_OFFLINE = "potocraft.proxy_command.report.ban.exempt";

    public static final @NotNull String[] values = {
            LOGIN_COMMAND,
            CHANGE_PIN_COMMAND,
            REGISTER_COMMAND,
            REPORT_COMMAND,
            REPORT_COMMAND_KICK,
            REPORT_COMMAND_KICK_EXEMPT,
            REPORT_COMMAND_HISTORY,
            REPORT_COMMAND_MUTE,
            REPORT_COMMAND_MUTE_EXEMPT,
            REPORT_COMMAND_MUTE_OFFLINE,
            REPORT_COMMAND_BAN,
            REPORT_COMMAND_BAN_EXEMPT,
            REPORT_COMMAND_BAN_OFFLINE,
            REPORT_COMMAND_UNREPORT,
    };

    private Permissions() {
    }
}
