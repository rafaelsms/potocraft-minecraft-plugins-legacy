package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.common.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperUser extends User {

    private final @NotNull Player player;

    public PaperUser(@NotNull Player player) {
        this.player = player;
    }
}
