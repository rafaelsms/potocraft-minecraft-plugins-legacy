package com.rafaelsms.potocraft.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern SPACED_STRING_PATTERN = Pattern.compile("\\s*(\\S+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("((\\d+)([wdhms]))+", Pattern.CASE_INSENSITIVE);

    // Private constructor
    private TextUtil() {
    }

    public static @NotNull Component toComponent(@NotNull String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public static @NotNull String getIpAddress(@NotNull InetSocketAddress address) {
        if (address.getAddress() != null) {
            return address.getAddress().getHostAddress();
        }
        return address.getHostString();
    }

    public static @NotNull List<String> parseArguments(@NotNull String string) {
        Matcher matcher = SPACED_STRING_PATTERN.matcher(string);
        List<String> arguments = new ArrayList<>();
        while (matcher.find()) {
            arguments.add(matcher.group(1));
        }
        return arguments;
    }

    public static @NotNull Optional<Duration> parseTime(@Nullable String string) {
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

    public static @NotNull Optional<Integer> parsePin(@NotNull String string) {
        if (string.strip().length() != 6) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(string));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<Integer> parseMatchingPins(@NotNull String firstPin, @NotNull String secondPin) {
        try {
            int newPin = TextUtil.parsePin(firstPin).orElseThrow();
            int newPinConfirmation = TextUtil.parsePin(secondPin).orElseThrow();
            if (newPin != newPinConfirmation) {
                return Optional.empty();
            }
            return Optional.of(newPin);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static <T> @NotNull String joinStrings(@NotNull Iterable<T> ts,
                                                  String separator,
                                                  @NotNull Function<T, String> stringFunction) {
        StringBuilder builder = new StringBuilder();
        Iterator<T> iterator = ts.iterator();
        while (iterator.hasNext()) {
            builder.append(stringFunction.apply(iterator.next()));
            if (iterator.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static @NotNull TextReplacementConfig replaceText(@NotNull String match, @NotNull String replacement) {
        return TextReplacementConfig.builder().matchLiteral(match).replacement(replacement).build();
    }

    public static @NotNull TextReplacementConfig replaceText(@NotNull String match, @NotNull Component replacement) {
        return TextReplacementConfig.builder().matchLiteral(match).replacement(replacement).build();
    }

    public static @NotNull Optional<String> closestMatch(@NotNull Iterable<String> list, @NotNull String search) {
        return closestStringMatch(list, string -> string, search);
    }

    public static <T> @NotNull Optional<T> closestStringMatch(@NotNull Iterable<T> list,
                                                              Function<T, String> stringFunction,
                                                              @NotNull String search) {
        T closestMatch = null;
        int closesMatchResult = search.length(); // Prevent matching when no character is equal
        LevenshteinDistance comparator = new LevenshteinDistance(closesMatchResult);

        for (T entry : list) {
            int compareResult = comparator.apply(stringFunction.apply(entry), search);
            // Return if exact match
            if (compareResult == 0) {
                return Optional.of(entry);
            }
            if (compareResult >= 0 && compareResult < closesMatchResult) {
                closestMatch = entry;
                closesMatchResult = compareResult;
            }
        }

        return Optional.ofNullable(closestMatch);
    }

    public static @NotNull Component getPrefix(@NotNull UUID playerId) {
        Component prefix = Component.empty();
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            // Get prefix
            String prefixString = user.getCachedData().getMetaData().getPrefix();
            if (prefixString != null) {
                prefix = toComponent(prefixString);
            }
        } catch (Exception ignored) {
        }
        return prefix;
    }

    public static @NotNull Component getSuffix(@NotNull UUID playerId) {
        Component suffix = Component.empty();
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            String suffixString = user.getCachedData().getMetaData().getSuffix();
            if (suffixString != null) {
                suffix = toComponent(suffixString);
            }
        } catch (Exception ignored) {
        }
        return suffix;
    }
}
