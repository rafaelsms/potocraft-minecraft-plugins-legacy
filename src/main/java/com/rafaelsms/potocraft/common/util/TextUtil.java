package com.rafaelsms.potocraft.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern SPACED_STRING_PATTERN = Pattern.compile("\\s*(\\S+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("((\\d+)([wdhms]))+");

    private TextUtil() {
    }

    public static @NotNull String getIpAddress(@NotNull InetSocketAddress address) {
        if (address.getAddress() != null) {
            return address.getAddress().getHostAddress();
        }
        return address.getHostString();
    }

    public static @NotNull Component getJsonLang(@NotNull String string) {
        return GsonComponentSerializer.gson().deserialize(string);
    }

    public static @NotNull Component getLegacyLang(@NotNull String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static @NotNull String toSimpleString(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static @NotNull Component getLang(@NotNull String string) {
        try {
            return getJsonLang(string);
        } catch (Exception exception) {
            return getLegacyLang(string);
        }
    }

    public static String[] parseArguments(@NotNull String string) {
        Matcher matcher = SPACED_STRING_PATTERN.matcher(string);
        List<String> arguments = new ArrayList<>(4);
        while (matcher.find()) {
            arguments.add(matcher.group(1));
        }
        return arguments.toArray(new String[0]);
    }

    public static Optional<Duration> parseTime(@Nullable String string) {
        if (string == null) {
            return Optional.empty();
        }
        Matcher matcher = TIME_PATTERN.matcher(string);
        Duration duration = Duration.ZERO;
        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(2));
            String unit = matcher.group(3);
            Duration parsed = switch (unit) {
                case "w" -> Duration.ofDays(amount * 7L);
                case "d" -> Duration.ofDays(amount);
                case "h" -> Duration.ofHours(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "s" -> Duration.ofSeconds(amount);
                default -> Duration.ZERO;
            };
            duration = duration.plus(parsed);
        }
        return duration == Duration.ZERO ? Optional.empty() : Optional.of(duration);
    }

    public static Optional<Integer> parsePin(@NotNull String string) {
        try {
            return Optional.of(Integer.parseInt(string));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public static String joinStrings(@NotNull Collection<String> strings, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string).append(separator);
        }
        return builder.toString();
    }

    public static String joinStrings(@NotNull String[] strings, int start) {
        return joinStrings(strings, start, " ");
    }

    public static String joinStrings(@NotNull String[] strings, int start, @NotNull String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < strings.length; i++) {
            builder.append(strings[i]).append(separator);
        }
        return builder.toString();
    }

    public static Optional<String> getArgument(@NotNull String[] arguments, int position) {
        if (position >= arguments.length) {
            return Optional.empty();
        }
        return Optional.of(arguments[position]);
    }

    public static @NotNull TextReplacementConfig replaceText(@NotNull String match, @NotNull String replacement) {
        return TextReplacementConfig.builder().matchLiteral(match).replacement(replacement).build();
    }

    public static @NotNull TextReplacementConfig replaceText(@NotNull String match, @NotNull Component replacement) {
        return TextReplacementConfig.builder().matchLiteral(match).replacement(replacement).build();
    }

    public static Component getPrefix(@NotNull UUID playerId) {
        Component prefix = Component.empty();
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            // Get prefix
            String prefixString = user.getCachedData().getMetaData().getPrefix();
            if (prefixString != null) {
                prefix = getLang(prefixString);
            }
        } catch (Exception ignored) {
        }
        return prefix;
    }

    public static Component getSuffix(@NotNull UUID playerId) {
        Component suffix = Component.empty();
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            String suffixString = user.getCachedData().getMetaData().getSuffix();
            if (suffixString != null) {
                suffix = getLang(suffixString);
            }
        } catch (Exception ignored) {
        }
        return suffix;
    }

    public static Component applyChatFormat(@NotNull Component chatFormat,
                                            @NotNull UUID playerId,
                                            @NotNull String playerName,
                                            @NotNull Component message) {

        return chatFormat
                .replaceText(replaceText("%prefix%", getPrefix(playerId)))
                .replaceText(replaceText("%suffix%", getSuffix(playerId)))
                .replaceText(replaceText("%username%", playerName))
                .replaceText(replaceText("%message%", message));
    }
}
