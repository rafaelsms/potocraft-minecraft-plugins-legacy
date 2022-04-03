package com.rafaelsms.teleporter;

import com.rafaelsms.potocraft.plugin.BaseConfiguration;
import com.rafaelsms.potocraft.plugin.BaseJavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Configuration extends BaseConfiguration {

    protected Configuration(@NotNull BaseJavaPlugin plugin) throws IOException {
        super(plugin);
    }

    @Override
    public @NotNull String getMongoURI() {
        return null;
    }

    @Override
    public @NotNull String getMongoDatabaseName() {
        return null;
    }

    @Override
    public @NotNull String getProfileCollectionName() {
        return null;
    }

    @Override
    public int getProfileSavingTaskTimer() {
        return 0;
    }

    public int getTeleportRequestDurationTicks() {
        return 0;
    }
}
