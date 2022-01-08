package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.common.profile.ReportEntry;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ReportChecker {

    private final @NotNull VelocityPlugin plugin;

    public ReportChecker(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        plugin.getDatabase().getProfile(player.getUniqueId()).whenComplete((profile, throwable) -> {
            if (throwable != null) {
                Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
                continuation.resume();
                return;
            } else if (profile != null) {
                // Check in every report if prevents joining
                for (ReportEntry reportEntry : profile.getReportEntries()) {
                    if (reportEntry.isPreventsJoining()) {
                        event.setResult(ResultedEvent.ComponentResult.denied(reportEntry.getMessage(plugin)));
                        return;
                    }
                }
            }
            continuation.resume();
        });
    }

    @Subscribe
    private void onChat(PlayerChatEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        plugin.getDatabase().getProfile(player.getUniqueId()).whenComplete((profile, throwable) -> {
            if (throwable != null) {
                Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
                player.sendMessage(reason);
                event.setResult(PlayerChatEvent.ChatResult.denied());
                continuation.resume();
                return;
            } else if (profile != null) {
                // Check in every report if prevents joining
                for (ReportEntry reportEntry : profile.getReportEntries()) {
                    if (reportEntry.isPreventsChatting()) {
                        player.sendMessage(reportEntry.getMessage(plugin));
                        event.setResult(PlayerChatEvent.ChatResult.denied());
                        continuation.resume();
                        return;
                    }
                }
            } else {
                // Don't allow unregistered players to chat
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }
            continuation.resume();
        });
    }
}
