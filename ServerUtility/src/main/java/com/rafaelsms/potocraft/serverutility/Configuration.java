package com.rafaelsms.potocraft.serverutility;

import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.World;
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
        defaults.put(Keys.SPECIAL_ENCHANTMENTS_REMOVE_FROM_INVENTORY, false);
        defaults.put(Keys.SPECIAL_ENCHANTMENTS_REMOVE_FROM_VILLAGERS, false);
        defaults.put(Keys.SINGLE_TOTEM_ONLY, false);
        defaults.put(Keys.ALLOW_LAVA_FLOW, false);
        defaults.put(Keys.HIDE_ALL_JOIN_QUIT_MESSAGES, false);
        defaults.put(Keys.WORLDS_SYNCED_REAL_TIME, List.of("world"));
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

    public boolean isRemoveSpecialEnchantmentsFromVillagers() {
        return get(Keys.SPECIAL_ENCHANTMENTS_REMOVE_FROM_VILLAGERS);
    }

    public boolean isRemoveSpecialEnchantmentsFromInventory() {
        return get(Keys.SPECIAL_ENCHANTMENTS_REMOVE_FROM_INVENTORY);
    }

    public boolean isSingleTotemOnly() {
        return get(Keys.SINGLE_TOTEM_ONLY);
    }

    public boolean isAllowLavaFlow() {
        return get(Keys.ALLOW_LAVA_FLOW);
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

    private static class Keys {

        public static final String SPECIAL_ENCHANTMENTS_REMOVE_FROM_VILLAGERS =
                "configuration.special_enchantments.remove_from_villagers";
        public static final String SPECIAL_ENCHANTMENTS_REMOVE_FROM_INVENTORY =
                "configuration.special_enchantments.remove_from_inventory";

        public static final String SINGLE_TOTEM_ONLY = "configuration.single_totem_only";
        public static final String ALLOW_LAVA_FLOW = "configuration.allow_lava_flow";
        public static final String HIDE_ALL_JOIN_QUIT_MESSAGES = "configuration.hide_join_quit_messages";
        public static final String WORLDS_SYNCED_REAL_TIME = "configuration.worlds_with_synced_real_time";
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
