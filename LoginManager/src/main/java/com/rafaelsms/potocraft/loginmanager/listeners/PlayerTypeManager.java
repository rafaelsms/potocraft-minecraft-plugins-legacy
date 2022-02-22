package com.rafaelsms.potocraft.loginmanager.listeners;

import com.rafaelsms.potocraft.loginmanager.LoginManagerPlugin;
import com.rafaelsms.potocraft.loginmanager.util.PlayerType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTypeManager implements Listener {

    private final Map<UUID, PlayerType> playerTypeMap = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull LoginManagerPlugin plugin;

    public PlayerTypeManager(@NotNull LoginManagerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Helper method to retrieve player connection type given player unique id.
     *
     * @param player player instance
     * @return returned value of {@link this#getPlayerType(UUID)} for {@link ProxiedPlayer#getUniqueId()}
     */
    public @NotNull PlayerType getPlayerType(@NotNull ProxiedPlayer player) {
        return getPlayerType(player.getUniqueId());
    }

    /**
     * Retrieves player connection type given player unique id. This method is available after {@link PreLoginEvent}'s
     * {@link EventPriority#HIGH} event priority for Floodgate players or {@link PostLoginEvent}'s
     * {@link EventPriority#LOWEST} event priority for every player.
     *
     * @param playerId player's unique id
     * @return player type of {@link PlayerType#OFFLINE_PLAYER} if it is called outside the bounds.
     */
    public @NotNull PlayerType getPlayerType(@NotNull UUID playerId) {
        // the safest fallback is offline player (we would require login)
        return playerTypeMap.get(playerId);
    }

    // We must wait Floodgate's changes on LOWEST
    // We must wait OfflineCheckerListener's changes on LOW
    @EventHandler(priority = EventPriority.NORMAL)
    public void registerPlayerType(PreLoginEvent event) {
        // Ignore null player ids
        if (event.getConnection().getUniqueId() == null) {
            return;
        }

        PlayerType playerType;
        if (event.getConnection().isOnlineMode()) {
            playerType = PlayerType.ONLINE_PLAYER;
        } else if (FloodgateApi.getInstance().getPlayer(event.getConnection().getUniqueId()) != null) {
            playerType = PlayerType.FLOODGATE_PLAYER;
        } else {
            playerType = PlayerType.OFFLINE_PLAYER;
        }
        playerTypeMap.put(event.getConnection().getUniqueId(), playerType);
    }

    // We must wait player id
    @EventHandler(priority = EventPriority.LOWEST)
    public void registerPlayerType(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PlayerType playerType;
        if (player.getPendingConnection().isOnlineMode()) {
            playerType = PlayerType.ONLINE_PLAYER;
        } else if (FloodgateApi.getInstance().getPlayer(player.getUniqueId()) != null) {
            playerType = PlayerType.FLOODGATE_PLAYER;
        } else {
            playerType = PlayerType.OFFLINE_PLAYER;
        }
        playerTypeMap.put(player.getUniqueId(), playerType);
    }

    @EventHandler
    public void printPlayerType(PostLoginEvent event) {
        plugin.logger()
              .info("Player {} (uuid = {}) connected with type {}",
                    event.getPlayer().getName(),
                    event.getPlayer().getUniqueId(),
                    getPlayerType(event.getPlayer()));
    }

    @EventHandler
    public void printPlayerType(PlayerDisconnectEvent event) {
        plugin.logger()
              .info("Player {} (uuid = {}) disconnected with type {}",
                    event.getPlayer().getName(),
                    event.getPlayer().getUniqueId(),
                    getPlayerType(event.getPlayer()));
    }
}
