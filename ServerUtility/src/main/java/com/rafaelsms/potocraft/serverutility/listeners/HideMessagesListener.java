package com.rafaelsms.potocraft.serverutility.listeners;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.serverutility.util.SpamMessageController;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HideMessagesListener implements Listener {

    private final Map<UUID, SpamMessageController> lastSpamMessages = Collections.synchronizedMap(new HashMap<>());

    private final @NotNull ServerUtilityPlugin plugin;

    public HideMessagesListener(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void hideJoinMessage(PlayerJoinEvent event) {
        if (plugin.getConfiguration().isHideJoinQuitMessages()) {
            event.joinMessage(Component.empty());
            return;
        }
        Duration delayMessage = plugin.getConfiguration().getDelayBetweenLoginMessages();
        if (shouldRemoveMessage(event.getPlayer(), SpamMessageController.Type.JOIN_QUIT_MESSAGES, delayMessage)) {
            event.joinMessage(Component.empty());
        }
        registerMessage(event.getPlayer(), SpamMessageController.Type.JOIN_QUIT_MESSAGES, delayMessage);
    }

    @EventHandler(ignoreCancelled = true)
    private void hideQuitMessage(PlayerQuitEvent event) {
        if (plugin.getConfiguration().isHideJoinQuitMessages()) {
            event.quitMessage(Component.empty());
            return;
        }
        Duration delayMessage = plugin.getConfiguration().getDelayBetweenLoginMessages();
        if (shouldRemoveMessage(event.getPlayer(), SpamMessageController.Type.JOIN_QUIT_MESSAGES, delayMessage)) {
            event.quitMessage(Component.empty());
        }
        registerMessage(event.getPlayer(), SpamMessageController.Type.JOIN_QUIT_MESSAGES, delayMessage);
    }

    @EventHandler(ignoreCancelled = true)
    private void hideDeathMessage(PlayerDeathEvent event) {
        Duration delayMessage = plugin.getConfiguration().getDelayBetweenDeathMessages();
        if (shouldRemoveMessage(event.getPlayer(), SpamMessageController.Type.DEATH_MESSAGE, delayMessage)) {
            event.deathMessage(Component.empty());
        }
        registerMessage(event.getPlayer(), SpamMessageController.Type.DEATH_MESSAGE, delayMessage);
    }

    private SpamMessageController getOrCreateController(@NotNull Player player) {
        SpamMessageController controller =
                lastSpamMessages.getOrDefault(player.getUniqueId(), new SpamMessageController());
        lastSpamMessages.put(player.getUniqueId(), controller);
        return controller;
    }

    private boolean shouldRemoveMessage(@NotNull Player player,
                                        @NotNull SpamMessageController.Type messageType,
                                        @NotNull Duration cooldownDuration) {
        SpamMessageController messageController = getOrCreateController(player);
        Optional<ZonedDateTime> dateTimeOptional = messageController.checkLastRecord(messageType);
        if (dateTimeOptional.isEmpty()) {
            return false;
        }
        return dateTimeOptional.get().plus(cooldownDuration).isAfter(ZonedDateTime.now());
    }

    private void registerMessage(@NotNull Player player,
                                 @NotNull SpamMessageController.Type messageType,
                                 @NotNull Duration duration) {
        SpamMessageController controller = getOrCreateController(player);
        controller.registerMessage(messageType);
        BukkitTask bukkitTask = plugin.getServer()
                                      .getScheduler()
                                      .runTaskLater(plugin,
                                                    () -> lastSpamMessages.remove(player.getUniqueId()),
                                                    duration.toSeconds() * 20L);
        controller.setRemovalTask(bukkitTask);
    }
}
