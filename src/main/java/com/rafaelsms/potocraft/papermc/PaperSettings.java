package com.rafaelsms.potocraft.papermc;

import com.rafaelsms.potocraft.common.Settings;
import com.rafaelsms.potocraft.common.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

public class PaperSettings extends Settings {

    private final @NotNull PaperPlugin plugin;

    public PaperSettings(@NotNull PaperPlugin plugin) throws Exception {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();
        /* CONFIGURATION */
        setDefault(Constants.PAPER_SERVER_NAME_ON_PROXY, "<FILL THIS IN OR LOCATIONS WONT BE RESTORED>");
        setDefault(Constants.DATABASE_MONGO_PAPER_SERVER_PROFILES_COLLECTION, null);

        setDefault(Constants.GLOBAL_CHAT_FORMAT, "&7! <&e%prefix%%username%%suffix%&7> &f%message%");
        setDefault(Constants.GLOBAL_CHAT_LIMITER_MESSAGES_AMOUNT, 3);
        setDefault(Constants.GLOBAL_CHAT_LIMITER_TIME_AMOUNT, 5000);
        setDefault(Constants.GLOBAL_CHAT_COMPARATOR_MIN_LENGTH, 3);
        setDefault(Constants.GLOBAL_CHAT_COMPARATOR_THRESHOLD, 3);
        setDefault(Constants.LOCAL_CHAT_FORMAT, "&e%prefix%%username%%suffix% &f%message%");
        setDefault(Constants.LOCAL_CHAT_SPY_FORMAT, "&e%prefix%%username%%suffix% &7(longe) &f%message%");
        setDefault(Constants.LOCAL_CHAT_LIMITER_MESSAGES_AMOUNT, 7);
        setDefault(Constants.LOCAL_CHAT_LIMITER_TIME_AMOUNT, 10000);
        setDefault(Constants.LOCAL_CHAT_COMPARATOR_MIN_LENGTH, 4);
        setDefault(Constants.LOCAL_CHAT_COMPARATOR_THRESHOLD, 2);
        setDefault(Constants.LOCAL_CHAT_RADIUS, 400.0);

        setDefault(Constants.TELEPORT_COOLDOWN, 60 * 5);
        setDefault(Constants.TELEPORT_DELAY, 20 * 15);

        setDefault(Constants.IN_COMBAT_SHOULD_USE_TOTEM, false);
        setDefault(Constants.IN_COMBAT_MOB_TICKS, 20 * 19);
        setDefault(Constants.IN_COMBAT_PLAYER_TICKS, 20 * 19 * 2);
        setDefault(Constants.IN_COMBAT_BLOCKED_COMMANDS, List.of("tp", "tphere", "warp", "spawn"));

        /* LANG */
        setDefault(Constants.LANG_TELEPORT_PROGRESS_BAR_TITLE, "&eTeleportando... Não entre em combate!");
        setDefault(Constants.LANG_TELEPORT_SUCCESS, "&6Teleportando... Digite &e&l/voltar &6para voltar");
        setDefault(Constants.LANG_TELEPORT_FAIL, "&cFalha ao teleportar!");
        setDefault(Constants.LANG_TELEPORT_DESTINATION_UNAVAILABLE, "&cDestino indisponível.");

        setDefault(Constants.LANG_COMBAT_PROGRESS_BAR_TITLE, "&cNão saia nem desconecte do jogo ou morrerá!");
        setDefault(Constants.LANG_COMBAT_BLOCKED_COMMAND, "&cEste comando está bloqueado em combate.");
        setDefault(Constants.LANG_COMBAT_LAST_DEATH_LOCATION,
                   "&cVocê morreu na posição &ex = %x%&c, &ey = %y%&c, &ez = %z%&c.");
        setDefault(Constants.LANG_COMBAT_LAST_DEATH_LOCATION_WORLD,
                   "&cVocê morreu no mundo &e\"%world%\" na posição &ex = %x%&c, &ey = %y%&c, &ez = %z%&c.");

    }

    public String getMongoServerProfilesCollection() {
        return get(Constants.DATABASE_MONGO_PAPER_SERVER_PROFILES_COLLECTION);
    }

    public String getServerName() {
        return get(Constants.PAPER_SERVER_NAME_ON_PROXY);
    }

    public Component getGlobalChatFormat() {
        return getLang(Constants.GLOBAL_CHAT_FORMAT);
    }

