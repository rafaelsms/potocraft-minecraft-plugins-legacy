package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.common.user.UserManager;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperUserManager extends UserManager<PaperUser, Player> {

    public PaperUserManager(@NotNull PaperPlugin plugin) {
        super(plugin);
    }

    @Override
    protected PaperUser makeUser(@NotNull Player playerInstance) {
        return new PaperUser(playerInstance);
    }
}
