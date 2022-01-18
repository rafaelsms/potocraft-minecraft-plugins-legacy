package com.rafaelsms.potocraft.hardcore.listeners;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.hardcore.HardcorePlugin;
import com.rafaelsms.potocraft.hardcore.Permissions;
import com.rafaelsms.potocraft.hardcore.players.Profile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class BannedChecker implements Listener {

    private final @NotNull HardcorePlugin plugin;

    public BannedChecker(@NotNull HardcorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void banPlayerOnDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        // Ignore players with permission
        if (player.hasPermission(Permissions.BAN_BYPASS)) {
            return;
        }

        // Attempt to get a profile
        Optional<Profile> profileOptional = plugin.getDatabase().getPlayerProfile(player.getUniqueId());
        if (profileOptional.isEmpty()) {
            // Ban through Paper if retrieving failed
            paperBanPlayer(player);
            return;
        }

        // Update profile and save it
        Profile profile = profileOptional.get();
        profile.setDeathDate();
        try {
            plugin.getDatabase().savePlayerProfile(profile);
        } catch (Database.DatabaseException ignored) {
            // Ban through Paper if saving failed
            paperBanPlayer(player);
            return;
        }

        // Kick player after saving profile
        ZonedDateTime expirationDate =
                profile.getDeathDate().orElse(ZonedDateTime.now()).plus(plugin.getConfiguration().getDeathBanTime());
        Component bannedMessage = plugin.getConfiguration().getBannedMessage(expirationDate);
        player.kick(bannedMessage);
    }

    private void paperBanPlayer(@NotNull Player player) {
        ZonedDateTime dateTime = ZonedDateTime.now().plus(plugin.getConfiguration().getDeathBanTime());
        String banReason = plugin.getConfiguration().getDatabaseFailureBanReason();
        player.banPlayer(banReason, Date.from(dateTime.toInstant()));
    }

    @EventHandler(ignoreCancelled = true)
    private void checkBanOnLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerId = event.getUniqueId();
        Optional<Profile> profileOptional = plugin.getDatabase().getPlayerProfile(playerId);
        if (profileOptional.isEmpty()) {
            Component message = plugin.getConfiguration().getFailedToRetrieveProfile();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
            return;
        }
        Profile profile = profileOptional.get();
        Optional<ZonedDateTime> deathDate = profile.getDeathDate();
        // Allow players to join if they didn't die yet
        if (deathDate.isEmpty()) {
            return;
        }

        // Calculate expiration date time
        ZonedDateTime expirationDate = deathDate.get().plus(plugin.getConfiguration().getDeathBanTime());
        // Allow if it already expired
        if (expirationDate.isBefore(ZonedDateTime.now())) {
            return;
        }

        // Prevent join if expiration date is in the future
        Component bannedMessage = plugin.getConfiguration().getBannedMessage(expirationDate);
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, bannedMessage);
    }
}
