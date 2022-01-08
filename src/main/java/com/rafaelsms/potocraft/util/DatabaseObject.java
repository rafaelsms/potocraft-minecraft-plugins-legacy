package com.rafaelsms.potocraft.util;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public abstract class DatabaseObject {

    @SuppressWarnings("unused")
    protected DatabaseObject(@NotNull Document document) {
    }

    public abstract Document toDocument();

}
