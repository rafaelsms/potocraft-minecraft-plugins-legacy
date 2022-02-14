package com.rafaelsms.potocraft.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern SPACED_STRING_PATTERN = Pattern.compile("\\s*(\\S+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("((\\d+)([wdhms]))+", Pattern.CASE_INSENSITIVE);

    // Private constructor
    private TextUtil() {
    }

    public static @NotNull String toColorizedString(@NotNull Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serializeOrNull(component);
    }

    public static @NotNull String toPlainString(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serializeOrNull(component);
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

    public static @NotNull Optional<String> joinStrings(String[] strings, int initialIndex) {
        if (initialIndex >= strings.length) {
            return Optional.empty();
        }
        return Optional.of(joinStrings(Arrays.asList(strings), " ", str -> str));
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

    public static @NotNull Optional<String> closestMatch(@NotNull Iterable<String> list, @NotNull String search) {
        return closestMatch(list, string -> string, search);
    }

    public static <T> @NotNull Optional<T> closestMatch(@NotNull Iterable<T> list,
                                                        Function<T, String> stringFunction,
                                                        @NotNull String search) {
        T closestMatch = null;
        int smallestStartIndex = Integer.MAX_VALUE;
        search = search.toLowerCase();

        for (T entry : list) {
            String string = stringFunction.apply(entry).toLowerCase();
            // If it is equals, return the best match possible
            if (string.equalsIgnoreCase(search)) {
                return Optional.of(entry);
            }
            // Else, search inside the text
            int startIndex = string.indexOf(search);
            if (startIndex >= 0) {
                if (startIndex == smallestStartIndex) {
                    closestMatch = null;
                } else if (startIndex < smallestStartIndex) {
                    smallestStartIndex = startIndex;
                    closestMatch = entry;
                }
            }
        }

        return Optional.ofNullable(closestMatch);
    }

    public static @NotNull String getPrefix(@NotNull UUID playerId) {
        String prefix = "";
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            // Get prefix
            String prefixString = user.getCachedData().getMetaData().getPrefix();
            if (prefixString != null) {
                prefix = prefixString;
            }
        } catch (Exception ignored) {
        }
        return prefix;
    }

    public static @NotNull String getSuffix(@NotNull UUID playerId) {
        String suffix = "";
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            String suffixString = user.getCachedData().getMetaData().getSuffix();
            if (suffixString != null) {
                suffix = suffixString;
            }
        } catch (Exception ignored) {
        }
        return suffix;
    }

    public static ComponentBuilder toComponent(@Nullable String base) {
        return ComponentBuilder.builder(Util.getOrElse(base, "&cFailed to load message"));
    }

    public static class ComponentBuilder {

        private final @NotNull String base;
        private HashMap<String, String> stringReplacements = null;
        private HashMap<String, Component> componentReplacements = null;

        private ComponentBuilder(@NotNull String base) {
            this.base = base;
        }

        private static ComponentBuilder builder(@NotNull String base) {
            return new ComponentBuilder(base);
        }

        public ComponentBuilder replace(@NotNull String search, @NotNull String replacement) {
            if (this.stringReplacements == null) {
                this.stringReplacements = new HashMap<>();
            }
            this.stringReplacements.put(search, replacement);
            return this;
        }

        public ComponentBuilder replace(@NotNull String search, @NotNull Component replacement) {
            if (this.componentReplacements == null) {
                this.componentReplacements = new HashMap<>();
            }
            this.componentReplacements.put(search, replacement);
            return this;
        }

        public Component build() {
            String replacedString = base;
            if (stringReplacements != null) {
                for (Map.Entry<String, String> entry : stringReplacements.entrySet()) {
                    replacedString = replacedString.replace(entry.getKey(), entry.getValue());
                }
            }
            Component replacedComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(replacedString);
            if (componentReplacements != null) {
                for (Map.Entry<String, Component> entry : componentReplacements.entrySet()) {
                    replacedComponent = replacedComponent.replaceText(replaceText(entry.getKey(), entry.getValue()));
                }
            }
            return replacedComponent;
        }

        public @NotNull BaseComponent[] buildBungee() {
            return BungeeComponentSerializer.get().serialize(build());
        }

        private static @NotNull TextReplacementConfig replaceText(@NotNull String match,
                                                                  @NotNull Component replacement) {
            return TextReplacementConfig.builder().matchLiteral(match).replacement(replacement).build();
        }
    }
}
