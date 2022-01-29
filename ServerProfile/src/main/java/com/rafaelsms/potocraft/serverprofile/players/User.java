package com.rafaelsms.potocraft.serverprofile.players;

import com.rafaelsms.potocraft.serverprofile.Permissions;
import com.rafaelsms.potocraft.serverprofile.ServerProfilePlugin;
import com.rafaelsms.potocraft.serverprofile.players.tasks.CombatTask;
import com.rafaelsms.potocraft.serverprofile.players.tasks.TeleportTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class User {

    private final ArrayDeque<TeleportRequest> teleportRequests = new ArrayDeque<>();

    private final @NotNull ServerProfilePlugin plugin;

    private final @NotNull Player player;
    private final @NotNull Profile profile;

    private @Nullable CombatTask combatTask = null;
    private @Nullable TeleportTask teleportTask = null;

    public User(@NotNull ServerProfilePlugin plugin, @NotNull Player player, @NotNull Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Profile getProfile() {
        return profile;
    }

    public @NotNull UUID getPlayerId() {
        return player.getUniqueId();
    }

    public int getMaxHomesSize() {
        int currentNumber = plugin.getConfiguration().getDefaultHomeNumber();
        if (player.hasPermission(Permissions.TELEPORT_HOME_UNLIMITED)) {
            return profile.getHomesSize() + 1;
        }
        for (Map.Entry<String, Integer> entry : plugin.getConfiguration().getHomePermissionGroups().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                currentNumber = Math.max(entry.getValue(), currentNumber);
            }
        }
        return currentNumber;
    }

    public TeleportRequestResponse addTeleportRequest(@NotNull User requester, boolean requesterTeleporting) {
        synchronized (teleportRequests) {
            Iterator<TeleportRequest> iterator = teleportRequests.iterator();
            while (iterator.hasNext()) {
                TeleportRequest teleportRequest = iterator.next();
                // Removed expired
                if (teleportRequest.isExpired(plugin.getConfiguration().getTeleportRequestDuration())) {
                    iterator.remove();
                    continue;
                }
                // Check if we should update it
                if (teleportRequest.getRequester().getPlayerId().equals(getPlayerId())) {
                    // If same teleporting player, update it and cancel
                    if ((requesterTeleporting &&
                         teleportRequest.getTeleporting().getPlayerId().equals(requester.getPlayerId())) ||
                        (!requesterTeleporting &&
                         teleportRequest.getTeleporting().getPlayerId().equals(getPlayerId()))) {
                        teleportRequest.updateAskingTime();
                        return TeleportRequestResponse.UPDATED;
                    }
                    // Otherwise, just cancel
                    return TeleportRequestResponse.NOT_UPDATED;
                }
            }
            User teleporting = requesterTeleporting ? requester : this;
            User destination = requesterTeleporting ? this : requester;
            teleportRequests.addLast(new TeleportRequest(requester, teleporting, destination));
            return TeleportRequestResponse.NEW_REQUEST;
        }
    }

    public List<TeleportRequest> getTeleportRequests() {
        synchronized (teleportRequests) {
            // Remove expired requests
            teleportRequests.removeIf(teleportRequest -> teleportRequest.isExpired(plugin.getConfiguration()
                                                                                         .getTeleportRequestDuration()));
            return List.copyOf(teleportRequests);
        }
    }

    public boolean isTeleporting() {
        return this.teleportTask != null;
    }

    public long getTeleportCooldownSeconds() {
        if (player.hasPermission(Permissions.TELEPORT_NO_COOLDOWN)) {
            return 0;
        }
        if (profile.getLastTeleportDate().isEmpty()) {
            return 0;
        }
        ZonedDateTime lastTeleport = profile.getLastTeleportDate().get();
        ZonedDateTime endOfCooldown = lastTeleport.plus(plugin.getConfiguration().getTeleportCooldown());
        ZonedDateTime now = ZonedDateTime.now();
        if (now.isAfter(endOfCooldown)) {
            return 0;
        }
        return Math.max(1L, now.until(endOfCooldown, ChronoUnit.SECONDS));
    }

    public TeleportResult isPlayerCanTeleport() {
        if (player.isDead() || !player.isOnline()) {
            return TeleportResult.PLAYER_UNAVAILABLE;
        }
        if (isInCombat()) {
            return TeleportResult.IN_COMBAT;
        }
        if (isTeleporting()) {
            return TeleportResult.ALREADY_TELEPORTING;
        }
        if (player.hasPermission(Permissions.TELEPORT_NO_COOLDOWN)) {
            return TeleportResult.ALLOWED;
        }
        if (profile.getLastTeleportDate().isPresent()) {
            ZonedDateTime lastTeleport = profile.getLastTeleportDate().get();
            ZonedDateTime endOfCooldown = lastTeleport.plus(plugin.getConfiguration().getTeleportCooldown());
            if (ZonedDateTime.now().isBefore(endOfCooldown)) {
                return TeleportResult.IN_COOLDOWN;
            }
        }
        return TeleportResult.ALLOWED;
    }

    public boolean isPlayerTeleportBlocked(boolean warnPlayer) {
        TeleportResult playerCanTeleport = isPlayerCanTeleport();
        if (warnPlayer) {
            switch (playerCanTeleport) {
                case IN_COOLDOWN -> player.sendMessage(plugin.getConfiguration()
                                                             .getTeleportInCooldown(getTeleportCooldownSeconds()));
                case PLAYER_UNAVAILABLE -> player.sendMessage(plugin.getConfiguration().getTeleportFailed());
                case ALREADY_TELEPORTING -> player.sendMessage(plugin.getConfiguration()
                                                                     .getTeleportAlreadyTeleporting());
                case IN_COMBAT -> player.sendMessage(plugin.getConfiguration().getTeleportPlayerInCombat());
            }
        }
        return playerCanTeleport.isBlocked();
    }

    public void setTeleportTask(@Nullable TeleportTask teleportTask) {
        if (this.teleportTask != null && teleportTask != this.teleportTask) {
            TeleportTask oldTask = this.teleportTask;
            this.teleportTask = null;
            oldTask.cancelTask();
        }
        this.teleportTask = teleportTask;
    }

    public void teleport(@NotNull Location destination, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        if (player.hasPermission(Permissions.TELEPORT_NO_DELAY)) {
            teleportNow(destination, cause);
        } else {
            setTeleportTask(new TeleportTask(plugin,
                                             this,
                                             new TeleportDestination.Location(destination),
                                             null,
                                             cause,
                                             plugin.getConfiguration().getTeleportDelayTicks()));
        }
    }

    public void teleport(@NotNull User destination, @NotNull PlayerTeleportEvent.TeleportCause cause) {
        if (player.hasPermission(Permissions.TELEPORT_NO_DELAY)) {
            teleportNow(destination.getPlayer().getLocation(), cause);
        } else {
            setTeleportTask(new TeleportTask(plugin,
                                             this,
                                             new TeleportDestination.Player(destination.getPlayer()),
                                             destination,
                                             cause,
                                             plugin.getConfiguration().getTeleportDelayTicks()));
        }
    }

    public CompletableFuture<Boolean> teleportNow(@NotNull Location location,
                                                  @NotNull PlayerTeleportEvent.TeleportCause cause) {
        Location oldLocation = player.getLocation().clone();
        setTeleportTask(null);
        return player.teleportAsync(location, cause).whenComplete((success, throwable) -> {
            if (throwable != null || success == null || !success) {
                player.sendMessage(plugin.getConfiguration().getTeleportFailed());
                return;
            }
            profile.setBackLocation(oldLocation);
            profile.setLastTeleportDate();
        });
    }

    public boolean isInCombat() {
        return this.combatTask != null;
    }

    public void setCombatTask(@NotNull CombatTask.Type combatType, int durationTicks) {
        setCombatTask(new CombatTask(plugin, this, combatType, durationTicks));
    }

    public void setCombatTask(@Nullable CombatTask combatTask) {
        // Check if player has bypass permission and remove combat if existing
        if (player.hasPermission(Permissions.COMBAT_BYPASS)) {
            // Cancel existing task if player received permission afterwards
            if (this.combatTask != null) {
                CombatTask oldTask = this.combatTask;
                this.combatTask = null;
                oldTask.cancelTask();
            }
            return;
        }
        // Prevent replacing "stronger" tasks
        if (combatTask != null && this.combatTask != null) {
            CombatTask.Type newType = combatTask.getType();
            CombatTask.Type oldType = this.combatTask.getType();
            if (!newType.canOverride(oldType)) {
                return;
            }
            if (newType.canResetTime(oldType)) {
                this.combatTask.resetTime();
                return;
            }
        }
        // Cancel other task
        if (this.combatTask != null && combatTask != this.combatTask) {
            // Prevent stackoverflow
            CombatTask oldTask = this.combatTask;
            this.combatTask = null;
            oldTask.cancelTask(); // this will call setCombatTask(null)
        }
        this.combatTask = combatTask;
    }

    public Optional<ZonedDateTime> getHardcoreExpirationDate() {
        return profile.getDeathDateTime().map(time -> time.plus(getHardcoreBanTime()));
    }

    private Duration getHardcoreBanTime() {
        if (player.hasPermission(Permissions.HARDCORE_BYPASS)) {
            return Duration.ZERO;
        }
        long seconds = plugin.getConfiguration().getHardcoreDefaultBanTime();
        for (Map.Entry<String, Integer> entry : plugin.getConfiguration().getHardcoreBanTimeGroups().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                seconds = Math.min(entry.getValue(), seconds);
            }
        }
        return Duration.ofSeconds(seconds);
    }

    public boolean isTotemInCooldown() {
        Optional<ZonedDateTime> optionalDate = profile.getLastTotemUsedDate();
        if (optionalDate.isEmpty()) {
            return false;
        }
        ZonedDateTime cooldownEndDate = optionalDate.get().plus(plugin.getConfiguration().getTotemCooldown());
        return ZonedDateTime.now().isBefore(cooldownEndDate);
    }

    public void runTick() {
        if (combatTask != null) {
            combatTask.run();
        }
        if (teleportTask != null) {
            teleportTask.run();
        }
    }

    public enum TeleportResult {

        PLAYER_UNAVAILABLE, IN_COMBAT, ALREADY_TELEPORTING, IN_COOLDOWN, ALLOWED;

        public boolean isAllowed() {
            return this == ALLOWED;
        }

        public boolean isBlocked() {
            return !isAllowed();
        }
    }

    public enum TeleportRequestResponse {
        NOT_UPDATED, UPDATED, NEW_REQUEST
    }
}
