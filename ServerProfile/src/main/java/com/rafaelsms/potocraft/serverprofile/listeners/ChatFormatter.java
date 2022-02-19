package com.rafaelsms.potocraft.serverprofile.listeners;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChatFormatter implements Listener {

    private final Map<UUID, Location> lastLocations = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull ServerProfilePlugin plugin;

    public ChatFormatter(@NotNull ServerProfilePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onReload(ServerLoadEvent event) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, new UpdateLocationsTask(), 6, 6);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        synchronized (lastLocations) {
            lastLocations.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation().clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void handleLocalChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID senderId = player.getUniqueId();
        String senderName = player.getName();
        Identity senderIdentity = player.identity();

        event.setCancelled(true);
        Component spyMessage = plugin.getConfiguration().getLocalChatSpyFormat(senderId, senderName, event.message());

        double chatRange = plugin.getConfiguration().getLocalChatRange();
        // If local chat is disable, we will send messages only to the world
        if (chatRange <= 0) {
            Component chatMessage = plugin.getConfiguration().getChatFormat(senderId, senderName, event.message());
            Location senderLocation = lastLocations.get(player.getUniqueId());
            if (senderLocation == null) {
                return;
            }
            // Send world message
            senderLocation.getWorld().sendMessage(senderIdentity, chatMessage, MessageType.CHAT);
            // Send spy message for other worlds
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                Location receiverLocation = lastLocations.get(onlinePlayer.getUniqueId());
                if (receiverLocation == null) {
                    continue;
                }
                // If is the same world, player already received the message
                if (!isDifferentWorld(senderLocation, receiverLocation)) {
                    continue;
                }
                if (onlinePlayer.hasPermission(Permissions.CHAT_SPY)) {
                    onlinePlayer.sendMessage(senderIdentity, spyMessage, MessageType.CHAT);
                }
            }
            plugin.getServer().getConsoleSender().sendMessage(senderIdentity, chatMessage, MessageType.CHAT);
            return;
        }

        Component localChatMessage =
                plugin.getConfiguration().getLocalChatFormat(senderId, senderName, event.message());

        double chatRangeSquared = chatRange * chatRange;
        Location senderLocation = lastLocations.get(senderId);
        if (senderLocation == null) {
            return;
        }
        // Check each player location and permission if it is far away
        boolean messageReceived = false;
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            UUID receiverId = onlinePlayer.getUniqueId();
            Location receiverLocation = lastLocations.get(receiverId);
            // Skips message for this player if location is null or same player
            if (receiverLocation == null || Objects.equals(receiverId, senderId)) {
                continue;
            }
            // Ignore if player doesn't have spy permission and is far away
            if (isOutsideRange(receiverLocation, senderLocation, chatRangeSquared)) {
                if (onlinePlayer.hasPermission(Permissions.CHAT_SPY)) {
                    onlinePlayer.sendMessage(senderIdentity, spyMessage, MessageType.CHAT);
                }
                continue;
            }
            onlinePlayer.sendMessage(senderIdentity, localChatMessage, MessageType.CHAT);
            if (!messageReceived) {
                messageReceived = onlinePlayer.getGameMode() != GameMode.SPECTATOR && !onlinePlayer.isInvisible();
            }
        }
        if (!messageReceived) {
            player.sendMessage(senderIdentity, plugin.getConfiguration().getNobodyHeardYou(), MessageType.CHAT);
        } else {
            player.sendMessage(senderIdentity, localChatMessage, MessageType.CHAT);
        }
        plugin.getServer().getConsoleSender().sendMessage(senderIdentity, localChatMessage, MessageType.CHAT);
    }

    private boolean isDifferentWorld(@NotNull Location location1, @NotNull Location location2) {
        if (location1.getWorld() == null || location2.getWorld() == null) {
            return true;
        }
        UUID world1 = location1.getWorld().getUID();
        UUID world2 = location2.getWorld().getUID();
        return !Objects.equals(world1, world2);
    }

    private boolean isOutsideRange(@NotNull Location location1, @NotNull Location location2, double rangeSq) {
        return isDifferentWorld(location1, location2) || location1.distanceSquared(location2) > rangeSq;
    }

    private class UpdateLocationsTask implements Runnable {

        @Override
        public void run() {
            synchronized (lastLocations) {
                lastLocations.clear();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    lastLocations.put(player.getUniqueId(), player.getLocation().clone());
                }
            }
        }
    }
}
