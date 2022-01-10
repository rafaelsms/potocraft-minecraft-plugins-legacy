package com.rafaelsms.potocraft.velocity;

import com.rafaelsms.potocraft.common.Settings;
import com.rafaelsms.potocraft.common.profile.ReportEntry;
import com.rafaelsms.potocraft.common.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
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
        /* CONFIGURATION */
        setDefault(Constants.LOGIN_MAX_LOGIN_DURATION_SECONDS, 60 * 30);
        setDefault(Constants.LOBBY_SERVER_NAME, "lobby");
        setDefault(Constants.KICK_IF_LOBBY_UNAVAILABLE, true);

        setDefault(Constants.UNIVERSAL_CHAT_FORMAT, "&6!! <&e%prefix%%username%%suffix%&6> &f%message%");
        setDefault(Constants.UNIVERSAL_CHAT_SPY_FORMAT, "&e%prefix%%username%%suffix% &7(%server_name%) &f%message%");
        setDefault(Constants.UNIVERSAL_CHAT_PREFIX, "!!");
        setDefault(Constants.UNIVERSAL_CHAT_LIMITER_MESSAGES_AMOUNT, 2);
        setDefault(Constants.UNIVERSAL_CHAT_LIMITER_TIME_AMOUNT, 5500);
        setDefault(Constants.UNIVERSAL_CHAT_COMPARATOR_MIN_LENGTH, 3);
        setDefault(Constants.UNIVERSAL_CHAT_COMPARATOR_THRESHOLD, 3);
        setDefault(Constants.REPLY_MESSAGE_TIMEOUT, 60 * 7L);
        setDefault(Constants.DIRECT_MESSAGE_INCOMING_FORMAT, "&3%username% &c-> &3você &f%message%");
        setDefault(Constants.DIRECT_MESSAGE_OUTGOING_FORMAT, "&3você &c-> &3%username% &f%message%");
        setDefault(Constants.DIRECT_MESSAGE_SPY_FORMAT, "&3%sending_name% &c-> &3%receiving_name% &f%message%");
        setDefault(Constants.DIRECT_MESSAGE_LIMITER_MESSAGES_AMOUNT, 4);
        setDefault(Constants.DIRECT_MESSAGE_LIMITER_TIME_AMOUNT, 5500);
        setDefault(Constants.DIRECT_MESSAGE_COMPARATOR_MIN_LENGTH, 5);
        setDefault(Constants.DIRECT_MESSAGE_COMPARATOR_THRESHOLD, 3);

        /* LANG */
        setDefault(Constants.LANG_OFFLINE_PLAYERS_ONLY, "&cComando disponível apenas para jogadores piratas.");
        setDefault(Constants.LANG_LOGIN_HELP,
                   "&6Lembre de sua senha de &3&l6 números &6e escreva &a&l/login senha &6para entrar.");
        setDefault(Constants.LANG_LOGIN_ALREADY_LOGGED_IN, "&3Você já está logado! Pode jogar :)");
        setDefault(Constants.LANG_LOGIN_MUST_REGISTER_FIRST, """
                                                             &cSua conta ainda não tem senha!
                                                             &6Pense em uma senha de &3&l6 números &6e digite &a&l/registrar senha senha &6para cadastrar.""");
        setDefault(Constants.LANG_LOGIN_INCORRECT_PIN, "&cSenha de 6 números incorreta.");
        setDefault(Constants.LANG_LOGIN_SUCCESSFULLY_LOGGED_IN, "&3Senha correta! Bom jogo!");
        setDefault(Constants.LANG_LOGIN_TRANSFER_UNAVAILABLE, "&cFalha ao mover para o próximo servidor.");

        setDefault(Constants.LANG_REGISTER_INVALID_PINS,
                   "&cVocê digitou a senha numérica incorretamente. &6Imite o exemplo: &a&l/registrar 123456 123456");
        setDefault(Constants.LANG_REGISTER_PINS_DO_NOT_MATCH, """
                                                              &cVocê digitou duas senhas diferentes! &6Elas precisam ser iguais para confirmar a senha.
                                                              &6Por exemplo: &3&l/registrar 123456 123456""");
        setDefault(Constants.LANG_REGISTER_PIN_FORMATTING_FAILED,
                   "&cFalha ao formatar sua senha numérica! Tente novamente ou contate o administrador.");
        setDefault(Constants.LANG_REGISTER_HELP,
                   "&6Para registrar sua conta, pense numa &3senha de 6 números &6e digite: &3&l/registrar senha senha");
        setDefault(Constants.LANG_REGISTER_TRY_LOGIN_INSTEAD,
                   "&cVocê já tem uma conta cadastrada! &6Tente entrar digitando &3&l/login senha");
        setDefault(Constants.LANG_REGISTER_TRY_CHANGE_PIN_INSTEAD,
                   "&cVocê já tem uma conta cadastrada! &6Deseja alterar sua senha? Utilize &3&l/mudarsenha");

        setDefault(Constants.LANG_CHANGE_PIN_HELP, """
                                                   &6Para mudar a senha, lembre de sua senha antiga, faça uma nova de 6 números e digite:
                                                   &a/mudarsenha <senha antiga> <senha nova> <senha nova>
                                                   &6Por exemplo: &a&l/mudarsenha 000000 123456 123456""");
        setDefault(Constants.LANG_CHANGE_PIN_PINS_DO_NOT_MATCH,
                   "&cSua nova senha precisa ser digitada de maneira igual duas vezes! Elas não estão iguais.");

        setDefault(Constants.LANG_REPORT_UNKNOWN_REASON, "&7(motivo não especificado)");
        setDefault(Constants.LANG_REPORT_NO_EXPIRATION_DATE, "&7(data não especificada)");
        setDefault(Constants.LANG_REPORT_YOU_HAVE_BEEN_MUTED,
                   "&cVocê foi silenciado por &e%reporter% &cpelo motivo \"&e%reason%&c\" &caté &e%expiration_date%&c.");
        setDefault(Constants.LANG_REPORT_HELP, "&6Uso: &e&l/report kick/ban/history/mute/unreport");
        setDefault(Constants.LANG_REPORT_PLAYER_EXEMPT, "&cEste jogador não pode ser punido desta forma.");
        setDefault(Constants.LANG_REPORT_COULD_NOT_SAVE_REPORT,
                   "&cNão foi possível salvar o incidente de &e%player%&c.");
        setDefault(Constants.LANG_REPORT_SUB_COMMAND_PLAYER_REASON_HELP,
                   "&6Uso: &e&l/report %subcommand% <nome do jogador> <motivo>");
        setDefault(Constants.LANG_REPORT_UNREPORT_HELP, "&6Uso: &e&l/report unreport <nome do jogador>");
        setDefault(Constants.LANG_REPORT_UNREPORT_NO_ENTRY, "&cNão há report ativo para este jogador.");
        setDefault(Constants.LANG_REPORT_UNREPORT_SUCCESSFULLY, "&6Report de &e%player% &6cancelado");
        setDefault(Constants.LANG_REPORT_HISTORY_HELP, "&6Uso: &e&l/report history <nome do jogador>");
        setDefault(Constants.LANG_REPORT_HISTORY_NO_ENTRIES, "&6Não há histórico para este jogador.");
        setDefault(Constants.LANG_REPORT_HISTORY_BASE, "&dHistórico de %player%:\n%entries%");
        setDefault(Constants.LANG_REPORT_HISTORY_ENTRY_BANNED,
                   "&7* &cBANIDO por &e%reporter% &cmotivo: \"&e%reason%&c\" &caté &e%expiration_date%");
        setDefault(Constants.LANG_REPORT_HISTORY_ENTRY_MUTED,
                   "&7* &cSILENCIADO por &e%reporter% &cmotivo: \"&e%reason%&c\" &caté &e%expiration_date%");
        setDefault(Constants.LANG_REPORT_HISTORY_ENTRY_KICKED,
                   "&7* &cEXPULSO por &e%reporter% &cmotivo: \"&e%reason%&c\"");

        setDefault(Constants.LANG_DIRECT_MESSAGE_REPLY_HELP, "&6Uso: &3&l/responder <mensagem>");
        setDefault(Constants.LANG_DIRECT_MESSAGE_NO_RECIPIENT, "&cPessoa não encontrada.");
        setDefault(Constants.LANG_DIRECT_MESSAGE_RECIPIENT_LEFT, "&cPessoa saiu recentemente.");

        setDefault(Constants.LANG_KICKED, """
                                          &cVocê foi expulso
                                          &cpor &e%reporter%
                                          &cpelo motivo "&e%reason%&c".""");
        setDefault(Constants.LANG_BANNED, """
                                          &cVocê foi banido
                                          &cpor &e%reporter%
                                          &cpelo motivo "&e%reason%&c"
                                          &caté &e%expiration_date%&c.""");
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

    public Component getCommandChangePinHelp() {
        return getLang(Constants.LANG_CHANGE_PIN_HELP);
    }

    public Component getCommandChangePinPinsDoNotMatch() {
        return getLang(Constants.LANG_CHANGE_PIN_PINS_DO_NOT_MATCH);
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
        TextReplacementConfig nameReplacer = TextUtil.replaceText("%player%", playerName);
        return getLang(Constants.LANG_REPORT_COULD_NOT_SAVE_REPORT).replaceText(nameReplacer);
    }

    public Component getCommandReportSubCommandHelp(@NotNull String subCommand) {
        TextReplacementConfig subCommandReplacer = TextUtil.replaceText("%subcommand%", subCommand);
        return getLang(Constants.LANG_REPORT_SUB_COMMAND_PLAYER_REASON_HELP).replaceText(subCommandReplacer);
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
        TextReplacementConfig nameReplacer = TextUtil.replaceText("%player%", playerName);
        return getLang(Constants.LANG_REPORT_UNREPORT_SUCCESSFULLY).replaceText(nameReplacer);
    }

    public Component getCommandReportHelp() {
        return getLang(Constants.LANG_REPORT_HELP);
    }

    public Component getCommandReportHistory(@NotNull String playerName,
                                             @NotNull Collection<ReportEntry> reportEntries) {
        if (reportEntries.isEmpty()) {
            return getLang(Constants.LANG_REPORT_HISTORY_NO_ENTRIES);
        }
        List<Component> entries = new ArrayList<>(reportEntries.size());
        for (ReportEntry reportEntry : reportEntries) {
            entries.add(reportEntry.getHistoryMessage(plugin));
        }
        Component entriesJoined =
                Component.join(JoinConfiguration.builder().separator(Component.newline()).build(), entries);
        return getLang(Constants.LANG_REPORT_HISTORY_BASE)
                .replaceText(TextUtil.replaceText("%player%", playerName))
                .replaceText(TextUtil.replaceText("%entries%", entriesJoined));
    }

    public Component getCommandReportYouHaveBeenMuted(@NotNull Component reporter,
                                                      @NotNull Component reason,
                                                      @NotNull Component expirationDate) {
        return getLang(Constants.LANG_REPORT_YOU_HAVE_BEEN_MUTED)
                .replaceText(TextUtil.replaceText("%reporter%", reporter))
                .replaceText(TextUtil.replaceText("%reason%", reason))
                .replaceText(TextUtil.replaceText("%expiration_date%", expirationDate));
    }

    public Component getKickMessageBanned(@NotNull Component reporter,
                                          @NotNull Component reason,
                                          @NotNull Component expirationDate) {
        return getLang(Constants.LANG_BANNED)
                .replaceText(TextUtil.replaceText("%reporter%", reporter))
                .replaceText(TextUtil.replaceText("%reason%", reason))
                .replaceText(TextUtil.replaceText("%expiration_date%", expirationDate));
    }

    public Component getKickMessageKicked(@NotNull Component reporter, @NotNull Component reason) {
        return getLang(Constants.LANG_KICKED)
                .replaceText(TextUtil.replaceText("%reporter%", reporter))
                .replaceText(TextUtil.replaceText("%reason%", reason));
    }

    public Component getReportHistoryEntryBanned(@NotNull Component reporter,
                                                 @NotNull Component reason,
                                                 @NotNull Component expirationDate) {
        return getLang(Constants.LANG_REPORT_HISTORY_ENTRY_BANNED)
                .replaceText(TextUtil.replaceText("%reporter%", reporter))
                .replaceText(TextUtil.replaceText("%reason%", reason))
                .replaceText(TextUtil.replaceText("%expiration_date%", expirationDate));
    }

    public Component getReportHistoryEntryMuted(@NotNull Component reporter,
                                                @NotNull Component reason,
                                                @NotNull Component expirationDate) {
        return getLang(Constants.LANG_REPORT_HISTORY_ENTRY_MUTED)
                .replaceText(TextUtil.replaceText("%reporter%", reporter))
                .replaceText(TextUtil.replaceText("%reason%", reason))
                .replaceText(TextUtil.replaceText("%expiration_date%", expirationDate));
    }

    public Component getReportHistoryEntryKicked(@NotNull Component reporter, @NotNull Component reason) {
        return getLang(Constants.LANG_REPORT_HISTORY_ENTRY_KICKED)
                .replaceText(TextUtil.replaceText("%reporter%", reporter))
                .replaceText(TextUtil.replaceText("%reason%", reason));
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

    public Component getUniversalChatFormat() {
        return getLang(Constants.UNIVERSAL_CHAT_FORMAT);
    }

    public Component getUniversalChatSpyFormat() {
        return getLang(Constants.UNIVERSAL_CHAT_SPY_FORMAT);
    }

    public String getUniversalChatPrefix() {
        return get(Constants.UNIVERSAL_CHAT_PREFIX);
    }

    public int getUniversalChatComparatorThreshold() {
        return get(Constants.UNIVERSAL_CHAT_COMPARATOR_THRESHOLD);
    }

    public int getUniversalChatComparatorMinLength() {
        return get(Constants.UNIVERSAL_CHAT_COMPARATOR_MIN_LENGTH);
    }

    public int getUniversalChatLimiterMessageAmount() {
        return get(Constants.UNIVERSAL_CHAT_LIMITER_MESSAGES_AMOUNT);
    }

    public long getUniversalChatLimiterTimeAmount() {
        return get(Constants.UNIVERSAL_CHAT_LIMITER_TIME_AMOUNT);
    }

    public Duration getPrivateMessageTimeout() {
        return Duration.ofSeconds(get(Constants.REPLY_MESSAGE_TIMEOUT));
    }

    public Component getDirectMessageIncomingFormat(@NotNull String playerName, @NotNull String message) {
        return getLang(Constants.DIRECT_MESSAGE_INCOMING_FORMAT)
                .replaceText(TextUtil.replaceText("%username%", playerName))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    public Component getDirectMessageOutgoingFormat(@NotNull String playerName, @NotNull String message) {
        return getLang(Constants.DIRECT_MESSAGE_OUTGOING_FORMAT)
                .replaceText(TextUtil.replaceText("%username%", playerName))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    public Component getDirectMessageSpyFormat(@NotNull String sendingName,
                                               @NotNull String receivingName,
                                               @NotNull String message) {
        return getLang(Constants.DIRECT_MESSAGE_OUTGOING_FORMAT)
                .replaceText(TextUtil.replaceText("%sending_name%", sendingName))
                .replaceText(TextUtil.replaceText("%receiving_name%", receivingName))
                .replaceText(TextUtil.replaceText("%message%", message));
    }

    public int getDirectMessageComparatorThreshold() {
        return get(Constants.DIRECT_MESSAGE_COMPARATOR_THRESHOLD);
    }

    public int getDirectMessageComparatorMinLength() {
        return get(Constants.DIRECT_MESSAGE_COMPARATOR_MIN_LENGTH);
    }

    public int getDirectMessageLimiterMessageAmount() {
        return get(Constants.DIRECT_MESSAGE_LIMITER_MESSAGES_AMOUNT);
    }

    public long getDirectMessageLimiterTimeAmount() {
        return get(Constants.DIRECT_MESSAGE_LIMITER_TIME_AMOUNT);
    }

    public Component getCommandDirectMessageReplyHelp() {
        return getLang(Constants.LANG_DIRECT_MESSAGE_REPLY_HELP);
    }

    public Component getCommandDirectMessageNoRecipient() {
        return getLang(Constants.LANG_DIRECT_MESSAGE_NO_RECIPIENT);
    }

    public Component getCommandDirectMessageRecipientLeft() {
        return getLang(Constants.LANG_DIRECT_MESSAGE_RECIPIENT_LEFT);
    }

}
