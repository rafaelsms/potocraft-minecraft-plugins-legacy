package com.rafaelsms.potocraft;

import org.jetbrains.annotations.NotNull;

public final class Permissions {

    public static final @NotNull String LOGIN_COMMAND = "potocraft.proxy.login";
    public static final @NotNull String REGISTER_COMMAND = "potocraft.proxy.register";

    public static final @NotNull String[] values = {
            LOGIN_COMMAND,
            REGISTER_COMMAND,
    };

    private Permissions() {
    }
}
