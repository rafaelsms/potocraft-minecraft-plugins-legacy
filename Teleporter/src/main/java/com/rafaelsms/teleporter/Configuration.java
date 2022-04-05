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
}
