package com.rafaelsms.potocraft.serverprofile;

import com.rafaelsms.potocraft.serverprofile.players.Home;
import com.rafaelsms.potocraft.serverprofile.players.TeleportRequest;
import com.rafaelsms.potocraft.serverprofile.warps.Warp;
import com.rafaelsms.potocraft.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class Configuration extends com.rafaelsms.potocraft.Configuration {

    private final @NotNull ServerProfilePlugin plugin;

    public Configuration(@NotNull ServerProfilePlugin plugin) throws IOException {
        super(plugin.getDataFolder(), "config.yml");
        loadConfiguration();
        this.plugin = plugin;
    }

    @Override
    protected @Nullable Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put(Keys.MONGO_URI, "mongodb://localhost:27017");
        defaults.put(Keys.MONGO_DATABASE_NAME, "serverNameDb");
        defaults.put(Keys.MONGO_DATABASE_FAILURE_FATAL, false);
        defaults.put(Keys.MONGO_PLAYER_PROFILES_COLLECTION_NAME, "playerProfiles");
        defaults.put(Keys.MONGO_WARPS_COLLECTION_NAME, "warps");

        defaults.put(Keys.LOCAL_CHAT_RANGE, 300.0);
        defaults.put(Keys.LOCAL_CHAT_FORMAT, "&e%prefix%%username%%suffix% &f%message%");
        defaults.put(Keys.CHAT_FORMAT, "&e%prefix%%username%%suffix% &f%message%");
        defaults.put(Keys.LOCAL_CHAT_SPY_FORMAT, "&e%prefix%%username%%suffix% &7(longe) &f%message%");

        defaults.put(Keys.SAVE_PLAYERS_TASK_TIMER_TICKS, 20 * 60 * 3);

        defaults.put(Keys.TELEPORT_COOLDOWN_SECONDS, 60 * 3 + 30);
        defaults.put(Keys.TELEPORT_DELAY_TICKS, 20 * 16);
        defaults.put(Keys.TELEPORT_REQUEST_DURATION_SECONDS, 60 * 3 + 30);

        defaults.put(Keys.DEFAULT_HOME_NUMBER, 1);
        defaults.put(Keys.HOME_NUMBER_PERMISSION_GROUPS,
                     Map.of("potocraft.homes.vip", 3, "potocraft.homes.premium", 5));

        defaults.put(Keys.COMBAT_MOB_DURATION_TICKS, 20 * 13);
        defaults.put(Keys.COMBAT_PLAYER_DURATION_TICKS, 20 * 41);
        defaults.put(Keys.COMBAT_BLOCKED_COMMANDS, List.of("tp", "tphere", "warp", "spawn", "tpaccept"));

        defaults.put(Keys.COMMAND_PLAYERS_ONLY, "&cComando disponível apenas para jogadores.");
        defaults.put(Keys.COMMAND_NO_PERMISSION, "&cVocê não tem permissão!");

        defaults.put(Keys.COMBAT_BAR_TITLE, "&cSe sair ou se a conexão cair, irá morrer!");
        defaults.put(Keys.COMBAT_BLOCKED_COMMAND, "&cEste comando está bloqueado em combate.");
        defaults.put(Keys.COMBAT_UNKNOWN_WORLD_NAME, "mundo desconhecido");
        defaults.put(Keys.COMBAT_DEATH_LOCATION,
                     "&cVocê morreu no &e%world% &cnas coordenadas &ex = %x%&c, &ey = %y%&c, &ez = %z%&c.");

        defaults.put(Keys.TELEPORT_BAR_TITLE, "&eTeleportando... Não entre em combate!");
        defaults.put(Keys.TELEPORT_PLAYER_QUIT, "&cTeleporte cancelado: pessoa saiu!");
        defaults.put(Keys.TELEPORT_ENTERED_COMBAT, "&cTeleporte cancelado: combate iniciado!");
        defaults.put(Keys.TELEPORT_PARTICIPANT_TELEPORTING, "&cTeleporte cancelado: outra pessoa teleportando!");
        defaults.put(Keys.TELEPORT_USER_TELEPORTING, "&cTeleporte cancelado: já teleportando!");
        defaults.put(Keys.TELEPORT_DESTINATION_UNAVAILABLE,
                     "&cTeleporte cancelado: destino do teleporte não está disponível!");
        defaults.put(Keys.TELEPORT_IN_COOLDOWN, "&cTeleporte estará indisponível por mais &e%cooldown% segundos&c.");
        defaults.put(Keys.TELEPORT_FAILED, "&cTeleporte falhou!");
        defaults.put(Keys.TELEPORT_NO_BACK_LOCATION, "&cNão há localização para voltar.");
        defaults.put(Keys.TELEPORT_PLAYER_NOT_FOUND, "&cNão encontramos alguém com este nome.");
        defaults.put(Keys.TELEPORT_HELP, "&6Para teleportar até alguém, digite &e&l/teleporte (nome)&6.");
        defaults.put(Keys.TELEPORT_REQUEST_RECEIVED, """
                                                     &6Você recebeu um pedido para &e%username% &6se teleportar até &evocê&6..
                                                     &6Digite &e&l/teleporteaceitar &6para aceitar ou &e&l/teleporterecusar &6para recusar.
                                                     """);
        defaults.put(Keys.TELEPORT_REQUEST_HERE_RECEIVED, """
                                                          &6Você recebeu um pedido para &evocê &6se teleportar até &3%username%&6.
                                                          &6Digite &e&l/teleporteaceitar &6para aceitar ou &e&l/teleporterecusar &6para recusar.
                                                          """);
        defaults.put(Keys.TELEPORT_REQUEST_SENT, "&6Pedido de teleporte enviado a &e%username%&6.");
        defaults.put(Keys.TELEPORT_REQUEST_NOT_UPDATED,
                     "&cPedido de teleporte anterior precisa ser cancelado por &e%username% &cprimeiro.");
        defaults.put(Keys.TELEPORT_REQUEST_NO_REQUEST_FOUND, "&cNenhum pedido de teleporte ativo.");
        defaults.put(Keys.TELEPORT_REQUEST_CANCELLED, "&6Pedido de teleporte cancelado!");
        defaults.put(Keys.TELEPORT_REQUEST_MANY_REQUESTS_FOUND, "&6Vários pedidos encontrados: &e%list%");
        defaults.put(Keys.TELEPORT_HOME_HELP,
                     "&6Para criar uma casa, digite &e&l/criarcasa &6e assim poderá voltar para ela digitando &e&l/casa&6!");
        defaults.put(Keys.TELEPORT_HOME_LIST, """
                                              &6Para criar mais casas, digite &e&l/criarcasa&6. Para apagar, &e&l/deletarcasa&6.
                                              &6Casas ativas: &e%list%
                                              """);
        defaults.put(Keys.TELEPORT_HOME_MAX_CAPACITY,
                     "&cVocê atingiu o número máximo de casas, digite &e&l/deletarcasa &cpara removê-las.");
        defaults.put(Keys.TELEPORT_HOME_CREATE_HELP, "&6Digite &e&l/criarcasa (nome) &6para criar uma casa.");
        defaults.put(Keys.TELEPORT_HOME_CREATED, "&6Casa criada! Você pode teleportar até ela digitando &e&l/casa&6!");
        defaults.put(Keys.TELEPORT_HOME_ALREADY_EXISTS,
                     "&cCasa já existe. Para substituí-la, digite &e&l/deletarcasa &ce recrie digitando &e&l/criarcasa&c.");
        defaults.put(Keys.TELEPORT_HOME_NOT_FOUND, "&cCasa não encontrada!");
        defaults.put(Keys.TELEPORT_HOME_DELETED, "&6Casa removida.");
        defaults.put(Keys.TELEPORT_HOME_DELETE_HELP, """
                                                     &6Para remover uma casa, digite &e&l/deletarcasa (nome)&c.
                                                     &6Casas: &e%list%
                                                     """);
        defaults.put(Keys.TELEPORT_WARP_MANAGE_HELP, "&6Uso: &e/criarportal (nome) &6ou &e/deletarportal (nome)&6.");
        defaults.put(Keys.TELEPORT_WARP_MANAGE_SUCCESS, "&6Portal alterado com sucesso!");
        defaults.put(Keys.TELEPORT_WARP_MANAGE_FAILURE, "&cFalha ao acessar banco de dados.");
        defaults.put(Keys.TELEPORT_WARP_FAIL_TO_RETRIEVE, "&cFalha ao obter lista.");
        defaults.put(Keys.TELEPORT_WARP_LIST, "&6Portais disponíveis: &c%list%");
        defaults.put(Keys.TELEPORT_WARP_NOT_FOUND, "&cPortal não encontrado.");

        defaults.put(Keys.KICK_MESSAGE_COULD_NOT_LOAD_PROFILE, "&cNão foi possível carregar seu perfil!");
        return defaults;
    }

    public String getMongoURI() {
        return get(Keys.MONGO_URI);
    }

    public String getMongoDatabaseName() {
        return get(Keys.MONGO_DATABASE_NAME);
    }

    public boolean isMongoDatabaseExceptionFatal() {
        return get(Keys.MONGO_DATABASE_FAILURE_FATAL);
    }

    public String getMongoPlayerProfileCollectionName() {
        return get(Keys.MONGO_PLAYER_PROFILES_COLLECTION_NAME);
    }

    public String getMongoWarpsCollectionName() {
        return get(Keys.MONGO_WARPS_COLLECTION_NAME);
    }

    public int getSavePlayersTaskTimerTicks() {
        return get(Keys.SAVE_PLAYERS_TASK_TIMER_TICKS);
    }

    public double getLocalChatRange() {
        return get(Keys.LOCAL_CHAT_RANGE);
    }

    public Component getLocalChatFormat(@NotNull UUID senderId,
                                        @NotNull String senderName,
                                        @NotNull Component message) {
        return getChatFormat(get(Keys.LOCAL_CHAT_FORMAT), senderId, senderName, message);
    }

    public Component getLocalChatSpyFormat(@NotNull UUID senderId,
                                           @NotNull String senderName,
                                           @NotNull Component message) {
        return getChatFormat(get(Keys.LOCAL_CHAT_SPY_FORMAT), senderId, senderName, message);
    }

    public Component getChatFormat(@NotNull UUID senderId, @NotNull String senderName, @NotNull Component message) {
        return getChatFormat(get(Keys.CHAT_FORMAT), senderId, senderName, message);
    }

    private Component getChatFormat(@NotNull String format,
                                    @NotNull UUID senderId,
                                    @NotNull String senderName,
                                    @NotNull Component message) {
        return TextUtil
                .toComponent(format)
                .replaceText(TextUtil.replaceText("%username%", senderName))
                .replaceText(TextUtil.replaceText("%prefix%", TextUtil.getPrefix(senderId)))
                .replaceText(TextUtil.replaceText("%suffix%", TextUtil.getSuffix(senderId)))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    public int getTeleportDelayTicks() {
        return get(Keys.TELEPORT_DELAY_TICKS);
    }

    public Duration getTeleportCooldown() {
        Integer cooldown = get(Keys.TELEPORT_COOLDOWN_SECONDS);
        return Duration.ofSeconds(cooldown.longValue());
    }

    public Duration getTeleportRequestDuration() {
        Integer cooldown = get(Keys.TELEPORT_REQUEST_DURATION_SECONDS);
        return Duration.ofSeconds(cooldown.longValue());
    }

    public int getDefaultHomeNumber() {
        return get(Keys.DEFAULT_HOME_NUMBER);
    }

    public Map<String, Integer> getHomePermissionGroups() {
        return get(Keys.HOME_NUMBER_PERMISSION_GROUPS);
    }

    public int getMobCombatDurationTicks() {
        return get(Keys.COMBAT_MOB_DURATION_TICKS);
    }

    public int getPlayerCombatDurationTicks() {
        return get(Keys.COMBAT_PLAYER_DURATION_TICKS);
    }

    public List<String> getCombatBlockedCommands() {
        return get(Keys.COMBAT_BLOCKED_COMMANDS);
    }

    public Component getPlayersOnly() {
        return TextUtil.toComponent(get(Keys.COMMAND_PLAYERS_ONLY));
    }

    public Component getNoPermission() {
        return TextUtil.toComponent(get(Keys.COMMAND_NO_PERMISSION));
    }

    public Component getCombatBarTitle() {
        return TextUtil.toComponent(get(Keys.COMBAT_BAR_TITLE));
    }

    public Component getCombatBlockedCommand() {
        return get(Keys.COMBAT_BLOCKED_COMMAND);
    }

    public String getUnknownWorldName() {
        return get(Keys.COMBAT_UNKNOWN_WORLD_NAME);
    }

    public Component getCombatDeathLocation(@NotNull Location location) {
        String worldName = getUnknownWorldName();
        World world = location.getWorld();
        if (world != null) {
            worldName = world.getName().replaceAll("_", " ");
        }
        return TextUtil
                .toComponent(get(Keys.COMBAT_DEATH_LOCATION))
                .replaceText(TextUtil.replaceText("%world%", worldName))
                .replaceText(TextUtil.replaceText("%x%", String.valueOf(location.getBlockX())))
                .replaceText(TextUtil.replaceText("%y%", String.valueOf(location.getBlockY())))
                .replaceText(TextUtil.replaceText("%z%", String.valueOf(location.getBlockZ())));
    }

    public Component getTeleportBarTitle() {
        return TextUtil.toComponent(get(Keys.TELEPORT_BAR_TITLE));
    }

    public Component getTeleportPlayerQuit() {
        return TextUtil.toComponent(get(Keys.TELEPORT_PLAYER_QUIT));
    }

    public Component getTeleportPlayerInCombat() {
        return TextUtil.toComponent(get(Keys.TELEPORT_ENTERED_COMBAT));
    }

    public Component getTeleportPlayerTeleporting() {
        return TextUtil.toComponent(get(Keys.TELEPORT_PARTICIPANT_TELEPORTING));
    }

    public Component getTeleportAlreadyTeleporting() {
        return TextUtil.toComponent(get(Keys.TELEPORT_USER_TELEPORTING));
    }

    public Component getTeleportDestinationUnavailable() {
        return TextUtil.toComponent(get(Keys.TELEPORT_DESTINATION_UNAVAILABLE));
    }

    public Component getTeleportInCooldown(long cooldownSeconds) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_IN_COOLDOWN))
                .replaceText(TextUtil.replaceText("%cooldown%", String.valueOf(cooldownSeconds)));
    }

    public Component getTeleportFailed() {
        return TextUtil.toComponent(get(Keys.TELEPORT_FAILED));
    }

    public Component getTeleportNoBackLocation() {
        return TextUtil.toComponent(get(Keys.TELEPORT_NO_BACK_LOCATION));
    }

    public Component getTeleportPlayerNotFound() {
        return TextUtil.toComponent(get(Keys.TELEPORT_PLAYER_NOT_FOUND));
    }

    public Component getTeleportHelp() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HELP));
    }

    public Component getTeleportRequestReceived(@NotNull String username) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_REQUEST_RECEIVED))
                .replaceText(TextUtil.replaceText("%username%", username));
    }

    public Component getTeleportHereRequestReceived(@NotNull String username) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_REQUEST_HERE_RECEIVED))
                .replaceText(TextUtil.replaceText("%username%", username));
    }

    public Component getTeleportRequestSent(@NotNull String username) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_REQUEST_SENT))
                .replaceText(TextUtil.replaceText("%username%", username));
    }

    public Component getTeleportRequestNotUpdated(@NotNull String username) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_REQUEST_NOT_UPDATED))
                .replaceText(TextUtil.replaceText("%username%", username));
    }

    public Component getTeleportRequestNotFound() {
        return TextUtil.toComponent(get(Keys.TELEPORT_REQUEST_NO_REQUEST_FOUND));
    }

    public Component getTeleportRequestCancelled() {
        return TextUtil.toComponent(get(Keys.TELEPORT_REQUEST_CANCELLED));
    }

    public Component getTeleportRequestManyFound(@NotNull Collection<TeleportRequest> requests) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_REQUEST_MANY_REQUESTS_FOUND))
                .replaceText(TextUtil.replaceText("%list%",
                                                  TextUtil.joinStrings(requests,
                                                                       ", ",
                                                                       request -> request
                                                                               .getRequester()
                                                                               .getPlayer()
                                                                               .getName())));
    }

    public Component getTeleportHomeHelp() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_HELP));

    }

    public Component getTeleportHomeList(@NotNull List<Home> homes) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_HOME_LIST))
                .replaceText(TextUtil.replaceText("%list%", TextUtil.joinStrings(homes, ", ", Home::getName)));
    }

    public Component getTeleportHomeMaxCapacity() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_MAX_CAPACITY));
    }

    public Component getTeleportHomeCreateHelp() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_CREATE_HELP));
    }

    public Component getTeleportHomeCreated() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_CREATED));
    }

    public Component getTeleportHomeAlreadyExists() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_ALREADY_EXISTS));
    }

    public Component getTeleportHomeNotFound() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_NOT_FOUND));
    }

    public Component getTeleportHomeDeleted() {
        return TextUtil.toComponent(get(Keys.TELEPORT_HOME_DELETED));
    }

    public Component getTeleportHomeDeleteHelp(@NotNull Collection<String> homes) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_HOME_DELETE_HELP))
                .replaceText(TextUtil.replaceText("%list%", TextUtil.joinStrings(homes, ", ", string -> string)));
    }

    public Component getTeleportWarpManageHelp() {
        return TextUtil.toComponent(get(Keys.TELEPORT_WARP_MANAGE_HELP));
    }

    public Component getTeleportWarpManageSuccess() {
        return TextUtil.toComponent(get(Keys.TELEPORT_WARP_MANAGE_SUCCESS));
    }

    public Component getTeleportWarpManageDatabaseFailure() {
        return TextUtil.toComponent(get(Keys.TELEPORT_WARP_MANAGE_FAILURE));
    }

    public Component getTeleportWarpFailedToRetrieve() {
        return TextUtil.toComponent(get(Keys.TELEPORT_WARP_FAIL_TO_RETRIEVE));
    }

    public Component getTeleportWarpList(@NotNull Collection<Warp> warps) {
        return TextUtil
                .toComponent(get(Keys.TELEPORT_WARP_LIST))
                .replaceText(TextUtil.replaceText("%list%", TextUtil.joinStrings(warps, ", ", Warp::getName)));
    }

    public Component getTeleportWarpNotFound() {
        return TextUtil.toComponent(get(Keys.TELEPORT_WARP_NOT_FOUND));
    }

    public Component getKickMessageCouldNotLoadProfile() {
        return TextUtil.toComponent(get(Keys.KICK_MESSAGE_COULD_NOT_LOAD_PROFILE));
    }

    private static final class Keys {

        public static final String MONGO_URI = "configuration.database.mongo_uri";
        public static final String MONGO_DATABASE_NAME = "configuration.database.database_name";
        public static final String MONGO_DATABASE_FAILURE_FATAL = "configuration.database.exception_fatal";
        public static final String MONGO_PLAYER_PROFILES_COLLECTION_NAME =
                "configuration.database.player_profiles_collection";
        public static final String MONGO_WARPS_COLLECTION_NAME = "configuration.database.warps_collection";
        public static final String SAVE_PLAYERS_TASK_TIMER_TICKS = "configuration.save_players_task_timer_in_ticks";

        public static final String LOCAL_CHAT_RANGE = "configuration.local_chat.range";
        public static final String LOCAL_CHAT_FORMAT = "configuration.local_chat.format";
        public static final String LOCAL_CHAT_SPY_FORMAT = "configuration.local_chat.spy_format";
        public static final String CHAT_FORMAT = "configuration.local_chat.global_format";

        public static final String TELEPORT_COOLDOWN_SECONDS = "configuration.teleport.teleport_cooldown_in_seconds";
        public static final String TELEPORT_DELAY_TICKS = "configuration.teleport.teleport_delay_in_ticks";
        public static final String TELEPORT_REQUEST_DURATION_SECONDS =
                "configuration.teleport.teleport_request_duration_in_seconds";

        public static final String DEFAULT_HOME_NUMBER = "configuration.homes.default_number_of_homes";
        public static final String HOME_NUMBER_PERMISSION_GROUPS = "configuration.homes.home_number_permission_groups";

        public static final String COMBAT_PLAYER_DURATION_TICKS = "configuration.combat.player_combat_duration_ticks";
        public static final String COMBAT_MOB_DURATION_TICKS = "configuration.combat.mob_combat_duration_ticks";
        public static final String COMBAT_BLOCKED_COMMANDS = "configuration.combat.blocked_commands";

        public static final String COMMAND_PLAYERS_ONLY = "language.players_only";
        public static final String COMMAND_NO_PERMISSION = "language.no_permission";

        public static final String COMBAT_BAR_TITLE = "language.combat.bar_title";
        public static final String COMBAT_BLOCKED_COMMAND = "language.combat.command_is_blocked_while_in_combat";
        public static final String COMBAT_UNKNOWN_WORLD_NAME = "language.combat.unknown_world_name";
        public static final String COMBAT_DEATH_LOCATION = "language.combat.your_death_location_is";

        public static final String TELEPORT_BAR_TITLE = "language.teleport.bar_title";
        public static final String TELEPORT_PLAYER_QUIT = "language.teleport.player_quit";
        public static final String TELEPORT_ENTERED_COMBAT = "language.teleport.entered_combat";
        public static final String TELEPORT_PARTICIPANT_TELEPORTING = "language.teleport.player_is_teleporting";
        public static final String TELEPORT_USER_TELEPORTING = "language.teleport.user_is_teleporting";
        public static final String TELEPORT_DESTINATION_UNAVAILABLE = "language.teleport.destination_unavailable";
        public static final String TELEPORT_IN_COOLDOWN = "language.teleport.in_cooldown";
        public static final String TELEPORT_FAILED = "language.teleport.failed";
        public static final String TELEPORT_NO_BACK_LOCATION = "language.teleport.back.no_back_location";
        public static final String TELEPORT_PLAYER_NOT_FOUND = "language.teleport.player_not_found";
        public static final String TELEPORT_HELP = "language.teleport.help";
        public static final String TELEPORT_REQUEST_RECEIVED = "language.teleport.requests.teleport_received";
        public static final String TELEPORT_REQUEST_HERE_RECEIVED = "language.teleport.requests.teleport_here_received";
        public static final String TELEPORT_REQUEST_SENT = "language.teleport.requests.sent";
        public static final String TELEPORT_REQUEST_NOT_UPDATED = "language.teleport.requests.not_updated";
        public static final String TELEPORT_REQUEST_NO_REQUEST_FOUND = "language.teleport.requests.no_found";
        public static final String TELEPORT_REQUEST_CANCELLED = "language.teleport.requests.request_cancelled";
        public static final String TELEPORT_REQUEST_MANY_REQUESTS_FOUND = "language.teleport.requests.many_found";
        public static final String TELEPORT_HOME_HELP = "language.teleport.homes.help";
        public static final String TELEPORT_HOME_LIST = "language.teleport.homes.list";
        public static final String TELEPORT_HOME_MAX_CAPACITY = "language.teleport.homes.at_max_capacity";
        public static final String TELEPORT_HOME_CREATED = "language.teleport.homes.created";
        public static final String TELEPORT_HOME_ALREADY_EXISTS = "language.teleport.homes.already_exists";
        public static final String TELEPORT_HOME_CREATE_HELP = "language.teleport.homes.create_help";
        public static final String TELEPORT_HOME_DELETED = "language.teleport.homes.deleted";
        public static final String TELEPORT_HOME_NOT_FOUND = "language.teleport.homes.not_found";
        public static final String TELEPORT_HOME_DELETE_HELP = "language.teleport.homes.delete_help";
        public static final String TELEPORT_WARP_MANAGE_HELP = "language.teleport.warps.manage.help";
        public static final String TELEPORT_WARP_MANAGE_SUCCESS = "language.teleport.warps.manage.success";
        public static final String TELEPORT_WARP_MANAGE_FAILURE = "language.teleport.warps.manage.failure";
        public static final String TELEPORT_WARP_FAIL_TO_RETRIEVE = "language.teleport.warps.failed_to_retrieve";
        public static final String TELEPORT_WARP_LIST = "language.teleport.warps.list";
        public static final String TELEPORT_WARP_NOT_FOUND = "language.teleport.warps.not_found";

        public static final String KICK_MESSAGE_COULD_NOT_LOAD_PROFILE =
                "language.kick_messages.could_not_load_profile";

        // Private constructor
        private Keys() {
        }
    }
}
