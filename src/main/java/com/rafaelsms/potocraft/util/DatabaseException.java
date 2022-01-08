package com.rafaelsms.potocraft.util;

public class DatabaseException extends Exception {

    public DatabaseException(Exception exception) {
        super(exception);
    }

    public DatabaseException(String string) {
        super(string);
    }

}
