package com.rafaelsms.potocraft.velocity.listeners;

import com.rafaelsms.potocraft.common.profile.ReportEntry;
import com.rafaelsms.potocraft.velocity.VelocityPlugin;
import com.rafaelsms.potocraft.velocity.profile.VelocityProfile;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ReportChecker {

    private final @NotNull VelocityPlugin plugin;

    public ReportChecker(@NotNull VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    private void onLogin(LoginEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        CompletableFuture.runAsync(() -> {
            try {
                // Get player profile
                Optional<VelocityProfile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
                if (profileOptional.isEmpty()) {
                    continuation.resume();
                    return;
                }
                // Check in every report if prevents joining
                VelocityProfile profile = profileOptional.get();
                for (ReportEntry reportEntry : profile.getReportEntries()) {
                    if (reportEntry.isPreventsJoining()) {
                        event.setResult(ResultedEvent.ComponentResult.denied(reportEntry.getMessage(plugin)));
                        continuation.resume();
                        return;
                    }
                }
            } catch (Exception ignored) {
                Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
                event.setResult(ResultedEvent.ComponentResult.denied(reason));
            }
            continuation.resume();
        });
    }

    @Subscribe
    private void onChat(PlayerChatEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        CompletableFuture.runAsync(() -> {
            try {
                // Get player profile
                Optional<VelocityProfile> profileOptional = plugin.getDatabase().getProfile(player.getUniqueId());
                if (profileOptional.isEmpty()) {
                    Component reason = plugin.getSettings().getCommandMustRegisterFirst();
                    player.sendMessage(reason);
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                    continuation.resume();
                    return;
                }
                // Check in every report if prevents joining
                VelocityProfile profile = profileOptional.get();
                for (ReportEntry reportEntry : profile.getReportEntries()) {
                    if (reportEntry.isPreventsChatting()) {
                        player.sendMessage(reportEntry.getMessage(plugin));
                        event.setResult(PlayerChatEvent.ChatResult.denied());
                        continuation.resume();
                        return;
                    }
                }
            } catch (Exception ignored) {
                Component reason = plugin.getSettings().getKickMessageCouldNotRetrieveProfile();
                player.sendMessage(reason);
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }
            continuation.resume();
        });
    }
}
