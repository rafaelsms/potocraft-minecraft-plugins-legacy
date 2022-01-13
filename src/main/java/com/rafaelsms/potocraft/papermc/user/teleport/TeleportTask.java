package com.rafaelsms.potocraft.papermc.user.teleport;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.user.PaperUser;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TeleportTask implements Runnable {

    private final @NotNull PaperPlugin plugin;

    private final @NotNull PaperUser teleporting;
    private final @NotNull TeleportDestination destination;
    private final @NotNull PlayerTeleportEvent.TeleportCause cause;

    private final @Nullable PaperUser participant;
    private final long initialDelay;
    private final boolean warnTeleporting;
    private final boolean warnParticipant;

    private final @NotNull BossBar progressBar;
    private long remainingTicks;

    private TeleportTask(@NotNull PaperPlugin plugin,
                         @NotNull PaperUser teleporting,
                         @Nullable PaperUser participant,
                         @NotNull TeleportDestination destination,
                         @NotNull PlayerTeleportEvent.TeleportCause cause,
                         @Nullable Long initialDelay,
                         @Nullable Boolean warnTeleporting,
                         @Nullable Boolean warnParticipant) {
        this.plugin = plugin;
        this.teleporting = teleporting;
        this.participant = participant;
        this.destination = destination;
        this.cause = cause;
        long defaultDelay = plugin.getSettings().getTeleportDelayTicks();
        this.initialDelay =
                handleInitialDelay(teleporting, participant, Objects.requireNonNullElse(initialDelay, defaultDelay));
        this.remainingTicks = this.initialDelay;
        this.warnTeleporting = Objects.requireNonNullElse(warnTeleporting, true);
        this.warnParticipant = Objects.requireNonNullElse(warnParticipant, true);
        this.progressBar = BossBar.bossBar(plugin.getSettings().getTeleportTitle(),
                                           BossBar.MIN_PROGRESS,
                                           BossBar.Color.YELLOW,
                                           BossBar.Overlay.PROGRESS);
    }

    private long handleInitialDelay(@NotNull PaperUser teleporting,
                                    @Nullable PaperUser participant,
                                    long initialDelay) {
        if (teleporting.getPlayer().hasPermission(Permissions.TELEPORT_COMMAND_NO_DELAY)) {
            return 0;
        }
        if (participant != null && participant.getPlayer().hasPermission(Permissions.TELEPORT_COMMAND_NO_DELAY)) {
            return 0;
        }
        return initialDelay;
    }

    private void warn(@Nullable PaperUser user, boolean warn, @NotNull Component message) {
        if (warn && user != null && user.getPlayer().isOnline()) {
            user.getPlayer().sendMessage(message);
        }
    }

    private void warnBoth(@NotNull Component warning) {
        warn(teleporting, warnTeleporting, warning);
        warn(participant, warnParticipant, warning);
    }

    public void cancel(@NotNull Component warning) {
        warnBoth(warning);
        teleporting.getPlayer().hideBossBar(progressBar);
        teleporting.setTeleportTask(null);
    }

    public void cancel() {
        teleporting.getPlayer().hideBossBar(progressBar);
        teleporting.setTeleportTask(null);
    }

    @Override
    public void run() {
        // Check requester conditions
        if (participant != null) {
            Player participantPlayer = participant.getPlayer();
            if (!participantPlayer.isOnline() || participantPlayer.isDead()) {
                cancel(plugin.getSettings().getTeleportFailed());
                return;
            }
            if (participant.getCombatTask().isPresent()) {
                cancel(plugin.getSettings().getTeleportPlayerInCombat());
                return;
            }
            if (participant.getTeleportTask().isPresent()) {
                cancel(plugin.getSettings().getTeleportCanNotTeleportNow());
                return;
            }
        }

        // Check teleporting conditions
        Player teleportingPlayer = teleporting.getPlayer();
        if (!teleportingPlayer.isOnline() || teleportingPlayer.isDead()) {
            cancel(plugin.getSettings().getTeleportFailed());
            return;
        }
        if (teleporting.getCombatTask().isPresent()) {
            // Ignore combat check only if participant has permission to teleport without requesting
            if (participant == null ||
                !participant.getPlayer().hasPermission(Permissions.TELEPORT_COMMAND_NO_REQUEST)) {
                cancel(plugin.getSettings().getTeleportPlayerInCombat());
                return;
            }
        }

        // Check destination conditions
        if (!this.destination.isAvailable()) {
            cancel(plugin.getSettings().getTeleportDestinationUnavailable());
            return;
        }

        // Decrease remaining ticks and show progress
        remainingTicks -= 1;
        if (remainingTicks > 0) {
            float progress = (initialDelay - remainingTicks) * 1.0f / initialDelay;
            progressBar.progress(progress);
            teleportingPlayer.showBossBar(progressBar);
            return;
        }

        // Finally, teleport player
        teleporting.teleportNow(destination.getLocation(), cause).whenComplete((success, throwable) -> {
            if (throwable != null || success == null || !success) {
                warnBoth(plugin.getSettings().getTeleportFailed());
                return;
            }
            warnBoth(plugin.getSettings().getTeleportedSuccessfully());
        });
    }

    public static class Builder {

        private final @NotNull PaperPlugin plugin;

        private final @NotNull PaperUser teleporting;
        private final @NotNull TeleportDestination destination;
        private final @NotNull PlayerTeleportEvent.TeleportCause cause;

        // When TeleportTask instance is constructed:
        // Nullables will fallback to default
        // Permissions will be checked
        private @Nullable PaperUser participant = null;
        private @Nullable Long initialDelay = null;
        private @Nullable Boolean warnTeleporting = null;
        private @Nullable Boolean warnParticipant = null;

        private Builder(@NotNull PaperPlugin plugin,
                        @NotNull PaperUser teleporting,
                        @NotNull TeleportDestination destination,
                        @NotNull PlayerTeleportEvent.TeleportCause cause) {
            this.plugin = plugin;
            this.teleporting = teleporting;
            this.destination = destination;
            this.cause = cause;
        }

        public static TeleportTask build(@NotNull PaperPlugin plugin,
                                         @NotNull PaperUser teleporting,
                                         @NotNull PaperUser destination,
                                         @NotNull PlayerTeleportEvent.TeleportCause cause) {
            return new Builder(plugin, teleporting, new PlayerDestination(destination), cause).build();
        }

        public static Builder builder(@NotNull PaperPlugin plugin,
                                      @NotNull PaperUser teleporting,
                                      @NotNull PaperUser destination,
                                      @NotNull PlayerTeleportEvent.TeleportCause cause) {
            return new Builder(plugin, teleporting, new PlayerDestination(destination), cause);
        }

        public static Builder builder(@NotNull PaperPlugin plugin,
                                      @NotNull PaperUser teleporting,
                                      @NotNull Location destination,
                                      @NotNull PlayerTeleportEvent.TeleportCause cause) {
            return new Builder(plugin, teleporting, new LocationDestination(destination), cause);
        }

        public Builder warnTeleporting(boolean warnTeleporting) {
            this.warnTeleporting = warnTeleporting;
            return this;
        }

        public Builder warnParticipant(boolean warnParticipant) {
            this.warnParticipant = warnParticipant;
            return this;
        }

        public Builder withParticipant(@Nullable PaperUser participant) {
            this.participant = participant;
            return this;
        }

        public Builder withDelay(long delay) {
            this.initialDelay = delay;
            return this;
        }

        public TeleportTask build() {
            return new TeleportTask(plugin,
                                    teleporting,
                                    participant,
                                    destination,
                                    cause,
                                    initialDelay,
                                    warnTeleporting,
                                    warnParticipant);
        }
    }

}
