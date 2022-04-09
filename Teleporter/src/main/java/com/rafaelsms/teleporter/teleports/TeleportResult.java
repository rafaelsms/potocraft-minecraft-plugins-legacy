package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.teleporter.TeleporterPlugin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum TeleportResult {

    SUCCESS,
    LOCATION_UNAVAILABLE,
    PLAYER_IN_COMBAT,
    ALREADY_TELEPORTING,
    TELEPORT_IN_COOLDOWN,
    PLAYER_OFFLINE,
    PLAYER_DEAD;

    public boolean isNegative() {
        return this != SUCCESS;
    }

    public Optional<Component> getTeleportStatusMessage(@NotNull TeleporterPlugin plugin) {
        return Optional.of(switch (this) {
            case ALREADY_TELEPORTING -> plugin.getConfiguration().getTeleportStatusAlreadyTeleporting();
            case PLAYER_DEAD -> plugin.getConfiguration().getTeleportStatusUserIsDead();
            case TELEPORT_IN_COOLDOWN -> plugin.getConfiguration().getTeleportStatusInCooldown();
            case PLAYER_IN_COMBAT -> plugin.getConfiguration().getTeleportStatusUserInCombat();
            case LOCATION_UNAVAILABLE -> plugin.getConfiguration().getTeleportStatusLocationUnavailable();
            case PLAYER_OFFLINE -> plugin.getConfiguration().getTeleportStatusUserIsOffline();
            case SUCCESS -> plugin.getConfiguration().getTeleportStatusUserIsTeleporting();
        });
    }
}
