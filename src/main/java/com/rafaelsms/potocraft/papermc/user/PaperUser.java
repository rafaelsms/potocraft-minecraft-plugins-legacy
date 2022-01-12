package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.user.User;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.database.ServerProfile;
import com.rafaelsms.potocraft.papermc.user.combat.CombatTask;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportRequest;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PaperUser extends User {

    private final @NotNull Player player;
    private final @NotNull PaperPlugin plugin;

    private final @NotNull GlobalChatHistory globalChatHistory;
    private final @NotNull LocalChatHistory localChatHistory;

    private final @NotNull ServerProfile serverProfile;

    private @Nullable BukkitTask tickingTask = null;
    private @Nullable TeleportTask teleportTask = null;
    private @Nullable CombatTask combatTask = null;

    private final ArrayDeque<TeleportRequest> teleportRequests = new ArrayDeque<>();

    public PaperUser(@NotNull Player player, @NotNull PaperPlugin plugin, @NotNull ServerProfile serverProfile) {
        this.player = player;
        this.plugin = plugin;
        this.globalChatHistory = new GlobalChatHistory();
        this.localChatHistory = new LocalChatHistory();
        this.serverProfile = serverProfile;
    }

    public void createTickingTask() {
        this.tickingTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new TickingTask(), 1, 1);
    }

    public void removeTickingTask() {
        if (tickingTask != null) {
            tickingTask.cancel();
        }
    }

    public @NotNull ServerProfile getServerProfile() {
        return serverProfile;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull UUID getUniqueId() {
        return player.getUniqueId();
    }

    public Optional<TeleportTask> getTeleportTask() {
        return Optional.ofNullable(this.teleportTask);
    }

    public void setTeleportTask(@Nullable TeleportTask teleportTask) {
        this.teleportTask = teleportTask;
    }

    public boolean addTeleportRequest(@NotNull TeleportRequest request) {
        synchronized (teleportRequests) {
            // Just update if existing request is available
            Iterator<TeleportRequest> iterator = teleportRequests.iterator();
            while (iterator.hasNext()) {
                TeleportRequest otherRequest = iterator.next();
                if (otherRequest.isExpired()) {
                    iterator.remove();
                    continue;
                }
                if (otherRequest.getRequester().getUniqueId().equals(request.getRequester().getUniqueId())) {
                    otherRequest.updateAskingTime();
                    return false;
                }
            }
            // If none found, insert it
            teleportRequests.addLast(request);
            return true;
        }
    }

    public List<TeleportRequest> getTeleportRequests() {
        return List.copyOf(teleportRequests);
    }

    public TeleportResult getTeleportStatus() {
        // Check player status
        if (player.isDead() || !player.isOnline()) {
            return TeleportResult.PLAYER_UNAVAILABLE;
        }
        // Check combat status (this will already consider the combat bypass permission)
        if (getCombatTask().isPresent()) {
            return TeleportResult.IN_COMBAT;
        }
        if (getTeleportTask().isPresent()) {
            return TeleportResult.ALREADY_TELEPORTING;
        }
        // Check profile status
        if (player.hasPermission(Permissions.TELEPORT_COMMAND_NO_COOLDOWN) || getTeleportCooldown() <= 0) {
            return TeleportResult.ALLOWED;
        }
        return TeleportResult.IN_COOLDOWN;
    }

    public long getTeleportCooldown() {
        Optional<ZonedDateTime> lastTeleportDate = this.serverProfile.getLastTeleportDate();
        // If there is no date, return no cooldown
        if (lastTeleportDate.isEmpty()) {
            return 0;
        }
        // If there is a date and it is in the past, return no cooldown
        ZonedDateTime endOfCooldown = lastTeleportDate.get().plus(plugin.getSettings().getTeleportCooldown());
        ZonedDateTime now = ZonedDateTime.now();
        if (now.isAfter(endOfCooldown)) {
            return 0;
        }
        // Return the difference in seconds
        return Math.max(now.until(endOfCooldown, ChronoUnit.SECONDS), 1L);
    }

    public CompletableFuture<Boolean> teleportNow(@NotNull Location location,
                                                  @NotNull PlayerTeleportEvent.TeleportCause cause) {
        // Store back location and cancel teleport task
        Location oldLocation = player.getLocation().clone();
        setTeleportTask(null);

        // Teleport player and set back location
        return player.teleportAsync(location, cause).whenComplete((success, throwable) -> {
            if (throwable != null || success == null || !success) {
                return;
            }
            String serverName = plugin.getSettings().getServerName();
            serverProfile.setBackLocation(com.rafaelsms.potocraft.common.profile.Location.fromPlayer(serverName,
                                                                                                     oldLocation));
        });
    }

    public Optional<CombatTask> getCombatTask() {
        return Optional.ofNullable(combatTask);
    }

    public void setCombatTask(@Nullable CombatTask combatTask) {
        if (this.combatTask != null &&
            combatTask != null &&
            !this.combatTask.getType().canOverride(combatTask.getType())) {
            return;
        }
        this.combatTask = combatTask;
    }

    public ChatHistory.ChatResult canSendGlobalMessage(@NotNull String message) {
        return globalChatHistory.canSendMessage(message);
    }

    public void sentGlobalMessage(@NotNull String message) {
        globalChatHistory.sentMessage(message);
    }

    public ChatHistory.ChatResult canSendLocalMessage(@NotNull String message) {
        return localChatHistory.canSendMessage(message);
    }

    public void sentLocalMessage(@NotNull String message) {
        localChatHistory.sentMessage(message);
    }

    private class TickingTask implements Runnable {

        @Override
        public void run() {
            if (combatTask != null) {
                combatTask.run();
            }
            if (teleportTask != null) {
                teleportTask.run();
            }

            synchronized (teleportRequests) {
                // Remove all expired requests
                while (teleportRequests.peekFirst() != null && teleportRequests.peekFirst().isExpired()) {
                    teleportRequests.pollFirst();
                }
            }
        }
    }

    public enum TeleportResult {

        PLAYER_UNAVAILABLE,
        IN_COMBAT,
        IN_COOLDOWN,
        ALREADY_TELEPORTING,
        ALLOWED;

        public boolean isAllowed() {
            return this == ALLOWED;
        }
    }

    private class GlobalChatHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getSettings().getGlobalChatComparatorThreshold();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getSettings().getGlobalChatComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return Duration.ofMillis(plugin.getSettings().getGlobalChatLimiterTimeAmount());
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getGlobalChatLimiterMessageAmount();
        }

        @Override
        public boolean playerHasBypassPermission() {
            return player.hasPermission(Permissions.GLOBAL_CHAT_BYPASS);
        }
    }

    private class LocalChatHistory extends ChatHistory {

        @Override
        protected int getComparatorThreshold() {
            return plugin.getSettings().getLocalChatComparatorThreshold();
        }

        @Override
        protected int getMinLengthToCompare() {
            return plugin.getSettings().getLocalChatComparatorMinLength();
        }

        @Override
        protected @NotNull Duration getLimiterTimeFrame() {
            return Duration.ofMillis(plugin.getSettings().getLocalChatLimiterTimeAmount());
        }

        @Override
        protected int getLimiterMaxMessageAmount() {
            return plugin.getSettings().getLocalChatLimiterMessageAmount();
        }

        @Override
        public boolean playerHasBypassPermission() {
            return player.hasPermission(Permissions.GLOBAL_CHAT_BYPASS);
        }
    }
}
