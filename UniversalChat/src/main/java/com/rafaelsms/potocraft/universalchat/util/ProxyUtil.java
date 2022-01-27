package com.rafaelsms.potocraft.universalchat.util;

import com.rafaelsms.potocraft.util.Util;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ProxyUtil {

    // Private constructor
    private ProxyUtil() {
    }

    public static TabSuggestion getNameSuggester(@NotNull ProxyServer server) {
        return invocation -> Util.convertList(server.getAllPlayers(), Player::getUsername);
    }

    public interface TabSuggestion {

        @NotNull List<String> getSuggestions(RawCommand.Invocation invocation);

    }
}
