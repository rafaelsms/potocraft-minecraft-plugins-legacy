package com.rafaelsms.potocraft.papermc.util;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.user.UserProfile;
import com.rafaelsms.potocraft.util.Database;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PaperDatabase extends Database {

    private final PaperPlugin plugin;

    public PaperDatabase(@NotNull PaperPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public @NotNull CompletableFuture<PaperUser> getProfile(@NotNull Player player) {
        return super.getProfile(player.getUniqueId(), document -> new PaperUser(plugin, player, document));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveProfile(@NotNull UserProfile userProfile) {
        return super.saveProfile(userProfile);
    }
}
