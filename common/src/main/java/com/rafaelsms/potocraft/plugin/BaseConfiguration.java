package com.rafaelsms.potocraft.plugin;

import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public abstract class BaseConfiguration extends YamlFile {

    protected final @NotNull BaseJavaPlugin plugin;

    protected BaseConfiguration(@NotNull BaseJavaPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        this.plugin = plugin;
    }

    public abstract @NotNull String getMongoURI();

    public abstract @NotNull String getMongoDatabaseName();

    public abstract @NotNull String getProfileCollectionName();

    public abstract int getProfileSavingTaskTimer();

    public abstract @NotNull Component getCommandIsPlayerOnly();

    public abstract @NotNull Component getFailedToRetrieveUserProfile();
}
