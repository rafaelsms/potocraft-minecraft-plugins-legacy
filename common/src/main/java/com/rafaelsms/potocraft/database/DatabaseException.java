package com.rafaelsms.potocraft.database;

import org.jetbrains.annotations.NotNull;

public class DatabaseException extends Exception {

    public DatabaseException(@NotNull String message, Exception exception) {
        super(message, exception);
    }

    public DatabaseException(@NotNull String message) {
        super(message);
    }

    public DatabaseException(Exception exception) {
        super(exception);
    }
}
