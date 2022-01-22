package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        defaults.put(Keys.GAME_RULES_LIST,
                     Map.of("default",
                            Map.of(GameRule.PLAYERS_SLEEPING_PERCENTAGE.getName(), 35),
                            "world",
                            Map.of(GameRule.MAX_ENTITY_CRAMMING.getName(), 4, GameRule.DO_FIRE_TICK.getName(), false)));

        defaults.put(Keys.COMMAND_PLAYER_ONLY, "&cComando disponível apenas para jogadores");
        defaults.put(Keys.COMMAND_PLAYER_TIME_HELP, "&6Uso: &e/tempo (dia/meiodia/noite/meianoite) [fixo]");
        defaults.put(Keys.COMMAND_PLAYER_WEATHER_HELP, "&6Uso: &e/clima (limpo/chuvoso)");
        defaults.put(Keys.COMMAND_PLAYER_VANISHED, "&6Ficou invisível.");
        defaults.put(Keys.COMMAND_PLAYER_APPEARED, "&6Ficou visível.");
        return defaults;
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

    public Component getPlayerOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_ONLY));
    }

    public Component getPlayerTimeHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_TIME_HELP));
    }

    public Component getPlayerWeatherHelp() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_WEATHER_HELP));
    }

    public Component getPlayerVanished() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_VANISHED));
    }

    public Component getPlayerAppeared() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYER_APPEARED));
    }

    private static class Keys {

        public static final String GAME_RULES_LIST = "configuration.game_rules_applied";

        public static final String COMMAND_PLAYER_ONLY = "language.commands.player_only";
        public static final String COMMAND_PLAYER_TIME_HELP = "language.commands.player_time.help";
        public static final String COMMAND_PLAYER_WEATHER_HELP = "language.commands.player_weather.help";
        public static final String COMMAND_PLAYER_VANISHED = "language.commands.vanish.player_vanished";
        public static final String COMMAND_PLAYER_APPEARED = "language.commands.vanish.player_appeared";

        // Private constructor
        private Keys() {
        }
    }
}
