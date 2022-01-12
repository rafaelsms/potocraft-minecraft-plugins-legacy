package com.rafaelsms.potocraft.papermc.user;

import com.rafaelsms.potocraft.common.Permissions;
import com.rafaelsms.potocraft.common.user.ChatHistory;
import com.rafaelsms.potocraft.common.user.User;
import com.rafaelsms.potocraft.papermc.PaperPlugin;
import com.rafaelsms.potocraft.papermc.database.ServerProfile;
import com.rafaelsms.potocraft.papermc.user.combat.CombatTask;
import com.rafaelsms.potocraft.papermc.user.teleport.TeleportTask;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PaperUser extends User {

    private final @NotNull Player player;
    private final @NotNull PaperPlugin plugin;

    private final @NotNull GlobalChatHistory globalChatHistory;
    private final @NotNull LocalChatHistory localChatHistory;

    private final @NotNull ServerProfile serverProfile;

    private @Nullable BukkitTask tickingTask = null;
    private @Nullable TeleportTask teleportTask = null;
    private @Nullable CombatTask combatTask = null;

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

    public Optional<TeleportTask> getTeleportTask() {
        return Optional.ofNullable(this.teleportTask);
    }

    public boolean canTeleport() {
        // Check player status
        if (player.isDead() || !player.isOnline() && getCombatTask().isPresent()) {
            return false;
        }
        // Check profile status
        Optional<ZonedDateTime> lastTeleportDate = this.serverProfile.getLastTeleportDate();
        ZonedDateTime teleportCooldown = ZonedDateTime.now().minus(plugin.getSettings().getTeleportCooldown());
        return lastTeleportDate.isEmpty() || lastTeleportDate.get().isBefore(teleportCooldown);
    }

    public void setTeleportTask(@Nullable TeleportTask teleportTask) {
        this.teleportTask = teleportTask;
    }

    public Optional<CombatTask> getCombatTask() {
        return Optional.ofNullable(this.combatTask);
    }

    public void setCombatTask(@Nullable CombatTask combatTask) {
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
