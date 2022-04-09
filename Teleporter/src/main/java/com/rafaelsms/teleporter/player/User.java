package com.rafaelsms.teleporter.player;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.plugin.PluginUtil;
import com.rafaelsms.potocraft.plugin.combat.CombatType;
import com.rafaelsms.potocraft.plugin.player.BaseUser;
import com.rafaelsms.potocraft.util.TickableTask;
import com.rafaelsms.teleporter.TeleporterPlugin;
import com.rafaelsms.teleporter.teleports.TeleportDestination;
import com.rafaelsms.teleporter.teleports.TeleportRequest;
import com.rafaelsms.teleporter.teleports.TeleportRequestResult;
import com.rafaelsms.teleporter.teleports.TeleportResult;
import com.rafaelsms.teleporter.teleports.TeleportingTask;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class User extends BaseUser<Profile> {

    private final @NotNull TeleporterPlugin plugin;
    private final Map<User, TeleportRequest> teleportRequestMap = new HashMap<>();

    private @Nullable Combat combat = null;
    private @Nullable TeleportingTask teleportingTask = null;

    public User(@Nullable Profile profile, @Nullable Player player, @NotNull TeleporterPlugin plugin) throws
                                                                                                      DatabaseException {
        super(profile, player);
        this.plugin = plugin;
    }

    @Override
    public void tick() {
        getProfile().tick();
        if (combat != null) {
            combat.tick();
        }
        if (teleportingTask != null) {
            teleportingTask.tick();
        }
        teleportRequestMap.values().forEach(TeleportRequest::tick);
    }

    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    public @NotNull Optional<CombatType> getCombatType() {
        return Optional.ofNullable(combat).map(Combat::getCombatType);
    }

    public boolean isInCombat() {
        return getCombatType().isPresent();
    }

    public boolean isInCombat(@NotNull CombatType requiredCombatType) {
        Optional<CombatType> combatType = getCombatType();
        return combatType.isPresent() && requiredCombatType.isHigherOrEqualPriority(combatType.get());
    }

    public void setCombatTicks(@NotNull CombatType combatType) {
        if (combatType.isHigherOrEqualPriority(getCombatType().orElse(null))) {
            int combatTicks = plugin.getConfiguration().getCombatTicks(combatType);
            if (combatTicks <= 0) {
                return;
            }
            this.combat = new Combat(combatType, combatTicks);
        }
    }

    public void clearCombat() {
        this.combat = null;
    }

    public @NotNull TeleportResult getTeleportStatus(boolean checkCooldown) {
        if (!getPlayer().isOnline() || !getPlayer().isValid() || !PluginUtil.isOnSurvival(getPlayer())) {
            return TeleportResult.PLAYER_OFFLINE;
        }
        if (getPlayer().isDead()) {
            return TeleportResult.PLAYER_DEAD;
        }
        if (isInCombat()) {
            return TeleportResult.PLAYER_IN_COMBAT;
        }
        if (isTeleporting()) {
            return TeleportResult.ALREADY_TELEPORTING;
        }
        if (checkCooldown && getProfile().getTeleportCooldownTicks() > 0) {
            return TeleportResult.TELEPORT_IN_COOLDOWN;
        }
        return TeleportResult.SUCCESS;
    }

    public boolean isTeleporting() {
        return teleportingTask != null && !teleportingTask.hasTaskEnded();
    }

    public @NotNull CompletableFuture<TeleportResult> teleport(@NotNull TeleportDestination destination,
                                                               boolean checkCooldown) {
        TeleportResult canTeleportResult = getTeleportStatus(checkCooldown);
        if (canTeleportResult.isNegative()) {
            return CompletableFuture.completedFuture(canTeleportResult);
        }
        if (getPlayer().hasPermission(plugin.getPermissions().getBypassTeleportTimer())) {
            return teleportNow(destination);
        }
        this.teleportingTask = new TeleportingTask(plugin,
                                                   this,
                                                   destination,
                                                   plugin.getConfiguration().getTeleportTaskDurationTicks());
        return teleportingTask.getFuture();
    }

    public @NotNull CompletableFuture<TeleportResult> teleportNow(@NotNull TeleportDestination destination) {
        CompletableFuture<TeleportResult> future = new CompletableFuture<>();
        TeleportResult canTeleportResult = getTeleportStatus(false);
        if (canTeleportResult.isNegative()) {
            future.complete(canTeleportResult);
            return future;
        }
        if (!destination.isAvailable()) {
            future.complete(TeleportResult.LOCATION_UNAVAILABLE);
            return future;
        }
        getPlayer().teleportAsync(destination.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND)
                   .thenApply(result -> future.complete(result ?
                                                                TeleportResult.SUCCESS :
                                                                TeleportResult.LOCATION_UNAVAILABLE))
                   .exceptionally(future::completeExceptionally);
        return future;
    }

    public void clearTeleportingTask() {
        this.teleportingTask = null;
    }

    public @NotNull TeleportRequestResult addTeleportRequest(@NotNull User requester, @NotNull User teleporting) {
        if (!getProfile().isAcceptingTeleportRequests()) {
            return TeleportRequestResult.USER_IS_NOT_ACCEPTING_REQUESTS;
        }
        TeleportRequest newRequest = new TeleportRequest(plugin, this, requester, teleporting);
        TeleportRequest existingRequest = teleportRequestMap.get(requester);
        if (existingRequest != null) {
            if (!Objects.equals(existingRequest.getTeleporting().getUniqueId(),
                                newRequest.getTeleporting().getUniqueId())) {
                teleportRequestMap.put(requester, newRequest);
                return TeleportRequestResult.REQUEST_REPLACED;
            } else {
                existingRequest.restartDuration();
                return TeleportRequestResult.REQUEST_RENEWED;
            }
        } else {
            boolean thisUserTeleporting = Objects.equals(teleporting.getUniqueId(), getUniqueId());
            getPlayer().sendMessage(plugin.getConfiguration()
                                          .getTeleportRequestReceived(requester.getPlayer().name(),
                                                                      thisUserTeleporting));
            teleportRequestMap.put(requester, newRequest);
            return TeleportRequestResult.REQUESTED;
        }
    }

    public @NotNull Optional<TeleportRequest> removeTeleportRequest(@NotNull User requester) {
        return Optional.ofNullable(teleportRequestMap.remove(requester));
    }

    private class Combat implements TickableTask {

        private final @NotNull CombatType combatType;
        private int combatTicks;

        private Combat(@NotNull CombatType combatType, int initialCombatTicks) {
            this.combatType = combatType;
            this.combatTicks = initialCombatTicks;
        }

        @Override
        public void tick() {
            combatTicks--;
            if (combatTicks <= 0) {
                clearCombat();
            }
        }

        public @NotNull CombatType getCombatType() {
            return combatType;
        }
    }
}
