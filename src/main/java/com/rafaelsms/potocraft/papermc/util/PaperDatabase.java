package com.rafaelsms.potocraft.papermc.util;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.profile.PaperProfile;
import com.rafaelsms.potocraft.profile.Profile;
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

    public @NotNull CompletableFuture<PaperProfile> getProfile(@NotNull Player player) {
        return super.getProfile(player.getUniqueId(), document -> new PaperProfile(plugin, player, document));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveProfile(@NotNull Profile profile) {
        return super.saveProfile(profile);
    }
}
