package com.rafaelsms.potocraft.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {

    private static final Pattern SPACED_STRING_PATTERN = Pattern.compile("\\h*(\\S+)");

    private Util() {
    }

    public static @NotNull String getIpAddress(@NotNull InetSocketAddress address) {
        if (address.getAddress() != null) {
            return address.getAddress().getHostAddress();
        }
        return address.getHostString();
    }

    public static Component getJsonLang(String string) {
        return GsonComponentSerializer.gson().deserialize(string);
    }

    public static Component getLegacyLang(String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static String toSimpleString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static Component getLang(String string) {
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

    public static Optional<Integer> parsePin(@NotNull String string) {
        try {
            return Optional.of(Integer.parseInt(string));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
