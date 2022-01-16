package com.rafaelsms.potocraft.database;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public abstract class DatabaseObject {

    @SuppressWarnings("unused")
    protected DatabaseObject(@NotNull Document document) {
    }

    @SuppressWarnings("unused")
    protected DatabaseObject() {
    }

    public abstract @NotNull Document toDocument();

}
