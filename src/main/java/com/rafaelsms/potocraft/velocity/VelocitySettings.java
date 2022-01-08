package com.rafaelsms.potocraft.velocity;

import com.rafaelsms.potocraft.Plugin;
import com.rafaelsms.potocraft.Settings;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class VelocitySettings extends Settings {

    public VelocitySettings(@NotNull Plugin plugin) throws Exception {
        super(plugin);
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();
        setDefault(Constants.LOGIN_MAX_LOGIN_DURATION_SECONDS, 60 * 30);
        setDefault(Constants.LOGIN_SERVER_NAME, "login");
        setDefault(Constants.LOBBY_SERVER_NAME, "lobby");
        setDefault(Constants.KICK_IF_LOBBY_UNAVAILABLE, true);

        setDefault(Constants.LANG_OFFLINE_PLAYERS_ONLY, "&cComando disponível apenas para jogadores piratas.");
        setDefault(Constants.LANG_LOGIN_HELP, "&6Lembre de sua senha de &3&l6 números &6e escreva &a&l/login senha &6para entrar.");
        setDefault(Constants.LANG_LOGIN_ALREADY_LOGGED_IN, "&3Você já está logado! Pode jogar :)");
        setDefault(Constants.LANG_LOGIN_MUST_REGISTER_FIRST, "&cSua conta ainda não tem senha!\n&6Pense em uma senha de &3&l6 números &6e digite &a&l/registrar senha senha &6para cadastrar.");
        setDefault(Constants.LANG_LOGIN_INCORRECT_PIN, "&cSenha de 6 números incorreta.");
        setDefault(Constants.LANG_LOGIN_SUCCESSFULLY_LOGGED_IN, "&3Senha correta! Bom jogo!");
        setDefault(Constants.LANG_LOGIN_TRANSFER_UNAVAILABLE, "&cFalha ao mover para o próximo servidor.");

        setDefault(Constants.LANG_COULD_NOT_CHECK_PLAYER_TYPE, "&cNão foi possível verificar o tipo de jogador.");
        setDefault(Constants.LANG_COULD_NOT_CHECK_MOJANG_USERNAME, "&cNão foi possível verificar o nome de usuário.");
        setDefault(Constants.LANG_FLOODGATE_PREFIX_ON_JAVA_PLAYER, "&cNome reservado para jogadores Bedrock Edition.");
        setDefault(Constants.LANG_NO_LOGIN_SERVER_AVAILABLE, "&cNão há servidor para login disponível.");
    }

    public long getMaxLoginDurationSeconds() {
        return get(Constants.LOGIN_MAX_LOGIN_DURATION_SECONDS);
    }

    public String getLoginServer() {
        return get(Constants.LOGIN_SERVER_NAME);
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
