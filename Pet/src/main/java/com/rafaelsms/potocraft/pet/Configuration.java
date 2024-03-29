package com.rafaelsms.potocraft.pet;

import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Configuration extends YamlFile {

    private final @NotNull PetPlugin plugin;
    private final @NotNull Random random = new Random();

    public Configuration(@NotNull PetPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        this.plugin = plugin;
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

    public long getSaveProfileTaskTimer() {
        return Objects.requireNonNull(getLongOrNull("configuration.database.save_player_timer_task_ticks"));
    }

    public long getPetTargetSetterTimer() {
        return Objects.requireNonNull(getLongOrNull("configuration.set_target_timer_ticks"));
    }

    public long getDistanceCheckerTimer() {
        return Objects.requireNonNull(getLongOrNull("configuration.check_distance_timer_ticks"));
    }

    public double getMaxDistanceFromOwner() {
        return Objects.requireNonNull(getDoubleOrNull("configuration.max_distance_from_player"));
    }

    public double getMinDistanceToTarget() {
        return Objects.requireNonNull(getDoubleOrNull("configuration.min_distance_to_target"));
    }

    public float getPetSpeedMultiplier() {
        return Objects.requireNonNull(getDoubleOrNull("configuration.pet_speed_multiplier")).floatValue();
    }

    public int getPetAwayForDamageTicks() {
        return Objects.requireNonNull(getIntOrNull("configuration.ticks_after_damage_pet_away"));
    }

    public @NotNull EntityType getDefaultPetType() {
        String name = getOrThrow("configuration.default_pet_type");
        try {
            return EntityType.valueOf(Objects.requireNonNull(name).toUpperCase());
        } catch (IllegalArgumentException exception) {
            plugin.logger().warn("Failed to parse entity type {}:", name, exception);
            return EntityType.CHICKEN;
        }
    }

    private List<String> getDefaultPetNameList() {
        return getOrThrow("configuration.default_pet_names");
    }

    public String getRandomDefaultPetNameList() {
        List<String> nameList = getDefaultPetNameList();
        return nameList.get(random.nextInt(nameList.size()));
    }

    public Component getFailedToRetrieveProfile() {
        return TextUtil.toComponent(getOrThrow("language.failed_to_retrieve_profile"));
    }

    public Component getPlayerOnlyCommand() {
        return TextUtil.toComponent(getOrThrow("language.command_for_players_only"));
    }

    public Component getCommandHelp() {
        return TextUtil.toComponent(getOrThrow("language.pet_command.help"));
    }

    public Component getCommandEntityTypeUnavailable() {
        return TextUtil.toComponent(getOrThrow("language.pet_command.entity_type_unavailable"));
    }

    public Component getCommandEntityTypeList(@NotNull Collection<EntityType> allowedEntityTypes) {
        return TextUtil.toComponent(getOrThrow("language.pet_command.allowed_entity_types"),
                                    Placeholder.unparsed("list",
                                                         TextUtil.joinStrings(allowedEntityTypes,
                                                                              ", ",
                                                                              type -> type.name().toLowerCase())));
    }

    public Component getPetNameTooLong() {
        return TextUtil.toComponent(getOrThrow("language.pet_command.pet_name_too_long"));
    }

    public Component getCommandPetEnabled() {
        return TextUtil.toComponent(getOrThrow("language.pet_command.pet_enabled"));
    }

    public Component getCommandPetDisabled() {
        return TextUtil.toComponent(getOrThrow("language.pet_command.pet_disabled"));
    }

    public Component getPetWentAwayBecauseCombat() {
        return TextUtil.toComponent(getOrThrow("language.pet_went_away_because_of_combat"));
    }

    public Component getPetCameBackAfterCombat() {
        return TextUtil.toComponent(getOrThrow("language.pet_came_back_after_combat"));
    }
}
