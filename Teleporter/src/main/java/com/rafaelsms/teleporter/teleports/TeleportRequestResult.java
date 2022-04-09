package com.rafaelsms.teleporter.teleports;

import com.rafaelsms.teleporter.TeleporterPlugin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum TeleportRequestResult {

    REQUESTED, REQUEST_RENEWED, REQUEST_REPLACED, USER_IS_NOT_ACCEPTING_REQUESTS;

    public @NotNull Optional<Component> getStatusMessage(@NotNull TeleporterPlugin plugin) {
        return Optional.of(switch (this) {
            case REQUESTED -> plugin.getConfiguration().getTeleportRequestRequested();
            case USER_IS_NOT_ACCEPTING_REQUESTS -> plugin.getConfiguration().getTeleportRequestUserNotAccepting();
            case REQUEST_RENEWED -> plugin.getConfiguration().getTeleportRequestRequestUpdated();
            case REQUEST_REPLACED -> plugin.getConfiguration().getTeleportRequestRequestReplaced();
        });
    }

}
