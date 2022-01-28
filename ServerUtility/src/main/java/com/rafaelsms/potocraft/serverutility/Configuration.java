package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    private final @NotNull ServerUtilityPlugin plugin;

    public Configuration(@NotNull ServerUtilityPlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        loadConfiguration();
        this.plugin = plugin;
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(Keys.NERF_MENDING, true);
        defaults.put(Keys.HIDE_ALL_JOIN_QUIT_MESSAGES, false);
        defaults.put(Keys.WORLDS_SYNCED_REAL_TIME, List.of("world"));
        defaults.put(Keys.GAME_RULES_LIST,
                     Map.of("default",
                            Map.of(GameRule.PLAYERS_SLEEPING_PERCENTAGE.getName(), 35),
                            "world",
                            Map.of(GameRule.MAX_ENTITY_CRAMMING.getName(), 4, GameRule.DO_FIRE_TICK.getName(), false)));

        defaults.put(Keys.RAINY_NIGHT_ENABLED, false);
        defaults.put(Keys.RAINY_NIGHT_POTION_EFFECTS,
                     List.of(Map.of("type",
                                    PotionEffectType.HEALTH_BOOST.getName(),
                                    "duration",
                                    20 * 60 * 6,
                                    "amplifier",
                                    4),
                             Map.of("type",
                                    PotionEffectType.INCREASE_DAMAGE.getKey().getKey(),
                                    "duration",
                                    20 * 60 * 6,
                                    "amplifier",
                                    1)));

        defaults.put(Keys.ALLOW_LAVA_FLOW, false);
        defaults.put(Keys.ALLOW_LAVA_FLOW_WORLDS, List.of("world_nether"));

        defaults.put(Keys.EXPERIENCE_MODIFIER_ENABLED, true);
        defaults.put(Keys.EXPERIENCE_MODIFIER_DEFAULT, 0.8);
        defaults.put(Keys.EXPERIENCE_MODIFIER_GROUPS, Map.of("potocraft.exp_modifier.vip", 1.4));

        defaults.put(Keys.COMMAND_PLAYER_ONLY, "&cComando disponível apenas para jogadores");
        defaults.put(Keys.COMMAND_PLAYER_TIME_HELP, "&6Uso: &e/tempo (dia/meiodia/noite/meianoite) [fixo]");
        defaults.put(Keys.COMMAND_PLAYER_WEATHER_HELP, "&6Uso: &e/clima (limpo/chuvoso)");
        defaults.put(Keys.COMMAND_PLAYER_VANISHED, "&6Ficou invisível.");
        defaults.put(Keys.COMMAND_PLAYER_APPEARED, "&6Ficou visível.");
        defaults.put(Keys.COMMAND_GAMEMODE_HELP, "&6Uso: &e&l/gamemode (creative/spectator/survival/adventure)");
        return defaults;
    }

    public boolean isMendingNerfed() {
        return get(Keys.NERF_MENDING);
    }

    @SuppressWarnings("rawtypes")
    public Map<GameRule, Object> getDefaultGameRules() {
        Map<String, Map<String, Object>> map = get(Keys.GAME_RULES_LIST);
        return parseGameRules(map.getOrDefault("default", Map.of()));
    }

    @SuppressWarnings("rawtypes")
    public Map<GameRule, Object> getWorldGameRule(@NotNull String worldName) {
        Map<String, Map<String, Object>> map = get(Keys.GAME_RULES_LIST);
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

    public boolean isRainyNightEnabled() {
        return get(Keys.RAINY_NIGHT_ENABLED);
    }

    public List<PotionEffect> getRainyNightPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<>();
        List<Map<String, Object>> effects = get(Keys.RAINY_NIGHT_POTION_EFFECTS);
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

    public boolean isAllowLavaFlow() {
        return get(Keys.ALLOW_LAVA_FLOW);
    }

    public List<World> getLavaFlowWorlds() {
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = get(Keys.ALLOW_LAVA_FLOW_WORLDS);
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public boolean isExperienceModifierEnabled() {
        return get(Keys.EXPERIENCE_MODIFIER_ENABLED);
    }

    public double getExperienceModifierDefault() {
        return get(Keys.EXPERIENCE_MODIFIER_DEFAULT);
    }

    public Map<String, Double> getExperienceModifierGroups() {
        return get(Keys.EXPERIENCE_MODIFIER_GROUPS);
    }

    public boolean isHideJoinQuitMessages() {
        return get(Keys.HIDE_ALL_JOIN_QUIT_MESSAGES);
    }

    public List<World> getSyncedTimeWorlds() {
        List<World> worlds = new ArrayList<>();
        List<String> worldNames = get(Keys.WORLDS_SYNCED_REAL_TIME);
        for (String worldName : worldNames) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                worlds.add(world);
            }
        }
        return worlds;
    }

    public Component getPlayerOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_ONLY)).build();
    }

    public Component getPlayerTimeHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_TIME_HELP)).build();
    }

    public Component getPlayerWeatherHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_WEATHER_HELP)).build();
    }

    public Component getPlayerVanished() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_VANISHED)).build();
    }

    public Component getPlayerAppeared() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_APPEARED)).build();
    }

    public Component getGameModeHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_GAMEMODE_HELP)).build();
    }

    private static class Keys {

        public static final String NERF_MENDING = "configuration.nerf_mending_to_one_repair_point_only";
        public static final String HIDE_ALL_JOIN_QUIT_MESSAGES = "configuration.hide_join_quit_messages";
        public static final String WORLDS_SYNCED_REAL_TIME = "configuration.worlds_with_synced_real_time";
        public static final String GAME_RULES_LIST = "configuration.game_rules_applied";

        public static final String RAINY_NIGHT_ENABLED = "configuration.rainy_night_event.enabled";
        public static final String RAINY_NIGHT_POTION_EFFECTS = "configuration.rainy_night_event.potion_effects";

        public static final String ALLOW_LAVA_FLOW = "configuration.lava_flow.allow_lava_flow";
        public static final String ALLOW_LAVA_FLOW_WORLDS = "configuration.lava_flow.allow_lava_flow_worlds";

        public static final String EXPERIENCE_MODIFIER_ENABLED = "configuration.experience_modifier.enabled";
        public static final String EXPERIENCE_MODIFIER_DEFAULT = "configuration.experience_modifier.default_modifier";
        public static final String EXPERIENCE_MODIFIER_GROUPS = "configuration.experience_modifier.groups";

        public static final String COMMAND_PLAYER_ONLY = "language.commands.player_only";
        public static final String COMMAND_PLAYER_TIME_HELP = "language.commands.player_time.help";
        public static final String COMMAND_PLAYER_WEATHER_HELP = "language.commands.player_weather.help";
        public static final String COMMAND_PLAYER_VANISHED = "language.commands.vanish.player_vanished";
        public static final String COMMAND_PLAYER_APPEARED = "language.commands.vanish.player_appeared";
        public static final String COMMAND_GAMEMODE_HELP = "language.commands.gamemode.help";

        // Private constructor
        private Keys() {
        }
    }
}
