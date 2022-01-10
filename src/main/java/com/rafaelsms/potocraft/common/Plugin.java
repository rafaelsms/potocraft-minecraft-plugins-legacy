package com.rafaelsms.potocraft.common;

import com.rafaelsms.potocraft.common.database.Database;
import com.rafaelsms.potocraft.common.profile.Profile;
import com.rafaelsms.potocraft.common.user.User;
import com.rafaelsms.potocraft.common.user.UserManager;
import com.rafaelsms.potocraft.common.util.DepedencyException;
import com.rafaelsms.potocraft.common.util.PluginType;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface Plugin<T extends Profile, U extends User, P> {

    @NotNull CommonServer getCommonServer();

    @NotNull Logger logger();

    default void debug(@NotNull String debugString) {
        if (getSettings().isDebugMessagesEnabled()) {
            logger().info(debugString);
        }
    }

    @NotNull PluginType getPluginType();

    @NotNull Settings getSettings();

    @NotNull Database<T> getDatabase();

    @NotNull UserManager<U, P> getUserManager();

    default FloodgateApi getFloodgate() throws DepedencyException {
        try {
            if (FloodgateApi.getInstance() == null) {
                throw new NullPointerException();
            }
            return FloodgateApi.getInstance();
        } catch (Exception exception) {
            throw new DepedencyException(exception);
        }
    }

    default LuckPerms getLuckPerms() throws DepedencyException {
        try {
            return LuckPermsProvider.get();
        } catch (Exception exception) {
            throw new DepedencyException(exception);
        }
    }
}
