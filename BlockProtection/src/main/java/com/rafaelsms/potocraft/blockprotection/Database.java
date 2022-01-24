package com.rafaelsms.potocraft.blockprotection;

import org.jetbrains.annotations.NotNull;

public class Database extends com.rafaelsms.potocraft.database.Database {

    private final @NotNull BlockProtectionPlugin plugin;

    public Database(@NotNull BlockProtectionPlugin plugin) {
        super(plugin.getConfiguration().getMongoURI(), plugin.getConfiguration().getMongoDatabaseName());
        this.plugin = plugin;
    }

    @Override
    protected void handleException(@NotNull DatabaseException databaseException) throws DatabaseException {
        plugin.logger().warn("Database exception:", databaseException);
        if (plugin.getConfiguration().isMongoDatabaseExceptionFatal()) {
            plugin.getServer().shutdown();
        }
        throw databaseException;
    }
}
