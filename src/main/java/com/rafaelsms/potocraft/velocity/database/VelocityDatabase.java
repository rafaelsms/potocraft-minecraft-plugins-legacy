package com.rafaelsms.potocraft.velocity.database;

import com.rafaelsms.potocraft.util.Database;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.user.VelocityUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class VelocityDatabase extends Database {

    private final VelocityPlugin plugin;

    public VelocityDatabase(@NotNull VelocityPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void retrieveProfile(@NotNull UUID playerUniqueId,
                                           @Nullable Consumer<VelocityUser> profileConsumer,
                                           @Nullable Runnable nullProfileRunnable,
                                           @Nullable Consumer<Exception> exceptionConsumer) {
        super.retrieveProfile(playerUniqueId, (document) -> new VelocityUser(plugin, document),
                profileConsumer, nullProfileRunnable, exceptionConsumer);
    }

    public void saveProfile(@NotNull VelocityUser velocityUser,
                            @Nullable Runnable successfulSaveRunnable,
                            @Nullable Consumer<Exception> exceptionConsumer)  {
        super.saveProfile(velocityUser, successfulSaveRunnable, exceptionConsumer);
    }
}