    public String getGlobalChatPrefix() {
        return get(Constants.GLOBAL_CHAT_PREFIX);
    }

    public int getGlobalChatLimiterMessageAmount() {
        return get(Constants.GLOBAL_CHAT_LIMITER_MESSAGES_AMOUNT);
    }

    public long getGlobalChatLimiterTimeAmount() {
        return get(Constants.GLOBAL_CHAT_LIMITER_TIME_AMOUNT);
    }

    public int getGlobalChatComparatorThreshold() {
        return get(Constants.GLOBAL_CHAT_COMPARATOR_THRESHOLD);
    }

    public int getGlobalChatComparatorMinLength() {
        return get(Constants.GLOBAL_CHAT_COMPARATOR_MIN_LENGTH);
    }

    public Component getLocalChatSpyFormat() {
        return getLang(Constants.LOCAL_CHAT_SPY_FORMAT);
    }

    public Component getLocalChatFormat() {
        return getLang(Constants.LOCAL_CHAT_FORMAT);
    }

    public double getLocalChatRadius() {
        return get(Constants.LOCAL_CHAT_RADIUS);
    }

    public int getLocalChatLimiterMessageAmount() {
        return get(Constants.LOCAL_CHAT_LIMITER_MESSAGES_AMOUNT);
    }

    public long getLocalChatLimiterTimeAmount() {
        return get(Constants.LOCAL_CHAT_LIMITER_TIME_AMOUNT);
    }

    public int getLocalChatComparatorThreshold() {
        return get(Constants.LOCAL_CHAT_COMPARATOR_THRESHOLD);
    }

    public int getLocalChatComparatorMinLength() {
        return get(Constants.LOCAL_CHAT_COMPARATOR_MIN_LENGTH);
    }

    public long getTeleportDelayTicks() {
        return get(Constants.TELEPORT_DELAY);
    }

    public Duration getTeleportCooldown() {
        return Duration.ofSeconds(get(Constants.TELEPORT_COOLDOWN));
    }

    public Component getTeleportTitle() {
        return getLang(Constants.LANG_TELEPORT_PROGRESS_BAR_TITLE);
    }

    public Component getTeleportedSuccessfully() {
        return getLang(Constants.LANG_TELEPORT_SUCCESS);
    }

    public Component getTeleportFailed() {
        return getLang(Constants.LANG_TELEPORT_FAIL);
    }

    public Component getTeleportDestinationUnavailable() {
        return getLang(Constants.LANG_TELEPORT_DESTINATION_UNAVAILABLE);
    }

    public boolean getCombatShouldUseTotem() {
        return get(Constants.IN_COMBAT_SHOULD_USE_TOTEM);
    }

    public long getCombatVsMobsTicks() {
        return get(Constants.IN_COMBAT_MOB_TICKS);
    }

    public long getCombatVsPlayersTicks() {
        return get(Constants.IN_COMBAT_PLAYER_TICKS);
    }

    public List<String> getCombatBlockedCommands() {
        return get(Constants.IN_COMBAT_BLOCKED_COMMANDS);
    }

    public Component getCombatTitle() {
        return getLang(Constants.LANG_COMBAT_PROGRESS_BAR_TITLE);
    }

    public Component getCombatBlockedCommand() {
        return getLang(Constants.LANG_COMBAT_BLOCKED_COMMAND);
    }

    public Component getCombatDeathLocation(@NotNull Location location) {
        World locationWorld = location.getWorld();
        if (locationWorld != null) {
            String worldName = locationWorld.getName();
            return getLang(Constants.LANG_COMBAT_LAST_DEATH_LOCATION_WORLD)
                    .replaceText(TextUtil.replaceText("%world%", worldName))
                    .replaceText(TextUtil.replaceText("%x%", String.valueOf(location.getBlockX())))
                    .replaceText(TextUtil.replaceText("%y%", String.valueOf(location.getBlockY())))
                    .replaceText(TextUtil.replaceText("%z%", String.valueOf(location.getBlockZ())));
        } else {
            return getLang(Constants.LANG_COMBAT_LAST_DEATH_LOCATION)
                    .replaceText(TextUtil.replaceText("%x%", String.valueOf(location.getBlockX())))
                    .replaceText(TextUtil.replaceText("%y%", String.valueOf(location.getBlockY())))
                    .replaceText(TextUtil.replaceText("%z%", String.valueOf(location.getBlockZ())));
        }
    }
}
