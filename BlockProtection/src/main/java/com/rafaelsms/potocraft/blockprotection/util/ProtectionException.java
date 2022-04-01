package com.rafaelsms.potocraft.blockprotection.util;

import org.jetbrains.annotations.NotNull;

public class ProtectionException extends Exception {

    public ProtectionException() {
        super();
    }

    public ProtectionException(@NotNull String message) {
        super(message);
    }
}
