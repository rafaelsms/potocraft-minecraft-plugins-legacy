package com.rafaelsms.potocraft.combatserver;

import org.jetbrains.annotations.NotNull;

public class Database extends com.rafaelsms.potocraft.database.Database {

    private final @NotNull CombatServerPlugin plugin;

    protected Database(@NotNull CombatServerPlugin plugin) {
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
