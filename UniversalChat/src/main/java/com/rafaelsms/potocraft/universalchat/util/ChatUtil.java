package com.rafaelsms.potocraft.universalchat.util;

import com.rafaelsms.potocraft.universalchat.Permissions;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class ChatUtil {

    // Private constructor
    private ChatUtil() {
    }

    public static Component parseMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (sender.hasPermission(Permissions.CHAT_COLORIZED)) {
            return TextUtil.toLegacyComponent(message);
        } else {
            return Component.text(message);
        }
    }
}
