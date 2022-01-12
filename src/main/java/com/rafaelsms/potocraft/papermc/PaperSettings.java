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
        setDefault(Constants.TELEPORT_REQUEST_TIME_TO_LIVE, 60 * 2);
        setDefault(Constants.TELEPORT_DELAY, 20 * 15);

        setDefault(Constants.IN_COMBAT_SHOULD_USE_TOTEM, false);
        setDefault(Constants.IN_COMBAT_MOB_TICKS, 20 * 19);
        setDefault(Constants.IN_COMBAT_PLAYER_TICKS, 20 * 19 * 2);
        setDefault(Constants.IN_COMBAT_BLOCKED_COMMANDS, List.of("tp", "tphere", "warp", "spawn"));

        /* LANG */
        setDefault(Constants.LANG_TELEPORT_HELP, "&6Uso: &e/teleporte <nome>");
        setDefault(Constants.LANG_TELEPORT_HERE_HELP, "&6Uso: &e/teleporteaqui <nome>");
        setDefault(Constants.LANG_TELEPORT_HELP_ADMIN, """
                                                       &6Uso: &e/teleporte <nome de quem teleportará> <pessoa destino do teleporte>
                                                       &6Uso: &e/teleporte <nome de quem teleportará> <mundo> <x> <y> <z>
                                                       """);
        setDefault(Constants.LANG_TELEPORT_PROGRESS_BAR_TITLE, "&eTeleportando... Não entre em combate!");
        setDefault(Constants.LANG_TELEPORT_SUCCESS, "&6Teleportando... Digite &e&l/voltar &6para voltar");
        setDefault(Constants.LANG_TELEPORT_FAIL, "&cFalha ao teleportar!");
        setDefault(Constants.LANG_TELEPORT_PLAYER_ENTERED_COMBAT, "&cTeleporte cancelado por combate!");
        setDefault(Constants.LANG_TELEPORT_DESTINATION_UNAVAILABLE, "&cDestino indisponível.");
        setDefault(Constants.LANG_TELEPORT_CAN_NOT_TELEPORT_NOW, "&cVocê não pode teleportar agora.");
        setDefault(Constants.LANG_TELEPORT_IN_COOLDOWN, "&cVocê só pode teleportar daqui &e%seconds% segundos&c.");
        setDefault(Constants.LANG_TELEPORT_REQUEST_ANSWER_HELP, """
                                                                &6Para aceitar ou recusar um pedido de teleporte, digite:
                                                                &a&l/tpaceitar <nome> &6ou &a&l/tprecusar <nome>
                                                                """);
        setDefault(Constants.LANG_TELEPORT_REQUEST_SENT, "&6Pedido enviado para &e%username%&6.");
        setDefault(Constants.LANG_TELEPORT_REQUESTS_LIST, "&6Você tem pedidos de teleporte de: &e%list%");
        setDefault(Constants.LANG_TELEPORT_REQUESTS_LIST_EMPTY, "&cNão há pedidos de teleporte válidos.");
        setDefault(Constants.LANG_TELEPORT_REQUEST_ACCEPTED, "&6Pedido de teleporte de &e%username% &6aceito.");
        setDefault(Constants.LANG_TELEPORT_REQUEST_DENIED, "&cPedido de teleporte de &e%username% &crecusado.");
        setDefault(Constants.LANG_TELEPORT_REQUEST_RECEIVED, """
                                                             &e%username% &6pediu para &e%username% &6se teleportar até &evocê&6.
                                                             &6Digite &e&l/tpaceitar %username% &6para aceitar ou &e&l/tprecusar %username% &6para recusar.
                                                             """);
        setDefault(Constants.LANG_TELEPORT_HERE_REQUEST_RECEIVED, """
                                                                  &3%username% &6pediu para &3você &6se teleportar até &3%username%&6.
                                                                  &6Digite &e&l/tpaceitar %username% &6para aceitar ou &e&l/tprecusar %username% &6para recusar.
                                                                  """);

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

    public Duration getTeleportRequestDuration() {
        return Duration.ofSeconds(get(Constants.TELEPORT_REQUEST_TIME_TO_LIVE));
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

    public Component getTeleportPlayerInCombat() {
        return getLang(Constants.LANG_TELEPORT_PLAYER_ENTERED_COMBAT);
    }

    public Component getTeleportDestinationUnavailable() {
        return getLang(Constants.LANG_TELEPORT_DESTINATION_UNAVAILABLE);
    }

    public Component getTeleportCanNotTeleportNow() {
        return getLang(Constants.LANG_TELEPORT_CAN_NOT_TELEPORT_NOW);
    }

    public Component getTeleportHelp() {
        return getLang(Constants.LANG_TELEPORT_HELP);
    }

    public Component getTeleportHereHelp() {
        return getLang(Constants.LANG_TELEPORT_HERE_HELP);
    }

    public Component getTeleportHelpAdmin() {
        return getLang(Constants.LANG_TELEPORT_HELP_ADMIN);
    }

    public Component getTeleportRequestAnswerHelp() {
        return getLang(Constants.LANG_TELEPORT_REQUEST_ANSWER_HELP);
    }

    public Component getTeleportRequestReceived(@NotNull String playerName) {
        return getLang(Constants.LANG_TELEPORT_REQUEST_RECEIVED).replaceText(TextUtil.replaceText("%username%",
                                                                                                  playerName));
    }

    public Component getTeleportHereRequestReceived(@NotNull String playerName) {
        return getLang(Constants.LANG_TELEPORT_HERE_REQUEST_RECEIVED).replaceText(TextUtil.replaceText("%username%",
                                                                                                       playerName));
    }

    public Component getTeleportRequestSent(@NotNull String playerName) {
        return getLang(Constants.LANG_TELEPORT_REQUEST_SENT).replaceText(TextUtil.replaceText("%username%",
                                                                                              playerName));
    }

    public Component getTeleportRequestsList(@NotNull List<String> playerNames) {
        return getLang(Constants.LANG_TELEPORT_REQUESTS_LIST).replaceText(TextUtil.replaceText("%list%",
                                                                                               TextUtil.joinStrings(
                                                                                                       playerNames,
                                                                                                       ", ")));
    }

    public Component getTeleportRequestAccepted(@NotNull String playerName) {
        return getLang(Constants.LANG_TELEPORT_REQUEST_ACCEPTED).replaceText(TextUtil.replaceText("%username%",
                                                                                                  playerName));
    }

    public Component getTeleportRequestDenied(@NotNull String playerName) {
        return getLang(Constants.LANG_TELEPORT_REQUEST_DENIED).replaceText(TextUtil.replaceText("%username%",
                                                                                                  playerName));
    }

    public Component getTeleportRequestsListEmpty() {
        return getLang(Constants.LANG_TELEPORT_REQUESTS_LIST_EMPTY);
    }

    public Component getTeleportInCooldown(long seconds) {
        return getLang(Constants.LANG_TELEPORT_IN_COOLDOWN).replaceText(TextUtil.replaceText("%seconds%",
                                                                                             Long.toString(seconds)));
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
