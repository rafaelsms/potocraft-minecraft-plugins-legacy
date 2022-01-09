package com.rafaelsms.potocraft.velocity;

import com.rafaelsms.potocraft.common.Settings;
import com.rafaelsms.potocraft.common.profile.ReportEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VelocitySettings extends Settings {

    private final @NotNull VelocityPlugin plugin;

    public VelocitySettings(@NotNull VelocityPlugin plugin) throws Exception {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();
        setDefault(Constants.LOGIN_MAX_LOGIN_DURATION_SECONDS, 60 * 30);
        setDefault(Constants.LOBBY_SERVER_NAME, "lobby");
        setDefault(Constants.KICK_IF_LOBBY_UNAVAILABLE, true);

        setDefault(Constants.LANG_OFFLINE_PLAYERS_ONLY, "&cComando disponível apenas para jogadores piratas.");
        setDefault(Constants.LANG_LOGIN_HELP, "&6Lembre de sua senha de &3&l6 números &6e escreva &a&l/login senha &6para entrar.");
        setDefault(Constants.LANG_LOGIN_ALREADY_LOGGED_IN, "&3Você já está logado! Pode jogar :)");
        setDefault(Constants.LANG_LOGIN_MUST_REGISTER_FIRST, "&cSua conta ainda não tem senha!\n&6Pense em uma senha de &3&l6 números &6e digite &a&l/registrar senha senha &6para cadastrar.");
        setDefault(Constants.LANG_LOGIN_INCORRECT_PIN, "&cSenha de 6 números incorreta.");
        setDefault(Constants.LANG_LOGIN_SUCCESSFULLY_LOGGED_IN, "&3Senha correta! Bom jogo!");
        setDefault(Constants.LANG_LOGIN_TRANSFER_UNAVAILABLE, "&cFalha ao mover para o próximo servidor.");

        setDefault(Constants.LANG_REGISTER_INVALID_PINS, "&cVocê digitou a senha numérica incorretamente. &6Imite o exemplo: &a&l/registrar 123456 123456");
        setDefault(Constants.LANG_REGISTER_PINS_DO_NOT_MATCH, "&cVocê digitou duas senhas diferentes! &6Elas precisam ser iguais para confirmar a senha.\n&6Por exemplo: &3&l/registrar 123456 123456");
        setDefault(Constants.LANG_REGISTER_PIN_FORMATTING_FAILED, "&cFalha ao formatar sua senha numérica! Tente novamente ou contate o administrador.");
        setDefault(Constants.LANG_REGISTER_HELP, "&6Para registrar sua conta, pense numa &3senha de 6 números &6e digite: &3&l/registrar senha senha");
        setDefault(Constants.LANG_REGISTER_TRY_LOGIN_INSTEAD, "&cVocê já tem uma conta cadastrada! &6Tente entrar digitando &3&l/login senha");
        setDefault(Constants.LANG_REGISTER_TRY_CHANGE_PIN_INSTEAD, "&cVocê já tem uma conta cadastrada! &6Deseja alterar sua senha? Utilize &3&l/mudarsenha");

        setDefault(Constants.LANG_REPORT_UNKNOWN_REASON, "&7(motivo não especificado)");
        setDefault(Constants.LANG_REPORT_NO_EXPIRATION_DATE, "&7(data não especificada)");
        setDefault(Constants.LANG_REPORT_YOU_HAVE_BEEN_MUTED, "&cVocê foi silenciado por &e%reporter% &cpelo motivo \"&e%reason%&c\" &caté &e%expiration_date%&c.");
        setDefault(Constants.LANG_REPORT_HELP, "&6Uso: &e&l/report kick/ban/history/mute/unreport");
        setDefault(Constants.LANG_REPORT_PLAYER_EXEMPT, "&cEste jogador não pode ser punido desta forma.");
        setDefault(Constants.LANG_REPORT_COULD_NOT_SAVE_REPORT, "&cNão foi possível salvar o incidente de &e%player%&c.");
        setDefault(Constants.LANG_REPORT_SUB_COMMAND_PLAYER_REASON_HELP, "&6Uso: &e&l/report %subcommand% <nome do jogador> <motivo>");
        setDefault(Constants.LANG_REPORT_UNREPORT_HELP, "&6Uso: &e&l/report unreport <nome do jogador>");
        setDefault(Constants.LANG_REPORT_UNREPORT_NO_ENTRY, "&cNão há report ativo para este jogador.");
        setDefault(Constants.LANG_REPORT_UNREPORT_SUCCESSFULLY, "&6Report de &e%player% &6cancelado");
        setDefault(Constants.LANG_REPORT_HISTORY_HELP, "&6Uso: &e&l/report history <nome do jogador>");
        setDefault(Constants.LANG_REPORT_HISTORY_NO_ENTRIES, "&6Não há histórico para este jogador.");
        setDefault(Constants.LANG_REPORT_HISTORY_BASE, "&dHistórico de %player%:\n%entries%");
        setDefault(Constants.LANG_REPORT_HISTORY_ENTRY_BANNED, "&7* &cBANIDO por &e%reporter% &cmotivo: \"&e%reason%&c\" &caté &e%expiration_date%");
        setDefault(Constants.LANG_REPORT_HISTORY_ENTRY_MUTED, "&7* &cSILENCIADO por &e%reporter% &cmotivo: \"&e%reason%&c\" &caté &e%expiration_date%");
        setDefault(Constants.LANG_REPORT_HISTORY_ENTRY_KICKED, "&7* &cEXPULSO por &e%reporter% &cmotivo: \"&e%reason%&c\"");

        setDefault(Constants.LANG_KICKED, "&cVocê foi expulso\n&cpor &e%reporter%\n&cpelo motivo \"&e%reason%&c\".");
        setDefault(Constants.LANG_BANNED, "&cVocê foi banido\n&cpor &e%reporter%\n&cpelo motivo \"&e%reason%&c\"\n&caté &e%expiration_date%&c.");
        setDefault(Constants.LANG_COULD_NOT_CHECK_PLAYER_TYPE, "&cNão foi possível verificar o tipo de jogador.");
        setDefault(Constants.LANG_COULD_NOT_CHECK_MOJANG_USERNAME, "&cNão foi possível verificar o nome de usuário.");
        setDefault(Constants.LANG_FLOODGATE_PREFIX_ON_JAVA_PLAYER, "&cNome reservado para jogadores Bedrock Edition.");
        setDefault(Constants.LANG_NO_LOGIN_SERVER_AVAILABLE, "&cNão há servidor para login disponível.");
    }

    public long getMaxLoginDurationSeconds() {
        return get(Constants.LOGIN_MAX_LOGIN_DURATION_SECONDS);
    }

    public String getLobbyServer() {
        return get(Constants.LOBBY_SERVER_NAME);
    }

    public boolean isKickIfLobbyUnavailable() {
        return get(Constants.KICK_IF_LOBBY_UNAVAILABLE);
    }

    public Component getCommandOfflinePlayersOnly() {
        return getLang(Constants.LANG_OFFLINE_PLAYERS_ONLY);
    }

    public Component getCommandLoginHelp() {
        return getLang(Constants.LANG_LOGIN_HELP);
    }

    public Component getCommandAlreadyLoggedIn() {
        return getLang(Constants.LANG_LOGIN_ALREADY_LOGGED_IN);
    }

    public Component getCommandMustRegisterFirst() {
        return getLang(Constants.LANG_LOGIN_MUST_REGISTER_FIRST);
    }

    public Component getCommandIncorrectPIN() {
        return getLang(Constants.LANG_LOGIN_INCORRECT_PIN);
    }

    public Component getCommandLoggedIn() {
        return getLang(Constants.LANG_LOGIN_SUCCESSFULLY_LOGGED_IN);
    }

    public Component getCommandRegisterInvalidPins() {
        return getLang(Constants.LANG_REGISTER_INVALID_PINS);
    }

    public Component getCommandRegisterPinsDoNotMatch() {
        return getLang(Constants.LANG_REGISTER_PINS_DO_NOT_MATCH);
    }

    public Component getCommandRegisterFormattingFailed() {
        return getLang(Constants.LANG_REGISTER_PIN_FORMATTING_FAILED);
    }

    public Component getCommandRegisterHelp() {
        return getLang(Constants.LANG_REGISTER_HELP);
    }

    public Component getCommandRegisterShouldLoginInstead() {
        return getLang(Constants.LANG_REGISTER_TRY_LOGIN_INSTEAD);
    }

    public Component getCommandRegisterShouldChangePinInstead() {
        return getLang(Constants.LANG_REGISTER_TRY_CHANGE_PIN_INSTEAD);
    }

    public Component getCommandReportUnknownReason() {
        return getLang(Constants.LANG_REPORT_UNKNOWN_REASON);
    }

    public Component getCommandReportNoExpirationDate() {
        return getLang(Constants.LANG_REPORT_NO_EXPIRATION_DATE);
    }

    public Component getCommandReportPlayerExempt() {
        return getLang(Constants.LANG_REPORT_PLAYER_EXEMPT);
    }

    public Component getCommandReportCouldNotSaveReport(@NotNull Component playerName) {
        return getLang(Constants.LANG_REPORT_COULD_NOT_SAVE_REPORT)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(playerName).build());
    }

    public Component getCommandReportSubCommandHelp(@NotNull String subCommand) {
        return getLang(Constants.LANG_REPORT_SUB_COMMAND_PLAYER_REASON_HELP)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%subcommand%").replacement(subCommand).build());
    }

    public Component getCommandReportHistoryHelp() {
        return getLang(Constants.LANG_REPORT_HISTORY_HELP);
    }

    public Component getCommandReportUnreportHelp() {
        return getLang(Constants.LANG_REPORT_UNREPORT_HELP);
    }

    public Component getCommandReportUnreportNoEntry() {
        return getLang(Constants.LANG_REPORT_UNREPORT_NO_ENTRY);
    }

    public Component getCommandReportUnreportSuccessfully(@NotNull String playerName) {
        return getLang(Constants.LANG_REPORT_UNREPORT_SUCCESSFULLY)
                       .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(playerName).build());
    }

    public Component getCommandReportHelp() {
        return getLang(Constants.LANG_REPORT_HELP);
    }

    public Component getCommandReportHistory(@NotNull String playerName, @NotNull Collection<ReportEntry> reportEntries) {
        if (reportEntries.isEmpty()) return getLang(Constants.LANG_REPORT_HISTORY_NO_ENTRIES);
        List<Component> entries = new ArrayList<>(reportEntries.size());
        for (ReportEntry reportEntry : reportEntries) {
            entries.add(reportEntry.getHistoryMessage(plugin));
        }
        Component entriesJoined = Component.join(JoinConfiguration.builder().separator(Component.newline()).build(), entries);
        return getLang(Constants.LANG_REPORT_HISTORY_BASE)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(playerName).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%entries%").replacement(entriesJoined).build());
    }

    public Component getCommandReportYouHaveBeenMuted(@NotNull Component reporter, @NotNull Component reason,
                                                      @NotNull Component expirationDate) {
        return getLang(Constants.LANG_REPORT_YOU_HAVE_BEEN_MUTED)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reporter%").replacement(reporter).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reason%").replacement(reason).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%expiration_date%").replacement(expirationDate).build());
    }

    public Component getKickMessageBanned(@NotNull Component reporter, @NotNull Component reason,
                                          @NotNull Component expirationDate) {
        return getLang(Constants.LANG_BANNED)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reporter%").replacement(reporter).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reason%").replacement(reason).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%expiration_date%").replacement(expirationDate).build());
    }

    public Component getKickMessageKicked(@NotNull Component reporter, @NotNull Component reason) {
        return getLang(Constants.LANG_KICKED)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reporter%").replacement(reporter).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reason%").replacement(reason).build());
    }

    public Component getReportHistoryEntryBanned(@NotNull Component reporter, @NotNull Component reason,
                                                 @NotNull Component expirationDate) {
        return getLang(Constants.LANG_REPORT_HISTORY_ENTRY_BANNED)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reporter%").replacement(reporter).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reason%").replacement(reason).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%expiration_date%").replacement(expirationDate).build());
    }

    public Component getReportHistoryEntryMuted(@NotNull Component reporter, @NotNull Component reason,
                                                @NotNull Component expirationDate) {
        return getLang(Constants.LANG_REPORT_HISTORY_ENTRY_MUTED)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reporter%").replacement(reporter).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reason%").replacement(reason).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%expiration_date%").replacement(expirationDate).build());
    }

    public Component getReportHistoryEntryKicked(@NotNull Component reporter, @NotNull Component reason) {
        return getLang(Constants.LANG_REPORT_HISTORY_ENTRY_KICKED)
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reporter%").replacement(reporter).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%reason%").replacement(reason).build());
    }

    public Component getKickMessageTransferServerUnavailable() {
        return getLang(Constants.LANG_LOGIN_TRANSFER_UNAVAILABLE);
    }

    public Component getKickMessageCouldNotCheckPlayerType() {
        return getLang(Constants.LANG_COULD_NOT_CHECK_PLAYER_TYPE);
    }

    public Component getKickMessageCouldNotCheckMojangUsername() {
        return getLang(Constants.LANG_COULD_NOT_CHECK_MOJANG_USERNAME);
    }

    public Component getKickMessageInvalidPrefixForJavaPlayer() {
        return getLang(Constants.LANG_FLOODGATE_PREFIX_ON_JAVA_PLAYER);
    }

    public Component getKickMessageNoLoginServer() {
        return getLang(Constants.LANG_NO_LOGIN_SERVER_AVAILABLE);
    }
}
