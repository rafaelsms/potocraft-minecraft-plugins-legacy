package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.serverutility.util.WorldCombatConfig;
import com.rafaelsms.potocraft.util.TextUtil;
import com.rafaelsms.potocraft.util.YamlFile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Configuration extends YamlFile {

    private final @NotNull ServerUtilityPlugin plugin;

    private final Map<String, WorldCombatConfig> worldCombatConfigurations = new HashMap<>();
    private final @NotNull WorldCombatConfig defaultCombatConfig;

    public Configuration(@NotNull ServerUtilityPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        this.plugin = plugin;

        // Parse world combat configurations
        Map<String, Map<String, Object>> worldSettings = getOrThrow("configuration.per_world_pvp_settings");
        assert worldSettings != null;
        // Parse default configuration
        Map<String, Object> defaultConfiguration = worldSettings.getOrDefault("default", Map.of());
        defaultCombatConfig = parseWorldConfiguration(defaultConfiguration, null);
        // Parse other world configurations
        for (Map.Entry<String, Map<String, Object>> entry : worldSettings.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("default")) {
                continue;
            }
            WorldCombatConfig worldCombatConfig = parseWorldConfiguration(entry.getValue(), defaultCombatConfig);
            worldCombatConfigurations.put(entry.getKey(), worldCombatConfig);
        }
    }

    public Double getOverallDamageDealtMultiplier() {
        return getDoubleOrNull("configuration.damage_modifiers.overall_damage_dealt_multiplier");
    }

    public Double getPlayerVersusPlayerDamageMultiplier() {
        return getDoubleOrNull("configuration.damage_modifiers.pvp_damage_multiplier");
    }

    public Double getCooldownDamageFactor() {
        return getDoubleOrNull("configuration.damage_modifiers.damage_cooldown_factor");
    }

    public Double getArrowDamageMultiplier() {
        return getDoubleOrNull("configuration.damage_modifiers.arrow_damage_multiplier");
    }

    public Double getArrowVelocityMultiplier() {
        return getDoubleOrNull("configuration.damage_modifiers.arrow_velocity_multiplier");
    }

    public Boolean isArrowAffectedByGravity() {
        return getOrThrow("configuration.damage_modifiers.arrow_affected_by_gravity");
    }

    public Double getLeatherArmorModifier() {
        return getDoubleOrNull("configuration.damage_modifiers.per_armor_type.leather_modifier");
    }

    public Double getIronChainArmorModifier() {
        return getDoubleOrNull("configuration.damage_modifiers.per_armor_type.iron_chain_modifier");
    }

    public Double getIronArmorModifier() {
        return getDoubleOrNull("configuration.damage_modifiers.per_armor_type.iron_modifier");
    }

    public Double getGoldArmorModifier() {
        return getDoubleOrNull("configuration.damage_modifiers.per_armor_type.gold_modifier");
    }

    public Double getDiamondArmorModifier() {
        return getDoubleOrNull("configuration.damage_modifiers.per_armor_type.diamond_modifier");
    }

    public Double getNetheriteArmorModifier() {
        return getDoubleOrNull("configuration.damage_modifiers.per_armor_type.netherite_modifier");
    }

    public List<Double> getProtectionEnchantmentArmorMultiplier() {
        return getOrThrow("configuration.damage_modifiers.per_protection_enchantment");
    }

    public String getWebhookUrl() {
        return getOrThrow("configuration.webhook_url");
    }

    public Boolean isPlayerLoggingEnabled() {
        return getOrThrow("configuration.enable_player_logging");
    }

    public Integer getDamageParticleAmount() {
        return getIntOrNull("configuration.damage_effects.damage_particle_amount");
    }

    public Boolean isSpawnLightningOnDead() {
        return getOrThrow("configuration.damage_effects.spawn_lightning_on_dead");
    }

    public Boolean isSpawnFireworkOnKiller() {
        return getOrThrow("configuration.damage_effects.spawn_firework_on_killer");
    }

    public Boolean isPreventingAllEnchantedBooks() {
        return getOrThrow("configuration.villager.prevent_all_enchanted_books");
    }

    public Boolean isPreventingTreasureEnchantedBooks() {
        return getOrThrow("configuration.villager.prevent_treasure_enchanted_books");
    }

    public Boolean isNerfVillagerEnchantedBooks() {
        return getOrThrow("configuration.villager.nerf_enchanted_books");
    }

    public Optional<Difficulty> getGameDifficulty() {
        String difficultyString = getOrThrow("configuration.game_difficulty");
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty.name().equalsIgnoreCase(difficultyString)) {
                return Optional.of(difficulty);
            }
        }
        return Optional.empty();
    }

    public Double getWorldSizeDiameter(@NotNull String worldName) {
        Map<String, Double> map = Objects.requireNonNull(getOrThrow("configuration.world_borders_radius"));
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(worldName)) {
                return entry.getValue() * 2.0;
            }
        }
        // This value is in radius on the config, but we must return the diameter
        return plugin.getServer().getMaxWorldSize() * 2.0;
    }

    @SuppressWarnings("rawtypes")
    public Map<GameRule, Object> getDefaultGameRules() {
        Map<String, Map<String, Object>> map = Objects.requireNonNull(getOrThrow("configuration.game_rules_applied"));
        return parseGameRules(map.getOrDefault("default", Map.of()));
    }

    @SuppressWarnings("rawtypes")
    public Map<GameRule, Object> getWorldGameRule(@NotNull String worldName) {
        Map<String, Map<String, Object>> map = Objects.requireNonNull(getOrThrow("configuration.game_rules_applied"));
        return parseGameRules(map.getOrDefault(worldName, Map.of()));
    }

    @SuppressWarnings("rawtypes")
    private Map<GameRule, Object> parseGameRules(@NotNull Map<String, Object> gameRulesName) {
        Map<GameRule, Object> gameRulesMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : gameRulesName.entrySet()) {
            GameRule gameRule = GameRule.getByName(entry.getKey());
            if (gameRule == null) {
                plugin.logger().warn("Couldn't parse \"{}\" to a GameRule.", entry.getKey());
                continue;
            }
            gameRulesMap.put(gameRule, entry.getValue());
        }
        return gameRulesMap;
    }

    public Boolean isRainyNightEnabled() {
        return getOrThrow("configuration.rainy_night_event.enabled");
    }

    public List<PotionEffect> getRainyNightPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        List<Map<String, Object>> effects =
                Objects.requireNonNull(getOrThrow("configuration.rainy_night_event.potion_effects"));
        for (Map<String, Object> effect : effects) {
            String effectType = (String) effect.get("type");
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectType);
            if (potionEffectType == null) {
                potionEffectType = PotionEffectType.getByKey(NamespacedKey.fromString(effectType));
                if (potionEffectType == null) {
                    plugin.logger().warn("Failed to identify potion effect: \"{}\"", effectType);
                    continue;
                }
            }
            int durationTicks = (int) effect.get("duration");
            int amplifier = (int) effect.get("amplifier");
            PotionEffect potionEffect = new PotionEffect(potionEffectType, durationTicks, amplifier);
            potionEffects.add(potionEffect);
        }
        return potionEffects;
    }

    public Boolean isAllowLavaFlow() {
        return getOrThrow("configuration.lava_flow.allow_lava_flow");
    }

    public List<World> getLavaFlowWorlds() {
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = Objects.requireNonNull(getOrThrow("configuration.lava_flow.allow_lava_flow_worlds"));
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public Boolean isExperienceModifierEnabled() {
        return getOrThrow("configuration.experience_modifier.enabled");
    }

    public Double getExperienceModifierDefault() {
        return getDoubleOrNull("configuration.experience_modifier.default_modifier");
    }

    public Map<String, Double> getExperienceModifierGroups() {
        return getOrThrow("configuration.experience_modifier.groups");
    }

    public Boolean isDroppedExperienceChangeEnabled() {
        return getOrThrow("configuration.dropped_experience.enabled");
    }

    public Double getExperienceKeptRatio() {
        return getDoubleOrNull("configuration.dropped_experience.kept_ratio");
    }

    public Double getExperienceDroppedRatio() {
        return getDoubleOrNull("configuration.dropped_experience.dropped_ratio");
    }

    public Boolean isPlayerHeadDropping() {
        return getOrThrow("configuration.drop_player_heads.enabled");
    }

    public List<Component> getPlayerHeadLore(@NotNull Player killed, @NotNull Player killer) {
        List<String> itemLoreStrings = Objects.requireNonNull(getOrThrow("configuration.drop_player_heads.item_lore"));
        ArrayList<Component> itemLore = new ArrayList<>();
        for (String itemLoreString : itemLoreStrings) {
            itemLore.add(TextUtil.toComponent(itemLoreString,
                                              Placeholder.component("killed", killed.displayName()),
                                              Placeholder.component("killer", killer.displayName()),
                                              Placeholder.unparsed("datetime",
                                                                   DateTimeFormatter.ofPattern("d-MMM-yy H:mm")
                                                                                    .format(ZonedDateTime.now()))));
        }
        return itemLore;
    }

    public Boolean isHideJoinQuitMessages() {
        return getOrThrow("configuration.hide_join_quit_messages");
    }

    public Duration getDelayBetweenDeathMessages() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.delay_between_death_messages_seconds")));
    }

    public Duration getDelayBetweenLoginMessages() {
        return Duration.ofSeconds(Objects.requireNonNull(getLongOrNull(
                "configuration.delay_between_login_messages_seconds")));
    }

    public Boolean isForceFirstSpawnLocation() {
        return getOrThrow("configuration.force_first_spawn_location");
    }

    public String getVipGroupName() {
        return getOrThrow("configuration.luck_perms_vip_group_name");
    }

    public Boolean isBlockEnderchestWithoutPermissions() {
        return getOrThrow("configuration.purpur_permissions.block_enderchest_without_permissions");
    }

    public Boolean isBlockSpawnerPlaceWithoutPermission() {
        return getOrThrow("configuration.purpur_permissions.block_spawner_place_without_permissions");
    }

    public Boolean isWarnOnSpawnerBlockDamage() {
        return getOrThrow("configuration.purpur_permissions.warn_on_spawner_damage");
    }

    public double getNearbyPlayersRange() {
        return Objects.requireNonNull(getDoubleOrNull("configuration.nearby_players_range_blocks"));
    }

    public List<World> getSyncedTimeWorlds() {
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = Objects.requireNonNull(getOrThrow("configuration.worlds_with_synced_real_time"));
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public @NotNull WorldCombatConfig getCombatConfiguration(@NotNull String worldName) {
        return worldCombatConfigurations.getOrDefault(worldName, defaultCombatConfig);
    }

    private WorldCombatConfig parseWorldConfiguration(@NotNull Map<String, Object> configuration,
                                                      @Nullable WorldCombatConfig defaultConfig) {
        WorldCombatConfig.Builder builder = WorldCombatConfig.builder(defaultConfig);

        Boolean preventNightSkip = (Boolean) configuration.get("prevent_night_skip");
        if (preventNightSkip != null) {
            builder.setPreventSkipNight(preventNightSkip);
        }

        Integer startTime = (Integer) configuration.get("start_pvp_time");
        Integer endTime = (Integer) configuration.get("end_pvp_time");
        if (startTime != null && endTime != null) {
            builder.setCombatTime(startTime, endTime);
        }

        Boolean constantCombatSetting = (Boolean) configuration.get("constant_pvp_setting");
        if (constantCombatSetting != null) {
            builder.setConstantCombatSetting(constantCombatSetting);
        }

        Boolean constantCombat = (Boolean) configuration.get("use_constant_pvp_setting");
        if (constantCombat != null) {
            builder.setConstantCombat(constantCombat);
        }
        return builder.build();
    }

    public Component getSpawnerNoPermissionToPlace() {
        return TextUtil.toComponent(getOrThrow("language.purpur_permissions.spawner_permissions.no_permission_to_place"));
    }

    public Component getSpawnerWarningBreak() {
        return TextUtil.toComponent(getOrThrow("language.purpur_permissions.spawner_permissions.warning_message"));
    }

    public Component getFailedEnderchestKickMessage() {
        return TextUtil.toComponent(getOrThrow("language.purpur_permissions.enderchest_permissions.kick_message"));
    }

    public Component getFailedEnderchestInventoryMessage() {
        return TextUtil.toComponent(getOrThrow("language.purpur_permissions.enderchest_permissions.warning_message"));
    }

    public Component getPlayerOnly() {
        return TextUtil.toComponent(getOrThrow("language.commands.player_only"));
    }

    public Component getPlayerNotFound() {
        return TextUtil.toComponent(getOrThrow("language.commands.player_not_found"));
    }

    public Component getPlayerTimeHelp() {
        return TextUtil.toComponent(getOrThrow("language.commands.player_time.help"));
    }

    public Component getPlayerWeatherHelp() {
        return TextUtil.toComponent(getOrThrow("language.commands.player_weather.help"));
    }

    public Component getGameModeHelp() {
        return TextUtil.toComponent(getOrThrow("language.commands.gamemode.help"));
    }

    public Component getEnchantHelp() {
        return TextUtil.toComponent(getOrThrow("language.commands.enchant.help"));
    }

    public Component getEnchantCantEnchantItem() {
        return TextUtil.toComponent(getOrThrow("language.commands.enchant.cant_enchant_item"));
    }

    public Component getKillHelp() {
        return TextUtil.toComponent(getOrThrow("language.commands.kill.help"));
    }

    public Component getFlyHelp() {
        return TextUtil.toComponent(getOrThrow("language.commands.fly.help"));
    }

    public Component getFlyStatus(@NotNull String playerName, boolean allowFlight) {
        return TextUtil.toComponent(getOrThrow("language.commands.fly.status"),
                                    Placeholder.unparsed("username", playerName),
                                    Placeholder.unparsed("flying", allowFlight ? "enabled" : "disabled"));
    }

    public Component getNearbyPlayers(@NotNull Map<Player, Double> nearbyPlayers) {
        if (nearbyPlayers.isEmpty()) {
            return TextUtil.toComponent(getOrThrow("language.commands.near.nobody_near_you"));
        }
        String playerList = TextUtil.joinStrings(nearbyPlayers.entrySet(),
                                                 ", ",
                                                 entry -> "%s (%.1f)".formatted(entry.getKey().getName(),
                                                                                entry.getValue()));
        return TextUtil.toComponent(getOrThrow("language.commands.near.nearby_players"),
                                    Placeholder.parsed("list", playerList));
    }

    public Component getVipRemainingTime(@Nullable InheritanceNode node) {
        if (node == null || node.hasExpired() || !node.getValue()) {
            return TextUtil.toComponent(getOrThrow("language.commands.vip.help"));
        }
        if (!node.hasExpiry()) {
            return TextUtil.toComponent(getOrThrow("language.commands.vip.unlimited_vip"));
        }
        assert node.getExpiryDuration() != null;
        String expirationDate =
                DateTimeFormatter.ofPattern(Objects.requireNonNull(getOrThrow("language.commands.vip.date_time_format")))
                                 .format(ZonedDateTime.now().plus(node.getExpiryDuration()));
        return TextUtil.toComponent(getOrThrow("language.commands.vip.vip_expiration_date"),
                                    Placeholder.unparsed("expiration_date", expirationDate));
    }
}
