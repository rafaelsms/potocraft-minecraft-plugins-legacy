package com.rafaelsms.potocraft.serverutility.util;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpamMessageController {

    private final Map<Type, ZonedDateTime> lastMessage = new HashMap<>();
    private BukkitTask spamMessageRemoval = null;

    public synchronized void registerMessage(@NotNull Type messageType) {
        lastMessage.put(messageType, ZonedDateTime.now());
    }

    public synchronized @NotNull Optional<ZonedDateTime> checkLastRecord(@NotNull Type messageType) {
        return Optional.ofNullable(lastMessage.get(messageType));
    }

    public synchronized void setRemovalTask(@Nullable BukkitTask spamMessageRemoval) {
        // Cancel previously removal task
        if (this.spamMessageRemoval != null) {
            this.spamMessageRemoval.cancel();
        }
        this.spamMessageRemoval = spamMessageRemoval;
    }

    public enum Type {
        DEATH_MESSAGE, JOIN_QUIT_MESSAGES
    }
}
