package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.YamlFile;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Configuration extends YamlFile {

    private final @NotNull ServerUtilityPlugin plugin;

    public Configuration(@NotNull ServerUtilityPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        this.plugin = plugin;
    }

    public Double getOverallDamageMultiplier() {
        return getDouble("configuration.damage_modifiers.overall_damage_multiplier");
    }

    public Double getCooldownDamageFactor() {
        return getDouble("configuration.damage_modifiers.damage_cooldown_factor");
    }

    public Boolean isPlayerLoggingEnabled() {
        return get("configuration.enable_player_logging");
    }

    public Boolean isMendingNerfed() {
        return get("configuration.nerf_mending_to_one_repair_point_only");
    }

    public Boolean isPreventingAllEnchantedBooks() {
        return get("configuration.villager.prevent_all_enchanted_books");
    }

    public Boolean isPreventingTreasureEnchantedBooks() {
        return get("configuration.villager.prevent_treasure_enchanted_books");
    }

    public Boolean isNerfVillagerEnchantedBooks() {
        return get("configuration.villager.nerf_enchanted_books");
    }

    @SuppressWarnings("rawtypes")
    public Map<GameRule, Object> getDefaultGameRules() {
        Map<String, Map<String, Object>> map = Objects.requireNonNull(get("configuration.game_rules_applied"));
        return parseGameRules(map.getOrDefault("default", Map.of()));
    }

    @SuppressWarnings("rawtypes")
    public Map<GameRule, Object> getWorldGameRule(@NotNull String worldName) {
        Map<String, Map<String, Object>> map = Objects.requireNonNull(get("configuration.game_rules_applied"));
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
        return get("configuration.rainy_night_event.enabled");
    }

    public List<PotionEffect> getRainyNightPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        List<Map<String, Object>> effects =
                Objects.requireNonNull(get("configuration.rainy_night_event.potion_effects"));
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
        return get("configuration.lava_flow.allow_lava_flow");
    }

    public List<World> getLavaFlowWorlds() {
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = Objects.requireNonNull(get("configuration.lava_flow.allow_lava_flow_worlds"));
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public Boolean isExperienceModifierEnabled() {
        return get("configuration.experience_modifier.enabled");
    }

    public Double getExperienceModifierDefault() {
        return getDouble("configuration.experience_modifier.default_modifier");
    }

    public Map<String, Double> getExperienceModifierGroups() {
        return get("configuration.experience_modifier.groups");
    }

    public Boolean isHideJoinQuitMessages() {
        return get("configuration.hide_join_quit_messages");
    }

    public List<World> getSyncedTimeWorlds() {
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = Objects.requireNonNull(get("configuration.worlds_with_synced_real_time"));
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public Component getPlayerOnly() {
        return TextUtil.toComponent(get("language.commands.player_only")).build();
    }

    public Component getPlayerTimeHelp() {
        return TextUtil.toComponent(get("language.commands.player_time.help")).build();
    }

    public Component getPlayerWeatherHelp() {
        return TextUtil.toComponent(get("language.commands.player_weather.help")).build();
    }

    public Component getPlayerVanished() {
        return TextUtil.toComponent(get("language.commands.vanish.player_vanished")).build();
    }

    public Component getPlayerAppeared() {
        return TextUtil.toComponent(get("language.commands.vanish.player_appeared")).build();
    }

    public Component getGameModeHelp() {
        return TextUtil.toComponent(get("language.commands.gamemode.help")).build();
    }

    public Component getEnchantHelp() {
        return TextUtil.toComponent(get("language.commands.enchant.help")).build();
    }

    public Component getEnchantCantEnchantItem() {
        return TextUtil.toComponent(get("language.commands.enchant.cant_enchant_item")).build();
    }
}
