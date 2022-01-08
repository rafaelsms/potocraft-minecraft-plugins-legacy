package com.rafaelsms.potocraft.papermc.util;

import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import com.rafaelsms.potocraft.user.UserProfile;
import com.rafaelsms.potocraft.util.Database;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PaperDatabase extends Database {

    private final PaperPlugin plugin;

    public PaperDatabase(@NotNull PaperPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void retrieveProfile(@NotNull Player player,
                                @Nullable Consumer<PaperUser> profileConsumer,
                                @Nullable Runnable nullProfileRunnable,
                                @Nullable Consumer<Exception> exceptionConsumer) {
        super.retrieveProfile(player.getUniqueId(), document -> new PaperUser(plugin, player, document),
                profileConsumer, nullProfileRunnable, exceptionConsumer);
    }

    @Override
    public void saveProfile(@NotNull UserProfile userProfile, @Nullable Runnable successfulSaveRunnable, @Nullable Consumer<Exception> exceptionConsumer) {
        super.saveProfile(userProfile, successfulSaveRunnable, exceptionConsumer);
    }
}
