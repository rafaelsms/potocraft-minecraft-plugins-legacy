package com.rafaelsms.teleporter;

import com.rafaelsms.potocraft.plugin.BaseConfiguration;
import com.rafaelsms.potocraft.plugin.BaseJavaPlugin;
import com.rafaelsms.potocraft.plugin.combat.CombatType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Configuration extends BaseConfiguration {

    protected Configuration(@NotNull BaseJavaPlugin plugin) throws IOException {
        super(plugin);
    }

    @Override
    public @NotNull String getMongoURI() {
        return null;
    }

    @Override
    public @NotNull String getMongoDatabaseName() {
        return null;
    }

    @Override
    public @NotNull String getProfileCollectionName() {
        return null;
    }

    @Override
    public int getProfileSavingTaskTimer() {
        return 0;
    }

    @Override
    public @NotNull Component getCommandIsPlayerOnly() {
        return null;
    }

    @Override
    public @NotNull Component getFailedToRetrieveUserProfile() {
        return null;
    }

    public int getTeleportRequestDurationTicks() {
        return 0;
    }

    public int getCombatTicks(@NotNull CombatType combatType) {
        return 0;
    }

    public int getTeleportTaskDurationTicks() {
        return 0;
    }

    public @NotNull Component getProgressBarTeleporting() {
        return null;
    }

    public @NotNull Component getTeleportRequestExpired(@NotNull Component requesterName) {
        return null;
    }

    public @NotNull Component getTeleportCommandHelp() {
        return null;
    }

    public @NotNull Component getTeleportStatusAlreadyTeleporting() {
        return null;
    }

    public @NotNull Component getTeleportStatusUserIsDead() {
        return null;
    }

    public @NotNull Component getTeleportStatusInCooldown() {
        return null;
    }

    public @NotNull Component getTeleportStatusUserInCombat() {
        return null;
    }

    public @NotNull Component getTeleportStatusLocationUnavailable() {
        return null;
    }

    public @NotNull Component getTeleportStatusUserIsOffline() {
        return null;
    }

    public @NotNull Component getTeleportStatusUserIsTeleporting() {
        return null;
    }

    public @NotNull Component getCommandPlayerNotFound() {
        return null;
    }

    public @NotNull Component getTeleportFailed() {
        return null;
    }

    public @NotNull Component getTeleportRequestRequested() {
        return null;
    }

    public @NotNull Component getTeleportRequestUserNotAccepting() {
        return null;
    }

    public @NotNull Component getTeleportRequestRequestUpdated() {
        return null;
    }

    public @NotNull Component getTeleportRequestRequestReplaced() {
        return null;
    }

    public @NotNull Component getTeleportRequestReceived(@NotNull Component playerName, boolean thisUserTeleporting) {
        return null;
    }
}
