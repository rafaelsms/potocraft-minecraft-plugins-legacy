package com.rafaelsms.potocraft.papermc.util;

import com.rafaelsms.potocraft.common.util.TextUtil;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaperUtil {

    public static @NotNull List<String> getPlayerNameList(@NotNull PaperPlugin plugin) {
        return TextUtil.toStringList(plugin.getServer().getOnlinePlayers(), Player::getName);
    }

    public static @NotNull Optional<Player> searchPlayerName(@NotNull PaperPlugin plugin, @NotNull String search) {
        return TextUtil.closestStringMatch(new ArrayList<>(plugin.getServer().getOnlinePlayers()),
                                           Player::getName,
                                           search);
    }

    public static boolean handleTeleportStatus(@NotNull PaperPlugin plugin, @NotNull PaperUser user) {
        PaperUser.TeleportResult teleportStatus = user.getTeleportStatus();
        if (teleportStatus.isAllowed()) {
            return true;
        }
        switch (teleportStatus) {
            case IN_COOLDOWN -> user
                    .getPlayer()
                    .sendMessage(plugin.getSettings().getTeleportInCooldown(user.getTeleportCooldown()));
            case IN_COMBAT -> user.getPlayer().sendMessage(plugin.getSettings().getTeleportPlayerInCombat());
            case ALREADY_TELEPORTING -> user
                    .getPlayer()
                    .sendMessage(plugin.getSettings().getTeleportCanNotTeleportNow());
        }
        return false;
    }
}
