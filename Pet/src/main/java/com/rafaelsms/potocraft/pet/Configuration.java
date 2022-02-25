package com.rafaelsms.potocraft.pet;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class Configuration extends YamlFile {

    public Configuration(@NotNull PetPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
    }

    public String getMongoURI() {
        return get("configuration.database.mongo_uri");
    }

    public String getMongoDatabaseName() {
        return get("configuration.database.database_name");
    }

    public Boolean isMongoDatabaseExceptionFatal() {
        return get("configuration.database.is_exception_fatal");
    }

    public String getMongoPlayerCollectionName() {
        return get("configuration.database.player_collection_name");
    }

    public long getSaveProfileTaskTimer() {
        return Objects.requireNonNull(getLong("configuration.database.save_player_timer_task_ticks"));
    }

    public long getPetTargetSetterTimer() {
        return Objects.requireNonNull(getLong("configuration.set_target_timer_ticks"));
    }

    public long getDistanceCheckerTimer() {
        return Objects.requireNonNull(getLong("configuration.check_distance_timer_ticks"));
    }

    public double getMaxDistanceFromOwner() {
        return Objects.requireNonNull(getDouble("configuration.max_distance_from_player"));
    }

    public float getPetSpeedMultiplier() {
        return Objects.requireNonNull(getDouble("configuration.pet_speed_multiplier")).floatValue();
    }

    public Component getFailedToRetrieveProfile() {
        return TextUtil.toComponent(get("language.failed_to_retrieve_profile"));
    }
}
