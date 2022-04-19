package com.rafaelsms.potocraft.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class LuckPermsUtil {

    private LuckPermsUtil() {
    }

    public static @NotNull Optional<String> getUncoloredPrefix(@NotNull UUID playerId) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            return Optional.of(Optional.ofNullable(user.getCachedData().getMetaData().getPrefix()).orElse(""));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<String> getColorizedPrefix(@NotNull UUID playerId) {
        return getUncoloredPrefix(playerId).map(TextUtil::toColorizedString);
    }

    public static @NotNull Optional<String> getUncoloredSuffix(@NotNull UUID playerId) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().loadUser(playerId).join();
            return Optional.of(Optional.ofNullable(user.getCachedData().getMetaData().getSuffix()).orElse(""));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<String> getColorizedSuffix(@NotNull UUID playerId) {
        return getUncoloredSuffix(playerId).map(TextUtil::toColorizedString);
    }
}
