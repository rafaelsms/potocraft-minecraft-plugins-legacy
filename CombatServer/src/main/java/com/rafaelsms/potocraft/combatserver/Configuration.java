package com.rafaelsms.potocraft.combatserver;

import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class Configuration extends YamlFile {

    public Configuration(@NotNull CombatServerPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public String getMongoURI() {
        return getOrThrow("configuration.database.mongo_uri");
    }

    public String getMongoDatabaseName() {
        return getOrThrow("configuration.database.database_name");
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return getOrThrow("configuration.database.is_exception_fatal");
    }

    public String getMongoPlayerCollectionName() {
        return getOrThrow("configuration.database.player_collection_name");
    }

    public String getMongoKitCollectionName() {
        return getOrThrow("configuration.database.kit_collection_name");
    }

    public long getSaveProfileTaskTimer() {
        return Objects.requireNonNull(getLongOrNull("configuration.database.save_player_timer_task_ticks"));
    }

    public Component getFailedToRetrieveProfile() {
        return TextUtil.toComponent(getOrThrow("language.failed_to_retrieve_profile"));
    }

    public Component getPlayerOnlyCommand() {
        return TextUtil.toComponent(getOrThrow("language.player_only_command"));
    }

    public Component getSomethingWentWrong() {
        return TextUtil.toComponent(getOrThrow("language.something_went_wrong"));
    }

    public Component getKitCommandHelp() {
        return TextUtil.toComponent(getOrThrow("language.command.kit.help"));
    }
}
