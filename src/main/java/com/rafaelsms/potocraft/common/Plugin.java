package com.rafaelsms.potocraft.common;

import com.rafaelsms.potocraft.common.util.Database;
import com.rafaelsms.potocraft.common.util.PluginType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface Plugin {

    @NotNull CommonServer getCommonServer();

    @NotNull Logger logger();

    default void debug(@NotNull String debugString) {
        if (getSettings().isDebugMessagesEnabled()) {
            logger().debug(debugString);
        }
    }

    @NotNull PluginType getPluginType();

    @NotNull Settings getSettings();

    @NotNull Database getDatabase();

}
