package com.rafaelsms.potocraft.common;

public final class Permissions {

    public static final String LOGIN_COMMAND = "potocraft.proxy_command.login";

    public static final String CHANGE_PIN_COMMAND = "potocraft.proxy_command.change_pin";
    public static final String CHANGE_PIN_COMMAND_OTHERS = "potocraft.proxy_command.change_pin.others";

    public static final String REGISTER_COMMAND = "potocraft.proxy_command.register";
    public static final String REGISTER_COMMAND_OTHERS = "potocraft.proxy_command.register.others";

    public static final String REPORT_COMMAND = "potocraft.proxy_command.report";
    public static final String REPORT_COMMAND_KICK = "potocraft.proxy_command.report.kick";
    public static final String REPORT_COMMAND_KICK_EXEMPT = "potocraft.proxy_command.report.kick.exempt";
    public static final String REPORT_COMMAND_HISTORY = "potocraft.proxy_command.report.history";
    public static final String REPORT_COMMAND_MUTE = "potocraft.proxy_command.report.mute";
    public static final String REPORT_COMMAND_MUTE_EXEMPT = "potocraft.proxy_command.report.mute.exempt";
    public static final String REPORT_COMMAND_MUTE_OFFLINE = "potocraft.proxy_command.report.mute.offline";
    public static final String REPORT_COMMAND_BAN = "potocraft.proxy_command.report.ban";
    public static final String REPORT_COMMAND_BAN_EXEMPT = "potocraft.proxy_command.report.ban.offline";
    public static final String REPORT_COMMAND_UNREPORT = "potocraft.proxy_command.report.unreport";
    public static final String REPORT_COMMAND_BAN_OFFLINE = "potocraft.proxy_command.report.ban.exempt";

    public static final String MESSAGE_COMMAND = "potocraft.proxy_command.message";
    public static final String MESSAGE_COMMAND_REPLY = "potocraft.proxy_command.message.reply";
    // TODO below
    public static final String MESSAGE_COMMAND_BYPASS_IGNORE = "potocraft.proxy_command.message.bypass.ignore";
    public static final String MESSAGE_COMMAND_BYPASS_LIMITER = "potocraft.proxy_command.message.bypass.limiter";

    public static final String LOCAL_CHAT_SPY = "potocraft.chat.spy";
    public static final String GLOBAL_CHAT = "potocraft.chat.global";
    public static final String GLOBAL_CHAT_BYPASS = "potocraft.chat.global.bypass";
    public static final String UNIVERSAL_CHAT = "potocraft.chat.universal";
    public static final String UNIVERSAL_CHAT_BYPASS = "potocraft.chat.universal.bypass";
    public static final String UNIVERSAL_CHAT_SPY = "potocraft.chat.spy.universal";

    public static final String[] values = {

            LOGIN_COMMAND,

            CHANGE_PIN_COMMAND,
            CHANGE_PIN_COMMAND_OTHERS,

            REGISTER_COMMAND,
            REGISTER_COMMAND_OTHERS,

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

            MESSAGE_COMMAND,
            MESSAGE_COMMAND_REPLY,
            MESSAGE_COMMAND_BYPASS_IGNORE,

            GLOBAL_CHAT,
            GLOBAL_CHAT_BYPASS,
            LOCAL_CHAT_SPY,
            UNIVERSAL_CHAT,
            UNIVERSAL_CHAT_BYPASS,
            UNIVERSAL_CHAT_SPY,

    };

    private Permissions() {
    }
}
