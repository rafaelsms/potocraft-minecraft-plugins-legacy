package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.common.user.UserManager;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.database.ServerProfile;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PaperUserManager extends UserManager<PaperUser, Player> {

    private final @NotNull PaperPlugin plugin;

    private final Map<UUID, ServerProfile> loadedProfiles = Collections.synchronizedMap(new HashMap<>());

    public PaperUserManager(@NotNull PaperPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public void insertServerProfile(@NotNull ServerProfile serverProfile) {
        this.loadedProfiles.put(serverProfile.getPlayerId(), serverProfile);
    }

    @Override
    protected PaperUser makeUser(@NotNull Player playerInstance) {
        ServerProfile serverProfile = loadedProfiles.remove(playerInstance.getUniqueId());
        if (serverProfile == null) {
            playerInstance.kick(plugin.getSettings().getKickMessageCouldNotRetrieveProfile());
            throw new IllegalStateException();
        }
        return new PaperUser(playerInstance, plugin, serverProfile);
    }

    @Override
    public PaperUser userJoinedListener(@NotNull UUID playerId, Player playerInstance) {
        PaperUser user = super.userJoinedListener(playerId, playerInstance);
        user.createTickingTask();
        return user;
    }

    @Override
    public Optional<PaperUser> userQuitListener(@NotNull UUID playerId) {
        // Assert we remove the profile
        loadedProfiles.remove(playerId);
        Optional<PaperUser> userOptional = super.userQuitListener(playerId);
        if (userOptional.isEmpty()) {
            return userOptional;
        }
        PaperUser user = userOptional.get();
        user.removeTickingTask();
        try {
            plugin.getDatabase().saveServerProfile(user.getServerProfile());
        } catch (Exception ignored) {
        }
        return userOptional;
    }
}
